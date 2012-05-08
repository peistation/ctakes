package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Define an interface for people to implement feature extractors.
 */
public interface RelationFeaturesExtractor {

  /**
   * Extract features for the pair of named entity mentions.
   * 
   * @param jCas
   *          The JCas containing the two named entity mentions.
   * @param arg1
   *          The first identified annotation in the text.
   * @param arg2
   *          The second identified annotation in the text.
   * @return A list of features indicative of the relation between the named entities
   */
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException;
}