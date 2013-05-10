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
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.HashableArguments;
import org.apache.ctakes.temporal.ae.EventTimeRelationAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.relation.TemporalTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class EvaluationOfTemporalRelations extends
    Evaluation_ImplBase<AnnotationStatistics<String>> {
	
  static interface TempRelOptions extends Evaluation_ImplBase.Options{
    @Option
    public boolean getTest();

    @Option
    public boolean getPrintFormattedRelations();
    
    @Option
    public boolean getClosure();
  }

  public static void main(String[] args) throws Exception {
    TempRelOptions options = CliFactory.parseArguments(TempRelOptions.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    List<Integer> testItems = THYMEData.getTestPatientSets(patientSets);
    
    EvaluationOfTemporalRelations evaluation = new EvaluationOfTemporalRelations(
        new File("target/eval/temporal-relations"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getXMIDirectory(),
        options.getTreebankDirectory(),
        options.getClosure(),
        options.getPrintErrors(),
        options.getPrintFormattedRelations());
    evaluation.prepareXMIsFor(patientSets);
    List<Integer> training = trainItems;
    List<Integer> testing = null;
    if(options.getTest()){
      training.addAll(devItems);
      testing = testItems;
    }else{
      testing = devItems;
    }    
    AnnotationStatistics<String> stats = evaluation.trainAndTest(training, testing);
    System.err.println(stats);
  }

  protected boolean useClosure;
  
  protected boolean printRelations = false;
  
  public EvaluationOfTemporalRelations(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory,
      File treebankDirectory,
      boolean useClosure,
      boolean printErrors,
      boolean printRelations) {
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory, treebankDirectory);
    this.useClosure = useClosure;
    this.printErrors = printErrors;
    this.printRelations = printRelations;
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class, TimeMention.class, BinaryTextRelation.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveNonTLINKRelations.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveCrossSentenceRelations.class));
    if (this.useClosure) {
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(AddTransitiveContainsRelations.class));
    }
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveEventEventRelations.class));
    aggregateBuilder.add(EventTimeRelationAnnotator.createDataWriterDescription(
        LIBSVMStringOutcomeDataWriter.class,
        directory,
        1.0));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());
    JarClassifierBuilder.trainAndPackage(directory, "-t", "2", "-d", "2", "-c", "10");
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
        RemoveCrossSentenceRelations.PARAM_SENTENCE_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        RemoveCrossSentenceRelations.PARAM_RELATION_VIEW,
        GOLD_VIEW_NAME));
    if (this.useClosure) {
      aggregateBuilder.add(
          AnalysisEngineFactory.createPrimitiveDescription(AddTransitiveContainsRelations.class),
          CAS.NAME_DEFAULT_SOFA,
          GOLD_VIEW_NAME);
    }
    aggregateBuilder.add(
        AnalysisEngineFactory.createPrimitiveDescription(RemoveEventEventRelations.class),
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
      if(this.printRelations){
    	  URI uri = ViewURIUtil.getURI(jCas);
    	  String[] path = uri.getPath().split("/");
    	  printRelationAnnotations(path[path.length - 1], systemRelations);
      }

      if(this.printErrors){
    	  Map<HashableArguments, BinaryTextRelation> goldMap = Maps.newHashMap();
    	  for (BinaryTextRelation relation : goldRelations) {
    		  goldMap.put(new HashableArguments(relation), relation);
    	  }
    	  Map<HashableArguments, BinaryTextRelation> systemMap = Maps.newHashMap();
    	  for (BinaryTextRelation relation : systemRelations) {
    		  systemMap.put(new HashableArguments(relation), relation);
    	  }
    	  Set<HashableArguments> all = Sets.union(goldMap.keySet(), systemMap.keySet());
    	  List<HashableArguments> sorted = Lists.newArrayList(all);
    	  Collections.sort(sorted);
    	  for (HashableArguments key : sorted) {
    		  BinaryTextRelation goldRelation = goldMap.get(key);
    		  BinaryTextRelation systemRelation = systemMap.get(key);
    		  if (goldRelation == null) {
    			  System.out.println("System added: " + formatRelation(systemRelation));
    		  } else if (systemRelation == null) {
    			  System.out.println("System dropped: " + formatRelation(goldRelation));
    		  } else if (!systemRelation.getCategory().equals(goldRelation.getCategory())) {
    			  String label = systemRelation.getCategory();
    			  System.out.printf("System labeled %s for %s\n", label, formatRelation(systemRelation));
    		  }
    	  }
      }
    }
    return stats;
  }

  private static String formatRelation(BinaryTextRelation relation) {
	  IdentifiedAnnotation arg1 = (IdentifiedAnnotation)relation.getArg1().getArgument();
	  IdentifiedAnnotation arg2 = (IdentifiedAnnotation)relation.getArg2().getArgument();
	  String text = arg1.getCAS().getDocumentText();
	  int begin = Math.min(arg1.getBegin(), arg2.getBegin());
	  int end = Math.max(arg1.getBegin(), arg2.getBegin());
	  begin = Math.max(0, begin - 50);
	  end = Math.min(text.length(), end + 50);
	  return String.format(
			  "%s(%s(type=%d), %s(type=%d)) in ...%s...",
			  relation.getCategory(),
			  arg1.getCoveredText(),
			  arg1.getTypeID(),
			  arg2.getCoveredText(),
			  arg2.getTypeID(),
			  text.substring(begin, end).replaceAll("[\r\n]", " "));
  }

  private static void printRelationAnnotations(String fileName, Collection<BinaryTextRelation> relations) {

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

		  System.out.format("%s\t%s\t%s\t%d\t%d\t%s\t%d\t%d\n", 
				  fileName, category, arg1Type, arg1Begin, arg1End, arg2Type, arg2Begin, arg2End);
	  }
  }

  public static class RemoveEventEventRelations extends JCasAnnotator_ImplBase {

	  @Override
	  public void process(JCas jCas) throws AnalysisEngineProcessException {
		  for(BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(jCas, BinaryTextRelation.class))){
			  if(relation.getCategory().equals("CONTAINS")){
				  RelationArgument arg1 = relation.getArg1();
				  RelationArgument arg2 = relation.getArg2();
				  if(arg1.getArgument() instanceof TimeMention && arg2.getArgument() instanceof EventMention ||
						  arg1.getArgument() instanceof EventMention && arg2.getArgument() instanceof TimeMention){
					  // these are the kind we keep.
					  continue;
				  }
				  arg1.removeFromIndexes();
				  arg2.removeFromIndexes();
				  relation.removeFromIndexes();
			  }
		  }
	  }	  
  }

  public static class RemoveNonTLINKRelations extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(
          jCas,
          BinaryTextRelation.class))) {
        if (!(relation instanceof TemporalTextRelation)) {
          relation.getArg1().removeFromIndexes();
          relation.getArg2().removeFromIndexes();
          relation.removeFromIndexes();
        }
      }
    }
  }

  public static class RemoveCrossSentenceRelations extends JCasAnnotator_ImplBase {

    public static final String PARAM_SENTENCE_VIEW = "SentenceView";

    @ConfigurationParameter(name = PARAM_SENTENCE_VIEW)
    private String sentenceViewName = CAS.NAME_DEFAULT_SOFA;

    public static final String PARAM_RELATION_VIEW = "RelationView";

    @ConfigurationParameter(name = PARAM_RELATION_VIEW)
    private String relationViewName = CAS.NAME_DEFAULT_SOFA;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas sentenceView, relationView;
      try {
        sentenceView = jCas.getView(this.sentenceViewName);
        relationView = jCas.getView(this.relationViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // map events and times to the sentences that contain them
      Map<IdentifiedAnnotation, Integer> sentenceIndex = Maps.newHashMap();
      int index = -1;
      for (Sentence sentence : JCasUtil.select(sentenceView, Sentence.class)) {
        ++index;
        for (EventMention event : JCasUtil.selectCovered(relationView, EventMention.class, sentence)) {
          sentenceIndex.put(event, index);
        }
        for (TimeMention time : JCasUtil.selectCovered(relationView, TimeMention.class, sentence)) {
          sentenceIndex.put(time, index);
        }
      }

      // remove any relations that are in different sentences.
      for (BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(
          relationView,
          BinaryTextRelation.class))) {
        Integer sent1 = sentenceIndex.get(relation.getArg1().getArgument());
        Integer sent2 = sentenceIndex.get(relation.getArg2().getArgument());
        if (sent1 == null || sent2 == null || !sent1.equals(sent2)) {
          relation.getArg1().removeFromIndexes();
          relation.getArg2().removeFromIndexes();
          relation.removeFromIndexes();
        }
      }
    }
  }

  public static class RemoveNonContainsRelations extends JCasAnnotator_ImplBase {
	  @Override
	  public void process(JCas jCas) throws AnalysisEngineProcessException {
		  for (BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(
				  jCas,
				  BinaryTextRelation.class))) {
			  if (!relation.getCategory().startsWith("CONTAINS")) {
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
  
  public static class AddTransitiveContainsRelations extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      
      // collect many-to-many mappings of containment relations 
      Multimap<Annotation, Annotation> isContainedIn = HashMultimap.create();
      Multimap<Annotation, Annotation> contains = HashMultimap.create();
      Set<BinaryTextRelation> containsRelations = Sets.newHashSet();
      for (BinaryTextRelation relation : JCasUtil.select(jCas, BinaryTextRelation.class)) {
        if (relation.getCategory().equals("CONTAINS")) {
          containsRelations.add(relation);
          Annotation arg1 = relation.getArg1().getArgument();
          Annotation arg2 = relation.getArg2().getArgument();
          contains.put(arg1, arg2);
          isContainedIn.put(arg2, arg1);
        }
      }

      // look for X -> Y -> Z containment chains and add X -> Z relations
      Deque<Annotation> todo = new ArrayDeque<Annotation>(isContainedIn.keySet());
      while (!todo.isEmpty()) {
        Annotation next = todo.removeFirst();
        for (Annotation parent : Lists.newArrayList(isContainedIn.get(next))) {
          for (Annotation grandParent : Lists.newArrayList(isContainedIn.get(parent))) {
            if (!isContainedIn.containsEntry(next, grandParent)) {
              isContainedIn.put(next, grandParent);
              contains.put(grandParent, next);
              
              // once X -> Z has been added, we need to re-do all W where W -> X
              for (Annotation child : contains.get(next)) {
                todo.add(child);
              }
            }
          }
        }
      }
      
      // remove old relations
      for (BinaryTextRelation relation : containsRelations) {
        relation.getArg1().removeFromIndexes();
        relation.getArg2().removeFromIndexes();
        relation.removeFromIndexes();
      }
      
      // add new, transitive relations
      for (Annotation contained : isContainedIn.keySet()) {
        for (Annotation container : isContainedIn.get(contained)) {
          RelationArgument arg1 = new RelationArgument(jCas);
          arg1.setArgument(container);
          RelationArgument arg2 = new RelationArgument(jCas);
          arg2.setArgument(contained);
          BinaryTextRelation relation = new BinaryTextRelation(jCas);
          relation.setArg1(arg1);
          relation.setArg2(arg2);
          relation.setCategory("CONTAINS");
          arg1.addToIndexes();
          arg2.addToIndexes();
          relation.addToIndexes();
        }
      }
    }
    
  }
}
