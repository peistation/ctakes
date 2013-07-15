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
package org.apache.ctakes.assertion.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.ctakes.assertion.attributes.features.selection.FeatureSelection;
import org.apache.ctakes.assertion.medfacts.cleartk.AlternateCuePhraseAnnotator;
import org.apache.ctakes.assertion.medfacts.cleartk.AssertionCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.AssertionComponents;
import org.apache.ctakes.assertion.medfacts.cleartk.ConditionalCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.HistoryCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine;
import org.apache.ctakes.assertion.pipelines.GoldEntityAndAttributeReaderPipelineForSeedCorpus;
import org.apache.ctakes.core.ae.DocumentIdPrinterAnalysisEngine;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ContractionToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCopier;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.mitre.medfacts.uima.ZoneAnnotator;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.NoOpAnnotator;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
//import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
//import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation;
//import org.chboston.cnlp.ctakes.relationextractor.ae.ModifierExtractorAnnotator;

public class AssertionEvaluation extends Evaluation_ImplBase<File, Map<String, AnnotationStatistics>> {
  
private static Logger logger = Logger.getLogger(AssertionEvaluation.class); 

  private static final String YTEX_NEGATION_DESCRIPTOR = "ytex/uima/NegexAnnotator.xml";

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "specify the directory containing the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = false)
    public String trainDirectory;
    
    @Option(
        name = "--test-dir",
        usage = "specify the directory containing the XMI testing files (for example, /NLP/Corpus/Relations/mipacq/xmi/test)",
        required = false)
    public File testDirectory;
    
    @Option(
            name = "--dev-dir",
            usage = "if running --preprocess, store the XMI development files here",
            required = false)
        public File devDirectory;

    @Option(
        name = "--models-dir",
        usage = "specify the directory where the models will be placed",
        required = false)
    public File modelsDirectory;
    
    @Option(
            name = "--evaluation-output-dir",
            usage = "specify the directory where the evaluation output xmi files will go",
            required = false)
    public File evaluationOutputDirectory;
        
    @Option(
            name = "--ignore-polarity",
            usage = "specify whether polarity processing should be ignored (true or false). default: false",
            required = false)
    public boolean ignorePolarity = false; // note that this is reversed from the "ignore" statement
        
    @Option(
            name = "--ignore-conditional",
            usage = "specify whether conditional processing should be ignored (true or false). default: false",
            required = false)
    public boolean ignoreConditional = false;
        
    @Option(
            name = "--ignore-uncertainty",
            usage = "specify whether uncertainty processing should be ignored (true or false). default: false",
            required = false)
    public boolean ignoreUncertainty = false;
        
    @Option(
            name = "--ignore-subject",
            usage = "specify whether subject processing should be ignored (true or false). default: false",
            required = false,
            handler=BooleanOptionHandler.class)
    public boolean ignoreSubject = false;
        
    @Option(
            name = "--ignore-generic",
            usage = "specify whether generic processing should be ignored (true or false). default: false",
            required = false)
    public boolean ignoreGeneric = false;
        
    // srh adding 2/20/13
    @Option(
            name = "--ignore-history",
            usage = "specify whether 'history of' processing should be run (true or false). default: false",
            required = false)
    public boolean ignoreHistory = false;
        
    @Option(
            name = "--cross-validation",
            usage = "ignore the test set and run n-fold cross-validation. default: n=2",
            required = false)
    public Integer crossValidationFolds;
    
    @Option(
            name = "--train-only",
            usage = "do not test a model, build one from xmi output and store in --models-dir",
            required = false)
    public boolean trainOnly = false;

    @Option(
            name = "--test-only",
            usage = "do not train a model, use the one specified in --models-dir",
            required = false)
    public boolean testOnly = false;

    @Option(
            name = "--preprocess-only",
            usage = "run preprocessing pipeline on a SHARP-style corpus, specify root directory",
            required = false)
    public File preprocessDir;

    @Option(
            name = "--no-cleartk",
            usage = "run the version of the assertion module released with cTAKES 2.5",
            required = false)
    public boolean noCleartk = false;
    
    @Option(
    		name = "--print-errors",
    		usage = "Flag to have test method print out error context for misclassified examples",
    		required = false)
    public boolean printErrors = false;

    @Option(
    		name = "--eval-only",
    		usage = "Evaluate a CASes (supply the directory as an argument) with both system and gold in them.",
    		required = false)
    public boolean evalOnly;

    @Option(
    		name = "--ytex-negation",
    		usage = "Use the negation detection from ytex, which is based on a more recent NegEx than the original cTAKES used." +
    		" Note that using this requires adding the directory for YTEX_NEGATION_DESCRIPTOR to the classpath as well" +
    		" as the annotator class itself, since ytex is under a different license than Apache cTAKES.",
    		required = false)
    public boolean useYtexNegation;

    @Option(
    		name = "--feature-selection",
    		usage = "Takes an argument: {c,m} corresponding to Chi-square or Mutual Information-based feature selection",
    		required = false)
    public String featureSelectionAlgorithm = null;
  }
  
  protected ArrayList<String> annotationTypes;

  private Class<? extends AssertionCleartkAnalysisEngine> classifierAnnotatorClass;

//  private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;
  
  private Class<? extends DataWriter<String>> dataWriterClass;
  
  private File evaluationOutputDirectory;

  private String sharpCorpusDirectory;

  protected static Options options = new Options();
  
  public static void main(String[] args) throws Exception {
	  
    System.out.println("Started assertion module at " + new Date());
    
    //Options options = new Options();
	  resetOptions();
	  options.parseOptions(args);
    
//    System.err.println("forcing skipping of subject processing!!!");
//    options.runSubject = false;
//    System.err.println("forcing skipping of generic processing!!!");
//    options.runGeneric = false;
//    System.err.println("forcing skipping of polarity processing!!!");
//    options.runPolarity = false;
//    System.err.println("forcing skipping of uncertainty processing!!!");
//    options.runUncertainty = false;
//    System.err.println("forcing skipping of conditional processing!!!");
//    options.runConditional = false;
    printOptionsForDebugging(options);
    List<File> trainFiles = new ArrayList<File>();
    if (null != options.trainDirectory) {
    	String[] dirs = options.trainDirectory.split("[;:]");
    	for (String dir : dirs) {
    		File trainDir = new File(dir);
    		if (trainDir.listFiles()!=null) {
    			trainFiles.addAll(Arrays.asList(trainDir.listFiles()));
        		//    	System.out.println(trainFiles.toString());
    		}
    	}
    }
    //File modelsDir = new File("models/modifier");
    File modelsDir = options.modelsDirectory;
    File evaluationOutputDirectory = options.evaluationOutputDirectory;

    // determine the type of classifier to be trained
//    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultMaxentDataWriterFactory.class;
//    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultMultiClassLIBSVMDataWriterFactory.class;
    
    // TODO Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultDataWriterFactory.class;
    //
    // A DataWriterFactory that creates a data writer from the class given by
    // PARAM_DATA_WRITER_CLASS_NAME and the directory given by
    // DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY.
    //
    // DefaultMaxentDataWriterFactory is deprecated and says to use DefaultDattaWriterFactory
    // with MaxentDataWriter.
    
    Class<? extends AssertionCleartkAnalysisEngine> annotatorClass = AssertionCleartkAnalysisEngine.class;

    //String [] annotationTypes = { "polarity", "conditional", "uncertainty", "subject", "generic" };
    ArrayList<String> annotationTypes = new ArrayList<String>();
    if (!options.ignorePolarity) { annotationTypes.add("polarity"); }
    if (!options.ignoreConditional) { annotationTypes.add("conditional"); }
    if (!options.ignoreUncertainty) { annotationTypes.add("uncertainty"); }
    if (!options.ignoreSubject) { annotationTypes.add("subject"); }
    if (!options.ignoreGeneric) { annotationTypes.add("generic"); }
    if (!options.ignoreHistory) { annotationTypes.add("historyOf"); }
    
    AssertionEvaluation evaluation = new AssertionEvaluation(
        modelsDir,
        evaluationOutputDirectory,
        annotationTypes,
        annotatorClass,
        LIBLINEARStringOutcomeDataWriter.class,
        "-c",
        "1"
//        "-t",
//        "0"
//        "100",
//        "2"
        );
    
    // if preprocessing, don't do anything else
    if(options.preprocessDir!=null ) {
    	preprocess(options.preprocessDir);
    }
    
    // run cross-validation
    else if(options.crossValidationFolds != null) {
      // run n-fold cross-validation
      List<Map<String, AnnotationStatistics>> foldStats = evaluation.crossValidation(trainFiles, options.crossValidationFolds);
      //AnnotationStatistics overallStats = AnnotationStatistics.addAll(foldStats);
      Map<String, AnnotationStatistics> overallStats = new TreeMap<String, AnnotationStatistics>();
      
      for (String currentAnnotationType : annotationTypes)
      {
    	  AnnotationStatistics currentAnnotationStatistics = new AnnotationStatistics();
    	  overallStats.put(currentAnnotationType, currentAnnotationStatistics);
      }
      for (Map<String, AnnotationStatistics> singleFoldMap : foldStats)
      {
    	  for (String currentAnnotationType : annotationTypes)
    	  {
    	    AnnotationStatistics currentFoldStatistics = singleFoldMap.get(currentAnnotationType);
    	    overallStats.get(currentAnnotationType).addAll(currentFoldStatistics);
    	  }
      }
      
      AssertionEvaluation.printScore(overallStats,  "CROSS FOLD OVERALL");
      
    } 
    
    // run on test set
    else {
      // train on the entire training set and evaluate on the test set
      List<File> testFiles;
      if (options.evalOnly) {
    	  testFiles = Arrays.asList(options.evaluationOutputDirectory.listFiles());
    	  logger.debug("evalOnly using files in directory " + evaluationOutputDirectory.getName() + " aka " + evaluationOutputDirectory.getCanonicalPath());
      } else if (options.trainOnly){
    	  testFiles = new ArrayList<File>();
      } else {
    	  testFiles = Arrays.asList(options.testDirectory.listFiles());
      }
      
      if (!options.testOnly && !options.evalOnly) {
    	  CollectionReader trainCollectionReader = evaluation.getCollectionReader(trainFiles);
    	  evaluation.train(trainCollectionReader, modelsDir);
      }
      
      if (!options.trainOnly) {
    	  if (testFiles==null || testFiles.size()==0) {
    		  throw new RuntimeException("testFiles = " + testFiles + " testFiles.size() = " + (testFiles==null ? "null": testFiles.size())) ;
    	  }
    	  logger.debug("testFiles.size() = " + testFiles.size());
    	  CollectionReader testCollectionReader = evaluation.getCollectionReader(testFiles);
    	  Map<String, AnnotationStatistics> stats = evaluation.test(testCollectionReader, modelsDir);

    	  AssertionEvaluation.printScore(stats,  modelsDir.getAbsolutePath());
      }
    }
    
    System.out.println("Finished assertion module at " + new Date());
    
  }
  
  private static void resetOptions() {
	  options.ignoreConditional = false;
	  options.ignoreGeneric = false;
	  options.ignoreHistory = false;
	  options.ignorePolarity = false;
	  options.ignoreSubject = false;
	  options.ignoreUncertainty = false;
	  
	  options.trainOnly = false;
	  options.testOnly = false;
	  options.noCleartk = false;
	  options.printErrors = false;
	  options.evalOnly = false;
	  
	  options.evaluationOutputDirectory = null;
	  options.trainDirectory = null;
	  options.testDirectory = null;
	  options.devDirectory = null;
	  options.modelsDirectory = null;
	  options.preprocessDir = null;
	  
	  options.crossValidationFolds = null;
  }

private static void printOptionsForDebugging(Options options)
  {
	String message;
	message = String.format(
		"Printing options: %n" +
		"training dir: %s%n" +
	    "test dir: %s%n" + 
	    "model dir: %s%n" +
	    "preprocess dir: %s%n" +
	    "evaluation output dir: %s%n" +
	    "cross-validation: %d%n" +
	    "ignore polarity: %b%n" +
	    "ignore conditional: %b%n" +
	    "ignore uncertainty: %b%n" +
	    "ignore subject: %b%n" +
	    "ignore generic: %b%n" +
	    "ignore history: %b%n" +
	    "train only: %b%n" +
	    "test only: %b%n" +
	    "eval only: %b%n" +
	    //"crossValidationFolds: %s%n" +
	    "noCleartk: %b%n" +
	    "%n%n",
	    options.trainDirectory, // just a String so no need to check for null because not using getAbsolutePath()
   	    (options.testDirectory != null) ? options.testDirectory.getAbsolutePath() : "",
	    (options.modelsDirectory!=null) ? options.modelsDirectory.getAbsolutePath() : "",
   		(options.preprocessDir!=null) ? options.preprocessDir.getAbsolutePath() : "",
	    (options.evaluationOutputDirectory!=null) ? options.evaluationOutputDirectory.getAbsolutePath() : "",
	    options.crossValidationFolds,
	    options.ignorePolarity,
	    options.ignoreConditional,
	    options.ignoreUncertainty,
	    options.ignoreSubject,
	    options.ignoreGeneric,
	    options.ignoreHistory,
		options.trainOnly,
		options.testOnly,
		options.evalOnly,
		//(options.crossValidationFolds != null) ? options.crossValidationFolds.intValue()+"" : "",
		options.noCleartk
	    );
	logger.info(message);
  }

public static void printScore(Map<String, AnnotationStatistics> map, String directory)
  {
      for (Map.Entry<String, AnnotationStatistics> currentEntry : map.entrySet())
	  {
    	  String annotationType = currentEntry.getKey();
    	  AnnotationStatistics stats = currentEntry.getValue();
    	  
    	  System.out.format("directory: \"%s\"; assertion type: %s%n%n%s%n%n",
    	    directory,
    	    annotationType.toUpperCase(),
    	    stats.toString());
	  }
      
  }

  private String[] trainingArguments;

  public AssertionEvaluation(
      File modelDirectory,
      File evaluationOutputDirectory,
      ArrayList<String> annotationTypes,
      Class<? extends AssertionCleartkAnalysisEngine> classifierAnnotatorClass,
      Class<? extends DataWriter<String>> dataWriterClass,
      String... trainingArguments
      ) {
    super(modelDirectory);
    
    this.annotationTypes = annotationTypes;

    this.classifierAnnotatorClass = classifierAnnotatorClass;
    this.dataWriterClass = dataWriterClass;

    this.trainingArguments = trainingArguments;
    this.evaluationOutputDirectory = evaluationOutputDirectory;
  }

  @Override
  public CollectionReader getCollectionReader(List<File> items)
      throws ResourceInitializationException {
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(),
        XMIReader.PARAM_FILES,
        paths);
  }

  public static void preprocess(File preprocessDir ) throws ResourceInitializationException, UIMAException, IOException {
//	  File devDirectory = new File(options.trainDirectory.getParentFile() + File.separator + "dev");
	  File trainDir = null;
	  if (options.trainDirectory.split("[;:]").length>1) {
		  throw new IOException("Assertion preprocess wants to write to one train directory, but you've supplied multiple: " + options.trainDirectory);
	  } else {
		  trainDir = new File(options.trainDirectory);
	  }
	  if (preprocessDir.getAbsolutePath().contains("i2b2")) {
		  GoldEntityAndAttributeReaderPipelineForSeedCorpus.readI2B2Challenge2010(preprocessDir, trainDir);
		  
	  } else {
		  GoldEntityAndAttributeReaderPipelineForSeedCorpus.readSharpUmlsCem(
				  preprocessDir, trainDir, options.testDirectory, options.devDirectory);
	  }
  }
  
  @Override
  public void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    
    //builder.add(AnalysisEngineFactory.createPrimitiveDescription(ReplaceCTakesEntityMentionsAndModifiersWithGold.class));

//    AnalysisEngineDescription assertionDescription = AssertionCleartkAnalysisEngine.getDescription(
//        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//        //MultiClassLIBSVMDataWriterFactory.class.getName(),
//        MaxentStringOutcomeDataWriter.class.getName(),
//        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
//        directory.getPath());
//    builder.add(assertionDescription);
    
    AnalysisEngineDescription documentIdPrinterAnnotator = AnalysisEngineFactory.createPrimitiveDescription(DocumentIdPrinterAnalysisEngine.class);
    builder.add(documentIdPrinterAnnotator);
    
    AnalysisEngineDescription goldCopierIdentifiedAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierIdentifiedAnnotsAnnotator);
    
    AnalysisEngineDescription goldCopierSupportingAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceSupportingAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierSupportingAnnotsAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    String generalSectionRegexFileUri =
        "org/mitre/medfacts/zoner/section_regex.xml";
    AnalysisEngineDescription zonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            generalSectionRegexFileUri
            );
//    builder.add(zonerAnnotator);

    String mayoSectionRegexFileUri =
        "org/mitre/medfacts/uima/mayo_sections.xml";
    AnalysisEngineDescription mayoZonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            mayoSectionRegexFileUri
            );
//    builder.add(mayoZonerAnnotator);
  
//    URL assertionCuePhraseLookupAnnotatorDescriptorUrl1 = this.getClass().getClassLoader().getResource("org/apache/ctakes/dictionary/lookup/AssertionCuePhraseDictionaryLookupAnnotator.xml");
//    logger.info(String.format("assertionCuePhraseLookupAnnotatorDescriptorUrl1 (slashes): %s", assertionCuePhraseLookupAnnotatorDescriptorUrl1));
//    URL assertionCuePhraseLookupAnnotatorDescriptorUrl2 = this.getClass().getClassLoader().getResource("org.apache.ctakes.dictionary.lookup.AssertionCuePhraseDictionaryLookupAnnotator.xml");
//    logger.info(String.format("assertionCuePhraseLookupAnnotatorDescriptorUrl2 (periods): %s", assertionCuePhraseLookupAnnotatorDescriptorUrl2));
//
//    
//    AnalysisEngineDescription cuePhraseLookupAnnotator =
//        AnalysisEngineFactory.createAnalysisEngineDescription("org/apache/ctakes/dictionary/lookup/AssertionCuePhraseDictionaryLookupAnnotator");
//    builder.add(cuePhraseLookupAnnotator);

    // Set up Feature Selection parameters
    Float featureSelectionThreshold = 0f;
    Class<? extends DataWriter> dataWriterClassFirstPass = getDataWriterClass(); 
    if (options.featureSelectionAlgorithm!=null) {
    	featureSelectionThreshold = .1f;
    }
    
    // Add each assertion Analysis Engine to the pipeline!
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(AlternateCuePhraseAnnotator.class, new Object[]{}));
    
    if (!options.ignorePolarity)
    {
    	if (options.useYtexNegation) {
    		AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(YTEX_NEGATION_DESCRIPTOR);
    		builder.add(polarityAnnotator);
    	} else {
    		AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    		ConfigurationParameterFactory.addConfigurationParameters(
    				polarityAnnotator,
    				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
    				AssertionEvaluation.GOLD_VIEW_NAME,
//    				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//    				this.dataWriterFactoryClass.getName(),
    				DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
    				dataWriterClassFirstPass,
    				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
    				new File(directory, "polarity").getPath(),
    				AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
    				PolarityCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "polarity")),
    				AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
    				featureSelectionThreshold
    				);
    		builder.add(polarityAnnotator);
    	}
    }

    if (!options.ignoreConditional)
    {
	    AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        conditionalAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvaluation.GOLD_VIEW_NAME,
//	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//	        this.dataWriterFactoryClass.getName(),
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
			dataWriterClassFirstPass,
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "conditional").getPath(),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
			ConditionalCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "conditional")),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
			featureSelectionThreshold
	        );
	    builder.add(conditionalAnnotator);
    }

    if (!options.ignoreUncertainty)
    {
	    AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        uncertaintyAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvaluation.GOLD_VIEW_NAME,
//	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//	        this.dataWriterFactoryClass.getName(),
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
			dataWriterClassFirstPass,
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "uncertainty").getPath(),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
			UncertaintyCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "uncertainty")),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
			featureSelectionThreshold
	        );
	    builder.add(uncertaintyAnnotator);
    }

    if (!options.ignoreSubject)
    {
	    AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        subjectAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvaluation.GOLD_VIEW_NAME,
//	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//	        this.dataWriterFactoryClass.getName(),
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
			dataWriterClassFirstPass,
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "subject").getPath(),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
			SubjectCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "subject")),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
			featureSelectionThreshold
	        );
	    builder.add(subjectAnnotator);
    }

    if (!options.ignoreGeneric)
    {
		AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
		    genericAnnotator,
		    AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
		    AssertionEvaluation.GOLD_VIEW_NAME,
//		    CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//		    this.dataWriterFactoryClass.getName(),
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
			dataWriterClassFirstPass,
		    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		    new File(directory, "generic").getPath(),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
			GenericCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "generic")),
			AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
			featureSelectionThreshold
		    );
		builder.add(genericAnnotator);
    }
    
    // 2/20/13 srh adding
    if (!options.ignoreHistory) {
    	AnalysisEngineDescription historyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(HistoryCleartkAnalysisEngine.class);
    	ConfigurationParameterFactory.addConfigurationParameters(
    			historyAnnotator,
    			AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
    			AssertionEvaluation.GOLD_VIEW_NAME,
//    			CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
//    			this.dataWriterFactoryClass.getName(),
          DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
			dataWriterClassFirstPass,
    			DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
    			new File(directory, "historyOf").getPath(),
				AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_URI,
				HistoryCleartkAnalysisEngine.createFeatureSelectionURI(new File(directory, "historyOf")),
				AssertionCleartkAnalysisEngine.PARAM_FEATURE_SELECTION_THRESHOLD,
				featureSelectionThreshold
    			);
    	builder.add(historyAnnotator);
    }

/*
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
*/    
    
    SimplePipeline.runPipeline(collectionReader,  builder.createAggregateDescription());
    
    //HideOutput hider = new HideOutput();
    for (String currentAssertionAttribute : annotationTypes)
    {
    	File currentDirectory = new File(directory, currentAssertionAttribute);
    	trainAndPackage(currentAssertionAttribute, currentDirectory, trainingArguments);
    }
    //hider.restoreOutput();
  }

  @Override
  protected Map<String, AnnotationStatistics> test(CollectionReader collectionReader, File directory)
      throws Exception {
//    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createPrimitive(AssertionCleartkAnalysisEngine.getDescription(
//        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
//        new File(directory, "model.jar").getPath()));

    AggregateBuilder builder = new AggregateBuilder();
    
    // directory is such as /cTAKES/workspaces/Apache-cTAKES-trunk/ctakes/ctakes-assertion/sharp_data/model/eval.model
    
    AnalysisEngineDescription goldCopierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    if ( options.noCleartk ) {
    	addExternalAttributeAnnotatorsToAggregate(builder);
    } else {
    	addCleartkAttributeAnnotatorsToAggregate(directory, builder);
    }

    if (options.evalOnly && evaluationOutputDirectory != null) {
    	// short circuit any other stuff in the pipeline
    	builder = new AggregateBuilder();
    	
    	// uimafit find available type systems on classpath
    	TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
    	
        AnalysisEngineDescription noOp =
    		AnalysisEngineFactory.createPrimitiveDescription(
	            NoOpAnnotator.class,
	            typeSystemDescription);
    	builder.add(noOp);
    	
        AnalysisEngineDescription mergeGold =
    		AnalysisEngineFactory.createPrimitiveDescription(org.apache.ctakes.assertion.eval.MergeGoldViewFromOneCasIntoInitialViewOfAnotherCas.class, typeSystemDescription);
    	builder.add(mergeGold);
    	
    } else if (evaluationOutputDirectory!=null)  {
        AnalysisEngineDescription xwriter =
    		AnalysisEngineFactory.createPrimitiveDescription(
	            XWriter.class,
	            AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
	            XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
	            evaluationOutputDirectory,
	            XWriter.PARAM_XML_SCHEME_NAME,
	            XWriter.XMI,
	            XWriter.PARAM_FILE_NAMER_CLASS_NAME,
	            CtakesFileNamer.class.getName());
        builder.add(xwriter);
    }
    
    //SimplePipeline.runPipeline(collectionReader,  builder.createAggregateDescription());
    //AnalysisEngineDescription aggregateDescription = builder.createAggregateDescription();
    
    AnalysisEngine aggregate = builder.createAggregate();
    
    AnnotationStatistics polarityStats = new AnnotationStatistics();
    AnnotationStatistics conditionalStats = new AnnotationStatistics();
    AnnotationStatistics uncertaintyStats = new AnnotationStatistics();
    AnnotationStatistics subjectStats = new AnnotationStatistics();
    AnnotationStatistics genericStats = new AnnotationStatistics();
    AnnotationStatistics historyStats = new AnnotationStatistics();	// srh 3/6/13
    
    Map<String, AnnotationStatistics> map = new TreeMap<String, AnnotationStatistics>(); 
    if (!options.ignorePolarity)
    {
      map.put("polarity",  polarityStats);
    }

    if (!options.ignoreConditional)
    {
      map.put("conditional",  conditionalStats);
    }

    if (!options.ignoreUncertainty)
    {
      map.put("uncertainty",  uncertaintyStats);
    }

    if (!options.ignoreSubject)
    {
      map.put("subject", subjectStats);
    }

    if (!options.ignoreGeneric)
    {
      map.put("generic", genericStats);
    }
    
    // srh 3/6/13
    if (!options.ignoreHistory)
    {
    	map.put("historyOf", historyStats);
    }

    // run on existing output that has both system (or instance gathering) and gold
    for (JCas jCas : new JCasIterable(collectionReader, aggregate)) {
    	
    	printViewNames("Views found by JCasIterable:", jCas);
    	
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
    	logger.info("jCas.getViewName() = " + jCas.getViewName());
        throw new AnalysisEngineProcessException(e);
      }

      String documentId = DocumentIDAnnotationUtil.getDocumentID(jCas);
      System.out.format("document id: %s%n", documentId);
      
      Collection<IdentifiedAnnotation> goldEntitiesAndEvents = new ArrayList<IdentifiedAnnotation>(); 
      Collection<EntityMention> goldEntities = JCasUtil.select(goldView, EntityMention.class);
      goldEntitiesAndEvents.addAll(goldEntities);
      Collection<EventMention> goldEvents = JCasUtil.select(goldView, EventMention.class);
      goldEntitiesAndEvents.addAll(goldEvents);
      // System.out.format("gold entities: %d%ngold events: %d%n%n", goldEntities.size(), goldEvents.size());
      
      if (goldEntitiesAndEvents.size()==0) { 
    	  // gold annotations might have been read in as just IdentifiedAnnotation annotations
    	  // since no EventMentio or EntityMention annotations were found, ok to just try IdentifiedAnnotation
    	  // without concern for using some twice.
          Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(goldView, IdentifiedAnnotation.class);
          goldEntitiesAndEvents.addAll(identifiedAnnotations);
    	  
    	  
      }
      
      Collection<IdentifiedAnnotation> systemEntitiesAndEvents = new ArrayList<IdentifiedAnnotation>();
      Collection<EntityMention> systemEntities = JCasUtil.select(jCas, EntityMention.class);
      systemEntitiesAndEvents.addAll(systemEntities);
      Collection<EventMention> systemEvents = JCasUtil.select(jCas, EventMention.class);
      systemEntitiesAndEvents.addAll(systemEvents);
//      System.out.format("system entities: %d%nsystem events: %d%n%n", systemEntities.size(), systemEvents.size());
      
      if (!options.ignorePolarity)
      {
	      polarityStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("polarity"));
	      if(options.printErrors){
	    	  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "polarity", CONST.NE_POLARITY_NEGATION_PRESENT, Integer.class);
	      }
      }

      if (!options.ignoreConditional)
      {
	      conditionalStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("conditional"));
	      if(options.printErrors){
	    	  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "conditional", CONST.NE_CONDITIONAL_TRUE, Boolean.class);
	      }
      }

      if (!options.ignoreUncertainty)
      {
	      uncertaintyStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("uncertainty"));
	      if(options.printErrors){
	    	  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "uncertainty", CONST.NE_UNCERTAINTY_PRESENT, Integer.class);
	      }
      }

      if (!options.ignoreSubject)
      {
	      subjectStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("subject"));
	      if(options.printErrors){
	    	  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "subject", null, CONST.ATTR_SUBJECT_PATIENT.getClass());
	      }
      }

      if (!options.ignoreGeneric)
      {
	      genericStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("generic"));
	      if(options.printErrors){
	    	  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "generic", CONST.NE_GENERIC_TRUE, Boolean.class);
	      }
      }
      
      // srh 3/6/13
      if (!options.ignoreHistory)
      {
    	  historyStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
    			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
    			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("historyOf"));
    	  if(options.printErrors){
    		  printErrors(jCas, goldEntitiesAndEvents, systemEntitiesAndEvents, "historyOf", CONST.NE_HISTORY_OF_PRESENT, Integer.class);
    	  }
      }
      
    }
    return map;
  }

  protected void trainAndPackage(String currentAssertionAttribute, File directory, String[] arguments) throws Exception {
	  if (options.featureSelectionAlgorithm!=null) {
//		  InstanceDataWriter.INSTANCES_OUTPUT_FILENAME = "training-data.liblinear";
		  // Extracting features and writing instances
		  Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(directory);

		  // Collect MinMax stats for feature normalization
		  FeatureSelection<String> featureSelection; 
		  if (currentAssertionAttribute.equals("polarity")) {
			  // TODO: parameterize the thresholds
			  featureSelection = PolarityCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(PolarityCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else if (currentAssertionAttribute.equals("uncertainty")) {
			  // TODO: parameterize the thresholds
			  featureSelection = UncertaintyCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(UncertaintyCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else if (currentAssertionAttribute.equals("conditional")) {
			  // TODO: parameterize the thresholds
			  featureSelection = ConditionalCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(ConditionalCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else if (currentAssertionAttribute.equals("subject")) {
			  // TODO: parameterize the thresholds
			  featureSelection = SubjectCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(SubjectCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else if (currentAssertionAttribute.equals("generic")) {
			  // TODO: parameterize the thresholds
			  featureSelection = GenericCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(GenericCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else if (currentAssertionAttribute.equals("historyOf")) {
			  // TODO: parameterize the thresholds
			  featureSelection = HistoryCleartkAnalysisEngine.createFeatureSelection(1f);
			  featureSelection.train(instances);
			  featureSelection.save(HistoryCleartkAnalysisEngine.createFeatureSelectionURI(directory));
		  }
		  else {
			  featureSelection = null;
		  }


	      // now write in the libsvm format
//	      LIBLINEARStringOutcomeDataWriter dataWriter = new LIBLINEARStringOutcomeDataWriter(directory);
		  Constructor c = this.dataWriterClass.getConstructor(File.class);
	      DataWriter dataWriter = (DataWriter) c.newInstance(directory);
	      
	      // try filtering
	      for (Instance<String> instance : instances) {
	    	  dataWriter.write(featureSelection.transform(instance));
	      }
	      dataWriter.finish();
	  }

	  // train models based on instances
	  JarClassifierBuilder.trainAndPackage(directory, "-c", "0.05");
  }
  
  protected Class<? extends DataWriter> getDataWriterClass()
      throws ResourceInitializationException {
    return (options.featureSelectionAlgorithm!=null)
        ? InstanceDataWriter.class
        : LIBLINEARStringOutcomeDataWriter.class;
  }
  
  private static boolean DEBUG = false;
  private static void printViewNames(String message, JCas jcas) {
	
  	Iterator<JCas> viewIter;
	try {
		viewIter = jcas.getViewIterator();
	} catch (CASException e) {
		e.printStackTrace();
		return;
	}
  	while (viewIter.hasNext()) {
  		JCas view = viewIter.next();
  		String viewName = view.getViewName();
  		logger.debug(message + " View name " + viewName);
  		int numIndexedAnnotations = view.getAnnotationIndex().size();
  		logger.debug(message + "  has " + numIndexedAnnotations + " indexed annotations");
  		if (viewName.toLowerCase().contains("gold")) {
  	  	    if (DEBUG) printAnnotations(IdentifiedAnnotation.type, view);
  		} else {
  			if (DEBUG) printAnnotations(EventMention.type, view);
  			if (DEBUG) printAnnotations(EntityMention.type, view);
  		}
  	}

  }

private static void printAnnotations(int uimaAnnotationType, JCas view) {
	
	AnnotationIndex<Annotation> index = view.getAnnotationIndex(uimaAnnotationType);
	Iterator<Annotation> iter = index.iterator();
	output("Printing annotations for view " + view.getViewName());
	while (iter.hasNext()) {
		Annotation a = iter.next();
		printAnnotation(a);
	}
	
	//// Temp debug code
	//if (view.getViewName().equals("GoldView")) {
	//	AnnotationIndex<Annotation> indexOfAll = view.getAnnotationIndex();
	//	Iterator<Annotation> iterOverAll = indexOfAll.iterator();
	//	output("Printing ALL annotations for view " + view.getViewName());
	//	while (iterOverAll.hasNext()) {
	//		Annotation a = iterOverAll.next();
	//		printAnnotation(a);
	//	}
	//	
	//}
	
}

private static void printAnnotation(Annotation a) {
	
	String s = String.format(" (%d, %d) ", a.getBegin(), a.getEnd());
	if (a instanceof IdentifiedAnnotation) {
		s = s + ((IdentifiedAnnotation) a).getTypeID() + "=typeID, ";
	}
	s = s + "|" + a.getCoveredText() + "|";
	s = s + a.getClass().getCanonicalName();
	output(s);
	
}


private static void output(Object o) {
	if (o==null) {
		System.out.println(o);
	} else {
		System.out.println(o.toString());
	}
}
private static void printErrors(JCas jCas,
		  Collection<IdentifiedAnnotation> goldEntitiesAndEvents,
		  Collection<IdentifiedAnnotation> systemEntitiesAndEvents, String classifierType, Object trueCategory, Class<? extends Object> categoryClass) throws ResourceProcessException {
	  Map<HashableAnnotation, IdentifiedAnnotation> goldMap = Maps.newHashMap();
	  for (IdentifiedAnnotation mention : goldEntitiesAndEvents) {
		  goldMap.put(new HashableAnnotation(mention), mention);
	  }
	  Map<HashableAnnotation, IdentifiedAnnotation> systemMap = Maps.newHashMap();
	  for (IdentifiedAnnotation relation : systemEntitiesAndEvents) {
		  systemMap.put(new HashableAnnotation(relation), relation);
	  }
	  Set<HashableAnnotation> all = Sets.union(goldMap.keySet(), systemMap.keySet());
	  List<HashableAnnotation> sorted = Lists.newArrayList(all);
	  Collections.sort(sorted);
	  for (HashableAnnotation key : sorted) {
		  IdentifiedAnnotation goldAnnotation = goldMap.get(key);
		  IdentifiedAnnotation systemAnnotation = systemMap.get(key);
		  Object goldLabel=null;
		  Object systemLabel=null;
		  if (goldAnnotation == null) {
			  logger.debug(key + " not found in gold annotations ");
		  } else {
			  Feature feature = goldAnnotation.getType().getFeatureByBaseName(classifierType);
			  goldLabel = getFeatureValue(feature, categoryClass, goldAnnotation);
			  //  Integer goldLabel = goldAnnotation.getIntValue(feature);
		  }
		  
		  if (systemAnnotation == null) {
			  logger.info(key + " not found in system annotations ");
		  } else {
			  Feature feature = systemAnnotation.getType().getFeatureByBaseName(classifierType);
			  systemLabel = getFeatureValue(feature, categoryClass, systemAnnotation);
			  //  Integer systemLabel = systemAnnotation.getIntValue(feature);
		  }
		  
		  String typeId;
		  if (systemAnnotation!=null) {
			  typeId = systemAnnotation.getTypeID()+"";
		  } else  {
			  typeId = "X";
		  }
		  
		  if (goldLabel==null) {
			  // skip counting the attribute value since we have no gold label to compare to
			  logger.debug("Skipping annotation with label " + systemLabel + " because gold label is null");
		  } else  {
			  if(!goldLabel.equals(systemLabel)){
				  if(trueCategory == null){
					  // used for multi-class case:
					  System.out.println(classifierType+" Incorrectly labeled as " + systemLabel + " when the example was " + goldLabel + ": " + formatError(jCas, goldAnnotation));
				  }else if(systemLabel.equals(trueCategory)){
					  System.out.println(classifierType+" FP: " + typeId  + " " + formatError(jCas, systemAnnotation) + "| gold:|" + formatError(jCas, goldAnnotation));
				  }else{
					  System.out.println(classifierType+" FN: " + typeId + " " + formatError(jCas, goldAnnotation)+ "| system:|" + formatError(jCas, systemAnnotation));
				  }
			  }else{
			    if(trueCategory == null){
			      // multi-class case -- probably don't want to print anything?
			    }else if(systemLabel.equals(trueCategory)){
					  System.out.println(classifierType+" TP: " + typeId + " " + formatError(jCas, systemAnnotation) + "| gold:|" + formatError(jCas, goldAnnotation));
				  }else{
					  System.out.println(classifierType+" TN: " + typeId + " " + formatError(jCas, systemAnnotation) + "| gold:|" + formatError(jCas, goldAnnotation));
				  }
			  }
		  }
	  }
  }
  
  private static Object getFeatureValue(Feature feature,
		  Class<? extends Object> class1, Annotation annotation) throws ResourceProcessException {
	  if(class1 == Integer.class){
		  return annotation.getIntValue(feature);
	  }else if(class1 == String.class){
		  return annotation.getStringValue(feature);
	  }else if(class1 == Boolean.class){
		  return annotation.getBooleanValue(feature);
	  }else{
		  throw new ResourceProcessException("Received a class type that I'm not familiar with: ", new Object[]{class1});
	  }
  }

  private static String formatError(JCas jcas, IdentifiedAnnotation mention){
	  List<Sentence> context = JCasUtil.selectCovering(jcas, Sentence.class, mention.getBegin(), mention.getEnd());
	  StringBuffer buff = new StringBuffer();
	  if(context.size() > 0){
		  Sentence sent = context.get(0);
		  buff.append(sent.getCoveredText());
		  long offset = mention.getBegin() - sent.getBegin();
		  if (offset>=Integer.MAX_VALUE || offset<=Integer.MIN_VALUE) { offset=0; } // for spanless annots
		  buff.insert((int)offset, "***");
		  offset += (mention.getEnd()-mention.getBegin() + 3);
		  buff.insert((int)offset, "***");
	  }
	  return buff.toString();
  }

public static class HashableAnnotation implements Comparable<HashableAnnotation> {

    protected int begin;

    protected int end;

    public HashableAnnotation(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    public HashableAnnotation(Annotation arg1) {
      this(arg1.getBegin(), arg1.getEnd());
    }

    @Override
    public boolean equals(Object otherObject) {
      boolean result = false;
      if (otherObject instanceof HashableAnnotation) {
        HashableAnnotation other = (HashableAnnotation) otherObject;
        result = (this.getClass() == other.getClass() && this.begin == other.begin
            && this.end == other.end);
      }
      
      return result;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.begin, this.end);
    }

    @Override
    public String toString() {
      return String.format(
          "%s(%s,%s)",
          this.getClass().getSimpleName(),
          this.begin,
          this.end);
    }

    @Override
    public int compareTo(HashableAnnotation that) {
      int thisBegin = this.begin;
      int thatBegin = that.begin;
      if (thisBegin < thatBegin) {
        return -1;
      } else if (thisBegin > thatBegin) {
        return +1;
      } else if (this.equals(that)) {
        return 0;
      } else {
        return +1; // arbitrary choice for overlapping
      }
    }
  }

private void addExternalAttributeAnnotatorsToAggregate(AggregateBuilder builder)
		throws UIMAException, IOException {
	// RUN ALL THE OLD (non-ClearTK) CLASSIFIERS
	AnalysisEngineDescription oldAssertionAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription("desc/assertionAnalysisEngine"); 
	ConfigurationParameterFactory.addConfigurationParameters(
			oldAssertionAnnotator,
			AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
			AssertionEvaluation.GOLD_VIEW_NAME
	);
	builder.add(oldAssertionAnnotator);

	AnalysisEngineDescription oldConversionAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription("desc/conceptConverterAnalysisEngine"); 
	ConfigurationParameterFactory.addConfigurationParameters(
			oldConversionAnnotator,
			AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
			AssertionEvaluation.GOLD_VIEW_NAME
	);
	builder.add(oldConversionAnnotator);

	AnalysisEngineDescription oldSubjectAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription("desc/SubjectAttributeAnalysisEngine"); 
	ConfigurationParameterFactory.addConfigurationParameters(
			oldSubjectAnnotator,
			AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
			AssertionEvaluation.GOLD_VIEW_NAME
	);
	builder.add(oldSubjectAnnotator);

	AnalysisEngineDescription oldGenericAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription("desc/GenericAttributeAnalysisEngine"); 
	ConfigurationParameterFactory.addConfigurationParameters(
			oldGenericAnnotator,
			AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
			AssertionEvaluation.GOLD_VIEW_NAME
	);
	builder.add(oldGenericAnnotator);
}

private void addCleartkAttributeAnnotatorsToAggregate(File directory,
		AggregateBuilder builder) throws UIMAException, IOException,
		ResourceInitializationException {
//	AnalysisEngineDescription cuePhraseLookupAnnotator =
//		AnalysisEngineFactory.createAnalysisEngineDescription("org/apache/ctakes/dictionary/lookup/AssertionCuePhraseDictionaryLookupAnnotator");
//	builder.add(cuePhraseLookupAnnotator);
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(AlternateCuePhraseAnnotator.class, new Object[]{}));

	String generalSectionRegexFileUri =
		"org/mitre/medfacts/zoner/section_regex.xml";
	AnalysisEngineDescription zonerAnnotator =
		AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
				ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
				generalSectionRegexFileUri
		);
//	builder.add(zonerAnnotator);

	String mayoSectionRegexFileUri =
		"org/mitre/medfacts/uima/mayo_sections.xml";
	AnalysisEngineDescription mayoZonerAnnotator =
		AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
				ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
				mayoSectionRegexFileUri
		);
//	builder.add(mayoZonerAnnotator);

	// Add the ClearTk or the ytex negation (polarity) classifier
	if (!options.ignorePolarity)
	{
    	if (options.useYtexNegation) {
    		AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(YTEX_NEGATION_DESCRIPTOR);
    		builder.add(polarityAnnotator);
    	} else {
    		AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    		ConfigurationParameterFactory.addConfigurationParameters(
    				polarityAnnotator,
    				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
    				AssertionEvaluation.GOLD_VIEW_NAME,
    				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
    				new File(new File(directory, "polarity"), "model.jar").getPath()
    				);
    		builder.add(polarityAnnotator);
    	}
	}

	// Add the rest of the ClearTk classifiers
	if (!options.ignoreConditional)
	{
		AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
				conditionalAnnotator,
				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
				AssertionEvaluation.GOLD_VIEW_NAME,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(new File(directory, "conditional"), "model.jar").getPath()
		);
		builder.add(conditionalAnnotator);
	}

	if (!options.ignoreUncertainty)
	{
		AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
				uncertaintyAnnotator,
				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
				AssertionEvaluation.GOLD_VIEW_NAME,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(new File(directory, "uncertainty"), "model.jar").getPath()
		);
		builder.add(uncertaintyAnnotator);
	}

	if (!options.ignoreSubject)
	{
		AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
				subjectAnnotator,
				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
				AssertionEvaluation.GOLD_VIEW_NAME,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(new File(directory, "subject"), "model.jar").getPath()
		);
		builder.add(subjectAnnotator);
	}

	if (!options.ignoreGeneric)
	{
		AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
				genericAnnotator,
				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
				AssertionEvaluation.GOLD_VIEW_NAME,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(new File(directory, "generic"), "model.jar").getPath()
		);
		builder.add(genericAnnotator);
	}
	
	if(!options.ignoreHistory){
		AnalysisEngineDescription historyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(HistoryCleartkAnalysisEngine.class);
		ConfigurationParameterFactory.addConfigurationParameters(
				historyAnnotator,
				AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
				AssertionEvaluation.GOLD_VIEW_NAME,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(new File(directory, "historyOf"), "model.jar").getPath()
				);
		builder.add(historyAnnotator);
	}
}

  public static final String GOLD_VIEW_NAME = "GoldView";

  /**
   * Class that copies the manual {@link Modifier} annotations to the default CAS.
   */
  public static class OnlyGoldAssertions extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove any automatically generated Modifiers
      for (EntityMention entityMention : JCasUtil.select(jCas, EntityMention.class)) {
        entityMention.removeFromIndexes();
      }

      // copy over the manually annotated Modifiers
      for (EntityMention entityMention : JCasUtil.select(goldView, EntityMention.class)) {
        EntityMention newEntityMention = new EntityMention(jCas, entityMention.getBegin(), entityMention.getEnd());
        newEntityMention.setTypeID(entityMention.getTypeID());
        newEntityMention.setId(entityMention.getId());
        newEntityMention.setDiscoveryTechnique(entityMention.getDiscoveryTechnique());
        newEntityMention.setConfidence(entityMention.getConfidence());
        newEntityMention.addToIndexes();
      }
    }
  }
  

  /**
   * Annotator that removes cTAKES EntityMentions and Modifiers from the system
   * view, and copies over the manually annotated EntityMentions and Modifiers
   * from the gold view.
   * 
   */
  public static class ReplaceCTakesEntityMentionsAndModifiersWithGold extends
      JCasAnnotator_ImplBase
  {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException
    {
      JCas goldView, systemView;
      try
      {
        goldView = jCas.getView(GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e)
      {
        throw new AnalysisEngineProcessException(e);
      }

      // remove cTAKES EntityMentions and Modifiers from system view
      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
      for (IdentifiedAnnotation cTakesMention : cTakesMentions)
      {
        cTakesMention.removeFromIndexes();
      }

      // copy gold EntityMentions and Modifiers to the system view
      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
      goldMentions.addAll(JCasUtil.select(goldView, EntityMention.class));
      goldMentions.addAll(JCasUtil.select(goldView, Modifier.class));
      CasCopier copier = new CasCopier(goldView.getCas(), systemView.getCas());
      for (IdentifiedAnnotation goldMention : goldMentions)
      {
        Annotation copy = (Annotation) copier.copyFs(goldMention);
        Feature sofaFeature = copy.getType().getFeatureByBaseName("sofa");
        copy.setFeatureValue(sofaFeature, systemView.getSofa());
        copy.addToIndexes();
      }
    }
  }
  
//  public static class ReplaceGoldEntityMentionsAndModifiersWithCTakes extends
//      JCasAnnotator_ImplBase
//  {
//
//    @Override
//    public void process(JCas jCas) throws AnalysisEngineProcessException
//    {
//      JCas goldView, systemView;
//      try
//      {
//        goldView = jCas.getView(GOLD_VIEW_NAME);
//        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
//      } catch (CASException e)
//      {
//        throw new AnalysisEngineProcessException(e);
//      }
//
//      // remove manual EntityMentions and Modifiers from gold view
//      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
//      goldMentions.addAll(JCasUtil.select(goldView, EntityMention.class));
//      goldMentions.addAll(JCasUtil.select(goldView, Modifier.class));
//      for (IdentifiedAnnotation goldMention : goldMentions)
//      {
//        goldMention.removeFromIndexes();
//      }
//
//      // copy cTAKES EntityMentions and Modifiers to gold view
//      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
//      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
//      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
//      CasCopier copier = new CasCopier(systemView.getCas(), goldView.getCas());
//      for (IdentifiedAnnotation cTakesMention : cTakesMentions)
//      {
//        Annotation copy = (Annotation) copier.copyFs(cTakesMention);
//        Feature sofaFeature = copy.getType().getFeatureByBaseName("sofa");
//        copy.setFeatureValue(sofaFeature, goldView.getSofa());
//        copy.addToIndexes();
//      }
//
//      // replace gold EntityMentions and Modifiers in relations with cTAKES ones
//      List<BinaryTextRelation> relations = new ArrayList<BinaryTextRelation>();
//      relations.addAll(JCasUtil.select(goldView, BinaryTextRelation.class));
//      for (BinaryTextRelation relation : relations)
//      {
//
//        // attempt to replace the gold RelationArguments with system ones
//        int replacedArgumentCount = 0;
//        for (RelationArgument relArg : Arrays.asList(relation.getArg1(),
//            relation.getArg2()))
//        {
//          Annotation goldArg = relArg.getArgument();
//          Class<? extends Annotation> argClass = goldArg.getClass();
//
//          // find all annotations covered by the gold argument and of the same
//          // class (these should
//          // be the ones copied over from the cTAKES output earlier)
//          List<? extends Annotation> systemArgs = JCasUtil.selectCovered(
//              goldView, argClass, goldArg);
//
//          // no ctakes annotation found
//          if (systemArgs.size() == 0)
//          {
//            String word = "no";
//            String className = argClass.getSimpleName();
//            String argText = goldArg.getCoveredText();
//            String message = String.format("%s %s for \"%s\"", word, className,
//                argText);
//            this.getContext().getLogger().log(Level.FINE, message);
//            continue;
//          }
//
//          // if there's exactly one annotation, replace the gold one with that
//          if (systemArgs.size() == 1)
//          {
//            relArg.setArgument(systemArgs.get(0));
//            replacedArgumentCount += 1;
//          }
//
//          else
//          {
//            // multiple ctakes arguments found; look for one that matches
//            // exactly
//            // e.g. gold: "right breast", ctakes: "right breast", "breast"
//            for (Annotation systemArg : systemArgs)
//            {
//              String goldArgText = goldArg.getCoveredText();
//              String systemArgText = systemArg.getCoveredText();
//              if (systemArgText.equals(goldArgText))
//              {
//                relArg.setArgument(systemArg);
//                replacedArgumentCount += 1;
//              }
//            }
//
//            if (replacedArgumentCount < 1)
//            {
//              // issue a warning message
//              String word = "multiple";
//              String className = argClass.getSimpleName();
//              String argText = goldArg.getCoveredText();
//              String message = String.format("%s %s for \"%s\"", word,
//                  className, argText);
//              this.getContext().getLogger().log(Level.FINE, message);
//
//              System.out.println("gold argument: " + goldArg.getCoveredText());
//              System.out.println("gold type: "
//                  + ((IdentifiedAnnotation) goldArg).getTypeID());
//              for (Annotation systemArg : systemArgs)
//              {
//                System.out.println("ctakes argument: "
//                    + systemArg.getCoveredText());
//                System.out.println("ctakes type: "
//                    + ((IdentifiedAnnotation) systemArg).getTypeID());
//              }
//              System.out.println();
//            }
//          }
//        }
//
//        // if replacements were not found for both arguments, remove the
//        // relation
//        if (replacedArgumentCount < 2)
//        {
//          relation.removeFromIndexes();
//        }
//      }
//    }
//  }
  
  /**
   * Class that copies the manual {@link Modifier} annotations to the default CAS.
   */
  public static class ReferenceIdentifiedAnnotationsSystemToGoldCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.createView(GOLD_VIEW_NAME);
        goldView.setSofaDataString(jCas.getSofaDataString(), jCas.getSofaMimeType());
        //goldView.setDocumentText(jCas.getDocumentText());
        //goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

//      // remove any automatically generated Modifiers
//      for (Modifier modifier : JCasUtil.select(jCas, Modifier.class)) {
//        modifier.removeFromIndexes();
//      }
      
      for (EntityMention oldSystemEntityMention : JCasUtil.select(jCas, EntityMention.class))
      {
        EntityMention newGoldEntityMention = new EntityMention(goldView, oldSystemEntityMention.getBegin(), oldSystemEntityMention.getEnd());
        
        // copying assertion fields
        newGoldEntityMention.setDiscoveryTechnique(oldSystemEntityMention.getDiscoveryTechnique());
        newGoldEntityMention.setUncertainty(oldSystemEntityMention.getUncertainty());
        newGoldEntityMention.setConditional(oldSystemEntityMention.getConditional());
        newGoldEntityMention.setGeneric(oldSystemEntityMention.getGeneric());
        newGoldEntityMention.setPolarity(oldSystemEntityMention.getPolarity());
        newGoldEntityMention.setSubject(oldSystemEntityMention.getSubject());
        newGoldEntityMention.setHistoryOf(oldSystemEntityMention.getHistoryOf());

        // copying non-assertion fields
        newGoldEntityMention.setConfidence(oldSystemEntityMention.getConfidence());
        newGoldEntityMention.setTypeID(oldSystemEntityMention.getTypeID());
        
        newGoldEntityMention.addToIndexes();
      }

      for (EventMention oldSystemEventMention : JCasUtil.select(jCas, EventMention.class))
      {
        EventMention newGoldEventMention = new EventMention(goldView, oldSystemEventMention.getBegin(), oldSystemEventMention.getEnd());
        
        // copying assertion fields
        newGoldEventMention.setDiscoveryTechnique(oldSystemEventMention.getDiscoveryTechnique());
        newGoldEventMention.setUncertainty(oldSystemEventMention.getUncertainty());
        newGoldEventMention.setConditional(oldSystemEventMention.getConditional());
        newGoldEventMention.setGeneric(oldSystemEventMention.getGeneric());
        newGoldEventMention.setPolarity(oldSystemEventMention.getPolarity());
        newGoldEventMention.setSubject(oldSystemEventMention.getSubject());
        newGoldEventMention.setHistoryOf(oldSystemEventMention.getHistoryOf());

        // copying non-assertion fields
        newGoldEventMention.setConfidence(oldSystemEventMention.getConfidence());
        newGoldEventMention.setTypeID(oldSystemEventMention.getTypeID());

        newGoldEventMention.addToIndexes();
      }
      
      // TODO do we need to copy supporting feature structures (particularly ontology concept array)??

    } // end of method ReferenceIdentifiedAnnotationsSystemToGoldCopier.process()
  } // end of class ReferenceIdentifiedAnnotationsSystemToGoldCopier

  /**
   * Class that copies the manual {@link Modifier} annotations to the default CAS.
   */
  public static class ReferenceSupportingAnnotationsSystemToGoldCopier extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

//      // remove any automatically generated Modifiers
//      for (Modifier modifier : JCasUtil.select(jCas, Modifier.class)) {
//        modifier.removeFromIndexes();
//      }
      
      for (Sentence oldSystemSentence : JCasUtil.select(jCas, Sentence.class))
      {
        Sentence newGoldSentence = new Sentence(goldView, oldSystemSentence.getBegin(), oldSystemSentence.getEnd());
        
        newGoldSentence.addToIndexes();
      }

      for (BaseToken oldSystemToken : JCasUtil.select(jCas, BaseToken.class))
      {
        BaseToken newGoldToken = null; //new BaseToken(goldView, oldSystemEventMention.getBegin(), oldSystemEventMention.getEnd());

        // TODO the following commented out block is an alternative to having the hard coded if..then..else-if..else block for constructing new BaseToken objects
//        Constructor<? extends BaseToken> constructor = null;
//        try
//        {
//          constructor = oldSystemToken.getClass().getConstructor(JCas.class, int.class, int.class);
//        } catch(NoSuchMethodException| SecurityException e)
//        {
//          logger.error("problem getting constructor for copying BaseToken instance (inside AssertionEvalBasedOnModifier.ReferenceSupportingAnnotationsSystemToGoldcopier.process())");
//          continue;
//        }
//        try
//        {
//          newGoldToken = constructor.newInstance(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
//        } catch (InstantiationException | IllegalAccessException
//            | IllegalArgumentException | InvocationTargetException e)
//        {
//          logger.error("problem invoking constructor to copy BaseToken instance (inside AssertionEvalBasedOnModifier.ReferenceSupportingAnnotationsSystemToGoldcopier.process())");
//          continue;
//        }
        
        String oldSystemTokenClass = oldSystemToken.getClass().getName();
        if (oldSystemTokenClass.equals(WordToken.class.getName()))
        {
          newGoldToken = new WordToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(ContractionToken.class.getName()))
        {
          newGoldToken = new ContractionToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(NewlineToken.class.getName()))
        {
          newGoldToken = new NewlineToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(NumToken.class.getName()))
        {
          newGoldToken = new NumToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(PunctuationToken.class.getName()))
        {
          newGoldToken = new PunctuationToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(SymbolToken.class.getName()))
        {
          newGoldToken = new SymbolToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else if (oldSystemTokenClass.equals(BaseToken.class.getName()))
        {
          newGoldToken = new BaseToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        } else
        {
          newGoldToken = new BaseToken(goldView, oldSystemToken.getBegin(), oldSystemToken.getEnd());
        }
        
        newGoldToken.setPartOfSpeech(oldSystemToken.getPartOfSpeech());
        newGoldToken.setTokenNumber(oldSystemToken.getTokenNumber());
        
        newGoldToken.addToIndexes();
      }

    } // end of method ReferenceSupportingAnnotationsSystemToGoldCopier.process()
  } // end of class ReferenceSupportingAnnotationsSystemToGoldCopier

  /**
   * Class that copies the manual {@link Modifier} annotations to the default CAS.
   */
  public static class ReferenceAnnotationsSystemAssertionClearer extends JCasAnnotator_ImplBase
  {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException
    {
      for (EntityMention entityMention : JCasUtil.select(jCas,  EntityMention.class))
      {
        entityMention.setPolarity(1);
      }
      for (EventMention eventMention : JCasUtil.select(jCas,  EventMention.class))
      {
        eventMention.setPolarity(1);
      }
    } // end method ReferenceAnnotationsSystemAssertionClearer.process()
  } // end class ReferenceAnnotationsSystemAssertionClearer

  
  
} // end of class AssertionEvalBasedOnModifier
