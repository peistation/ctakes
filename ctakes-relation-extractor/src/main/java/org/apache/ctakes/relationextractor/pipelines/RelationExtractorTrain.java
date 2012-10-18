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
package org.apache.ctakes.relationextractor.pipelines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.ModifierExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.eval.ModifierExtractorEvaluation;
import org.apache.ctakes.relationextractor.eval.MultiClassLIBSVMDataWriterFactory;
import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation;
import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.ParameterSettings;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.xml.sax.SAXException;

/**
 * This class produces production models for the RelationExtractor module.  Specifically it produces model and descriptor files for the 
 * ModifierExtractor, DegreeOfRelationExtractor, and EntityMentionPairRelationExtractor.  Additionally it produces an aggregrate descriptor
 * for the entire pipeline from pre-processing to relation extraction.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorTrain {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/sharp/xmi/all)",
        required = true)
    public File trainDirectory;

    @Option(
        name = "--desc-dir",
        usage = "specify the directory to write out description files",
        required = false)
    public File descDir = new File("desc/analysis_engine");
  }
  
  public static AnalysisEngineDescription trainModifierExtractor(
		  File modelsDir,
		  List<File> trainFiles,
		  Class<? extends DataWriterFactory<String>> dataWriterFactoryClass) throws Exception {
				  
	  ModifierExtractorEvaluation evaluation = new ModifierExtractorEvaluation(
			  modelsDir,
			  "-t", "0",   // svm kernel index
			  "-c", "1000" // svm cost
			  );
	  CollectionReader collectionReader = evaluation.getCollectionReader(trainFiles);
	  evaluation.train(collectionReader, modelsDir);
	  
	  // create the description
	  AnalysisEngineDescription modifierExtractorDescription = AnalysisEngineFactory.createPrimitiveDescription(
			  ModifierExtractorAnnotator.class,
			  GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
			  new File(modelsDir, "model.jar").getPath());
	  
	  return modifierExtractorDescription;
  }
  
  public static AnalysisEngineDescription trainRelationExtractor(
		  File modelsDir, 
		  List<File> trainFiles,
		  Class<? extends RelationExtractorAnnotator> annotatorClass, 
		  Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
		  ParameterSettings params) throws Exception {

	  // define additional configuration parameters for the annotator
	  Object[] additionalParameters = new Object[] {
			  RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
			  params.probabilityOfKeepingANegativeExample,
        EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
			  params.classifyBothDirections,
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
	    
	    // create the description
	    AnalysisEngineDescription relationExtractorDescription = AnalysisEngineFactory.createPrimitiveDescription(
	        annotatorClass,
	        RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
	        params.probabilityOfKeepingANegativeExample,
	        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	        new File(modelsDir, "model.jar").getPath());
	    if (annotatorClass == EntityMentionPairRelationExtractorAnnotator.class) {
	      ConfigurationParameterFactory.addConfigurationParameters(
	          relationExtractorDescription,
	          EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
	          params.classifyBothDirections);
	    }    
	    
	    return relationExtractorDescription;
  }
	
  public static void main(String[] args) throws Exception {
		
    Options options = new Options();
    options.parseOptions(args);
    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());
    
    // Initialize model directories
    File modelsDirBase = new File("resources/models");
    File modelsDirModExtractor = new File(modelsDirBase, "modifier_extractor");
    File modelsDirDegreeOf = new File(modelsDirBase, "degree_of");
    File modelsDirEMPair = new File(modelsDirBase, "em_pair");
    
    // Initialize component parameters
    ParameterSettings degreeOfParams = new ParameterSettings(false, 1.0f, "linear", 0.05, 1.0);
    ParameterSettings emPairParams = new ParameterSettings(false, 0.5f, "linear", 0.05, 1.0);

    // For now all three components use MultiClass SVMs for classification
    Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = MultiClassLIBSVMDataWriterFactory.class;
        
    // Train and write models
    AnalysisEngineDescription modifierExtractorDesc = trainModifierExtractor(modelsDirModExtractor, trainFiles, dataWriterFactoryClass);
    writeDesc(options.descDir, "ModifierExtractorAnnotator", modifierExtractorDesc);
    AnalysisEngineDescription degreeOfRelationExtractorDesc = trainRelationExtractor(modelsDirDegreeOf, trainFiles, DegreeOfRelationExtractorAnnotator.class, dataWriterFactoryClass, degreeOfParams);
    writeDesc(options.descDir, "DegreeOfRelationExtractorAnnotator", degreeOfRelationExtractorDesc);
    AnalysisEngineDescription emPairRelationExtractorDesc = trainRelationExtractor(modelsDirEMPair, trainFiles, EntityMentionPairRelationExtractorAnnotator.class, dataWriterFactoryClass, emPairParams);
    writeDesc(options.descDir, "EntityMentionPairRelationExtractorAnnotator", emPairRelationExtractorDesc);

    // create the aggregate description
    AggregateBuilder builder = new AggregateBuilder();
    File preprocessDescFile = new File("desc/analysis_engine/RelationExtractorPreprocessor.xml");
    XMLParser parser = UIMAFramework.getXMLParser();
    XMLInputSource source = new XMLInputSource(preprocessDescFile);
    builder.add(parser.parseAnalysisEngineDescription(source));
    builder.add(modifierExtractorDesc);
    builder.add(degreeOfRelationExtractorDesc);
    builder.add(emPairRelationExtractorDesc);
    AnalysisEngineDescription aggregateDescription = builder.createAggregateDescription();
    
    // write out the XML version of the aggregate description
    writeDesc(options.descDir, "RelationExtractorAggregate", aggregateDescription);
  }

  private static void writeDesc(File descDir, String filePrefix, AnalysisEngineDescription desc)
      throws SAXException, IOException {
    File descFile = new File(descDir, filePrefix + ".xml");
    FileOutputStream output = new FileOutputStream(descFile);
    desc.toXML(output);
    output.close();
    System.out.println("description written to " + descFile);
  }
}
