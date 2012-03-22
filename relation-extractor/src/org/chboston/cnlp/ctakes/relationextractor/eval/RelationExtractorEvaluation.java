package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.eval.Evaluation;
import org.cleartk.eval.provider.BatchBasedEvaluationPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;

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
  }

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());
    List<File> testFiles = Arrays.asList(); // TODO: add a --test-dir option

    // defines train and test corpora
    CorpusReaderProvider readerProvider;
    readerProvider = new CorpusReaderProvider_ImplBase<File>(trainFiles, testFiles) {
      @Override
      protected CollectionReader getReader(List<File> files) throws UIMAException {
        String[] paths = new String[files.size()];
        for (int i = 0; i < paths.length; ++i) {
          paths[i] = files.get(i).getPath();
        }
        return CollectionReaderFactory.createCollectionReader(
            XMIReader.class,
            TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml"),
            XMIReader.PARAM_FILES,
            paths);
      }
    };
    readerProvider.setNumberOfFolds(2);

    // defines pipelines that train a classifier and classify with it
    float probabilityOfKeepingANegativeExample = 0.05f;
    PipelineProvider pipelineProvider = new PipelineProvider(
        new File("models"),
        DefaultMultiClassLIBSVMDataWriterFactory.class,
        probabilityOfKeepingANegativeExample);

    // defines how to evaluate
    EvaluationPipelineProvider evaluationProvider = new BatchBasedEvaluationPipelineProvider(
        AnalysisEngineFactory.createPrimitive(RelationEvaluator.class));

    // runs the evaluation
    Evaluation evaluation = new Evaluation();
    evaluation.runCrossValidation(readerProvider, pipelineProvider, evaluationProvider, "-t", "2", "-c", "1", "-g", "100");
  }

  /**
   * UIMA CollectionReader that reads in CASes from XMI files.
   */
  public static class XMIReader extends JCasCollectionReader_ImplBase {

    public static final String PARAM_FILES = "files";

    @ConfigurationParameter(
        name = PARAM_FILES,
        mandatory = true,
        description = "The XMI files to be loaded")
    private List<File> files;

    private Iterator<File> filesIter;

    private int completed;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.filesIter = files.iterator();
      this.completed = 0;
    }

    @Override
    public Progress[] getProgress() {
      return new Progress[] { new ProgressImpl(
          this.completed,
          this.files.size(),
          Progress.ENTITIES) };
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
      return this.filesIter.hasNext();
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
      FileInputStream inputStream = new FileInputStream(this.filesIter.next());
      try {
        XmiCasDeserializer.deserialize(new BufferedInputStream(inputStream), jCas.getCas());
      } catch (SAXException e) {
        throw new CollectionException(e);
      }
      inputStream.close();
      this.completed += 1;
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

    private float probabilityOfKeepingANegativeExample;

    public PipelineProvider(
        File modelsDirectory,
        Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
        float probabilityOfKeepingANegativeExample) throws UIMAException, IOException {
      this.modelsDirectory = modelsDirectory;
      this.dataWriterFactoryClass = dataWriterFactoryClass;
      this.probabilityOfKeepingANegativeExample = probabilityOfKeepingANegativeExample;
    }

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {

      return Arrays.asList(
          // use gold entities during training (removing cTAKES entities) 
          AnalysisEngineFactory.createPrimitive(EntityMentionRemover.class),
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          // run the relation extractor in training mode
          AnalysisEngineFactory.createPrimitive(
              RelationExtractorAnnotator.class,
              RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
              GOLD_VIEW_NAME,
              CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
              this.dataWriterFactoryClass.getName(),
              DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
              this.getDir(name).getPath(),
              RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
              this.probabilityOfKeepingANegativeExample));
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      // run an annotator that copies over the entity mentions,
      // and the relation extractor in classification mode
      return Arrays.asList(
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          AnalysisEngineFactory.createPrimitive(
              RelationExtractorAnnotator.class,
              GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
              new File(this.getDir(name), "model.jar").getPath()));
    }

    @Override
    public void train(String name, String... trainingArguments) throws Exception {
      // train the classifier and package it into a .jar file
      JarClassifierBuilder.trainAndPackage(this.getDir(name), trainingArguments);
    }

    private File getDir(String name) {
      return new File(this.modelsDirectory, name);
    }
  }

  /**
   * Annotator that compares system-predicted relations to manually-annotated relations.
   */
  public static class RelationEvaluator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // collect the manually annotated relations
      List<HashableRelation> goldRelations = new ArrayList<HashableRelation>();
      for (BinaryTextRelation relation : JCasUtil.select(goldView, BinaryTextRelation.class)) {
        goldRelations.add(new HashableRelation(relation));
      }

      // collect the system-predicted relations
      List<HashableRelation> systemRelations = new ArrayList<HashableRelation>();
      for (BinaryTextRelation relation : JCasUtil.select(jCas, BinaryTextRelation.class)) {
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
    }
    
    private static List<String> categories(Collection<HashableRelation> relations) {
      List<String> categories = new ArrayList<String>();
      for (HashableRelation relation: relations) {
        categories.add(relation.category);
      }
      return categories;
    }

    private EvaluationStatistics<String> batchStats;

    private EvaluationStatistics<String> collectionStats;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.batchStats = new EvaluationStatistics<String>();
      this.collectionStats = new EvaluationStatistics<String>();
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
      super.batchProcessComplete();
      System.err.printf("Batch: %s\n", this.batchStats);
      this.batchStats = new EvaluationStatistics<String>();
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
      super.collectionProcessComplete();
      System.err.printf("Collection: %s\n", this.collectionStats);
      this.collectionStats = new EvaluationStatistics<String>();
    }

    /**
     * Wrapper for relations that makes two relations with the same spans and category label have
     * the same hash and compare equal.
     */
    private static class HashableRelation {
      private int arg1begin;

      private int arg1end;

      private int arg2begin;

      private int arg2end;

      private String category;

      public HashableRelation(
          int arg1begin,
          int arg1end,
          int arg2begin,
          int arg2end,
          String category) {
        this.arg1begin = arg1begin;
        this.arg1end = arg1end;
        this.arg2begin = arg2begin;
        this.arg2end = arg2end;
        this.category = category;
      }

      public HashableRelation(BinaryTextRelation relation) {
        this(
            relation.getArg1().getArgument().getBegin(),
            relation.getArg1().getArgument().getEnd(),
            relation.getArg2().getArgument().getBegin(),
            relation.getArg2().getArgument().getEnd(),
            relation.getCategory());
      }

      @Override
      public boolean equals(Object thatObject) {
        if (thatObject instanceof HashableRelation) {
          HashableRelation that = (HashableRelation) thatObject;
          return this.getClass() == that.getClass() && this.arg1begin == that.arg1begin
              && this.arg1end == that.arg1end && this.arg2begin == that.arg2begin
              && this.arg2end == that.arg2end && Objects.equal(this.category, that.category);
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
