package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;

import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.NamedEntity;

public class PartOfSpeechFeaturesExtractor implements RelationFeaturesExtractor {

  private SimpleFeatureExtractor pos = new TypePathExtractor(BaseToken.class, "partOfSpeech");

  /**
   * All part-of-speech tags of the mention as a bag
   */
  private SimpleFeatureExtractor tokenPOS = new ContextExtractor<BaseToken>(
      BaseToken.class,
      pos,
      new Bag(new Covered()));

  /**
   * All extractors for mention 1, with features named to distinguish them from mention 2
   */
  private SimpleFeatureExtractor mention1FeaturesExtractor = new NamingExtractor(
      "mention1",
      tokenPOS);

  /**
   * All extractors for mention 2, with features named to distinguish them from mention 1
   */
  private SimpleFeatureExtractor mention2FeaturesExtractor = new NamingExtractor(
      "mention1",
      tokenPOS);

  @Override
  public List<Feature> extract(JCas jCas, NamedEntity arg1, NamedEntity arg2)
      throws AnalysisEngineProcessException {
    List<Feature> features = new ArrayList<Feature>();
    features.addAll(this.mention1FeaturesExtractor.extract(jCas, arg1));
    features.addAll(this.mention2FeaturesExtractor.extract(jCas, arg2));
    return features;
  }

}
