package org.chboston.cnlp.ctakes.parser.util;

import java.util.Iterator;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.syntax.TerminalTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TopTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;


public class AnnotationTreeUtils {
	
	public static TreebankNode annotationNode(JCas jcas, Annotation annot){
		return annotationNode(jcas, annot.getBegin(), annot.getEnd());
	}
	
	public static TreebankNode annotationNode(JCas jcas, int a, int b){
		TreebankNode lowestDom = null;
		int overage = Integer.MAX_VALUE;
		FSIterator<Annotation> iter = jcas.getJFSIndexRepository().getAnnotationIndex(TreebankNode.type).iterator();
		while(iter.hasNext()){
			TreebankNode node = (TreebankNode) iter.next();
			if(node.getBegin() == a && node.getEnd() == b){
				// this code will drill down -- actually want to go other way
//				while(node.getChildren() != null && node.getChildren().size() == 1){
//					node = node.getChildren(0);
//				}
				
				// this code will head up as long as parent has the same span
				try{
					while(node.getParent() != null && node.getParent().getChildren().size() == 1 && !node.getParent().getNodeType().equals("TOP")){
						node = node.getParent();
					}
				}catch(NullPointerException e){
					System.err.println("Null pointer exception in AttributeCalculator::markableNode()");
				}
				return node;
			}else if(node.getBegin() <= a && node.getEnd() >= b){
				int tempOver = (a-node.getBegin()) + (node.getEnd()-b);
				if(tempOver < overage){
					lowestDom = node;
					overage = tempOver;
				}
			}
		}
		// There are lots of reasons to get this far -- error in the parse, personal pronoun in an NP (not annotated so not a markable),
		// unrecognized NML structure, etc.
		// Some other work will add any such nodes to the parse tree as in haghighi klein 09 (simple synt sem ...)
		// In contrast, we don't add node to the parse tree, just find the lowest node dominating the markable range
		// TODO test this
		return lowestDom;
	}
	
	public static TerminalTreebankNode getHead(TreebankNode n){
		TerminalTreebankNode ttn = null;
		int headIndex = n.getHeadIndex();
		ttn = getRoot(n).getTerminals(headIndex);
		return ttn;
	}
	
	public static TopTreebankNode getRoot(TreebankNode n){
		TopTreebankNode ret = null;
		while(!(n instanceof TopTreebankNode)){
			n = n.getParent();
		}
		ret = (TopTreebankNode) n;
		return ret;
	}

}
