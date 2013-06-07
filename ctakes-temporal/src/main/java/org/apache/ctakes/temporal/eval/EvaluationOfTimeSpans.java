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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.ConstituencyBasedTimeAnnotator;
import org.apache.ctakes.temporal.ae.TimeAnnotator;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.factory.AnalysisEngineFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfTimeSpans extends EvaluationOfAnnotationSpans_ImplBase {

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    
    // specify the annotator classes to use
    List<Class<? extends CleartkAnnotator<String>>> annotatorClasses = Lists.newArrayList();
    annotatorClasses.add(TimeAnnotator.class);
    annotatorClasses.add(ConstituencyBasedTimeAnnotator.class);
    Map<Class<? extends CleartkAnnotator<String>>, String[]> annotatorTrainingArguments = Maps.newHashMap();
    annotatorTrainingArguments.put(TimeAnnotator.class, new String[]{"-c", "0.1"});
    annotatorTrainingArguments.put(ConstituencyBasedTimeAnnotator.class, new String[]{"-c", "1"});
    
    // run one evaluation per annotator class
    final Map<Class<?>, AnnotationStatistics<?>> annotatorStats = Maps.newHashMap();
    for (Class<? extends CleartkAnnotator<String>> annotatorClass : annotatorClasses) {
      EvaluationOfTimeSpans evaluation = new EvaluationOfTimeSpans(
          new File("target/eval/time-spans"),
          options.getRawTextDirectory(),
          options.getKnowtatorXMLDirectory(),
          options.getXMIDirectory(),
          options.getTreebankDirectory(),
          annotatorClass,
          annotatorTrainingArguments.get(annotatorClass));
      evaluation.prepareXMIsFor(patientSets);
      String name = String.format("%s.errors", annotatorClass.getSimpleName());
      evaluation.setLogging(Level.FINE, new File("target/eval", name));
      AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
      annotatorStats.put(annotatorClass, stats);
    }

    // allow ordering of models by F1
    Ordering<Class<? extends CleartkAnnotator<String>>> byF1 = Ordering.natural().onResultOf(
      new Function<Class<? extends CleartkAnnotator<String>>, Double>() {
        @Override
        public Double apply(
            Class<? extends CleartkAnnotator<String>> annotatorClass) {
          return annotatorStats.get(annotatorClass).f1();
        }
      });

    // print out models, ordered by F1
    for (Class<?> annotatorClass : byF1.sortedCopy(annotatorClasses)) {
      System.err.printf("===== %s =====\n", annotatorClass.getSimpleName());
      System.err.println(annotatorStats.get(annotatorClass));
    }
  }

  private Class<? extends CleartkAnnotator<String>> annotatorClass;

  private String[] trainingArguments;

  public EvaluationOfTimeSpans(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory,
      File treebankDirectory,
      Class<? extends CleartkAnnotator<String>> annotatorClass,
      String[] trainingArguments) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory, treebankDirectory, TimeMention.class);
    this.annotatorClass = annotatorClass;
    this.trainingArguments = trainingArguments;
  }

  @Override
  protected AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        this.annotatorClass,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        LIBLINEARStringOutcomeDataWriter.class,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        this.getModelDirectory(directory));
  }

  @Override
  protected void trainAndPackage(File directory) throws Exception {
    JarClassifierBuilder.trainAndPackage(this.getModelDirectory(directory), this.trainingArguments);
  }

  @Override
  protected AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        this.annotatorClass,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(this.getModelDirectory(directory), "model.jar"));
  }

  @Override
  protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas, Segment segment) {
    return selectExact(jCas, TimeMention.class, segment);
  }

  @Override
  protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas, Segment segment) {
    return selectExact(jCas, TimeMention.class, segment);
  }
  
  private File getModelDirectory(File directory) {
    return new File(directory, this.annotatorClass.getSimpleName());
  }
}
