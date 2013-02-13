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

import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.HashableArguments;
import org.apache.ctakes.temporal.ae.EventTimeRelationAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.lexicalscope.jewel.cli.CliFactory;

/**
 * Print temporal relations that the system adds to the CAS.
 * This is useful for conversion to an external format (e.g. for evaluation).
 * 
 * @author dmitriy dligach
 */
public class TemporalRelationAnnotationPrinter extends
    EvaluationOfTemporalRelations {

  public static void main(String[] args) throws Exception {
    
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    
    TemporalRelationAnnotationPrinter evaluation = new TemporalRelationAnnotationPrinter(
        new File("target/eval/temporal-relations"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getXMIDirectory());

    AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
    System.err.println(stats);
  }

  public TemporalRelationAnnotationPrinter(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory);
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class, TimeMention.class));
    aggregateBuilder.add(
        AnalysisEngineFactory.createPrimitiveDescription(RemoveNonTLINKRelations.class),
        CAS.NAME_DEFAULT_SOFA,
        GOLD_VIEW_NAME);
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        RemoveCrossSentenceRelations.class,
        RemoveCrossSentenceRelations.PARAM_RELATION_VIEW,
        GOLD_VIEW_NAME));
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
      
      printRelationAnnotations(systemRelations);
      
      stats.add(goldRelations, systemRelations, getSpan, getOutcome);
    }
    return stats;
  }

  private static void printRelationAnnotations(Collection<BinaryTextRelation> relations) {
    
    for(BinaryTextRelation binaryTextRelation : relations) {
      
      Annotation arg1 = binaryTextRelation.getArg1().getArgument();
      Annotation arg2 = binaryTextRelation.getArg2().getArgument();

      String arg1Type = arg1.getClass().getSimpleName();
      String arg2Type = arg2.getClass().getSimpleName();
      
      int arg1Begin = arg1.getBegin();
      int arg1End = arg1.getEnd();
      int arg2Begin = arg2.getBegin();
      int arg2End = arg2.getEnd();
      
      String category = binaryTextRelation.getCategory();

      System.out.format("%s\t%s\t%d\t%d\t%s\t%d\t%d\n", category, arg1Type, arg1Begin, arg1End, arg2Type, arg2Begin, arg2End);
    }
  }
}
