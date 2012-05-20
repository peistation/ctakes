package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.FirstCovered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.LastCovered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.annotationpair.DistanceExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class TokenFeaturesExtractor implements RelationFeaturesExtractor {

  private SimpleFeatureExtractor coveredText = new SpannedTextExtractor();

  /**
   * First word of the mention, last word of the mention, all words of the mention as a bag, the
   * preceding 3 words, the following 3 words
   */
  private SimpleFeatureExtractor tokenContext = new ContextExtractor<BaseToken>(
      BaseToken.class,
      coveredText,
      new FirstCovered(1),
      new LastCovered(1),
      new Bag(new Covered()),
      new Preceding(3),
      new Following(3));

  /**
   * All extractors for mention 1, with features named to distinguish them from mention 2
   */
  private SimpleFeatureExtractor mention1FeaturesExtractor = new NamingExtractor(
      "mention1",
      coveredText,
      tokenContext);

  /**
   * All extractors for mention 2, with features named to distinguish them from mention 1
   */
  private SimpleFeatureExtractor mention2FeaturesExtractor = new NamingExtractor(
      "mention2",
      coveredText,
      tokenContext);

  /**
   * First word, last word, and all words between the mentions
   */
  private ContextExtractor<?> tokensBetween = new ContextExtractor<BaseToken>(
      BaseToken.class,
      new NamingExtractor("BetweenMentions", coveredText),
      new FirstCovered(1),
      new LastCovered(1),
      new Bag(new Covered()));

  /**
   * Number of words between the mentions
   */
  private DistanceExtractor nTokensBetween = new DistanceExtractor(null, BaseToken.class);

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException {
    List<Feature> features = new ArrayList<Feature>();
    features.addAll(this.mention1FeaturesExtractor.extract(jCas, arg1));
    features.addAll(this.mention2FeaturesExtractor.extract(jCas, arg2));
    features.addAll(this.tokensBetween.extractBetween(jCas, arg1, arg2));
    features.addAll(this.nTokensBetween.extract(jCas, arg1, arg2));
    return features;
  }

}
