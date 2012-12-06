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
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.ctakes.temporal.ae.EventAnnotator;
import org.apache.ctakes.temporal.ae.feature.selection.Chi2NeighborFSExtractor;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public abstract class EvaluationOfAnnotationSpans_ImplBase extends
    Evaluation_ImplBase<AnnotationStatistics<String>> {

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  public void setLogging(Level level, File outputFile) throws IOException {
    if (!outputFile.getParentFile().exists()) {
      outputFile.getParentFile().mkdirs();
    }
    this.logger.setLevel(level);
    FileHandler handler = new FileHandler(outputFile.getPath());
    handler.setFormatter(new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage() + '\n';
      }
    });
    this.logger.addHandler(handler);
  }

  public EvaluationOfAnnotationSpans_ImplBase(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      List<Integer> patientSets,
      Set<AnnotatorType> annotatorFlags) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, patientSets, annotatorFlags);
  }

  protected abstract AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException;

  protected abstract void trainAndPackage(File directory) throws Exception;

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(this.getPreprocessorTrainDescription());
    aggregateBuilder.add(this.getDataWriterDescription(directory));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
    
    if( EventAnnotator.featureTrim > 0 ){
    	//Extracting features and writing instances
        Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(directory);
        // Collect MinMax stats for feature normalization
        URI chi2NbFsURI = EventAnnotator.createNbFSURI(directory);
        Chi2NeighborFSExtractor<String> chi2NbFsExtractor = new Chi2NeighborFSExtractor<String>(EventAnnotator.FS_NEIGHBOR_EXTRACTOR_KEY, EventAnnotator.featureTrim);
        chi2NbFsExtractor.train(instances);
        chi2NbFsExtractor.save(chi2NbFsURI);
        //now write in the libsvm format
        this.logger.info("Write out model training data");
        LIBSVMStringOutcomeDataWriter dataWriter = new LIBSVMStringOutcomeDataWriter(directory);
        for (Instance<String> instance : instances) {
          instance = chi2NbFsExtractor.transform(instance);
          dataWriter.write(instance);
        }
        dataWriter.finish();
    }
    
    this.trainAndPackage(directory);
  }

  protected abstract AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException;

  protected abstract Collection<? extends Annotation> getGoldAnnotations(JCas jCas);

  protected abstract Collection<? extends Annotation> getSystemAnnotations(JCas jCas);

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(this.getPreprocessorTestDescription());
    aggregateBuilder.add(this.getAnnotatorDescription(directory));

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    Ordering<Annotation> bySpans = Ordering.<Integer> natural().lexicographical().onResultOf(
        new Function<Annotation, List<Integer>>() {
          @Override
          public List<Integer> apply(Annotation annotation) {
            return Arrays.asList(annotation.getBegin(), annotation.getEnd());
          }
        });
    for (JCas jCas : new JCasIterable(collectionReader, aggregateBuilder.createAggregate())) {
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      Collection<? extends Annotation> goldAnnotations = this.getGoldAnnotations(goldView);
      Collection<? extends Annotation> systemAnnotations = this.getSystemAnnotations(systemView);
      stats.add(goldAnnotations, systemAnnotations);

      Set<Annotation> goldSet = new TreeSet<Annotation>(bySpans);
      goldSet.addAll(goldAnnotations);
      Set<Annotation> systemSet = new TreeSet<Annotation>(bySpans);
      systemSet.addAll(systemAnnotations);

      Set<Annotation> goldOnly = new TreeSet<Annotation>(bySpans);
      goldOnly.addAll(goldSet);
      goldOnly.removeAll(systemSet);

      Set<Annotation> systemOnly = new TreeSet<Annotation>(bySpans);
      systemOnly.addAll(systemSet);
      systemOnly.removeAll(goldSet);

      String text = jCas.getDocumentText().replaceAll("[\r\n]", " ");
      if (!goldOnly.isEmpty() || !systemOnly.isEmpty()) {
        this.logger.fine("Errors in : " + ViewURIUtil.getURI(jCas).toString());
        Set<Annotation> errors = new TreeSet<Annotation>(bySpans);
        errors.addAll(goldOnly);
        errors.addAll(systemOnly);
        for (Annotation annotation : errors) {
          int begin = annotation.getBegin();
          int end = annotation.getEnd();
          int windowBegin = Math.max(0, begin - 50);
          int windowEnd = Math.min(text.length(), end + 50);
          String label = goldOnly.contains(annotation) ? "DROPPED:" : "ADDED:  ";
          this.logger.fine(String.format(
              "%s  ...%s[!%s!]%s...",
              label,
              text.substring(windowBegin, begin),
              text.substring(begin, end),
              text.substring(end, windowEnd)));
        }
      }
    }
    return stats;
  }
}
