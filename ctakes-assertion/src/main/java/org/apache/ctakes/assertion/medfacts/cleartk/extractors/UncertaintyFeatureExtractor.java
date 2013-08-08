package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

public class UncertaintyFeatureExtractor implements SimpleFeatureExtractor {

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    List<Feature> featList = new ArrayList<Feature>();
    
    IdentifiedAnnotation mention = (IdentifiedAnnotation) focusAnnotation;
    featList.add(new Feature("IsNegated", mention.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT));
    return featList;
  }

}
