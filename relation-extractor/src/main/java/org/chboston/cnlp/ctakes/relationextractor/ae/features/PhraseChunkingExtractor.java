package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;
import edu.mayo.bmi.uima.core.type.syntax.TerminalTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class PhraseChunkingExtractor implements RelationFeaturesExtractor {
	  
	  List<TerminalTreebankNode> extractPhraseHeadByTreenode(JCas jCas, List<TreebankNode> treenodesList) throws AnalysisEngineProcessException {
		  List<TerminalTreebankNode> rTNodeList = new ArrayList<TerminalTreebankNode>();
		  // get head index from phrase
		  for(TreebankNode tb : treenodesList) {
			  if(tb.getHeadIndex() > 0 && tb.getNodeType().endsWith("P") && !tb.getNodeType().equals("NNP")) {
				  	int headIndex = tb.getHeadIndex();
				  	for(TerminalTreebankNode ttb : JCasUtil.selectCovered(jCas, TerminalTreebankNode.class, tb)) {
				  		if(ttb.getIndex() == headIndex) {
				  			this.addPhraseHead(rTNodeList, ttb);
				  			break;
				  		}
				  	}		  
			  }
		  }
		  return rTNodeList;
	  }
	  
		private void addPhraseHead(List<TerminalTreebankNode> headList,
				TerminalTreebankNode newHead) {
			
			int insertPos = 0;
			for(int i=headList.size();i>0;i--) {
				if(newHead.getEnd() > headList.get(i-1).getEnd())
				{
					insertPos = i;
					break;
				}
				else if(newHead.getEnd() == headList.get(i-1).getEnd())
				{
					insertPos = -1;
					break;
				}
			}

			if(insertPos >=0 )
				headList.add(insertPos, newHead);
		}
	  
	@Override
	public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {


		List<Feature> features = new ArrayList<Feature>();
				
		// Extract features between 
		List<TerminalTreebankNode> headList = this.extractPhraseHeadByTreenode(jCas, JCasUtil.selectCovered(jCas, TreebankNode.class, arg1.getEnd(), arg2.getBegin()));

		if(headList.size() > 0) {
			features.add(new Feature("PhraseChunk_Between_FirstHead", headList.get(0).getNodeValue()));
			features.add(new Feature("PhraseChunk_Between_LastHead", headList.get(headList.size()-1).getNodeValue()));
			
			if(headList.size() >= 2) {
				String inBetweenValue = "";
				for(int i=1;i<headList.size()-1;i++) {
					if(i>1)
						inBetweenValue += "_";
					inBetweenValue += headList.get(i).getNodeValue();
				}
				features.add(new Feature("PhraseChunk_Between_BetweenHeads", inBetweenValue));
			}
		}
		
		// Extract feature before M1
		headList = this.extractPhraseHeadByTreenode(jCas, JCasUtil.selectPreceding(jCas, TreebankNode.class, arg1, 20));

		boolean isFirst = false;
		for(int i=headList.size()-1;i>=0;i--) {
			if(headList.get(i).getEnd() < arg1.getBegin()) {
				if(!isFirst) { 
				features.add(new Feature("PhraseChunk_Before_FirstHead", headList.get(i).getNodeValue()));
				isFirst = true;
				}
				else {
					features.add(new Feature("PhraseChunk_Before_SecondHead", headList.get(i).getNodeValue()));
					break;
				}
			}
		}
		
		// Extract feature after M2
		headList = this.extractPhraseHeadByTreenode(jCas, JCasUtil.selectFollowing(jCas, TreebankNode.class, arg2, 20));
		
		
		isFirst = false;
		for(int i=0;i<headList.size();i++) {
			if(headList.get(i).getBegin() > arg2.getEnd() ) {
				if(!isFirst) {
					features.add(new Feature("PhraseChunk_After_FirstHead", headList.get(i).getNodeValue()));
					isFirst = true;
				}
				else {
					features.add(new Feature("PhraseChunk_After_SecondHead", headList.get(i).getNodeValue()));
					break;
				}
			}
		}
		
		
		return features;
	}
}
