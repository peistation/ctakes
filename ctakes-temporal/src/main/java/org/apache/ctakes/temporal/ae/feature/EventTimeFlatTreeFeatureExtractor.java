package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;


public class EventTimeFlatTreeFeatureExtractor implements RelationFeaturesExtractor{

	@Override
	public List<Feature> extract(JCas jcas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		List<Feature> feats = new ArrayList<Feature>();
		
		// make sure they're in the correct order
		if(arg1.getBegin() > arg2.getBegin()){
			IdentifiedAnnotation temp = arg1;
			arg1 = arg2;
			arg2 = temp;
		}
		
		SimpleTree tree = new SimpleTree("BOP");
		
		SimpleTree arg1Tree = null;
		SimpleTree arg2Tree = null;
		String eventModality="";
		String timeClass;
	
		// make sure we have the right classes
		if(arg1 instanceof EventMention){
			eventModality = ((EventMention)arg1).getEvent().getProperties().getContextualModality();
			timeClass = ((TimeMention)arg2).getTimeClass();

			arg1Tree = new SimpleTree("EVENT-"+eventModality);
			arg2Tree = new SimpleTree("TIMEX-"+timeClass);
		}else{
			eventModality = ((EventMention)arg2).getEvent().getProperties().getContextualModality();
			timeClass = ((TimeMention)arg1).getTimeClass();

			arg1Tree = new SimpleTree("TIMEX-"+timeClass);
			arg2Tree = new SimpleTree("EVENT-"+eventModality);
		}
		
		List<BaseToken> tokens = JCasUtil.selectCovered(jcas, BaseToken.class, arg1.getBegin(), arg2.getEnd());
		tree.addChild(arg1Tree);
		for(BaseToken token : tokens){
			SimpleTree tokenTree = new SimpleTree("TOK");
			tokenTree.addChild(new SimpleTree(token.getPartOfSpeech()));
			
			if(token.getEnd() <= arg1.getEnd()){
				arg1Tree.addChild(tokenTree);
			}else if(token.getBegin() >= arg2.getBegin()){
				arg2Tree.addChild(tokenTree);
			}else{
				tree.addChild(tokenTree);
			}
		}
		tree.addChild(arg2Tree);
		
		feats.add(new Feature("TK_BOP", tree.toString()));
		return feats;
	}


}
