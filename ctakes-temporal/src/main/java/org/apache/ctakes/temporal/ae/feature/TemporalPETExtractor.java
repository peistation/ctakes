package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.treekernel.TreeExtractor;
import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

public class TemporalPETExtractor implements RelationFeaturesExtractor {

	@Override
	public List<Feature> extract(JCas jcas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		List<Feature> features = new ArrayList<Feature>();
		// first get the root and print it out...
		TopTreebankNode root = AnnotationTreeUtils.getTreeCopy(jcas, AnnotationTreeUtils.getAnnotationTree(jcas, arg1));
		
		if(root == null){
			SimpleTree fakeTree = new SimpleTree("(S (NN null))");
			features.add(new Feature("TK_PET", fakeTree.toString()));
			return features;
		}
		
		// swap the order if necessary:
		if(arg2.getBegin() <= arg1.getBegin() && arg2.getEnd() <= arg1.getEnd()){
			IdentifiedAnnotation temp = arg1;
			arg1 = arg2;
			arg2 = temp;
		}
		
		String a1type, a2type;
		String eventModality="";
		String timeClass;
		
		if(arg1 instanceof EventMention){
			eventModality = ((EventMention)arg1).getEvent().getProperties().getContextualModality();
			timeClass = ((TimeMention)arg2).getTimeClass();
			
			a1type = "EVENT-"+eventModality;
			a2type = "TIMEX-"+timeClass;
		}else{
			eventModality = ((EventMention)arg2).getEvent().getProperties().getContextualModality();
			timeClass = ((TimeMention)arg1).getTimeClass();
			
			a1type = "TIMEX-"+timeClass;
			a2type = "EVENT-"+eventModality;
		}
		
		TreebankNode t1 = AnnotationTreeUtils.insertAnnotationNode(jcas, root, arg1, "ARG1-"+a1type);
		TreebankNode t2 = AnnotationTreeUtils.insertAnnotationNode(jcas, root, arg2, "ARG2-"+a2type);

//		addOtherTimes(jcas,root, arg1, arg2);
				
		SimpleTree tree = null;
		if(t1.getBegin() <= t2.getBegin() && t1.getEnd() >= t2.getEnd()){
			// t1 encloses t2
			tree = TreeExtractor.getSimpleClone(t1);
		}else if(t2.getBegin() <= t1.getBegin() && t2.getEnd() >= t1.getEnd()){
			// t2 encloses t1
			tree = TreeExtractor.getSimpleClone(t2);
		}else{
			tree = TreeExtractor.extractPathEnclosedTree(t1, t2, jcas);
		}

		moveTimexDownToNP(tree);
		
		features.add(new Feature("TK_PET", tree.toString()));
		return features;
	}

	public static void addOtherTimes(JCas jcas, TopTreebankNode root, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {
		List<TimeMention> timexes = JCasUtil.selectCovered(TimeMention.class, root);
		
		for(TimeMention timex : timexes){
			// don't want the same timex that we are looking at the argument for...
			if(timex.getBegin() == arg1.getBegin() && timex.getEnd() == arg1.getEnd() ||
			   timex.getBegin() == arg2.getBegin() && timex.getEnd() == arg2.getEnd()) continue;
			// but make sure it is in the correct span...
			if(timex.getBegin() > arg1.getBegin() && timex.getEnd() <= arg2.getBegin()){
				AnnotationTreeUtils.insertAnnotationNode(jcas, root, timex, "TIMEX");
			}
		}
	}

	public static void moveTimexDownToNP(SimpleTree tree) {
		if(tree.cat.contains("-TIMEX-")){
			SimpleTree child = tree.children.get(0);
			// we've found the correct node: only correct clear violations: PP -> IN NP
			if(child.cat.contains("PP") && child.children.size() == 2 && child.children.get(0).cat.equals("IN") && child.children.get(1).cat.startsWith("NP")){
				// swap labels
				String fullCat = tree.cat;
				tree.cat = "PP";
				child.cat = fullCat;
				
				// now point the new PP at the preposition and stop the new TIMEX from pointing at it:
				tree.children.add(0, child.children.get(0));
				child.children.remove(0);
			}
			return;
		}else if(tree.children == null || tree.children.size() == 0){
			return;
		}
		// if we are not there we have to return
		for(SimpleTree child : tree.children){
			moveTimexDownToNP(child);
		}
	}

}
