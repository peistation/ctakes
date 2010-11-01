package edu.mayo.bmi.nlp.parser.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.UimaDepNode;
import clear.dep.UimaDepTree;


import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.Sentence;

/**
 * @author m081914
 *
 */
/**
 * @author m081914
 *
 */
/**
 * @author m081914
 *
 */
/**
 * @author m081914
 *
 */
/**
 * @author m081914
 *
 */
/**
 * @author m081914
 *
 */
public class ClearDependencyUtility extends DependencyUtility {

//	public static UimaDepNode DUMMY_PARENT = new UimaDepNode();
	
	// LOG4J logger based on class name
	public Logger logger = Logger.getLogger(getClass().getName());
	
	public static UimaDepTree getDepTreeFromArray(ArrayList<ConllDependencyNode> nodes) {
	    UimaDepTree tree = new UimaDepTree();
	    
	    for (ConllDependencyNode cNode : nodes)
	    {
	        UimaDepNode node = new UimaDepNode();
	        node.id     = cNode.getID();
	        node.form   = cNode.getFORM();
	        node.lemma  = cNode.getLEMMA();
	        node.pos    = cNode.getPOSTAG();
	        node.headId = cNode.getHEAD().getID();
	        node.deprel = cNode.getDEPREL();
	        node.begin  = cNode.getBegin();
	        node.end    = cNode.getEnd();
	        node.hasHead= (cNode.getHEAD()!=null);
	        
	        tree.add(node);
	    }
	    
	    return tree;
	}


	public static UimaDepTree getDepTreeFromCas(JCas jCas, Sentence sentence) {    
		return getDepTreeFromArray( getConllNodesFromCas(jCas,sentence) );
	}


	public static ArrayList<ConllDependencyNode> convert(JCas jcas, DepTree clearTree, Sentence sentence, List<BaseToken> tokens)  {
        
        ArrayList<ConllDependencyNode> uimaNodes = new ArrayList<ConllDependencyNode>(tokens.size()+1);
        
        // Create the root node first
        int rootBegin = tokens.get(0).getBegin();
        int rootEnd = tokens.get(tokens.size()-1).getEnd();
        uimaNodes.add( 0, new ConllDependencyNode(jcas, rootBegin, rootEnd));

        // Create all the other nodes
        for (int i=1; i<clearTree.size(); i++) {
            int nodeBegin = tokens.get(i-1).getBegin(); // assumes that tokens are off 1 from clearTree
            int nodeEnd = tokens.get(i-1).getEnd();
            ConllDependencyNode uimaNode = new ConllDependencyNode(jcas, nodeBegin, nodeEnd);
            uimaNodes.add(i,uimaNode);
        }
        
        // Set values in all the other nodes
        for (int i=1; i<clearTree.size(); i++) {
            

            DepNode clearNode = clearTree.get(i);
            ConllDependencyNode uimaNode = uimaNodes.get(i);

            uimaNode.setID(clearNode.id);
            uimaNode.setFORM(clearNode.form);
            uimaNode.setLEMMA(clearNode.lemma);
            uimaNode.setCPOSTAG(clearNode.pos);
            uimaNode.setPOSTAG(clearNode.pos);
            uimaNode.setFEATS("_");
            uimaNode.setHEAD(uimaNodes.get(clearNode.headId));
            uimaNode.setDEPREL(clearNode.deprel);
            uimaNode.setPHEAD(null);
            uimaNode.setPDEPREL("_");

             
       }
        
        return uimaNodes;//uimaNodes.get(0); //return the root node
    }
    
//	/** Conversion to a ConllDependencyNode.  This is a shallow
//	 * conversion, in that the getHEAD() method will not return another
//	 * fully-functional node -- it will only have the head node's index.
//	 * @param clearNode
//	 * @return
//	 */
//    public ConllDependencyNode convert( UimaDepNode clearNode ) {
//    	ConllDependencyNode uimaNode;
//        ConllDependencyNode uimaHead;
//        uimaHead.set
//        uimaNode.setID(clearNode.id);
//        uimaNode.setFORM(clearNode.form);
//        uimaNode.setLEMMA(clearNode.lemma);
//        uimaNode.setCPOSTAG(clearNode.pos);
//        uimaNode.setPOSTAG(clearNode.pos);
//        uimaNode.setFEATS("_");
//        uimaNode.setHEAD(uimaNodes.get(clearNode.headId));
//        uimaNode.setDEPREL(clearNode.deprel);
//        uimaNode.setPHEAD(null);
//        uimaNode.setPDEPREL("_");
//    }
	
	
	/** Equality expressions to aid in converting between DepNodes and CAS objects */
	public static boolean equalCoverage(Annotation annot1,Annotation annot2) {
		return annot1.getBegin()==annot2.getBegin() && 
			annot1.getEnd()==annot2.getEnd() && 
			annot1.getCoveredText()==annot2.getCoveredText();
	}
	public static boolean equalCoverage(Annotation annot,UimaDepNode udNode) {
		return annot.getBegin()==udNode.begin && 
			annot.getEnd()==udNode.end && 
			annot.getCoveredText()==udNode.form;
	}
	public static boolean equalCoverage(UimaDepNode udNode, Annotation annot) {
		return annot.getBegin()==udNode.begin && 
			annot.getEnd()==udNode.end && 
			annot.getCoveredText()==udNode.form;
	}
	public static boolean equalCoverage(UimaDepNode udNode, UimaDepNode udNode2) {
		return udNode2.begin==udNode.begin && 
			udNode2.end==udNode.end && 
			udNode2.form==udNode.form;
	}

//	/** Find path between two nodes */
//	public static ClearDependencyPath getPath(UimaDepNode node1, UimaDepNode node2, UimaDepTree tree) {
//		ClearDependencyPath pathUp1 = new ClearDependencyPath();
//		ClearDependencyPath pathUp2 = new ClearDependencyPath();
//		ClearDependencyPath pathLtoR = new ClearDependencyPath();
//		ClearDependencyPath pathRtoL = new ClearDependencyPath();
//		
//		if (node1==null || node2==null) {
//			System.err.println(" WARNING: looking for path between null nodes.");
//			return null;
//		}
//		
//		pathUp1.add(node1);
//		while (node1.hasHead) {
//			node1 = tree.get(node1.headId);
//			pathUp1.add(node1);
//		}
//		pathUp2.add(node2);
//		while (node2.hasHead) {
//			node2 = tree.get(node2.headId);
//			pathUp2.add(node2);
//		}
//		
//		pathLtoR.clear();
//		boolean foundMatch = false;
//		for (int i=0; i<pathUp1.size(); i++ ) {
//			UimaDepNode nodeUp1 = pathUp1.get(i);
//			pathLtoR.add(nodeUp1);
//			
//			pathRtoL.clear();
//			for (int j=0; j<pathUp2.size(); j++ ) {
//				UimaDepNode nodeUp2 = pathUp2.get(j);				
//				if (ClearDependencyUtility.equalCoverage(nodeUp1, nodeUp2)) {
//					// word-rel<word-rel<commonparent>rel-word>rel-word
//					pathLtoR.setCommonNode(nodeUp1);
//					pathLtoR.addAll(pathRtoL);
//					foundMatch = true;
//					break;
//				} else {
//					pathRtoL.push(nodeUp2);
//				}
//			}
//
//			if (foundMatch) break;
//		}
//		
//		return pathLtoR;
//	}
//
//	public static DependencyPath getPath(ConllDependencyNode node1, ConllDependencyNode node2, List<ConllDependencyNode> nodes) {
//		
//		return DependencyUtility.getPath(node1,node2,nodes);
//	}
	
}
