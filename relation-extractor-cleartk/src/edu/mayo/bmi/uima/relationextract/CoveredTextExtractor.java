package edu.mayo.bmi.uima.relationextract;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;

/**
 * Implement a feature extractor that returns the text from each of the arguments.
 */
public class CoveredTextExtractor implements FeatureExtractor {
  @Override
  public List<Feature> extract(JCas jCas, Annotation arg1, Annotation arg2) {
    Feature arg1Text = new Feature("arg1_text", arg1.getCoveredText());
    Feature arg2Text = new Feature("arg2_text", arg2.getCoveredText());
    return Arrays.asList(arg1Text, arg2Text);
  }
}