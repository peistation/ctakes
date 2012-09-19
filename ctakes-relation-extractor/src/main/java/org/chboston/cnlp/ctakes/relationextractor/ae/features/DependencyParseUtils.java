/*
 * Copyright: (c) 2012  Children's Hospital Boston, Regents of the University of Colorado 
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Lee Becker
 */

package org.chboston.cnlp.ctakes.relationextractor.ae.features;

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
		
		// We will remove the last item in each path until they diverge
		ConllDependencyNode ancestor = null;
		while (!node1ToHeadPath.isEmpty() && !node2ToHeadPath.isEmpty()) {
			if (node1ToHeadPath.getLast() == node2ToHeadPath.getLast()) {
				node1ToHeadPath.removeLast();
				ancestor = node2ToHeadPath.removeLast();
			} else {
				break;
			}
		}
		
		// Put the common ancestor back on both paths
		if (ancestor != null) {
	    	 node1ToHeadPath.add(ancestor);
	    	 node2ToHeadPath.add(ancestor);
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
	    	
	    	ConllDependencyNode head = depNode.getHead();
	    	if (head == null || head.getEnd() <= annotation.getBegin() || head.getBegin() > annotation.getEnd()) {
	    		// The head is outside the bounds of the annotation, so this node must be the annotation's head
	    		return depNode;
	    	}
	    }
	    // Can this happen?
	    return null;
	}

	public static LinkedList<ConllDependencyNode> getPathToSentenceHead(ConllDependencyNode node) {
	     LinkedList<ConllDependencyNode> path = new LinkedList<ConllDependencyNode>();
	     ConllDependencyNode currNode = node;
	     while (currNode.getHead() != null) { 
	         path.add(currNode);
	         currNode = currNode.getHead();
	     }
	     return path;
	}

	/**
	 * Finds the path between two dependency nodes
	 * @param srcNode
	 * @param tgtNode
	 * @return
	 */
	public static LinkedList<ConllDependencyNode> getPathBetweenNodes(ConllDependencyNode srcNode, ConllDependencyNode tgtNode) {
		LinkedList<ConllDependencyNode> path = new LinkedList<ConllDependencyNode>();
		List<LinkedList<ConllDependencyNode>> paths = getPathsToCommonAncestor(srcNode, tgtNode);
		LinkedList<ConllDependencyNode> srcToAncestorPath = paths.get(0);
		LinkedList<ConllDependencyNode> tgtToAncestorPath = paths.get(1);
		
		if (srcNode == tgtNode) {
			return path;
		}
		
		// Join the two paths
		if (!srcToAncestorPath.isEmpty()) {
			srcToAncestorPath.removeLast();
		}
		path = srcToAncestorPath;
		while (!tgtToAncestorPath.isEmpty()) {
			path.add(tgtToAncestorPath.removeLast());
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
			if (node != path.getLast()) {
				builder.append("/");
			}
		}
		return builder.toString();
	}
	

	

	public static String dumpDependencyRelations(JCas jcas, Annotation annotation) {
		StringBuilder builder = new StringBuilder();
	    for (ConllDependencyNode depNode : JCasUtil.selectCovered(jcas, ConllDependencyNode.class, annotation)) {
	    	if (depNode.getHead() != null) {
	    		builder.append(String.format("%s(%s,%s)\n", depNode.getDeprel(), depNode.getCoveredText(), depNode.getHead().getCoveredText()));
	    	}
	    }
	    return builder.toString();
		
	}

}
