package org.apache.ctakes.temporal.eval;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.HashableArguments;
import org.apache.ctakes.temporal.ae.EventEventRelationAnnotator;
import org.apache.ctakes.temporal.eval.EvaluationOfEventTimeRelations.ParameterSettings;
import org.apache.ctakes.temporal.eval.EvaluationOfEventTimeRelations.RemoveCrossSentenceRelations;
import org.apache.ctakes.temporal.eval.EvaluationOfEventTimeRelations.RemoveNonContainsRelations;
import org.apache.ctakes.temporal.eval.EvaluationOfEventTimeRelations.RemoveRelations;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfEventEventRelations extends
EvaluationOfTemporalRelations_ImplBase {

	private boolean baseline = false;
  public EvaluationOfEventEventRelations(      
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory,
      File treebankDirectory,
      boolean printErrors,
      boolean printRelations,
      boolean baseline,
      ParameterSettings params){
    super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory,
        treebankDirectory, printErrors, printRelations, params);
    this.baseline = baseline;
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception
       {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class, TimeMention.class, BinaryTextRelation.class));
    //	    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(MergeContainsOverlap.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveNonContainsRelations.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveCrossSentenceRelations.class));
    // TODO -- see if this applies to this relation:
    //	    if (this.useClosure) {
    //	      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(AddTransitiveContainsRelations.class));
    //	    }
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(PreserveEventEventRelations.class));
    aggregateBuilder.add(EventEventRelationAnnotator.createDataWriterDescription(
        LIBSVMStringOutcomeDataWriter.class,
//        	        TKSVMlightStringOutcomeDataWriter.class,
        directory,
        1.0));
    SimplePipeline.runPipeline(collectionReader, aggregateBuilder.createAggregate());

    HideOutput hider = new HideOutput();
    // libsvm:
    JarClassifierBuilder.trainAndPackage(directory,  "-t", "0", "-c", "10");
    // tksvmlight with no tk features:
//    JarClassifierBuilder.trainAndPackage(directory, "-t", "0", "-c", "10", "-N", "0");
//    JarClassifierBuilder.trainAndPackage(directory,  "-t", "5", "-S", "0", "-N", "3", "-C", "+", "-T", "1.0");
    hider.restoreOutput();
    hider.close();
  }

  @Override
  protected AnnotationStatistics<String> test(
      CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder aggregateBuilder = this.getPreprocessorAggregateBuilder();
    aggregateBuilder.add(CopyFromGold.getDescription(EventMention.class, TimeMention.class));
    //	    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(MergeContainsOverlap.class,
    //	    		MergeContainsOverlap.PARAM_RELATION_VIEW,
    //	    		GOLD_VIEW_NAME));
    aggregateBuilder.add(
        AnalysisEngineFactory.createPrimitiveDescription(RemoveNonContainsRelations.class),
        CAS.NAME_DEFAULT_SOFA,
        GOLD_VIEW_NAME);
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        RemoveCrossSentenceRelations.class,
        RemoveCrossSentenceRelations.PARAM_SENTENCE_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        RemoveCrossSentenceRelations.PARAM_RELATION_VIEW,
        GOLD_VIEW_NAME));
    // TODO - use if relevant.
    //	    if (this.useClosure) {
    //	      aggregateBuilder.add(
    //	          AnalysisEngineFactory.createPrimitiveDescription(AddTransitiveContainsRelations.class),
    //	          CAS.NAME_DEFAULT_SOFA,
    //	          GOLD_VIEW_NAME);
    //	    }
    aggregateBuilder.add(
        AnalysisEngineFactory.createPrimitiveDescription(PreserveEventEventRelations.class),
        CAS.NAME_DEFAULT_SOFA,
        GOLD_VIEW_NAME);

    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveRelations.class));
    aggregateBuilder.add(
        EventEventRelationAnnotator.createAnnotatorDescription(directory));

    Function<BinaryTextRelation, ?> getSpan = new Function<BinaryTextRelation, HashableArguments>() {
      public HashableArguments apply(BinaryTextRelation relation) {
        return new HashableArguments(relation);
      }
    };
    Function<BinaryTextRelation, String> getOutcome = AnnotationStatistics.annotationToFeatureValue("category");

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    JCasIterable jcasIter =new JCasIterable(collectionReader, aggregateBuilder.createAggregate());
    JCas jCas = null;
    while(jcasIter.hasNext()) {
      jCas = jcasIter.next();
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
            System.out.printf("System labeled %s for %s\n", label, formatRelation(goldRelation));
          } else{
            System.out.println("Nailed it! " + formatRelation(systemRelation));
          }
        }
      }
    }
    return stats;
  }

  public static void main(String[] args) throws Exception{
    TempRelOptions options = CliFactory.parseArguments(TempRelOptions.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    List<Integer> testItems = THYMEData.getTestPatientSets(patientSets);
    File workingDir = new File("target/eval/temporal-relations/event-event/");
    ParameterSettings params = defaultParams;
    EvaluationOfEventEventRelations evaluation = new EvaluationOfEventEventRelations(
        workingDir,
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getXMIDirectory(),
        options.getTreebankDirectory(),
        options.getPrintErrors(),
        options.getPrintFormattedRelations(),
        options.getBaseline(),
        params);
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

}
