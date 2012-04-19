package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.ModifierExtractorAnnotator;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.Evaluation;
import org.cleartk.eval.provider.BatchBasedEvaluationPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Objects;
import edu.mayo.bmi.uima.core.type.textsem.Modifier;

public class ModifierExtractorEvaluation {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = true)
    public File trainDirectory;
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());
    List<File> testFiles = Arrays.asList();
    File modelsDir = new File("models/modifier");

    TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml");
    CorpusReaderProvider readerProvider = new XMICorpusReaderProvider(tsd, trainFiles, testFiles);
    readerProvider.setNumberOfFolds(2);

    CleartkPipelineProvider pipelineProvider = new ModifierPipelineProvider(modelsDir);

    EvaluationPipelineProvider evaluationProvider = new BatchBasedEvaluationPipelineProvider(
        AnalysisEngineFactory.createPrimitive(ModifierEvaluator.class));

    Evaluation evaluation = new Evaluation();
    evaluation.runCrossValidation(
        readerProvider,
        pipelineProvider,
        evaluationProvider,
        "-t",
        "0",
        "-c",
        "1000");
  }

  private static final String GOLD_VIEW_NAME = "GoldView";

  public static class ModifierPipelineProvider extends CleartkPipelineProvider_ImplBase {

    private File modelsDirectory;

    public ModifierPipelineProvider(File modelsDirectory) {
      this.modelsDirectory = modelsDirectory;
    }

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      AnalysisEngineDescription classifierDescription = ModifierExtractorAnnotator.getDescription(
          CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          MultiClassLIBSVMDataWriterFactory.class.getName(),
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getDir(name).getPath());
      return Arrays.asList(
          AnalysisEngineFactory.createPrimitive(OnlyGoldModifiers.class),
          AnalysisEngineFactory.createPrimitive(classifierDescription));
    }

    @Override
    public void train(String name, String... trainingArguments) throws Exception {
      JarClassifierBuilder.trainAndPackage(this.getDir(name), trainingArguments);
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      AnalysisEngineDescription classifierDescription = ModifierExtractorAnnotator.getDescription(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          new File(this.getDir(name), "model.jar").getPath());
      return Arrays.asList(AnalysisEngineFactory.createPrimitive(classifierDescription));
    }

    private File getDir(String name) {
      return new File(this.modelsDirectory, name);
    }
  }

  public static class ModifierEvaluator extends JCasAnnotator_ImplBase {

    private EvaluationStatistics<Integer> batchStats;

    private EvaluationStatistics<Integer> collectionStats;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      Set<HashableModifier> system = getModifiers(jCas);
      for (HashableModifier modifier : system) {
        System.err.println(modifier.modifier.getCoveredText());
      }
      Set<HashableModifier> gold = getModifiers(goldView);
      Set<HashableModifier> intersection = new HashSet<HashableModifier>();
      intersection.addAll(system);
      intersection.retainAll(gold);
      this.batchStats.update(toTypeIDs(gold), toTypeIDs(system), toTypeIDs(intersection));
      this.collectionStats.update(toTypeIDs(gold), toTypeIDs(system), toTypeIDs(intersection));
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.batchStats = new EvaluationStatistics<Integer>();
      this.collectionStats = new EvaluationStatistics<Integer>();
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
      super.batchProcessComplete();
      System.err.println("Batch:");
      System.err.println(this.batchStats);
      this.batchStats = new EvaluationStatistics<Integer>();
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
      super.collectionProcessComplete();
      System.err.println("Collection:");
      System.err.println(this.collectionStats);
      this.collectionStats = new EvaluationStatistics<Integer>();
    }
  }

  /**
   * Class that copies the manual {@link Modifier} annotations to the default CAS.
   */
  public static class OnlyGoldModifiers extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove any automatically generated Modifiers
      for (Modifier modifier : JCasUtil.select(jCas, Modifier.class)) {
        modifier.removeFromIndexes();
      }

      // copy over the manually annotated Modifiers
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

  private static Set<HashableModifier> getModifiers(JCas jCas) {
    Set<HashableModifier> result = new HashSet<HashableModifier>();
    for (Modifier modifier : JCasUtil.select(jCas, Modifier.class)) {
      result.add(new HashableModifier(modifier));
    }
    return result;
  }

  private static List<Integer> toTypeIDs(Set<HashableModifier> modifiers) {
    List<Integer> result = new ArrayList<Integer>();
    for (HashableModifier modifier : modifiers) {
      result.add(modifier.modifier.getTypeID());
    }
    return result;
  }

  public static class HashableModifier {
    private Modifier modifier;

    public HashableModifier(Modifier modifier) {
      this.modifier = modifier;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.modifier.getBegin(), this.modifier.getEnd());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj.getClass() != this.getClass()) {
        return false;
      }
      HashableModifier that = (HashableModifier) obj;
      return Objects.equal(this.modifier.getBegin(), that.modifier.getBegin())
          && Objects.equal(this.modifier.getEnd(), that.modifier.getEnd());
    }

  }
}
