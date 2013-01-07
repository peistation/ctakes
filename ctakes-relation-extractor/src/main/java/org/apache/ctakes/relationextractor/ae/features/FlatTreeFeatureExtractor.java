package org.apache.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

public class FlatTreeFeatureExtractor implements RelationFeaturesExtractor {

	// Returns flat POS-trees a la Hovy et al 2012 (EACL)
	@Override
	public List<Feature> extract(JCas jcas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		List<Feature> features = new ArrayList<Feature>();
		
		SimpleTree tree = null;
		tree = new SimpleTree("BOP");
		TreebankNode lca = AnnotationTreeUtils.getCommonAncestor(AnnotationTreeUtils.annotationNode(jcas, arg1),
																   AnnotationTreeUtils.annotationNode(jcas, arg2));
		SimpleTree arg1Tree = new SimpleTree("ARG1");
		SimpleTree arg2Tree = new SimpleTree("ARG2");
		
		tree.addChild(arg1Tree);
		List<BaseToken> coveredTokens = JCasUtil.selectCovered(jcas, BaseToken.class, lca);
		for(BaseToken token : coveredTokens){
			// create pre-terminal tree
			SimpleTree tokenTree = new SimpleTree("TOK");
//			tokenTree.addChild(new SimpleTree(token.getCoveredText()));
			tokenTree.addChild(new SimpleTree(token.getPartOfSpeech()));
			
			// do we add this to one of the arg trees or to the root?
			if(token.getEnd() <= arg1.getEnd()){
				arg1Tree.addChild(tokenTree);
			}else if(token.getBegin() >= arg2.getBegin()){
				arg2Tree.addChild(tokenTree);
			}else{
				SimpleTree termTree = new SimpleTree("TERM");
				termTree.addChild(tokenTree);
				tree.addChild(termTree);
			}
		}
		tree.addChild(arg2Tree);
		
		features.add(new Feature("TK_BOP", tree.toString()));
		return features;
	}

}
