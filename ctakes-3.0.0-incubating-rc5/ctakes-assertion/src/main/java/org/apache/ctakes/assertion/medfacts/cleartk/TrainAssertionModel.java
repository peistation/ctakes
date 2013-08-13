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
package org.apache.ctakes.assertion.medfacts.cleartk;

import java.io.File;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier.ReferenceAnnotationsSystemAssertionClearer;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier.ReferenceIdentifiedAnnotationsSystemToGoldCopier;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.opennlp.MaxentDataWriter;
import org.cleartk.classifier.opennlp.MaxentStringOutcomeDataWriter;
import org.cleartk.util.cr.FilesCollectionReader;
import org.cleartk.util.cr.XReader;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;
//import org.junit.Test;
import org.apache.ctakes.assertion.medfacts.AssertionAnalysisEngine;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
//import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.examples.pos.ExamplePOSPlainTextWriter;


public class TrainAssertionModel {
  
  public static final String PARAM_NAME_DECODING_OUTPUT_DIRECTORY = "decoding-output-directory";

  public static final String PARAM_NAME_DECODING_INPUT_DIRECTORY = "decoding-input-directory";

  public static final String PARAM_NAME_TRAINING_INPUT_DIRECTORY = "training-input-directory";

  public static final String PARAM_NAME_MODEL_DIRECTORY = "model-directory";

  protected static final Logger logger = Logger.getLogger(TrainAssertionModel.class.getName());

	/**
	 * @param args
	 */
  /*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String trainDir = args[0];
		String outputDir = args[1];
		
		try {
		CollectionReader reader = FilesCollectionReader.getCollectionReader(trainDir);
		AggregateBuilder builder = new AggregateBuilder();
		//builder.add(AnalysisEngineFactory.createAnalysisEngineDescription("desc/AssertionMiniPipelineAnalysisEngine.xml", null));
		//builder.add(AnalysisEngineFactory.createPrimitiveDescription(IdentifiedAnnotation.class));
		//builder.add(AnalysisEngineFactory.createAnalysisEngineDescription("edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation"));
	    builder.add(AssertionCleartkAnalysisEngine.getWriterDescription(outputDir));
	    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
	    org.cleartk.classifier.jar.Train.main(outputDir);
		} catch (Exception e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	    
	}
	*/
	
	protected String modelOutputDirectory = "/work/medfacts/cleartk/data/train.model";

  //@Test
  public void testMaxent() throws Exception {
    
    String trainingDataDirectory = "/work/medfacts/cleartk/data/train";
    String evaluationDataDirectory = "/work/medfacts/cleartk/data/eval2.input";
    String evaluationOutputDataDirectory = "/work/medfacts/cleartk/data/eval2.output";

    String maxentModelOutputDirectory = modelOutputDirectory + "/maxent";
    AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createPrimitiveDescription(
        AssertionCleartkAnalysisEngine.class,
        AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        maxentModelOutputDirectory);
    testClassifier(
        dataWriter,
        maxentModelOutputDirectory,
        trainingDataDirectory,
        evaluationDataDirectory,
        evaluationOutputDataDirectory
    );

//    // Not sure why the _SPLIT is here, but we will throw it out for good measure
//    String firstLine = FileUtil.loadListOfStrings(new File(maxentDirectoryName
//        + "/2008_Sichuan_earthquake.txt.pos"))[0].trim().replace("_SPLIT", "");
//    checkPOS(firstLine);
  }
  
  public static void main(String args[])
  {
    
    Options options = new Options();
    
    Option modelDirectoryOption =
        OptionBuilder
          .withLongOpt(TrainAssertionModel.PARAM_NAME_MODEL_DIRECTORY)
          .withArgName("DIR")
          .hasArg()
          .isRequired()
          .withDescription("the directory where the model is written to for training, or read from for decoding")
          .create();
    options.addOption(modelDirectoryOption);
    
    Option trainingInputDirectoryOption =
        OptionBuilder
          .withLongOpt(TrainAssertionModel.PARAM_NAME_TRAINING_INPUT_DIRECTORY)
          .withArgName("DIR")
          .hasArg()
          .isRequired()
          .withDescription("directory where input training xmi files are located")
          .create();
    options.addOption(trainingInputDirectoryOption);
    
    Option decodingInputDirectoryOption =
        OptionBuilder
          .withLongOpt(TrainAssertionModel.PARAM_NAME_DECODING_INPUT_DIRECTORY)
          .withArgName("DIR")
          .hasArg()
          .isRequired()
          .withDescription("directory where input xmi files are located for decoding")
          .create();
    options.addOption(decodingInputDirectoryOption);
    
    Option decodingOutputDirectoryOption =
        OptionBuilder
          .withLongOpt(TrainAssertionModel.PARAM_NAME_DECODING_OUTPUT_DIRECTORY)
          .withArgName("DIR")
          .hasArg()
          .isRequired()
          .withDescription("directory where output xmi files that are generated in decoding are placed")
          .create();
    options.addOption(decodingOutputDirectoryOption);
    
    CommandLineParser parser = new GnuParser();
    
    boolean invalidInput = false;
    
    CommandLine commandLine = null;
    String modelDirectory = null;
    String trainingInputDirectory = null;
    String decodingInputDirectory = null;
    String decodingOutputDirectory = null;
    try
    {
      commandLine = parser.parse(options, args);
      
      modelDirectory = commandLine.getOptionValue(TrainAssertionModel.PARAM_NAME_MODEL_DIRECTORY);
      trainingInputDirectory = commandLine.getOptionValue(TrainAssertionModel.PARAM_NAME_TRAINING_INPUT_DIRECTORY);
      decodingInputDirectory = commandLine.getOptionValue(TrainAssertionModel.PARAM_NAME_DECODING_INPUT_DIRECTORY);
      decodingOutputDirectory = commandLine.getOptionValue(TrainAssertionModel.PARAM_NAME_DECODING_OUTPUT_DIRECTORY);
    } catch (ParseException e)
    {
      invalidInput = true;
      logger.error("unable to parse command-line arguments", e);
    }
    
    if (modelDirectory == null || modelDirectory.isEmpty() ||
        trainingInputDirectory == null || trainingInputDirectory.isEmpty() ||
        decodingInputDirectory == null || decodingInputDirectory.isEmpty() ||
        decodingOutputDirectory == null || decodingOutputDirectory.isEmpty()
        )
    {
      logger.error("required parameters not supplied");
      invalidInput = true;
    }
    
    if (invalidInput)
    {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(TrainAssertionModel.class.getName(), options, true);
      return;
    }
    
    logger.info(String.format(
        "%n" +
        "model dir:           \"%s\"%n" +
        "training input dir:  \"%s\"%n" +
        "decoding input dir:  \"%s\"%n" +
        "decoding output dir: \"%s\"%n",
        modelDirectory,
        trainingInputDirectory,
        decodingInputDirectory,
        decodingOutputDirectory));
    
    String maxentModelOutputDirectory = modelDirectory + "/maxent";
    try
    {
      AnalysisEngineDescription dataWriter = AnalysisEngineFactory.createPrimitiveDescription(
          AssertionCleartkAnalysisEngine.class,
          AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
          MaxentStringOutcomeDataWriter.class.getName(),
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          maxentModelOutputDirectory);
      testClassifier(
          dataWriter,
          maxentModelOutputDirectory,
          trainingInputDirectory,
          decodingInputDirectory,
          decodingOutputDirectory
      );
    } catch (Exception e)
    {
      logger.error("Some exception happened while training or decoding...", e);
      return;
    }
    
 }

	
	
  public static void testClassifier(
      AnalysisEngineDescription dataWriter,
      String modelOutputDirectory,
      String trainingDataInputDirectory,
      String decodingInputDirectory,
      String decodingOutputDirectory,
      String... trainingArgs) throws Exception
  {
    
    CollectionReader trainingCollectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        XReader.PARAM_ROOT_FILE,
        trainingDataInputDirectory,
        XReader.PARAM_XML_SCHEME,
        XReader.XMI);
    CollectionReader evaluationCollectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        XReader.PARAM_ROOT_FILE,
        decodingInputDirectory,
        XReader.PARAM_XML_SCHEME,
        XReader.XMI);
    
    
    AggregateBuilder trainingBuilder = new AggregateBuilder();
    
    AnalysisEngineDescription goldCopierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    trainingBuilder.add(goldCopierAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    trainingBuilder.add(assertionAttributeClearerAnnotator);
    
    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultMaxentDataWriterFactory.class;
    AnalysisEngineDescription trainingAssertionAnnotator = 
        AnalysisEngineFactory.createPrimitiveDescription(
            AssertionCleartkAnalysisEngine.class,
            AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION
            );
    ConfigurationParameterFactory.addConfigurationParameters(
        trainingAssertionAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        modelOutputDirectory
        );
    trainingBuilder.add(trainingAssertionAnnotator);
    
    
    
//    CollectionReader collectionReader = XReader.getCollectionReader(
//        trainingDataDirectory);
//    collectionReader.setConfigParameterValue(XReader.PARAM_XML_SCHEME, XReader.XMI);
//    collectionReader.reconfigure();

    logger.info("starting feature generation...");
    SimplePipeline.runPipeline(
        trainingCollectionReader,
//        FilesCollectionReader.getCollectionReaderWithView(
//            "src/test/resources/data/treebank/11597317.tree",
//            TreebankConstants.TREEBANK_VIEW)
//        ,
//        TreebankGoldAnnotator.getDescriptionPOSTagsOnly(),
//        DefaultSnowballStemmer.getDescription("English"),
//        dataWriter);
        trainingBuilder.createAggregateDescription());
    logger.info("finished feature generation.");

    String[] args;
    if (trainingArgs != null && trainingArgs.length > 0) {
      args = new String[trainingArgs.length + 1];
      args[0] = modelOutputDirectory;
      System.arraycopy(trainingArgs, 0, args, 1, trainingArgs.length);
    } else {
      args = new String[] { modelOutputDirectory };
    }

    HideOutput hider = new HideOutput();
    logger.info("starting training...");
    org.cleartk.classifier.jar.Train.main(args);
    logger.info("finished training.");
    hider.restoreOutput();

    AggregateBuilder decodingBuilder = new AggregateBuilder();
    
    //AnalysisEngineDescription goldCopierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    decodingBuilder.add(goldCopierAnnotator);
    
    //AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    decodingBuilder.add(assertionAttributeClearerAnnotator);
    
    AnalysisEngineDescription decodingAssertionAnnotator =
      AnalysisEngineFactory.createPrimitiveDescription(
        AssertionCleartkAnalysisEngine.class,
        AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION
        );
    ConfigurationParameterFactory.addConfigurationParameters(
        decodingAssertionAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(modelOutputDirectory, "model.jar").getPath()
        );
    decodingBuilder.add(decodingAssertionAnnotator);
    
    //SimplePipeline.runPipeline(collectionReader,  builder.createAggregateDescription());
    AnalysisEngineDescription decodingAggregateDescription = decodingBuilder.createAggregateDescription();
    
    
    
//    AnalysisEngineDescription taggerDescription = AnalysisEngineFactory.createPrimitiveDescription(
//        AssertionCleartkAnalysisEngine.class,
//        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
//        //AssertionComponents.TYPE_SYSTEM_DESCRIPTION,
//        modelOutputDirectory + "/model.jar");

    logger.info("starting decoding...");
    SimplePipeline.runPipeline(
        evaluationCollectionReader,
//        BreakIteratorAnnotatorFactory.createSentenceAnnotator(Locale.US),
//        TokenAnnotator.getDescription(),
//        DefaultSnowballStemmer.getDescription("English"),
        //taggerDescription,
        decodingAggregateDescription,
        AnalysisEngineFactory.createPrimitiveDescription(
            XWriter.class,
            AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
            XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
            decodingOutputDirectory,
            XWriter.PARAM_XML_SCHEME_NAME,
            XWriter.XMI));
    logger.info("finished decoding.");

  }

}
