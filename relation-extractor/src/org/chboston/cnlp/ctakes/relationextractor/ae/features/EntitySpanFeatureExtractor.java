package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import edu.mayo.bmi.uima.core.type.syntax.NP;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

/**
 * Features that indicate whether both arguments are contained within an NP. 
 * 
 * E.g.: this extractor should find things like:
 * 
 * NP: "Degenerative knee with valgus deformity"
 * arg1: "Degenerative knee"
 * arg2: "valgus deformity"
 *
 */
public class EntitySpanFeatureExtractor implements RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, EntityMention arg1, EntityMention arg2) throws AnalysisEngineProcessException {

  	List<Feature> features = new ArrayList<Feature>();
 
  	// entity1 ... entity2 scenario
  	if(arg1.getEnd() < arg2.getBegin()) {
  		for(NP np : JCasUtil.selectCovering(jCas, NP.class, arg1.getBegin(), arg2.getEnd())) {
  			if(arg1.getBegin() == np.getBegin() && arg2.getEnd() == np.getEnd()) {
  				features.add(new Feature("arg1arg2insideNP", true));
  			}
  		}
  	}

 // entity2 ... entity1 scenario
  	if(arg2.getEnd() < arg1.getBegin()) {
  		for(NP np : JCasUtil.selectCovering(jCas, NP.class, arg2.getBegin(), arg1.getEnd())) {
  			if(arg2.getBegin() == np.getBegin() && arg1.getEnd() == np.getEnd()) {
  				features.add(new Feature("arg2arg1insideNP", true));
  			}
  		}
  	}
  	
    return features;
  }
}
