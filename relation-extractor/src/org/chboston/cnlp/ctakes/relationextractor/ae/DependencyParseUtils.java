package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;

public class DependencyParseUtils {

	/**
	 * Returns the paths from each node to the common ancestor between them
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static List<LinkedList<ConllDependencyNode>> getPathsToCommonAncestor(ConllDependencyNode node1, ConllDependencyNode node2) {
		List<LinkedList<ConllDependencyNode>> paths = new ArrayList<LinkedList<ConllDependencyNode>>(2);
		LinkedList<ConllDependencyNode> node1ToHeadPath = DependencyParseUtils.getPathToSentenceHead(node1);
		LinkedList<ConllDependencyNode> node2ToHeadPath = DependencyParseUtils.getPathToSentenceHead(node2);
	     
	     while (!node1ToHeadPath.isEmpty() && !node2ToHeadPath.isEmpty()) {
	    	 if (node1ToHeadPath.getLast() == node2ToHeadPath.getLast()) {
	    		 break;
	    	 }
	    	 node1ToHeadPath.pop();
	    	 node2ToHeadPath.pop();
	     }
	
	     paths.add(node1ToHeadPath);
	     paths.add(node2ToHeadPath);
	     return paths;
	}

	/**
	 * Finds the head word within a given annotation span
	 * @param jcas
	 * @param annotation
	 * @return
	 */
	public static ConllDependencyNode findAnnotationHead(JCas jcas, Annotation annotation) {
	
	    for (ConllDependencyNode depNode : JCasUtil.selectCovered(jcas, ConllDependencyNode.class, annotation)) {
	    	
	    	System.out.println(depNode);
	    	System.out.printf("DepNode %d %d\n", depNode.getBegin(), depNode.getEnd());
	    	ConllDependencyNode head = depNode.getHead();
	    	System.out.println(head);
	    	System.out.printf("Head %d %d\n", head.getBegin(), head.getEnd());
	
	    	if (head.getEnd() <= annotation.getBegin() || head.getBegin() > annotation.getEnd()) {
	    		return head;
	    	}
	    }
	    return null;
	}

	public static LinkedList<ConllDependencyNode> getPathToSentenceHead(ConllDependencyNode node) {
	     LinkedList<ConllDependencyNode> path = new LinkedList<ConllDependencyNode>();
	     ConllDependencyNode currNode = node;
	     while (currNode != null) {
	         path.add(currNode);
	         currNode = currNode.getHead();
	     }
	     return path;
	}

	/*
	 * 
	 */
	public static LinkedList<ConllDependencyNode> getPathBetweenNodes(ConllDependencyNode srcNode, ConllDependencyNode tgtNode) {
		LinkedList<ConllDependencyNode> path = new LinkedList<ConllDependencyNode>();
		List<LinkedList<ConllDependencyNode>> paths = getPathsToCommonAncestor(srcNode, tgtNode);
		LinkedList<ConllDependencyNode> srcToAncestorPath = paths.get(0);
		LinkedList<ConllDependencyNode> tgtToAncestorPath = paths.get(1);
		
		// Join the two paths
		srcToAncestorPath.pop();
		path = srcToAncestorPath;
		while (!tgtToAncestorPath.isEmpty()) {
			path.add(path.removeFirst());
		}
		return path;
	}

	/**
	 * This will convert a path into a string lexicalized at the end points with arc labels and POS tags in between
	 * @param path
	 * @return
	 */
	public static String pathToString(LinkedList<ConllDependencyNode> path) {
		
		StringBuilder builder = new StringBuilder();
		for (ConllDependencyNode node : path) {
			if (node == path.getFirst() || node == path.getLast()) {
				builder.append(node.getCoveredText());
			} else {
				builder.append(node.getPostag());
			}
			
			builder.append("-");
			builder.append(node.getDeprel());
			builder.append(">");
		}
		return builder.toString();
	}

}
