package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.chboston.cnlp.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.ParameterSettings;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;

public class RelationExtractorTrain {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/sharp/xmi/all)",
        required = true)
    public File trainDirectory;

    @Option(
        name = "--run-degree-of",
        usage = "if true runs the degree of relation extractor otherwise "
            + "it uses the normal entity mention pair relation extractor")
    public boolean runDegreeOf = false;
  }
	
	public static void main(String[] args) throws Exception {
		
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());

    // define the output directory for models
    File modelsDir = options.runDegreeOf
        ? new File("resources/models/degree_of")
        : new File("resources/models/em_pair");

    // determine class for the classifier annotator
    Class<? extends RelationExtractorAnnotator> annotatorClass = options.runDegreeOf
        ? DegreeOfRelationExtractorAnnotator.class
        : EntityMentionPairRelationExtractorAnnotator.class;

    // determine the type of classifier to be trained
    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = MultiClassLIBSVMDataWriterFactory.class;

    // best training parameters to use for production models
    ParameterSettings params = options.runDegreeOf
    		? new ParameterSettings(false, 1.0f, "linear", 0.05, 1.0)
        : new ParameterSettings(true, 0.5f, "linear", 0.05, 1.0);

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

    RelationExtractorEvaluation evaluation = new RelationExtractorEvaluation(
    		modelsDir,
    		annotatorClass,
    		dataWriterFactoryClass,
    		additionalParameters,
    		trainingArguments);
    
    CollectionReader collectionReader = evaluation.getCollectionReader(trainFiles);
    evaluation.train(collectionReader, modelsDir);
    
    System.out.println("model written to " + modelsDir.getAbsolutePath());
	}
}
