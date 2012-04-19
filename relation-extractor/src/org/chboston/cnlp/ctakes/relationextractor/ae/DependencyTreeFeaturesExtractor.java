package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class DependencyTreeFeaturesExtractor implements RelationFeaturesExtractor{

	@Override
	public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		
	    List<Feature> features = new ArrayList<Feature>();
	    features.addAll(extractForNode(jCas, arg1, "MENTION1"));
	    features.addAll(extractForNode(jCas, arg2, "MENTION2"));
	    return features;
	}
	
	public List<Feature> extractForNode(JCas jCas, IdentifiedAnnotation mention, String ftrPrefix) {
	    List<Feature> features = new ArrayList<Feature>();
	    ConllDependencyNode mentionHeadNode = DependencyParseUtils.findAnnotationHead(jCas, mention);
		
	    if (mentionHeadNode != null) {
	    	ConllDependencyNode dependsOn = mentionHeadNode.getHead();
	    	if (dependsOn != null) {
	    		features.add(new Feature(ftrPrefix + "_DEPENDS_ON_WORD", dependsOn.getCoveredText()));
	    		features.add(new Feature(ftrPrefix + "_DEPENDS_ON_POS", dependsOn.getPostag()));
	    		// Following features come from Zhou et al. 2005
	    		// ET1DW1: combination of the entity type and the dependent word for M1
	    		features.add(new Feature(ftrPrefix + "_TYPE-GOVERNING_WORD", String.format("%d-%s", mention.getTypeID(), dependsOn.getCoveredText())));
	    		// H1DW1: combination of the head word and the dependent word for M1 
	    		features.add(new Feature(ftrPrefix + "_HEAD_WORD-GOVERNING_WORD", String.format("%s-%s", mentionHeadNode.getCoveredText(), dependsOn.getCoveredText())));
	    		features.add(new Feature(ftrPrefix + "_TYPE-GOVERNING_POS", String.format("%d-%s", mention.getTypeID(), dependsOn.getPostag())));
	    		features.add(new Feature(ftrPrefix + "_HEAD_POS-GOVERNING_POS", String.format("%s-%s", mentionHeadNode.getPostag(), dependsOn.getPostag())));
	    	}
	    }
	    return features;
	}
	

}
