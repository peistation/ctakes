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
package org.apache.ctakes.relationextractor.eval;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import org.apache.ctakes.relationextractor.ae.ModifierExtractorAnnotator;
import org.apache.ctakes.typesystem.type.textsem.Modifier;

public class ModifierExtractorEvaluation extends Evaluation_ImplBase<File, AnnotationStatistics<String>> {

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
    File modelsDir = new File("models/modifier");

    ModifierExtractorEvaluation evaluation = new ModifierExtractorEvaluation(
        modelsDir,
        "-t",
        "0",
        "-c",
        "1000");

    List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(trainFiles, 2);
    AnnotationStatistics<String> overallStats = AnnotationStatistics.addAll(foldStats);
    System.err.println("Overall:");
    System.err.println(overallStats);
  }

  private String[] trainingArguments;

  public ModifierExtractorEvaluation(File directory, String... trainingArguments) {
    super(directory);
    this.trainingArguments = trainingArguments;
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
        TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../ctakes-type-system/desc/common_type_system.xml"),
        XMIReader.PARAM_FILES,
        paths);
  }

  @Override
  public void train(CollectionReader collectionReader, File directory) throws Exception {
    SimplePipeline.runPipeline(
        collectionReader,
        AnalysisEngineFactory.createPrimitiveDescription(OnlyGoldModifiers.class),
        ModifierExtractorAnnotator.getDescription(
            CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
            MultiClassLIBSVMDataWriterFactory.class.getName(),
            DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
            directory.getPath()));
    JarClassifierBuilder.trainAndPackage(directory, this.trainingArguments);
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createPrimitive(ModifierExtractorAnnotator.getDescription(
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(directory, "model.jar").getPath()));

    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    for (JCas jCas : new JCasIterable(collectionReader, classifierAnnotator)) {
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      Collection<Modifier> goldModifiers = JCasUtil.select(goldView, Modifier.class);
      Collection<Modifier> systemModifiers = JCasUtil.select(jCas, Modifier.class);
      stats.add(goldModifiers, systemModifiers);
    }
    System.err.println(directory.getName() + ":");
    System.err.println(stats);
    return stats;
  }

  private static final String GOLD_VIEW_NAME = "GoldView";

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
}
