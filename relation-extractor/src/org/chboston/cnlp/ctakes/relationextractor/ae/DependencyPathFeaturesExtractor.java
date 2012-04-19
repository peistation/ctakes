package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class DependencyPathFeaturesExtractor implements RelationFeaturesExtractor {
	
	
	@Override
	public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		
		List<Feature> features = new ArrayList<Feature>();
		
	    ConllDependencyNode node1 = DependencyParseUtils.findAnnotationHead(jCas, arg1);
	    ConllDependencyNode node2 = DependencyParseUtils.findAnnotationHead(jCas, arg2);
	    if (node1 == null || node2 == null) { return features; }
	    
	    List<LinkedList<ConllDependencyNode>> paths = DependencyParseUtils.getPathsToCommonAncestor(node1, node2);
	    LinkedList<ConllDependencyNode> path1 = paths.get(0);
	    LinkedList<ConllDependencyNode> path2 = paths.get(1);
	    
	    features.add(new Feature("DEPENDENCY_PATH_MEAN_DISTANCE_TO_COMMON_ANCESTOR", (path1.size() + path2.size()) / 2.0));
	    features.add(new Feature("DEPENDENCY_PATH_MAX_DISTANCE_TO_COMMON_ANCESTOR", Math.max(path1.size(), path2.size())));
	    features.add(new Feature("DEPENDENCY_PATH_MIN_DISTANCE_TO_COMMON_ANCESTOR", Math.min(path1.size(), path2.size())));
	    
	    LinkedList<ConllDependencyNode> node1ToNode2Path = DependencyParseUtils.getPathBetweenNodes(node1, node2);
	    features.add(new Feature("DEPENDENCY_PATH", DependencyParseUtils.pathToString(node1ToNode2Path)));
	    
	    return features;
	}

}
