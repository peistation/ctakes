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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.Level;
//import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
//import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation;
//import org.chboston.cnlp.ctakes.relationextractor.ae.ModifierExtractorAnnotator;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.opennlp.MaxentStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.mitre.medfacts.uima.ZoneAnnotator;
import org.apache.ctakes.assertion.medfacts.cleartk.AssertionCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.AssertionComponents;
import org.apache.ctakes.assertion.medfacts.cleartk.ConditionalCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
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

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
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

public class AssertionEvalBasedOnModifier extends Evaluation_ImplBase<File, Map<String, AnnotationStatistics>> {
  
  private static Logger logger = Logger.getLogger(AssertionEvalBasedOnModifier.class); 

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = true)
    public File trainDirectory;
    
    @Option(
        name = "--test-dir",
        usage = "specify the directory contraining the XMI testing files (for example, /NLP/Corpus/Relations/mipacq/xmi/test)",
        required = false)
    public File testDirectory;
    
    @Option(
        name = "--models-dir",
        usage = "specify the directory where the models will be placed",
        required = true)
    public File modelsDirectory;
    
    @Option(
            name = "--evaluation-output-dir",
            usage = "specify the directory where the evaluation output xmi files will go",
            required = false)
    public File evaluationOutputDirectory;
        
    @Option(
            name = "--run-polarity",
            usage = "specify whether polarity processing should be run (true or false). default: true",
            required = false)
    public boolean runPolarity = true;
        
    @Option(
            name = "--run-conditional",
            usage = "specify whether conditional processing should be run (true or false). default: true",
            required = false)
    public boolean runConditional = true;
        
    @Option(
            name = "--run-uncertainty",
            usage = "specify whether uncertainty processing should be run (true or false). default: true",
            required = false)
    public boolean runUncertainty = true;
        
    @Option(
            name = "--run-subject",
            usage = "specify whether subject processing should be run (true or false). default: true",
            required = false,
            handler=BooleanOptionHandler.class)
    public boolean runSubject = true;
        
    @Option(
            name = "--run-generic",
            usage = "specify whether generic processing should be run (true or false). default: true",
            required = false)
    public boolean runGeneric = true;
        
    
  }
  
  protected ArrayList<String> annotationTypes;

  private Class<? extends AssertionCleartkAnalysisEngine> classifierAnnotatorClass;

  private Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;
  
  private File evaluationOutputDirectory;

  
  protected static Options options = new Options();
  
  public static void main(String[] args) throws Exception {
    //Options options = new Options();
    options.parseOptions(args);
    
//    System.err.println("forcing skipping of subject processing!!!");
//    options.runSubject = false;
    System.err.println("forcing skipping of generic processing!!!");
    options.runGeneric = false;
    System.err.println("forcing skipping of polarity processing!!!");
    options.runPolarity = false;
    System.err.println("forcing skipping of uncertainty processing!!!");
    options.runUncertainty = false;
    System.err.println("forcing skipping of conditional processing!!!");
    options.runConditional = false;
    printOptionsForDebugging(options);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());
    //File modelsDir = new File("models/modifier");
    File modelsDir = options.modelsDirectory;
    File evaluationOutputDirectory = options.evaluationOutputDirectory;

    // determine the type of classifier to be trained
    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultMaxentDataWriterFactory.class;
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
    if (options.runPolarity) { annotationTypes.add("polarity"); }
    if (options.runConditional) { annotationTypes.add("conditional"); }
    if (options.runUncertainty) { annotationTypes.add("uncertainty"); }
    if (options.runSubject) { annotationTypes.add("subject"); }
    if (options.runGeneric) { annotationTypes.add("generic"); }
    
    AssertionEvalBasedOnModifier evaluation = new AssertionEvalBasedOnModifier(
        modelsDir,
        evaluationOutputDirectory,
        annotationTypes,
        annotatorClass,
        dataWriterFactoryClass
        );
    /*
        ,
        "-t",
        "0",
        "-c",
        "1000");
        */

//    List<AnnotationStatistics> foldStats = evaluation.crossValidation(trainFiles, 2);
//    //AnnotationStatistics overallStats = AnnotationStatistics.addAll(foldStats);
//    //AnnotationStatistics overallStats = new AnnotationStatistics();
//    //overallStats.addAll(foldStats);
//    AnnotationStatistics overallStats = new AnnotationStatistics();
//    for (AnnotationStatistics singleFoldStats : foldStats)
//    {
//    	overallStats.addAll(singleFoldStats);
//    }
//    System.err.println("Overall:");
//    System.err.println(overallStats);
    
    
    
    if(options.testDirectory == null) {
      // run n-fold cross-validation
      List<Map<String, AnnotationStatistics>> foldStats = evaluation.crossValidation(trainFiles, 2);
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
      
      AssertionEvalBasedOnModifier.printScore(overallStats,  "CROSS FOLD OVERALL");
      
    } else {
      // train on the entire training set and evaluate on the test set
      List<File> testFiles = Arrays.asList(options.testDirectory.listFiles());
      
      CollectionReader trainCollectionReader = evaluation.getCollectionReader(trainFiles);
      evaluation.train(trainCollectionReader, modelsDir);
      
      CollectionReader testCollectionReader = evaluation.getCollectionReader(testFiles);
      Map<String, AnnotationStatistics> stats = evaluation.test(testCollectionReader, modelsDir);
      
      AssertionEvalBasedOnModifier.printScore(stats,  modelsDir.getAbsolutePath());
    }
    
    System.out.println("Finished.");
    
  }
  
  private static void printOptionsForDebugging(Options options)
  {
	System.out.format(
		"training dir: %s%n" +
	    "test dir: %s%n" + 
	    "model dir: %s%n" +
	    "run polarity: %b%n" +
	    "run conditional: %b%n" +
	    "run uncertainty: %b%n" +
	    "run subject: %b%n" +
	    "run generic: %b%n" +
	    "%n%n",
	    options.trainDirectory.getAbsolutePath(),
	    options.testDirectory.getAbsolutePath(),
	    options.modelsDirectory.getAbsolutePath(),
	    options.runPolarity,
	    options.runConditional,
	    options.runUncertainty,
	    options.runSubject,
	    options.runGeneric
	    );
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

  public AssertionEvalBasedOnModifier(
      File modelDirectory,
      File evaluationOutputDirectory,
      ArrayList<String> annotationTypes,
      Class<? extends AssertionCleartkAnalysisEngine> classifierAnnotatorClass,
      Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
      String... trainingArguments
      ) {
    super(modelDirectory);
    
    this.annotationTypes = annotationTypes;

    this.classifierAnnotatorClass = classifierAnnotatorClass;
    this.dataWriterFactoryClass = dataWriterFactoryClass;

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
    
    AnalysisEngineDescription goldCopierIdentifiedAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierIdentifiedAnnotsAnnotator);
    
    AnalysisEngineDescription goldCopierSupportingAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceSupportingAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierSupportingAnnotsAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    URI generalSectionRegexFileUri =
        this.getClass().getClassLoader().getResource("org/mitre/medfacts/zoner/section_regex.xml").toURI();
//      ExternalResourceDescription generalSectionRegexDescription = ExternalResourceFactory.createExternalResourceDescription(
//          SectionRegexConfigurationResource.class, new File(generalSectionRegexFileUri));
      AnalysisEngineDescription zonerAnnotator =
          AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
              ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
              generalSectionRegexFileUri
              );
      builder.add(zonerAnnotator);

      URI mayoSectionRegexFileUri =
          this.getClass().getClassLoader().getResource("org/mitre/medfacts/uima/mayo_sections.xml").toURI();
//        ExternalResourceDescription mayoSectionRegexDescription = ExternalResourceFactory.createExternalResourceDescription(
//            SectionRegexConfigurationResource.class, new File(mayoSectionRegexFileUri));
      AnalysisEngineDescription mayoZonerAnnotator =
          AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
              ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
              mayoSectionRegexFileUri
              );
      builder.add(mayoZonerAnnotator);
    
    
    if (options.runPolarity)
    {
	    AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        polarityAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
	        this.dataWriterFactoryClass.getName(),
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "polarity").getPath()
	        );
	    builder.add(polarityAnnotator);
    }

    if (options.runConditional)
    {
	    AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        conditionalAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
	        this.dataWriterFactoryClass.getName(),
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "conditional").getPath()
	        );
	    builder.add(conditionalAnnotator);
    }

    if (options.runUncertainty)
    {
	    AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        uncertaintyAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
	        this.dataWriterFactoryClass.getName(),
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "uncertainty").getPath()
	        );
	    builder.add(uncertaintyAnnotator);
    }

    if (options.runSubject)
    {
	    AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        subjectAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
	        this.dataWriterFactoryClass.getName(),
	        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
	        new File(directory, "subject").getPath()
	        );
	    builder.add(subjectAnnotator);
    }

    if (options.runGeneric)
    {
		AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
		ConfigurationParameterFactory.addConfigurationParameters(
		    genericAnnotator,
		    AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
		    AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
		    CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
		    this.dataWriterFactoryClass.getName(),
		    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		    new File(directory, "generic").getPath()
		    );
		builder.add(genericAnnotator);
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
    
    HideOutput hider = new HideOutput();
    for (String currentAssertionAttribute : annotationTypes)
    {
    	File currentDirectory = new File(directory, currentAssertionAttribute);
    	JarClassifierBuilder.trainAndPackage(currentDirectory, trainingArguments);
    }
    hider.restoreOutput();
  }

  @Override
  protected Map<String, AnnotationStatistics> test(CollectionReader collectionReader, File directory)
      throws Exception {
//    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createPrimitive(AssertionCleartkAnalysisEngine.getDescription(
//        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
//        new File(directory, "model.jar").getPath()));

    AggregateBuilder builder = new AggregateBuilder();
    
    AnalysisEngineDescription goldCopierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    if (options.runPolarity)
    {
	    AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        polarityAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(new File(directory, "polarity"), "model.jar").getPath()
	        );
	    builder.add(polarityAnnotator);
    }
    
    if (options.runConditional)
    {
	    AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        conditionalAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(new File(directory, "conditional"), "model.jar").getPath()
	        );
	    builder.add(conditionalAnnotator);
    }
    
    if (options.runUncertainty)
    {
	    AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        uncertaintyAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(new File(directory, "uncertainty"), "model.jar").getPath()
	        );
	    builder.add(uncertaintyAnnotator);
    }
    
    if (options.runSubject)
    {
	    AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        subjectAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(new File(directory, "subject"), "model.jar").getPath()
	        );
	    builder.add(subjectAnnotator);
    }
    
    if (options.runGeneric)
    {
	    AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
	    ConfigurationParameterFactory.addConfigurationParameters(
	        genericAnnotator,
	        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
	        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(new File(directory, "generic"), "model.jar").getPath()
	        );
	    builder.add(genericAnnotator);
    }
    
    if (evaluationOutputDirectory != null)
    {
        AnalysisEngineDescription xwriter =
    		AnalysisEngineFactory.createPrimitiveDescription(
	            XWriter.class,
	            AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
	            XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
	            evaluationOutputDirectory,
	            XWriter.PARAM_XML_SCHEME_NAME,
	            XWriter.XMI);
        builder.add(xwriter);
    }
    
    //SimplePipeline.runPipeline(collectionReader,  builder.createAggregateDescription());
    AnalysisEngineDescription aggregateDescription = builder.createAggregateDescription();
    AnalysisEngine aggregate = builder.createAggregate();
    
    AnnotationStatistics polarityStats = new AnnotationStatistics();
    AnnotationStatistics conditionalStats = new AnnotationStatistics();
    AnnotationStatistics uncertaintyStats = new AnnotationStatistics();
    AnnotationStatistics subjectStats = new AnnotationStatistics();
    AnnotationStatistics genericStats = new AnnotationStatistics();
    
    Map<String, AnnotationStatistics> map = new TreeMap<String, AnnotationStatistics>(); 
    if (options.runPolarity)
    {
      map.put("polarity",  polarityStats);
    }

    if (options.runConditional)
    {
      map.put("conditional",  conditionalStats);
    }

    if (options.runUncertainty)
    {
      map.put("uncertainty",  uncertaintyStats);
    }

    if (options.runSubject)
    {
      map.put("subject", subjectStats);
    }

    if (options.runGeneric)
    {
      map.put("generic", genericStats);
    }

    for (JCas jCas : new JCasIterable(collectionReader, aggregate)) {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      
      String documentId = DocumentIDAnnotationUtil.getDocumentID(jCas);
      System.out.format("document id: %s%n", documentId);
      
      Collection<IdentifiedAnnotation> goldEntitiesAndEvents = new ArrayList<IdentifiedAnnotation>(); 
      Collection<EntityMention> goldEntities = JCasUtil.select(goldView, EntityMention.class);
	  goldEntitiesAndEvents.addAll(goldEntities);
      Collection<EventMention> goldEvents = JCasUtil.select(goldView, EventMention.class);
      goldEntitiesAndEvents.addAll(goldEvents);
      System.out.format("gold entities: %d%ngold events: %d%n%n", goldEntities.size(), goldEvents.size());
      
      Collection<IdentifiedAnnotation> systemEntitiesAndEvents = new ArrayList<IdentifiedAnnotation>();
      Collection<EntityMention> systemEntities = JCasUtil.select(jCas, EntityMention.class);
      systemEntitiesAndEvents.addAll(systemEntities);
      Collection<EventMention> systemEvents = JCasUtil.select(jCas, EventMention.class);
      systemEntitiesAndEvents.addAll(systemEvents);
      System.out.format("system entities: %d%nsystem events: %d%n%n", systemEntities.size(), systemEvents.size());
      
      if (options.runPolarity)
      {
	      polarityStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("polarity"));
      }

      if (options.runConditional)
      {
	      conditionalStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("conditional"));
      }

      if (options.runUncertainty)
      {
	      uncertaintyStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("uncertainty"));
      }

      if (options.runSubject)
      {
	      subjectStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("subject"));
      }

      if (options.runGeneric)
      {
	      genericStats.add(goldEntitiesAndEvents, systemEntitiesAndEvents,
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToSpan(),
			  AnnotationStatistics.<IdentifiedAnnotation>annotationToFeatureValue("generic"));
      }

    }
    
    return map;
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

        // copying non-assertion fields
        newGoldEntityMention.setConfidence(oldSystemEntityMention.getConfidence());

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

        // copying non-assertion fields
        newGoldEventMention.setConfidence(oldSystemEventMention.getConfidence());

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
