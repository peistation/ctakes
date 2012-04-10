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
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class PrepositionFeatureExtractor implements RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, EntityMention arg1, EntityMention arg2) throws AnalysisEngineProcessException {

  	List<Feature> features = new ArrayList<Feature>();
  	
  	// entity1 ... entity2 scenario
  	if(arg1.getEnd() < arg2.getBegin()) {
  		for(BaseToken token : JCasUtil.selectCovered(jCas, BaseToken.class, arg1.getEnd(), arg2.getBegin())) {
  			if(token.getPartOfSpeech().equals("IN")) { // may need to hardcode list of prepositions
  				features.add(new Feature("preposition", token.getCoveredText()));
  			}
  		}
  	}
  	
  	// entity2 ... entity1 scenario
  	if(arg2.getEnd() < arg1.getBegin()) {
  		for(BaseToken token : JCasUtil.selectCovered(jCas, BaseToken.class, arg2.getEnd(), arg1.getBegin())) {
  			if(token.getPartOfSpeech().equals("IN")) {
  				features.add(new Feature("preposition-1", token.getCoveredText()));
  			}
  		}
  	}
  	
    return features;
  }

}
