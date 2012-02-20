package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
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
import org.uimafit.factory.AnalysisEngineFactory;

import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;
import edu.mayo.bmi.uima.core.type.structured.DocumentID;

public class RelationExtractorEvaluation {

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
    int nFiles = Math.min(10, textFileNames.size()); // FIXME: use whole set
    List<String> trainFileNames = textFileNames.subList(0, nFiles / 2);
    List<String> testFileNames = textFileNames.subList(nFiles / 2, nFiles);

    // defines train and test corpora
    CorpusReaderProvider readerProvider = new FilesCollectionReaderProvider(
        textRoot,
        trainFileNames,
        testFileNames);
    readerProvider.setNumberOfFolds(5);

    // defines pipelines that load training data and train a classifier
    PipelineProvider pipelineProvider = new PipelineProvider(
        preprocessDescFile,
        xmlRoot,
        new File("models"),
        DefaultMultiClassLIBSVMDataWriterFactory.class);

    // defines how to evaluate
    EvaluationPipelineProvider evaluationProvider = new BatchBasedEvaluationPipelineProvider();

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
   * Defines how to write training data, train a classifier, and apply the classifier to new data.
   */
  public static class PipelineProvider extends CleartkPipelineProvider_ImplBase {
    
    private File knowtatorXMLDirectory;

    private File modelsDirectory;

    private final AnalysisEngine preprocessing;

    private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;

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
    }

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      // run the pre-processor, the gold reader, and the relation extractor in training mode
      return Arrays.asList(
          this.preprocessing,
          AnalysisEngineFactory.createPrimitive(DocumentIDAnnotator.class),
          AnalysisEngineFactory.createPrimitive(
              GoldEntityAndRelationReader.class,
              GoldEntityAndRelationReader.PARAM_INPUTDIR,
              this.knowtatorXMLDirectory.getPath()),
          AnalysisEngineFactory.createPrimitive(
              RelationExtractorAnnotator.class,
              RelationExtractorAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
              this.dataWriterFactoryClass.getName(),
              DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
              this.getDir(name).getPath()));
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      // run the pre-processor, followed by the relation extractor in classification mode
      return Arrays.asList(
          this.preprocessing,
          AnalysisEngineFactory.createPrimitive(DocumentIDAnnotator.class),
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
}
