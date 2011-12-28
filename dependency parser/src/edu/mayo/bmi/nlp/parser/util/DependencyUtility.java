/*
 * Copyright: (c) 2010   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
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
 */
package edu.mayo.bmi.nlp.parser.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.Sentence;

/**
 * @author m081914
 *
 */
public abstract class DependencyUtility {

	
	public static void addToIndexes( JCas jcas, ArrayList<ConllDependencyNode> nodes ) {
		for (int i=0; i<nodes.size(); i++) {
			// Enter UIMA nodes into index
			nodes.get(i).addToIndexes(jcas);
		}
	}

	
	/** Equality expressions to aid in converting between DepNodes and CAS objects */
	public static boolean equalCoverage(Annotation annot1,Annotation annot2) {
		return annot1.getBegin()==annot2.getBegin() && 
			annot1.getEnd()==annot2.getEnd() && 
			annot1.getCoveredText().equals(annot2.getCoveredText());
	}

	public static ArrayList<ConllDependencyNode> getConllNodesFromCas(JCas jCas, Sentence sentence) {
		ArrayList<ConllDependencyNode> nodes = new ArrayList<ConllDependencyNode>();
	
		AnnotationIndex nodeIndex = jCas.getAnnotationIndex(ConllDependencyNode.type);
	    FSIterator nodeIterator = nodeIndex.subiterator(sentence);
	    while (nodeIterator.hasNext()) {
	        ConllDependencyNode node = (ConllDependencyNode) nodeIterator.next();
	        if (node.getID()!=0) {
	            nodes.add(node);
	        }
	    }
	    return nodes;
	}

	public static DependencyPath getPath(ConllDependencyNode node1, ConllDependencyNode node2, List<ConllDependencyNode> nodes) {

		DependencyPath pathUp1 = new DependencyPath();
		DependencyPath pathUp2 = new DependencyPath();
		DependencyPath pathLtoR = new DependencyPath();
		DependencyPath pathRtoL = new DependencyPath();

		if (node1==null || node2==null) {
			System.err.println(" WARNING: looking for path between null nodes.");
			return null;
		}

		pathUp1.add(node1);
		while (node1.getHEAD()!=null) {
			node1 = node1.getHEAD();
			pathUp1.add(node1);
		}
		pathUp2.add(node2);
		while (node2.getHEAD()!=null) {
			node2 = node2.getHEAD();
			pathUp2.add(node2);
		}
//		System.out.println(" Path up1:"+pathUp1.toString());
//		System.out.println(" Path up2:"+pathUp2.toString());
		
		pathLtoR.clear();
		boolean foundMatch = false;
		for (int i=0; i<pathUp1.size(); i++ ) {
			ConllDependencyNode nodeUp1 = pathUp1.get(i);
			pathLtoR.add(nodeUp1);

			pathRtoL.clear();
			for (int j=0; j<pathUp2.size(); j++ ) {
				ConllDependencyNode nodeUp2 = pathUp2.get(j);				
				if (DependencyUtility.equalCoverage(nodeUp1, nodeUp2)) {
					// word-rel<word-rel<commonparent>rel-word>rel-word
					pathLtoR.setCommonNode(nodeUp1);
					pathLtoR.addAll(pathRtoL);
					foundMatch = true;
					break;
				} else {
					pathRtoL.push(nodeUp2);
				}
			}

			if (foundMatch) break;
		}

		return pathLtoR;

	}

	public static List<ConllDependencyNode> getRightSibs( ConllDependencyNode refNode, List<ConllDependencyNode> tree ) {
		
		ConllDependencyNode parent = refNode.getHEAD();
		List<ConllDependencyNode> out = new ArrayList<ConllDependencyNode>();
//		System.out.println("right side is: "+tree.subList( tree.indexOf(refNode)+1, tree.size() ));
		
		for ( ConllDependencyNode node : tree.subList( tree.indexOf(refNode)+1, tree.size() ) ) {
			if ( node.getHEAD().equals(parent) ) {
				out.add(node);
			}
		}
		return out;
	}
	
	public static List<ConllDependencyNode> getLeftSibs( ConllDependencyNode refNode, List<ConllDependencyNode> tree ) {
		
		ConllDependencyNode parent = refNode.getHEAD();
		List<ConllDependencyNode> out = new ArrayList<ConllDependencyNode>();
//		System.out.println("right side is: "+tree.subList( tree.indexOf(refNode)+1, tree.size() ));
		
		List<ConllDependencyNode> lSide = tree.subList(0,tree.indexOf(refNode));
		for ( int i=tree.indexOf(refNode)-1; i>=0; i-- ) {
			ConllDependencyNode node = lSide.get(i);
			if ( node.getHEAD().equals(parent) ) {
				out.add(node);
			}
		}
		return out;
	}
	
	public static List<ConllDependencyNode> getProgeny( ConllDependencyNode refNode, List<ConllDependencyNode> tree) {

		List<ConllDependencyNode> out = new ArrayList<ConllDependencyNode>();
		
		// Find the path to root for every node
		for ( ConllDependencyNode node : tree ) {
			
			// Progeny includes the reference node itself
			if ( node.equals(refNode) ) {
				out.add(node);
			} else {
				
				// Anything with refNode on its path to root is progeny.  Requires acyclicity
				ConllDependencyNode upNode = node;
				while (upNode.getHEAD()!=null) {
					upNode = upNode.getHEAD();
					if (upNode.equals(refNode)) {
						out.add(node);
						break;
					}
				}
				
			}
			
		}
		
		return out;
	}
	
	public static List<ConllDependencyNode> getProgeny( List<ConllDependencyNode> refNodes, List<ConllDependencyNode> tree) {

		List<ConllDependencyNode> out = new ArrayList<ConllDependencyNode>();
		
		// Find the path to root for every node
		for ( ConllDependencyNode node : tree ) {
			
			// Progeny includes the reference nodes themselves
			if ( refNodes.contains(node) ) {
				out.add(node);
			} else {
				
				// Anything with refNode on its path to root is progeny.  Requires acyclicity
				ConllDependencyNode upNode = node;
				while (upNode.getHEAD()!=null) {
					upNode = upNode.getHEAD();
					
					if (refNodes.contains(upNode)) {
						out.add(node);
						break;
					}
				}
				
			}
			
		}
		
		return out;
	}

	public static List<ConllDependencyNode> getRightSibProgeny( ConllDependencyNode refNode, List<ConllDependencyNode> tree) {
		return getProgeny( getRightSibs(refNode,tree), tree );
	}
	
	public static List<ConllDependencyNode> getLeftSibProgeny( ConllDependencyNode refNode, List<ConllDependencyNode> tree) {
		return getProgeny( getLeftSibs(refNode,tree), tree );
	}
}
