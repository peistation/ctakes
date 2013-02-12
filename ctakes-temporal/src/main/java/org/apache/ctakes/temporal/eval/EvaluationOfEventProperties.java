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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.temporal.ae.DocTimeRelAnnotator;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
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
    Map<String, AnnotationStatistics<String>> stats = evaluation.trainAndTest(trainItems, devItems);
    for (String name : PROPERTY_NAMES) {
      System.err.println("====================");
      System.err.println(name);
      System.err.println("--------------------");
      System.err.println(stats.get(name));
    }
  }

  public EvaluationOfEventProperties(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory);
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class));
    aggregateBuilder.add(DocTimeRelAnnotator.createDataWriterDescription(
        LIBSVMStringOutcomeDataWriter.class,
        directory));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
    JarClassifierBuilder.trainAndPackage(directory, "-c", "1000");
  }

  @Override
  protected Map<String, AnnotationStatistics<String>> test(
      CollectionReader collectionReader,
      File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class));
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
      Collection<EventMention> goldEvents = selectExact(goldView, EventMention.class);
      Collection<EventMention> systemEvents = selectExact(systemView, EventMention.class);
      for (String name : PROPERTY_NAMES) {
        statsMap.get(name).add(
            goldEvents,
            systemEvents,
            eventMentionToSpan,
            propertyGetters.get(name));
      }
    }
    return statsMap;
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
