package org.chboston.cnlp.ctakes.relationextractor.eval.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.EntityMentionRemover;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.GoldEntityMentionCopier;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.testing.util.HideOutput;

/**
   * Defines how to write training data, train a classifier, and apply the classifier to new data.
   */
  public abstract class RelationExtractionPipelineProvider extends CleartkPipelineProvider_ImplBase {

    protected File modelsDirectory;

    protected Class<? extends DataWriterFactory<String>> dataWriterFactoryClass;

    protected Object[] additionalParameters;

    public RelationExtractionPipelineProvider(
        File modelsDirectory,
        Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
        Object... additionalParameters) throws UIMAException, IOException {
      this.modelsDirectory = modelsDirectory;
      this.dataWriterFactoryClass = dataWriterFactoryClass;
      this.additionalParameters = additionalParameters;
    }

    protected abstract AnalysisEngineDescription getClassifierAnnotatorDescription()
        throws ResourceInitializationException; 
    
   
    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      // configure the relation extractor for training mode
      AnalysisEngineDescription desc = this.getClassifierAnnotatorDescription();
      ConfigurationParameterFactory.addConfigurationParameters(
          desc,
          RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
          RelationExtractorEvaluation.GOLD_VIEW_NAME,
          CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
          this.dataWriterFactoryClass.getName(),
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getDir(name).getPath());

      return Arrays.asList(
          // remove cTAKES entities from the system view
          AnalysisEngineFactory.createPrimitive(EntityMentionRemover.class),
          // copy gold mentions into the system view
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          // run the relation extractor
          AnalysisEngineFactory.createPrimitive(desc));
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      // configure the relation extractor for classification mode
      AnalysisEngineDescription desc = this.getClassifierAnnotatorDescription();
      ConfigurationParameterFactory.addConfigurationParameters(
          desc,
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          new File(this.getDir(name), "model.jar").getPath());

      return Arrays.asList(
          // remove cTAKES entities from the system view
          AnalysisEngineFactory.createPrimitive(EntityMentionRemover.class),
          // copy gold mentions into the system view
          AnalysisEngineFactory.createPrimitive(GoldEntityMentionCopier.class),
          // run the relation extractor
          AnalysisEngineFactory.createPrimitive(desc));
    }

    @Override
    public void train(String name, String... trainingArguments) throws Exception {
      // train the classifier and package it into a .jar file
      HideOutput hider = new HideOutput();
      JarClassifierBuilder.trainAndPackage(this.getDir(name), trainingArguments);
      hider.restoreOutput();
    }

    private File getDir(String name) {
      return new File(this.modelsDirectory, name);
    }
  }