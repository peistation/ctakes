package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator.HashableArguments;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.Evaluation;
import org.cleartk.eval.provider.BatchBasedEvaluationPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.cleartk.eval.util.ConfusionMatrix;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;
import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.structured.DocumentID;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class RelationExtractorEvaluation {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = true)
    public File trainDirectory;

    @Option(name = "--grid-search", usage = "run a grid search to select the best parameters")
    public boolean gridSearch = false;
    
  }

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());
    List<File> testFiles = Arrays.asList(); // TODO: add a --test-dir option

    // defines train and test corpora
    TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml");
    CorpusReaderProvider readerProvider = new XMICorpusReaderProvider(tsd, trainFiles, testFiles);
    readerProvider.setNumberOfFolds(2);

    // define the grid of parameters over which we will search
    List<ParameterSettings> possibleParams = new ArrayList<ParameterSettings>();
    if (options.gridSearch) {
      for (boolean classifyBothDirections : new boolean[] { false, true }) {
        for (float probabilityOfKeepingANegativeExample : new float[] { 0.1f, 0.15f, 0.2f, 0.25f, 0.5f }) {
          for (double svmCost : new double[] { 0.05, 0.1, 0.5, 1, 5, 10, 50 }) {
            
            // linear kernel (gamma doesn't matter)
            possibleParams.add(new ParameterSettings(
                classifyBothDirections,
                probabilityOfKeepingANegativeExample,
                "linear",
                svmCost,
                1.0));
            
//            // RBF kernel with different gamma values
//            for (double svmGamma : new double[] { 1, 10, 100, 1000 }) {
//              possibleParams.add(new ParameterSettings(
//                  classifyBothDirections,
//                  probabilityOfKeepingANegativeExample,
//                  "radial basis function",
//                  svmCost,
//                  svmGamma));
//            }
          }
        }
      }
    } else {
      possibleParams.add(new ParameterSettings(true, 0.15f, "linear", 0.05, 1.0));
    }

    // run an evaluation for each set of parameters
    Map<ParameterSettings, Double> scoredParams = new HashMap<ParameterSettings, Double>();
    for (ParameterSettings params : possibleParams) {

      // defines pipelines that train a classifier and classify with it
      PipelineProvider pipelineProvider = new PipelineProvider(
          new File("models"),
          MultiClassLIBSVMDataWriterFactory.class, // defined below, no row-normalization
          RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
          params.probabilityOfKeepingANegativeExample,
          RelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
          params.classifyBothDirections,
          RelationExtractorAnnotator.PARAM_PRINT_ERRORS,
          false);

      // defines how to evaluate
      File statisticsFile = new File("models", "collection.statistics");
      File confusionMatrixFile = new File("models", "collection.confusion_matrix.html");
      EvaluationPipelineProvider evaluationProvider = new BatchBasedEvaluationPipelineProvider(
          AnalysisEngineFactory.createPrimitive(
              RelationEvaluator.class,
              RelationEvaluator.PARAM_STATISTICS_FILE,
              statisticsFile.getPath(),
              RelationEvaluator.PARAM_CONFUSION_MATRIX_FILE,
              confusionMatrixFile.getPath()));

      // runs the evaluation
      Evaluation evaluation = new Evaluation();
      evaluation.runCrossValidation(
          readerProvider,
          pipelineProvider,
          evaluationProvider,
          "-t",
          String.valueOf(params.svmKernelIndex),
          "-c",
          String.valueOf(params.svmCost),
          "-g",
          String.valueOf(params.svmGamma));

      // collect the statistics from the evaluation
      FileInputStream stream = new FileInputStream(statisticsFile);
      params.stats = (EvaluationStatistics<?>) SerializationUtils.deserialize(stream);
      stream.close();
      System.err.println(params);
      System.err.println(params.stats);

      // store these parameter settings
      scoredParams.put(params, params.stats.f1());
    }

    // print parameters sorted by F1
    List<ParameterSettings> list = new ArrayList<ParameterSettings>(scoredParams.keySet());
    Function<ParameterSettings, Double> getCount = Functions.forMap(scoredParams);
    Collections.sort(list, Ordering.natural().onResultOf(getCount));

    System.err.println("Summary:");
    ParameterSettings lastParams = null;
    for (ParameterSettings params : list) {
      System.err.printf(
          "F1=%.3f P=%.3f R=%.3f %s\n",
          params.stats.f1(),
          params.stats.precision(),
          params.stats.recall(),
          params);
      lastParams = params;
    }
    if (lastParams != null) {
      System.err.println();
      System.err.println("Best model:");
      System.err.println(lastParams);
      System.err.println(lastParams.stats);
    }
  }

  private static class ParameterSettings {
    public boolean classifyBothDirections;
    
    public float probabilityOfKeepingANegativeExample;
    
    public String svmKernel;
    
    public int svmKernelIndex;

    public double svmCost;
    
    public double svmGamma;

    public EvaluationStatistics<?> stats;
    
    private static List<String> SVM_KERNELS = Arrays.asList(
        "linear",
        "polynomial",
        "radial basis function",
        "sigmoid");

    public ParameterSettings(
        boolean classifyBothDirections,
        float probabilityOfKeepingANegativeExample,
        String svmKernel,
        double svmCost,
        double svmGamma) {
      super();
      this.classifyBothDirections = classifyBothDirections;
      this.probabilityOfKeepingANegativeExample = probabilityOfKeepingANegativeExample;
      this.svmKernel = svmKernel;
      this.svmKernelIndex = SVM_KERNELS.indexOf(this.svmKernel);
      if (this.svmKernelIndex == -1) {
        throw new IllegalArgumentException("Unrecognized kernel: " + this.svmKernel);
      }
      this.svmCost = svmCost;
      this.svmGamma = svmGamma;
    }

    @Override
    public String toString() {
      ToStringHelper helper = Objects.toStringHelper(this);
      helper.add("classifyBothDirections", this.classifyBothDirections);
      helper.add("probabilityOfKeepingANegativeExample", this.probabilityOfKeepingANegativeExample);
      helper.add("svmKernel", this.svmKernel);
      helper.add("svmCost", this.svmCost);
      helper.add("svmGamma", this.svmGamma);
      return helper.toString();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(
          this.classifyBothDirections,
          this.probabilityOfKeepingANegativeExample,
          this.svmKernel,
          this.svmCost,
          this.svmGamma);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ParameterSettings)) {
        return false;
      }
      ParameterSettings that = (ParameterSettings) obj;
      return this.classifyBothDirections == that.classifyBothDirections
          && this.probabilityOfKeepingANegativeExample == that.probabilityOfKeepingANegativeExample
          && this.svmKernel == that.svmKernel
          && this.svmCost == that.svmCost
          && this.svmGamma == that.svmGamma;
    }

  }

  /**
   * Class for adding DocumentID annotations.
   * 
   * Needed because {@link FilesInDirectoryCollectionReader} creates {@link DocumentID} annotations
   * but doesn't allow specific files to be loaded, while {@link FilesCollectionReader} allows
   * specific files to be loaded but creates URIs instead of {@link DocumentID} annotations.
   */
  public static class DocumentIDAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String documentID = new File(ViewURIUtil.getURI(jCas)).getName();
      DocumentID documentIDAnnotation = new DocumentID(jCas);
      documentIDAnnotation.setDocumentID(documentID);
      documentIDAnnotation.addToIndexes();
    }

  }

  /**
   * Class that copies {@link EntityMention} annotations from the CAS with the manual annotations to
   * the CAS that will be used by the system.
   */
  public static class GoldEntityMentionCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      for (EntityMention goldMention : JCasUtil.select(goldView, EntityMention.class)) {
        EntityMention mention = new EntityMention(
            jCas,
            goldMention.getBegin(),
            goldMention.getEnd());
        mention.setTypeID(goldMention.getTypeID());
        mention.setId(goldMention.getId());
        mention.setDiscoveryTechnique(goldMention.getDiscoveryTechnique());
        mention.setConfidence(goldMention.getConfidence());
        mention.addToIndexes();
      }
    }

  }

  /**
   * Class that removes {@link EntityMention} annotations from the CAS's default view
   */
  public static class EntityMentionRemover extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      Collection<EntityMention> mentions = JCasUtil.select(jCas, EntityMention.class);
      // iterate over copy of collection so that we can delete mentions
      for (EntityMention mention : new ArrayList<EntityMention>(mentions)) {
        mention.removeFromIndexes();
      }
    }
  }

  /**
   * Defines how to write training data, train a classifier, and apply the classifier to new data.
   */
  public static class PipelineProvider extends CleartkPipelineProvider_ImplBase {

    private File modelsDirectory;

    private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;

    private Object[] additionalParameters;

    public PipelineProvider(
        File modelsDirectory,
        Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
        Object... additionalParameters) throws UIMAException, IOException {
      this.modelsDirectory = modelsDirectory;
      this.dataWriterFactoryClass = dataWriterFactoryClass;
      this.additionalParameters = additionalParameters;
    }

    private AnalysisEngineDescription getClassifierAnnotatorDescription()
        throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(
          RelationExtractorAnnotator.class,
          this.additionalParameters);
    }

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      // configure the relation extractor for training mode
      AnalysisEngineDescription desc = this.getClassifierAnnotatorDescription();
      ConfigurationParameterFactory.addConfigurationParameters(
          desc,
          RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
          GOLD_VIEW_NAME,
          CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          this.dataWriterFactoryClass.getName(),
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getDir(name).getPath());

      return Arrays.asList(
          // remove cTAKES entities from the system view
          AnalysisEngineFactory.createPrimitive(EntityMentionRemover.class),
          // copy gold mentions into the system view
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          // run the relation extractor
          AnalysisEngineFactory.createPrimitive(desc));
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      // configure the relation extractor for classification mode
      AnalysisEngineDescription desc = this.getClassifierAnnotatorDescription();
      ConfigurationParameterFactory.addConfigurationParameters(
          desc,
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          new File(this.getDir(name), "model.jar").getPath());

      return Arrays.asList(
          // remove cTAKES entities from the system view
          AnalysisEngineFactory.createPrimitive(EntityMentionRemover.class),
          // copy gold mentions into the system view
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          // run the relation extractor
          AnalysisEngineFactory.createPrimitive(desc));
    }

    @Override
    public void train(String name, String... trainingArguments) throws Exception {
      // train the classifier and package it into a .jar file
      HideOutput hider = new HideOutput();
      JarClassifierBuilder.trainAndPackage(this.getDir(name), trainingArguments);
      hider.restoreOutput();
    }

    private File getDir(String name) {
      return new File(this.modelsDirectory, name);
    }
  }

  /**
   * Annotator that compares system-predicted relations to manually-annotated relations.
   */
  public static class RelationEvaluator extends JCasAnnotator_ImplBase {

    public static final String PARAM_STATISTICS_FILE = "StatisticsFile";
    public static final String PARAM_CONFUSION_MATRIX_FILE = "ConfusionMatrixFile";

    @ConfigurationParameter(
        name = PARAM_STATISTICS_FILE,
        mandatory = true,
        description = "The file where overall evaluation statistics should be written")
    private File statisticsFile;
    
    @ConfigurationParameter(
        name = PARAM_CONFUSION_MATRIX_FILE,
        mandatory = true,
        description = "The file where the confusion matrix HTML file should be written")
    private File confusionMatrixFile;


    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // collect the manually annotated relations
      Collection<BinaryTextRelation> goldBinaryTextRelations = JCasUtil.select(goldView, BinaryTextRelation.class);
      List<HashableRelation> goldRelations = new ArrayList<HashableRelation>();
      
      for (BinaryTextRelation relation : goldBinaryTextRelations) {
        goldRelations.add(new HashableRelation(relation));
      }

      // collect the system-predicted relations
      Collection<BinaryTextRelation> systemBinaryTextRelations = JCasUtil.select(jCas, BinaryTextRelation.class);
      List<HashableRelation> systemRelations = new ArrayList<HashableRelation>();
      
      for (BinaryTextRelation relation : systemBinaryTextRelations) {
        systemRelations.add(new HashableRelation(relation));
      }

      // determine which system relations were also in the gold relations
      Set<HashableRelation> intersection = new HashSet<HashableRelation>(goldRelations);
      intersection.retainAll(systemRelations);

      // update stats
      this.batchStats.update(
          categories(goldRelations),
          categories(systemRelations),
          categories(intersection));
      this.collectionStats.update(
          categories(goldRelations),
          categories(systemRelations),
          categories(intersection));
      
      // update confusion matrices
      RelationEvaluator.updateConfusionMatrix(this.batchConfusion, goldBinaryTextRelations, systemBinaryTextRelations);
      RelationEvaluator.updateConfusionMatrix(this.collectionConfusion, goldBinaryTextRelations, systemBinaryTextRelations);
      
    }

    private static List<String> categories(Collection<HashableRelation> relations) {
      List<String> categories = new ArrayList<String>();
      for (HashableRelation relation : relations) {
        categories.add(relation.category);
      }
      return categories;
    }
    
    private static void updateConfusionMatrix(ConfusionMatrix<String> confusionMatrix, Collection<BinaryTextRelation> gold, Collection<BinaryTextRelation> system) {
    	Map<HashableArguments, String> goldRelationSpans = new HashMap<HashableArguments, String>();
    	Map<HashableArguments, String> systemRelationSpans = new HashMap<HashableArguments, String>();
    	
    	for (BinaryTextRelation goldRel : gold) {
    		goldRelationSpans.put(new HashableArguments(goldRel), goldRel.getCategory());
    	}

    	for (BinaryTextRelation sysRel : system) {
    		systemRelationSpans.put(new HashableArguments(sysRel), sysRel.getCategory());
    	}
    	
    	Set<HashableArguments> allSpans = new HashSet<HashableArguments>();
    	allSpans.addAll(goldRelationSpans.keySet());
    	allSpans.addAll(systemRelationSpans.keySet());

    	for (HashableArguments relationKey : allSpans) {
    		String goldCategory = goldRelationSpans.get(relationKey);
    		String sysCategory = systemRelationSpans.get(relationKey);

    		if (goldCategory == null) {
    			goldCategory = RelationExtractorAnnotator.NO_RELATION_CATEGORY;
    		}

    		if (sysCategory == null) {
    			sysCategory = RelationExtractorAnnotator.NO_RELATION_CATEGORY;
    		}

    		confusionMatrix.add(goldCategory, sysCategory);
    	}
    }

    private EvaluationStatistics<String> batchStats;

    private EvaluationStatistics<String> collectionStats;
    
    private ConfusionMatrix<String> batchConfusion;
    
    private ConfusionMatrix<String> collectionConfusion;
    
    

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.batchStats = new EvaluationStatistics<String>();
      this.collectionStats = new EvaluationStatistics<String>();
      this.batchConfusion = new ConfusionMatrix<String>();
      this.collectionConfusion = new ConfusionMatrix<String>();
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
      super.batchProcessComplete();
      System.err.println("Batch:");
      System.err.println(this.batchStats);
      System.err.println(this.batchConfusion.toHTML());
      this.batchStats = new EvaluationStatistics<String>();
      this.batchConfusion = new ConfusionMatrix<String>();
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
      super.collectionProcessComplete();
      try {
        FileOutputStream stream = new FileOutputStream(this.statisticsFile);
        SerializationUtils.serialize(this.collectionStats, stream);
        stream.close();
        
        Files.write(this.collectionConfusion.toHTML(), this.confusionMatrixFile, Charsets.UTF_8);
        
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
      this.collectionStats = new EvaluationStatistics<String>();
    }
    
    
    /**
     * Wrapper for relations that makes two relations with the same spans and category label have
     * the same hash and compare equal.
     */
    private static class HashableRelation extends HashableArguments {

      private String category;

      public HashableRelation(BinaryTextRelation relation) {
    	super(relation);
    	this.category = relation.getCategory();
      }

      @Override
      public boolean equals(Object otherObject) {
        if (otherObject instanceof HashableRelation) {
        	HashableRelation other = (HashableRelation) otherObject;
    	  return super.equals(otherObject) && Objects.equal(this.category, other.category);
        } else {
          return false;
        }
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(
            this.arg1begin,
            this.arg1end,
            this.arg2begin,
            this.arg2end,
            this.category);
      }

      @Override
      public String toString() {
        return String.format(
            "%s(%s,%s,%s,%s,%s)",
            this.getClass().getSimpleName(),
            this.arg1begin,
            this.arg1end,
            this.arg2begin,
            this.arg2end,
            this.category);
      }
    }
  }
}
