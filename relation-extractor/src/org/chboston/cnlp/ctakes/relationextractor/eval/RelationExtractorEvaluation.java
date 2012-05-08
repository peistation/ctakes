package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.chboston.cnlp.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator.HashableArguments;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Ordering;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;
import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.structured.DocumentID;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.Modifier;

public class RelationExtractorEvaluation extends Evaluation_ImplBase<File, AnnotationStatistics> {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = true)
    public File trainDirectory;

    @Option(name = "--grid-search", usage = "run a grid search to select the best parameters")
    public boolean gridSearch = false;

    @Option(
        name = "--run-degree-of",
        usage = "if true runs the degree of relation extractor otherwise "
            + "it uses the normal entity mention pair relation extractor")
    public boolean runDegreeOf = false;

  }

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());

    // define the output directory for models
    File modelsDir = options.runDegreeOf
        ? new File("models/degree_of")
        : new File("models/em_pair");

    // determine class for the classifier annotator
    Class<? extends RelationExtractorAnnotator> annotatorClass = options.runDegreeOf
        ? DegreeOfRelationExtractorAnnotator.class
        : EntityMentionPairRelationExtractorAnnotator.class;

    // determine the type of classifier to be trained
    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = MultiClassLIBSVMDataWriterFactory.class;

    // define the set of possible training parameters
    List<ParameterSettings> possibleParams = options.runDegreeOf
        ? getDegreeOfParameterSpace(options.gridSearch)
        : getEMPairParameterSpace(options.gridSearch);

    // run an evaluation for each set of parameters
    Map<ParameterSettings, Double> scoredParams = new HashMap<ParameterSettings, Double>();
    for (ParameterSettings params : possibleParams) {
      System.err.println(params);
      System.err.println();

      // define additional configuration parameters for the annotator
      Object[] additionalParameters = new Object[] {
          RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
          params.probabilityOfKeepingANegativeExample,
          RelationExtractorAnnotator.PARAM_PRINT_ERRORS,
          false };

      // define arguments to be passed to the classifier
      String[] trainingArguments = new String[] {
          "-t",
          String.valueOf(params.svmKernelIndex),
          "-c",
          String.valueOf(params.svmCost),
          "-g",
          String.valueOf(params.svmGamma) };

      // create the evaluation
      RelationExtractorEvaluation evaluation = new RelationExtractorEvaluation(
          modelsDir,
          annotatorClass,
          dataWriterFactoryClass,
          additionalParameters,
          trainingArguments);
      List<AnnotationStatistics> foldStats = evaluation.crossValidation(trainFiles, 2);
      params.stats = AnnotationStatistics.addAll(foldStats);

      System.err.println("overall:");
      System.err.print(params.stats);
      System.err.println(params.stats.confusions());
      System.err.println();
      
      // store these parameter settings
      scoredParams.put(params, params.stats.f1());
    }

    // print parameters sorted by F1
    List<ParameterSettings> list = new ArrayList<ParameterSettings>(scoredParams.keySet());
    Function<ParameterSettings, Double> getCount = Functions.forMap(scoredParams);
    Collections.sort(list, Ordering.natural().onResultOf(getCount));

    // print performance of each set of parameters
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

    // print overall best model
    if (lastParams != null) {
      System.err.println();
      System.err.println("Best model:");
      System.err.print(lastParams.stats);
      System.err.println(lastParams);
      System.err.println(lastParams.stats.confusions());
      System.err.println();
      System.err.println(lastParams.stats.confusions().toHTML());
    }
  }

  /**
   * An evaluation of a relation extractor.
   * 
   * @param baseDirectory
   *          The directory where models, etc. should be written
   * @param classifierAnnotatorClass
   *          The CleartkAnnotator class that learns a relation extractor model
   * @param dataWriterFactoryClass
   *          The DataWriterFactory defining what type of classifier to train
   * @param additionalParameters
   *          Additional parameters that should be supplied when creating the CleartkAnnotator
   * @param trainingArguments
   *          Arguments that should be passed to the classifier's train method
   */
  public RelationExtractorEvaluation(
      File baseDirectory,
      Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass,
      Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
      Object[] additionalParameters,
      String[] trainingArguments) {
    super(baseDirectory);
    this.classifierAnnotatorClass = classifierAnnotatorClass;
    this.dataWriterFactoryClass = dataWriterFactoryClass;
    this.additionalParameters = additionalParameters;
    this.trainingArguments = trainingArguments;
  }

  private Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass;

  private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;

  private Object[] additionalParameters;

  private String[] trainingArguments;

  @Override
  protected CollectionReader getCollectionReader(List<File> items) throws Exception {
    // convert the List<File> to a String[]
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }
    // return a reader that will load each of the XMI files
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml"),
        XMIReader.PARAM_FILES,
        paths);
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    // remove cTAKES entity mentions and modifiers from the system view
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(EntityMentionRemover.class));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(ModifierRemover.class));
    // copy gold entity mentions and modifiers into the system view
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(GoldEntityMentionCopier.class));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(GoldModifierCopier.class));
    // add the relation extractor, configured for training mode
    AnalysisEngineDescription classifierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
        this.classifierAnnotatorClass,
        this.additionalParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
        RelationExtractorEvaluation.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        directory.getPath());
    builder.add(classifierAnnotator);

    // run the data-writing pipeline
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // train the classifier and package it into a .jar file
    HideOutput hider = new HideOutput();
    JarClassifierBuilder.trainAndPackage(directory, this.trainingArguments);
    hider.restoreOutput();
  }

  @Override
  protected AnnotationStatistics test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    // remove cTAKES entity mentions and modifiers from the system view
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(EntityMentionRemover.class));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(ModifierRemover.class));
    // copy gold entity mentions and modifiers into the system view
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(GoldEntityMentionCopier.class));
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(GoldModifierCopier.class));
    // add the relation extractor, configured for classification mode
    AnalysisEngineDescription classifierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
        this.classifierAnnotatorClass,
        this.additionalParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(directory, "model.jar").getPath());
    builder.add(classifierAnnotator);

    // statistics will be based on the "category" feature of the BinaryTextRelations
    AnnotationStatistics stats = new AnnotationStatistics("category");

    // calculate statistics, iterating over the results of the classifier
    AnalysisEngine engine = builder.createAggregate();
    for (JCas jCas : new JCasIterable(collectionReader, engine)) {

      // get the gold view
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // get the gold and system annotations
      Collection<BinaryTextRelation> goldBinaryTextRelations = JCasUtil.select(
          goldView,
          BinaryTextRelation.class);
      Collection<BinaryTextRelation> systemBinaryTextRelations = JCasUtil.select(
          jCas,
          BinaryTextRelation.class);

      // update the statistics based on the argument spans of the relation
      stats.add(
          goldBinaryTextRelations,
          systemBinaryTextRelations,
          new Function<BinaryTextRelation, HashableArguments>() {
            @Override
            public HashableArguments apply(BinaryTextRelation relation) {
              return new HashableArguments(relation);
            }
          });
    }

    System.err.println(directory.getName() + ":");
    System.err.print(stats);
    System.err.println(stats.confusions());
    System.err.println();
    return stats;
  }

  /**
   * Defines the parameter space for Entity Mention Pair Relation Extraction
   * 
   * @param gridSearch
   * @return
   */
  private static List<ParameterSettings> getEMPairParameterSpace(boolean gridSearch) {
    // define the grid of parameters over which we will search
    List<ParameterSettings> possibleParams = new ArrayList<ParameterSettings>();
    if (gridSearch) {
      for (boolean classifyBothDirections : new boolean[] { false, true }) {
        for (float probabilityOfKeepingANegativeExample : new float[] {
            0.1f,
            0.15f,
            0.2f,
            0.25f,
            0.5f }) {
          for (double svmCost : new double[] { 0.05, 0.1, 0.5, 1, 5, 10, 50 }) {
            // linear kernel (gamma doesn't matter)
            possibleParams.add(new ParameterSettings(
                classifyBothDirections,
                probabilityOfKeepingANegativeExample,
                "linear",
                svmCost,
                1.0));
          }
        }
      }
    } else {
      possibleParams.add(new ParameterSettings(true, 0.15f, "linear", 0.05, 1.0));
    }
    return possibleParams;

  }

  /**
   * Defines the parameter space for Degree_Of Relation Extraction
   * 
   * @param gridSearch
   * @return
   */
  private static List<ParameterSettings> getDegreeOfParameterSpace(boolean gridSearch) {
    // define the grid of parameters over which we will search
    List<ParameterSettings> possibleParams = new ArrayList<ParameterSettings>();
    if (gridSearch) {
      for (float probabilityOfKeepingANegativeExample : new float[] { 0.5f, 1.0f }) {
        for (double svmCost : new double[] { 0.05, 0.1, 0.5, 1, 5, 10, 50 }) {
          // linear kernel (gamma doesn't matter)
          possibleParams.add(new ParameterSettings(
              false,
              probabilityOfKeepingANegativeExample,
              "linear",
              svmCost,
              1.0));
        }
      }
    } else {
      possibleParams.add(new ParameterSettings(false, 1.0f, "linear", 0.1, 1.0));
    }
    return possibleParams;
  }

  /**
   * Holds a set of parameters for a relation extraction model
   */
  private static class ParameterSettings {
    public boolean classifyBothDirections;

    public float probabilityOfKeepingANegativeExample;

    public String svmKernel;

    public int svmKernelIndex;

    public double svmCost;

    public double svmGamma;

    public AnnotationStatistics stats;

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
          && this.svmKernel == that.svmKernel && this.svmCost == that.svmCost
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
   * Class that copies {@link Modifier} annotations from the CAS with the manual annotations to the
   * CAS that will be used by the system.
   */
  public static class GoldModifierCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      for (Modifier goldModifier : JCasUtil.select(goldView, Modifier.class)) {
        Modifier modifier = new Modifier(jCas, goldModifier.getBegin(), goldModifier.getEnd());
        modifier.setTypeID(goldModifier.getTypeID());
        modifier.setId(goldModifier.getId());
        modifier.setDiscoveryTechnique(goldModifier.getDiscoveryTechnique());
        modifier.setConfidence(goldModifier.getConfidence());
        modifier.addToIndexes();
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
   * Class that removes {@link Modifier} annotations from the CAS's default view
   */
  public static class ModifierRemover extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      Collection<Modifier> modifiers = JCasUtil.select(jCas, Modifier.class);
      // iterate over copy of collection so that we can delete mentions
      for (Modifier modifier : new ArrayList<Modifier>(modifiers)) {
        modifier.removeFromIndexes();
      }
    }
  }
}
