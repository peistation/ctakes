package edu.mayo.bmi.uima.relationextract;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;

/**
 * Define an interface for people to implement feature extractors.
 */
public interface FeatureExtractor {
  public List<Feature> extract(JCas jCas, Annotation arg1, Annotation arg2)
      throws AnalysisEngineProcessException;
}