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

import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.HashableArguments;
import org.apache.ctakes.temporal.ae.EventTimeRelationAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
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
import com.google.common.collect.Lists;
import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfTemporalRelations extends
    Evaluation_ImplBase<AnnotationStatistics<String>> {

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    EvaluationOfTemporalRelations evaluation = new EvaluationOfTemporalRelations(
        new File("target/eval/temporal-relations"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory());
    AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
    System.err.println(stats);
  }

  public EvaluationOfTemporalRelations(
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
  protected List<Class<? extends TOP>> getAnnotationClassesThatShouldBeGoldAtTestTime() {
    List<Class<? extends TOP>> result = super.getAnnotationClassesThatShouldBeGoldAtTestTime();
    result.add(EventMention.class);
    result.add(TimeMention.class);
    return result;
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(this.getPreprocessorTrainDescription());
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveNonTLINKRelations.class));
    aggregateBuilder.add(EventTimeRelationAnnotator.createDataWriterDescription(
        LIBSVMStringOutcomeDataWriter.class,
        directory,
        1.0));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
    JarClassifierBuilder.trainAndPackage(directory, "-c", "1000");
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(this.getPreprocessorTestDescription());
    aggregateBuilder.add(
        AnalysisEngineFactory.createPrimitiveDescription(RemoveNonTLINKRelations.class),
        CAS.NAME_DEFAULT_SOFA,
        GOLD_VIEW_NAME);
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveRelations.class));
    aggregateBuilder.add(EventTimeRelationAnnotator.createAnnotatorDescription(directory));

    Function<BinaryTextRelation, ?> getSpan = new Function<BinaryTextRelation, HashableArguments>() {
      public HashableArguments apply(BinaryTextRelation relation) {
        return new HashableArguments(relation);
      }
    };
    Function<BinaryTextRelation, String> getOutcome = AnnotationStatistics.annotationToFeatureValue("category");

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    for (JCas jCas : new JCasIterable(collectionReader, aggregateBuilder.createAggregate())) {
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      Collection<BinaryTextRelation> goldRelations = JCasUtil.select(
          goldView,
          BinaryTextRelation.class);
      Collection<BinaryTextRelation> systemRelations = JCasUtil.select(
          systemView,
          BinaryTextRelation.class);
      stats.add(goldRelations, systemRelations, getSpan, getOutcome);
    }
    return stats;
  }

  public static class RemoveNonTLINKRelations extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(
          jCas,
          BinaryTextRelation.class))) {
        if (!relation.getCategory().startsWith("TLINK")) {
          relation.getArg1().removeFromIndexes();
          relation.getArg2().removeFromIndexes();
          relation.removeFromIndexes();
        }
      }
    }
  }

  public static class RemoveRelations extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(
          jCas,
          BinaryTextRelation.class))) {
        relation.getArg1().removeFromIndexes();
        relation.getArg2().removeFromIndexes();
        relation.removeFromIndexes();
      }
    }
  }
}
