package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.syntax.PunctuationToken;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Features that capture punctuation marks between the two arguments.
 */
public class PunctuationFeatureExtractor implements RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {

  	List<Feature> features = new ArrayList<Feature>();
  	
  	// entity1 ... entity2 scenario
  	if(arg1.getEnd() < arg2.getBegin()) {
  		for(PunctuationToken token : JCasUtil.selectCovered(jCas, PunctuationToken.class, arg1.getEnd(), arg2.getBegin())) {
  			features.add(new Feature("arg1_punctuation_arg2", token.getCoveredText()));
  			break;
  		}
  	}
  	
  	// entity2 ... entity1 scenario
  	if(arg2.getEnd() < arg1.getBegin()) {
  		for(PunctuationToken token : JCasUtil.selectCovered(jCas, PunctuationToken.class, arg2.getEnd(), arg1.getBegin())) {
  			features.add(new Feature("arg2_punctuation_arg1", token.getCoveredText()));
  			break;
  		}
  	}
  	
    return features;
  }

}
