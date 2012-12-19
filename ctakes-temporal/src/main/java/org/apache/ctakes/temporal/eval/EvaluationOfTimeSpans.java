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
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.TimeAnnotator;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.util.JCasUtil;

import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfTimeSpans extends EvaluationOfAnnotationSpans_ImplBase {

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    EvaluationOfTimeSpans evaluation = new EvaluationOfTimeSpans(
        new File("target/eval"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory());
    evaluation.setLogging(Level.FINE, new File("target/eval/ctakes-time-errors.log"));
    List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(
        options.getPatients().getList(),
        4);
    for (AnnotationStatistics<String> stats : foldStats) {
      System.err.println(stats);
    }
    System.err.println("OVERALL");
    System.err.println(AnnotationStatistics.addAll(foldStats));
  }

  public EvaluationOfTimeSpans(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory) {
    super(
        baseDirectory,
        rawTextDirectory,
        knowtatorXMLDirectory,
        EnumSet.of(AnnotatorType.PART_OF_SPEECH_TAGS));
  }

  @Override
  protected AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException {
    return TimeAnnotator.createDataWriterDescription(LIBSVMStringOutcomeDataWriter.class, directory);
  }

  @Override
  protected void trainAndPackage(File directory) throws Exception {
    JarClassifierBuilder.trainAndPackage(directory, "-c", "10000");
  }

  @Override
  protected AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException {
    return TimeAnnotator.createAnnotatorDescription(directory);
  }

  @Override
  protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, TimeMention.class);
  }

  @Override
  protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, TimeMention.class);
  }
}
