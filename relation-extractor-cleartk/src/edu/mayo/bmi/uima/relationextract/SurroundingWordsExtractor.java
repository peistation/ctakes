package edu.mayo.bmi.uima.relationextract;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;

/**
 * Implement a feature extractor that extracts: a bag of the 3 words preceding the first argument, a
 * bag of the words between the relation arguments, and a bag of the 3 words following the second
 * argument.
 * 
 * Delegates to {@link ContextExtractor}, which provides most of this functionality already.
 */
public class SurroundingWordsExtractor implements FeatureExtractor {

  private ContextExtractor<BaseToken> extractor = new ContextExtractor<BaseToken>(
      BaseToken.class,
      new SpannedTextExtractor(),
      new Bag(new Preceding(3)),
      new Bag(new Covered()),
      new Bag(new Following(3)));

  @Override
  public List<Feature> extract(JCas jCas, Annotation arg1, Annotation arg2)
      throws AnalysisEngineProcessException {
    return this.extractor.extractBetween(jCas, arg1, arg2);
  }

}