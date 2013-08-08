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

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.syntax.opennlp.ParserAnnotator;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.timeml.event.EventAnnotator;
import org.cleartk.timeml.type.Event;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.uimafit.component.NoOpAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfClearTKEventSpans extends EvaluationOfAnnotationSpans_ImplBase {

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    EvaluationOfClearTKEventSpans evaluation = new EvaluationOfClearTKEventSpans(
        new File("target/eval"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getPatients().getList());
    evaluation.setLogging(Level.FINE, new File("target/eval/cleartk-event-errors.log"));
    List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(4);
    for (AnnotationStatistics<String> stats : foldStats) {
      System.err.println(stats);
    }
    System.err.println("OVERALL");
    System.err.println(AnnotationStatistics.addAll(foldStats));
  }

  public EvaluationOfClearTKEventSpans(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      List<Integer> patientSets) {
    super(
        baseDirectory,
        rawTextDirectory,
        knowtatorXMLDirectory,
        patientSets,
        EnumSet.noneOf(AnnotatorType.class));
  }

  @Override
  protected AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException {
    // not training a model - just using the ClearTK one
    return AnalysisEngineFactory.createPrimitiveDescription(NoOpAnnotator.class);
  }

  @Override
  protected void trainAndPackage(File directory) throws Exception {
    // not training a model - just using the ClearTK one
  }

  @Override
  protected AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(SentenceAnnotator.getDescription());
    aggregateBuilder.add(TokenAnnotator.getDescription());
    aggregateBuilder.add(PosTaggerAnnotator.getDescription());
    aggregateBuilder.add(DefaultSnowballStemmer.getDescription("English"));
    aggregateBuilder.add(ParserAnnotator.getDescription());
    aggregateBuilder.add(EventAnnotator.FACTORY.getAnnotatorDescription());
    return aggregateBuilder.createAggregateDescription();
  }

  @Override
  protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, EventMention.class);
  }

  @Override
  protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, Event.class);
  }
}
