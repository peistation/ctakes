package org.apache.ctakes.assertion.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.ctakes.constituency.parser.treekernel.TreeExtractor;
import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public class AssertionTreeUtils {

	public static SimpleTree extractAboveLeftConceptTree(JCas jcas, Annotation mention, SemanticClasses sems){
		SimpleTree tree = null;
		TopTreebankNode annotationTree = AnnotationTreeUtils.getAnnotationTree(jcas, mention);
		if(annotationTree != null){
			TopTreebankNode root = AnnotationTreeUtils.getTreeCopy(jcas, annotationTree);
			TreebankNode conceptNode = AnnotationTreeUtils.insertAnnotationNode(jcas, root, mention, "CONCEPT");
			// navigate up the tree to retrieve the first "S" above this node.
			TreebankNode node = conceptNode;
			while(node.getParent() != null && !node.getNodeType().startsWith("S")){
				node =  node.getParent();
			}

			// remove nodes to the right of the CONCEPT node
			AnnotationTreeUtils.removeRightOfAnnotation(jcas, node, conceptNode);
			
			tree = TreeExtractor.getSimpleClone(node);
		}else{
			tree = SimpleTree.fromString("(S noparse)");
		}

		TreeExtractor.lowercaseWords(tree);
		if(sems != null){
			replaceWordsWithSemanticClasses(tree, sems);
		}
		return tree;
	}
	
	public static SimpleTree extractAboveRightConceptTree(JCas jcas, Annotation mention, SemanticClasses sems){
		SimpleTree tree = null;
		TopTreebankNode annotationTree = AnnotationTreeUtils.getAnnotationTree(jcas, mention);
		if(annotationTree != null){
			TopTreebankNode root = AnnotationTreeUtils.getTreeCopy(jcas, annotationTree);
			TreebankNode conceptNode = AnnotationTreeUtils.insertAnnotationNode(jcas, root, mention, "CONCEPT");
			//						SimpleTree tree = null;
			//						tree = TreeExtractor.getSurroundingTreeWithAnnotation(node, "CONCEPT");
			// navigate up the tree to retrieve the first "S" above this node.
			TreebankNode node = conceptNode;
			while(node.getParent() != null && !node.getNodeType().startsWith("S")){
				node =  node.getParent();
			}

			// get the VP node (clause) or S that most closely dominates the concept, and remove everything after that
			// should smallen the tree while also permitting post-mention negation like "problem resolved" or "problem ruled out"
			
			// remove nodes to the right of the CONCEPT node
			AnnotationTreeUtils.removeLeftOfAnnotation(jcas, node, conceptNode);
		
			tree = TreeExtractor.getSimpleClone(node);
		}else{
			tree = SimpleTree.fromString("(S noparse)");
		}

		TreeExtractor.lowercaseWords(tree);
		if(sems != null){
			replaceWordsWithSemanticClasses(tree, sems);
		}
		return tree;
	}
	
	public static void replaceWordsWithSemanticClasses(SimpleTree tree, SemanticClasses sems){
		// recursion base case... actually apply semantic classes...
		if(tree.isLeaf()){
			for(Map.Entry<String,HashSet<String>> semClass : sems.entrySet()){
				if(semClass.getValue().contains(tree.cat)){
					tree.cat = "semclass_" + semClass.getKey();
				}
			}
		}else{
			// iterate over children
			for(SimpleTree child : tree.children){
				replaceWordsWithSemanticClasses(child, sems);
			}
		}
	}
	
	static HashMap<String,String> wordMap = new HashMap<String,String>();
    static Random random = new Random();
	public void randomizeWords(SimpleTree tree, boolean dep) {
		if(!tree.cat.equals("CONCEPT") && !tree.cat.equals("TOP") && (dep || tree.children.size() == 0)){
			if(wordMap.containsKey(tree.cat)){
				tree.cat = wordMap.get(tree.cat);
			}else{
				// generate new random word... (from http://stackoverflow.com/a/4952066)
				String oldWord = tree.cat;
				char[] word = new char[random.nextInt(8)+3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
				for(int j = 0; j < word.length; j++)
				{
					word[j] = (char)('a' + random.nextInt(26));
				}
				tree.cat = new String(word);
				wordMap.put(oldWord, tree.cat);
			}
		}
		if(tree.children.size() > 0){
			for(SimpleTree child : tree.children){
				randomizeWords(child, dep);
			}
		}
	}

}
