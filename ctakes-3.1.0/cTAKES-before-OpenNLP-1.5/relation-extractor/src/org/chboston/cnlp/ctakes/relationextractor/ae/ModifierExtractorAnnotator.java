package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunker.ChunkLabeler_ImplBase;
import org.cleartk.chunker.Chunker;
import org.cleartk.chunker.ChunkerFeatureExtractor;
import org.cleartk.chunker.DefaultChunkLabeler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.Modifier;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public class ModifierExtractorAnnotator extends Chunker {

  public static class FeatureExtractor implements ChunkerFeatureExtractor {

    private List<SimpleFeatureExtractor> extractors;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      SpannedTextExtractor coveredTextExtractor = new SpannedTextExtractor();
      //TypePathExtractor posExtractor = new TypePathExtractor(BaseToken.class, "partOfSpeech");
      this.extractors = Arrays.<SimpleFeatureExtractor>asList(coveredTextExtractor);
    }

    @Override
    public Instance<String> extractFeatures(
        JCas jCas,
        Annotation labeledAnnotation,
        Annotation sequence) throws CleartkExtractorException {
      Instance<String> instance = new Instance<String>();
      for (SimpleFeatureExtractor extractor : this.extractors) {
        instance.addAll(extractor.extract(jCas, labeledAnnotation));
      }
      instance.add(new Feature(labeledAnnotation.getCoveredText()));
      return instance;
    }

  }

  public static AnalysisEngineDescription getDescription(Object... additionalConfiguration)
      throws ResourceInitializationException {
    AnalysisEngineDescription desc = AnalysisEngineFactory.createPrimitiveDescription(
        ModifierExtractorAnnotator.class,
        Chunker.PARAM_SEQUENCE_CLASS_NAME,
        Sentence.class.getName(),
        Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME,
        BaseToken.class.getName(),
        Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME,
        FeatureExtractor.class.getName(),
        Chunker.PARAM_CHUNK_LABELER_CLASS_NAME,
        DefaultChunkLabeler.class.getName(),
        ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME,
        Modifier.class.getName());
    if (additionalConfiguration.length > 0) {
      ConfigurationParameterFactory.addConfigurationParameters(desc, additionalConfiguration);
    }
    return desc;
  }

}
