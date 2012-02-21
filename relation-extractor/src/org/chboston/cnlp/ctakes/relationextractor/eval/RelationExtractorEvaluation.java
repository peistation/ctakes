package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.eval.Evaluation;
import org.cleartk.eval.provider.BatchBasedEvaluationPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.cleartk.eval.provider.FilesCollectionReaderProvider;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.cr.FilesCollectionReader;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Objects;

import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;
import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.structured.DocumentID;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class RelationExtractorEvaluation {

  public static final String GOLD_VIEW_NAME = "GoldView";
  
  public static void main(String[] args) throws Exception {
    // check command-line arguments
    if (args.length != 2) {
      String className = RelationExtractorEvaluation.class.getName();
      System.err.printf("usage: java %s text-dir xml-dir\n", className);
      System.exit(1);
    }
    
    // Something like /Volumes/sharp/NLP/Corpus/MiPACQ/Text/
    File textRoot = new File(args[0]);
    // Something like /Volumes/sharp/NLP/Corpus/MiPACQ/UMLS/XML_exported_corpus_1_2_show_all/
    File xmlRoot = new File(args[1]);
    // the pre-processor descriptor file
    File preprocessDescFile = new File("desc/analysis_engine/RelationExtractorPreprocessor.xml");

    // randomized selection of train/text, should be replaced with an official train and test set
    List<String> textFileNames = Arrays.asList(textRoot.list());
    Collections.shuffle(textFileNames, new Random(42L));
    int nFiles = Math.min(20, textFileNames.size()); // FIXME: use whole set
    List<String> trainFileNames = textFileNames.subList(0, nFiles / 2);
    List<String> testFileNames = textFileNames.subList(nFiles / 2, nFiles);

    // defines train and test corpora
    CorpusReaderProvider readerProvider = new FilesCollectionReaderProvider(
        textRoot,
        trainFileNames,
        testFileNames);
    readerProvider.setNumberOfFolds(2);

    // defines pipelines that load training data and train a classifier
    PipelineProvider pipelineProvider = new PipelineProvider(
        preprocessDescFile,
        xmlRoot,
        new File("models"),
        DefaultMultiClassLIBSVMDataWriterFactory.class);

    // defines how to evaluate
    EvaluationPipelineProvider evaluationProvider = new BatchBasedEvaluationPipelineProvider(
        AnalysisEngineFactory.createPrimitive(RelationEvaluator.class));

    // runs the evaluation
    Evaluation evaluation = new Evaluation();
    evaluation.runCrossValidation(readerProvider, pipelineProvider, evaluationProvider);
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
   * Class that copies {@link EntityMention} annotations from the CAS with the manual annotations
   * to the CAS that will be used by the system.
   */
  public static class EntityMentionCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      for (EntityMention goldMention : JCasUtil.select(goldView, EntityMention.class)) {
        EntityMention mention = new EntityMention(jCas, goldMention.getBegin(), goldMention.getEnd());
        mention.setTypeID(goldMention.getTypeID());
        mention.setId(goldMention.getId());
        mention.setDiscoveryTechnique(goldMention.getDiscoveryTechnique());
        mention.setConfidence(goldMention.getConfidence());
        mention.addToIndexes();
      }
    }
    
  }

  /**
   * Defines how to write training data, train a classifier, and apply the classifier to new data.
   */
  public static class PipelineProvider extends CleartkPipelineProvider_ImplBase {
    
    private File knowtatorXMLDirectory;

    private File modelsDirectory;

    private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;

    private final AnalysisEngine preprocessing;

    private AnalysisEngine goldAnnotator;

    public PipelineProvider(
        File preprocessDescFile,
        File knowtatorXMLDirectory,
        File modelsDirectory,
        Class<? extends DataWriterFactory<String>> dataWriterFactoryClass) throws UIMAException,
        IOException {
      this.knowtatorXMLDirectory = knowtatorXMLDirectory;
      this.modelsDirectory = modelsDirectory;
      this.dataWriterFactoryClass = dataWriterFactoryClass;
      
      // create the pre-processing pipeline
      XMLParser parser = UIMAFramework.getXMLParser();
      XMLInputSource source = new XMLInputSource(preprocessDescFile);
      AnalysisEngineDescription desc = parser.parseAnalysisEngineDescription(source);
      this.preprocessing = UIMAFramework.produceAnalysisEngine(desc);

      // pipeline to read manual annotations into the gold view, not the default view
      AggregateBuilder goldAnnotatorBuilder = new AggregateBuilder();
      goldAnnotatorBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          ViewCreatorAnnotator.class,
          ViewCreatorAnnotator.PARAM_VIEW_NAME,
          GOLD_VIEW_NAME));
      goldAnnotatorBuilder.add(
          AnalysisEngineFactory.createPrimitiveDescription(DocumentIDAnnotator.class),
          CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);
      goldAnnotatorBuilder.add(
          AnalysisEngineFactory.createPrimitiveDescription(
            GoldEntityAndRelationReader.class,
            GoldEntityAndRelationReader.PARAM_INPUTDIR,
            this.knowtatorXMLDirectory.getPath()),
          CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);
      this.goldAnnotator = goldAnnotatorBuilder.createAggregate();
    }

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      // run the pre-processor, the gold reader, and the relation extractor in training mode
      return Arrays.asList(
          this.preprocessing,
          this.goldAnnotator,
          AnalysisEngineFactory.createPrimitive(
              RelationExtractorAnnotator.class,
              RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
              GOLD_VIEW_NAME,
              RelationExtractorAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
              this.dataWriterFactoryClass.getName(),
              DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
              this.getDir(name).getPath()));
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      // run the pre-processor, the gold reader, an annotator that copies over the entity mentions,
      // and the relation extractor in classification mode
      return Arrays.asList(
          this.preprocessing,
          this.goldAnnotator,
          AnalysisEngineFactory.createPrimitive(EntityMentionCopier.class),
          AnalysisEngineFactory.createPrimitive(
              RelationExtractorAnnotator.class,
              JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
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
      
      // determine precision and recall
      Set<HashableRelation> intersection = new HashSet<HashableRelation>(goldRelations);
      intersection.retainAll(systemRelations);
      
      // update stats
      this.batchStats.update(goldRelations.size(), systemRelations.size(), intersection.size());
      this.collectionStats.update(goldRelations.size(), systemRelations.size(), intersection.size());
    }
    
    private Stats batchStats;
    private Stats collectionStats;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.batchStats = new Stats();
      this.collectionStats = new Stats();
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
      super.batchProcessComplete();
      System.err.printf("Batch: %s\n", this.batchStats);
      this.batchStats = new Stats();
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
      super.collectionProcessComplete();
      System.err.printf("Collection: %s\n", this.collectionStats);
      this.collectionStats = new Stats();
    }

    private static class Stats {
      public int gold = 0;
      public int system = 0;
      public int correct = 0;
      
      public void update(int gold, int system, int correct) {
        this.gold += gold;
        this.system += system;
        this.correct += correct;
      }
      
      public double precision() {
        return ((double)this.correct) / this.system;
      }
      
      public double recall() {
        return ((double)this.correct) / this.gold;
      }
      
      public double f1() {
        double p = this.precision();
        double r = this.recall();
        return (2 * p * r) / (p + r);
      }
      
      @Override
      public String toString() {
        return String.format(
            "P=%.3f R=%.3f F1=%.3f gold=%d system=%d correct=%d",
            this.precision(),
            this.recall(),
            this.f1(),
            this.gold,
            this.system,
            this.correct);
      }
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

      public HashableRelation(int arg1begin, int arg1end, int arg2begin, int arg2end, String category) {
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
