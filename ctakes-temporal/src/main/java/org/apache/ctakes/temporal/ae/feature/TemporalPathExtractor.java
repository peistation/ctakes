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

public class TemporalPathExtractor implements RelationFeaturesExtractor {

	@Override
	public List<Feature> extract(JCas jcas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		List<Feature> features = new ArrayList<Feature>();
		// first get the root and print it out...
		TopTreebankNode root = AnnotationTreeUtils.getTreeCopy(jcas, AnnotationTreeUtils.getAnnotationTree(jcas, arg1));
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
			a2type = "EVENT"+eventModality;
		}
		
		TreebankNode t1 = AnnotationTreeUtils.insertAnnotationNode(jcas, root, arg1, "ARG1-"+a1type);
		TreebankNode t2 = AnnotationTreeUtils.insertAnnotationNode(jcas, root, arg2, "ARG2-"+a2type);

		SimpleTree tree = null;
//		if(t1.getBegin() <= t2.getBegin() && t1.getEnd() >= t2.getEnd()){
//			// t1 encloses t2
//			tree = TreeExtractor.getSimpleClone(t1);
//		}else if(t2.getBegin() <= t1.getBegin() && t2.getEnd() >= t1.getEnd()){
//			// t2 encloses t1
//			tree = TreeExtractor.getSimpleClone(t2);
//		}else{
		tree = TreeExtractor.extractPathTree(t1, t2);
//		}

		features.add(new Feature("TK_PATH", tree.toString()));
		return features;
	}

}
