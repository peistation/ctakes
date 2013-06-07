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
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.EventAnnotator;
import org.apache.ctakes.temporal.ae.feature.selection.FeatureSelection;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class EvaluationOfEventSpans extends EvaluationOfAnnotationSpans_ImplBase {

  static interface Options extends Evaluation_ImplBase.Options {

    @Option(longName = "downratio", defaultValue = "1")
    public float getProbabilityOfKeepingANegativeExample();

    @Option(longName = "featureSelectionThreshold", defaultValue = "0")
    public float getFeatureSelectionThreshold();

    @Option(longName = "SMOTENeighborNumber", defaultValue = "1")
    public float getSMOTENeighborNumber();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    EvaluationOfEventSpans evaluation = new EvaluationOfEventSpans(
        new File("target/eval/event-spans"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getXMIDirectory(),
        options.getProbabilityOfKeepingANegativeExample(),
        options.getFeatureSelectionThreshold(),
        options.getSMOTENeighborNumber());
    evaluation.prepareXMIsFor(patientSets);
    evaluation.setLogging(Level.FINE, new File("target/eval/ctakes-event-errors.log"));
    AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
    System.err.println(stats);
  }

  private float probabilityOfKeepingANegativeExample;

  private float featureSelectionThreshold;

  private float smoteNeighborNumber;

  public EvaluationOfEventSpans(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory,
      float probabilityOfKeepingANegativeExample,
      float featureSelectionThreshold,
      float numOfSmoteNeighbors) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory, EventMention.class);
    this.probabilityOfKeepingANegativeExample = probabilityOfKeepingANegativeExample;
    this.featureSelectionThreshold = featureSelectionThreshold;
    this.smoteNeighborNumber = numOfSmoteNeighbors;
  }

  @Override
  protected AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException {
    Class<?> dataWriterClass = this.featureSelectionThreshold > 0f
        ? InstanceDataWriter.class
        : LIBLINEARStringOutcomeDataWriter.class;
    return EventAnnotator.createDataWriterDescription(
        dataWriterClass,
        directory,
        this.probabilityOfKeepingANegativeExample,
        this.featureSelectionThreshold,
        this.smoteNeighborNumber);
  }

  @Override
  protected void trainAndPackage(File directory) throws Exception {
    if (this.featureSelectionThreshold > 0) {
      // Extracting features and writing instances
      Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(directory);
      // Collect MinMax stats for feature normalization
      FeatureSelection<String> featureSelection = EventAnnotator.createFeatureSelection(this.featureSelectionThreshold);
      featureSelection.train(instances);
      featureSelection.save(EventAnnotator.createFeatureSelectionURI(directory));
      // now write in the libsvm format
      LIBLINEARStringOutcomeDataWriter dataWriter = new LIBLINEARStringOutcomeDataWriter(directory);
      for (Instance<String> instance : instances) {
        dataWriter.write(featureSelection.transform(instance));
      }
      dataWriter.finish();
    }

    JarClassifierBuilder.trainAndPackage(directory, "-c", "0.05");
  }

  @Override
  protected AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException {
    return EventAnnotator.createAnnotatorDescription(directory);
  }

  @Override
  protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas) {
    return selectExact(jCas, EventMention.class);
  }

  @Override
  protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas) {
    return selectExact(jCas, EventMention.class);
  }
}
