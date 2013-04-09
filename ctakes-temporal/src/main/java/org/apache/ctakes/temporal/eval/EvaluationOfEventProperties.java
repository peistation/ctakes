/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.temporal.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.ctakes.temporal.ae.DocTimeRelAnnotator;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
//import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfEventProperties extends
    Evaluation_ImplBase<Map<String, AnnotationStatistics<String>>> {

  private static final String DOC_TIME_REL = "docTimeRel";

  private static final List<String> PROPERTY_NAMES = Arrays.asList(DOC_TIME_REL);

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    EvaluationOfEventProperties evaluation = new EvaluationOfEventProperties(
        new File("target/eval/event-properties"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getXMIDirectory());
    evaluation.prepareXMIsFor(patientSets);
    evaluation.logClassificationErrors(new File("target/eval"), "ctakes-event-property-errors");
    Map<String, AnnotationStatistics<String>> stats = evaluation.trainAndTest(trainItems, devItems);
    for (String name : PROPERTY_NAMES) {
      System.err.println("====================");
      System.err.println(name);
      System.err.println("--------------------");
      System.err.println(stats.get(name));
    }
  }

  private Map<String, Logger> loggers = Maps.newHashMap();
  
  public EvaluationOfEventProperties(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory);
    for (String name : PROPERTY_NAMES) {
      this.loggers.put(name, Logger.getLogger(String.format("%s.%s", this.getClass().getName(), name)));
    }
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class));
    aggregateBuilder.add(CopyFromGold.getDescription(TimeMention.class));
    aggregateBuilder.add(DocTimeRelAnnotator.createDataWriterDescription(
    	LIBSVMStringOutcomeDataWriter.class,
        directory));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
    JarClassifierBuilder.trainAndPackage(directory, "-c", "10000");
  }

  @Override
  protected Map<String, AnnotationStatistics<String>> test(
      CollectionReader collectionReader,
      File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class));
    aggregateBuilder.add(CopyFromGold.getDescription(TimeMention.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ClearEventProperties.class));
    aggregateBuilder.add(DocTimeRelAnnotator.createAnnotatorDescription(directory));

    Function<EventMention, ?> eventMentionToSpan = AnnotationStatistics.annotationToSpan();
    Map<String, Function<EventMention, String>> propertyGetters;
    propertyGetters = new HashMap<String, Function<EventMention, String>>();
    for (String name : PROPERTY_NAMES) {
      propertyGetters.put(name, getPropertyGetter(name));
    }

    Map<String, AnnotationStatistics<String>> statsMap = new HashMap<String, AnnotationStatistics<String>>();
    statsMap.put(DOC_TIME_REL, new AnnotationStatistics<String>());
    for (JCas jCas : new JCasIterable(collectionReader, aggregateBuilder.createAggregate())) {
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      String text = goldView.getDocumentText();
      List<EventMention> goldEvents = selectExact(goldView, EventMention.class);
      List<EventMention> systemEvents = selectExact(systemView, EventMention.class);
      for (String name : PROPERTY_NAMES) {
        Function<EventMention, String> getProperty = propertyGetters.get(name);
        statsMap.get(name).add(
            goldEvents,
            systemEvents,
            eventMentionToSpan,
            getProperty);
        for (int i = 0; i < goldEvents.size(); ++i) {
          String goldOutcome = getProperty.apply(goldEvents.get(i));
          String systemOutcome = getProperty.apply(systemEvents.get(i));
          if (!goldOutcome.equals(systemOutcome)) {
            EventMention event = goldEvents.get(i);
            int begin = event.getBegin();
            int end = event.getEnd();
            int windowBegin = Math.max(0, begin - 50);
            int windowEnd = Math.min(text.length(), end + 50);
            this.loggers.get(name).fine(String.format(
                "%s was %s but should be %s, in  ...%s[!%s!]%s...",
                name,
                systemOutcome,
                goldOutcome,
                text.substring(windowBegin, begin).replaceAll("[\r\n]", " "),
                text.substring(begin, end),
                text.substring(end, windowEnd).replaceAll("[\r\n]", " ")));
          }
        }
      }
    }
    return statsMap;
  }
  
  public void logClassificationErrors(File outputDir, String outputFilePrefix) throws IOException {
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    for (String name : PROPERTY_NAMES) {
      Logger logger = this.loggers.get(name);
      logger.setLevel(Level.FINE);
      File outputFile = new File(outputDir, String.format("%s.%s.log", outputFilePrefix, name));
      FileHandler handler = new FileHandler(outputFile.getPath());
      handler.setFormatter(new Formatter() {
        @Override
        public String format(LogRecord record) {
          return record.getMessage() + '\n';
        }
      });
      logger.addHandler(handler);
    }
  }

  private static Function<EventMention, String> getPropertyGetter(final String propertyName) {
    return new Function<EventMention, String>() {
      @Override
      public String apply(EventMention eventMention) {
        EventProperties eventProperties = eventMention.getEvent().getProperties();
        Feature feature = eventProperties.getType().getFeatureByBaseName(propertyName);
        return eventProperties.getFeatureValueAsString(feature);
      }
    };
  }

  public static class ClearEventProperties extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (EventProperties eventProperties : JCasUtil.select(jCas, EventProperties.class)) {
        eventProperties.setAspect(null);
        eventProperties.setCategory(null);
        eventProperties.setContextualAspect(null);
        eventProperties.setContextualModality(null);
        eventProperties.setDegree(null);
        eventProperties.setDocTimeRel(null);
        eventProperties.setPermanence(null);
        eventProperties.setPolarity(0);
      }
    }

  }
}
