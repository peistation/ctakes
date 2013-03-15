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
import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation;
import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.ParameterSettings;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.testing.util.HideOutput;
import org.xml.sax.SAXException;

/**
 * This class produces production models for the RelationExtractor module. Specifically it produces
 * model and descriptor files for the ModifierExtractor, DegreeOfRelationExtractor, and
 * EntityMentionPairRelationExtractor. Additionally it produces an aggregrate descriptor for the
 * entire pipeline from pre-processing to relation extraction.
 * 
 * @author dmitriy dligach
 * 
 */
public class RelationExtractorTrain {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--output-dir",
        usage = "the output directory; typically the ctakes-relation-extractor directory")
    public File outputDirectory = new File(".");

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/sharp/xmi/all)",
        required = true)
    public File trainDirectory;
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    if (!options.outputDirectory.exists()) {
      throw new IllegalArgumentException("directory not found: "
          + options.outputDirectory.getCanonicalPath());
    }
    File resourcesDirectory = new File(options.outputDirectory, "src/main/resources");
    File descriptorsDirectory = new File(options.outputDirectory, "desc/analysis_engine");

    File preprocessDescFile = new File(descriptorsDirectory, "RelationExtractorPreprocessor.xml");
    if (!preprocessDescFile.exists()) {
      throw new IllegalArgumentException("Can't create aggregate without "
          + preprocessDescFile.getCanonicalPath());
    }

    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());

    // Initialize model directories
    String modelPathPrefix = "org/apache/ctakes/relationextractor/models/";
    String modifierModelPath = modelPathPrefix + "modifier_extractor";
    String degreeOfModelPath = modelPathPrefix + "degree_of";
    String locationOfModelPath = modelPathPrefix + "em_pair";

    // create the modifier extractor
    System.out.println("training modifier extractor");
    File modifierTrainDirectory = new File(resourcesDirectory, modifierModelPath);
    ModifierExtractorEvaluation evaluation = new ModifierExtractorEvaluation(
        modifierTrainDirectory,
        "-t",
        "0", // svm kernel index
        "-c",
        "1000" // svm cost
    );
    HideOutput hider = new HideOutput();
    evaluation.train(evaluation.getCollectionReader(trainFiles), modifierTrainDirectory);
    hider.restoreOutput();
    hider.close(); // workaround for https://code.google.com/p/uimafit/issues/detail?id=129
    AnalysisEngineDescription modifierExtractorDesc = AnalysisEngineFactory.createPrimitiveDescription(
        ModifierExtractorAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        "/" + modifierModelPath + "/model.jar");
    writeDesc(descriptorsDirectory, ModifierExtractorAnnotator.class, modifierExtractorDesc);

    // create the degree_of extractor
    System.out.println("training degree_of extractor");
    AnalysisEngineDescription degreeOfRelationExtractorDesc = trainRelationExtractor(
        resourcesDirectory,
        degreeOfModelPath,
        trainFiles,
        "degree_of",
        DegreeOfRelationExtractorAnnotator.class,
        LIBSVMStringOutcomeDataWriter.class,
        RelationExtractorEvaluation.BEST_DEGREE_OF_PARAMETERS,
        descriptorsDirectory);

    // create the location_of extractor
    System.out.println("training location_of extractor");
    AnalysisEngineDescription locationOfRelationExtractorDesc = trainRelationExtractor(
        resourcesDirectory,
        locationOfModelPath,
        trainFiles,
        "location_of",
        EntityMentionPairRelationExtractorAnnotator.class,
        LIBSVMStringOutcomeDataWriter.class,
        RelationExtractorEvaluation.BEST_NON_DEGREE_OF_PARAMETERS,
        descriptorsDirectory);

    // create the aggregate
    System.out.println("assembling relation extraction aggregate");
    AggregateBuilder builder = new AggregateBuilder();
    XMLParser parser = UIMAFramework.getXMLParser();
    XMLInputSource source = new XMLInputSource(preprocessDescFile);
    builder.add(parser.parseAnalysisEngineDescription(source));
    builder.add(modifierExtractorDesc);
    builder.add(degreeOfRelationExtractorDesc);
    builder.add(locationOfRelationExtractorDesc);
    AnalysisEngineDescription aggregateDescription = builder.createAggregateDescription();
    writeDesc(descriptorsDirectory, "RelationExtractorAggregate", aggregateDescription);

    // cleanup unnecessary model files
    for (File modelDir : new File(resourcesDirectory, modelPathPrefix).listFiles()) {
      for (File modelFile : modelDir.listFiles()) {
        if (!modelFile.getName().equals("model.jar")) {
          modelFile.delete();
        }
      }
    }
  }

  private static AnalysisEngineDescription trainRelationExtractor(
      File resourcesDirectory,
      String modelPath,
      List<File> trainFiles,
      String relationCategory,
      Class<? extends RelationExtractorAnnotator> annotatorClass,
      Class<? extends DataWriter<String>> dataWriterClass,
      ParameterSettings params,
      File descriptorsDirectory) throws Exception {

    // define additional configuration parameters for the annotator
    Object[] additionalParameters = new Object[] {
        RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
        params.probabilityOfKeepingANegativeExample,
        EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
        params.classifyBothDirections };

    // define arguments to be passed to the classifier
    String[] trainingArguments = new String[] {
        "-t",
        String.valueOf(params.svmKernelIndex),
        "-c",
        String.valueOf(params.svmCost),
        "-g",
        String.valueOf(params.svmGamma) };

    File trainDirectory = new File(resourcesDirectory, modelPath);
    RelationExtractorEvaluation evaluation = new RelationExtractorEvaluation(
        trainDirectory,
        relationCategory,
        annotatorClass,
        dataWriterClass,
        additionalParameters,
        trainingArguments,
        false,
        false,
        false,
        false);

    evaluation.train(evaluation.getCollectionReader(trainFiles), trainDirectory);

    // create the description
    AnalysisEngineDescription relationExtractorDescription = AnalysisEngineFactory.createPrimitiveDescription(
        annotatorClass,
        RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
        params.probabilityOfKeepingANegativeExample,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        "/" + modelPath + "/model.jar");
    if (annotatorClass == EntityMentionPairRelationExtractorAnnotator.class) {
      ConfigurationParameterFactory.addConfigurationParameters(
          relationExtractorDescription,
          EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
          params.classifyBothDirections);
    }

    // write the description
    writeDesc(descriptorsDirectory, annotatorClass, relationExtractorDescription);

    // return the description (for use in an aggregate)
    return relationExtractorDescription;
  }

  private static void writeDesc(
      File descDir,
      Class<?> annotatorClass,
      AnalysisEngineDescription desc) throws SAXException, IOException {
    // set the type system (uimaFIT expands all imports, so this simplifies the descriptor)
    desc.getAnalysisEngineMetaData().setTypeSystem(TypeSystemDescriptionFactory.createTypeSystemDescription(
        "org.apache.ctakes.typesystem.types.TypeSystem"));
    writeDesc(descDir, annotatorClass.getSimpleName(), desc);
  }

  private static void writeDesc(File descDir, String name, AnalysisEngineDescription desc)
      throws SAXException, IOException {
    // set the name (not done by uimaFIT)
    desc.getMetaData().setName(name);
    File descFile = new File(descDir, name + ".xml");
    System.out.println("writing description to " + descFile);
    FileOutputStream output = new FileOutputStream(descFile);
    desc.toXML(output);
    output.close();
  }
}
