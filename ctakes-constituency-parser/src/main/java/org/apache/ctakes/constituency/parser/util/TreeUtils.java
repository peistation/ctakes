/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.constituency.parser.util;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.syntax.TerminalTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

public class TreeUtils {

	public static List<TreebankNode> getNodeList(TopTreebankNode tree){
		ArrayList<TreebankNode> list = new ArrayList<TreebankNode>();
		list.add(tree);
		int ind = 0;	
		while(ind < list.size()){
			TreebankNode cur = list.get(ind);
			for(int i = 0; i < cur.getChildren().size(); i++){
				list.add(cur.getChildren(i));
			}
			ind++;
		}
		return list;
	}
	
	public static List<Parse> getNodeList(Parse tree){
		ArrayList<Parse> list = new ArrayList<Parse>();
		list.add(tree);
		int ind = 0;	
		while(ind < list.size()){
			Parse cur = list.get(ind);
			Parse[] children = cur.getChildren();
			for(int i = 0; i < children.length; i++){
				list.add(children[i]);
			}
			ind++;
		}
		return list;
	}
	
	public static String tree2str(TreebankNode pathTree){
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		try{
		buff.append(pathTree.getNodeType());
		}catch(Exception e){
			System.err.println("Caught NPE");
		}
		if(pathTree.getLeaf()){ //pathTree.getChildren().size() == 1 && pathTree.getChildren(0).getLeaf()){
			buff.append(" ");
			buff.append(pathTree.getNodeValue());
//			buff.append(")");
		}else{
			for(int i = 0; i < pathTree.getChildren().size(); i++){
				buff.append(" ");
				buff.append(tree2str(pathTree.getChildren(i)));
			}
		}
		buff.append(")");
		return buff.toString();
	}

//	public static boolean contains(TreebankNode n, SimpleTree frag){
//		if(fragmentMatch(n,frag)) return true;
//		
//		for(int i = 0; i < n.getChildren().size(); i++){
//			if(fragmentMatch(n.getChildren(i), frag)) return true;
//		}
//		return false;
//	}
//	
//	private static boolean fragmentMatch(TreebankNode n, SimpleTree frag){
//		boolean same = false;
//		if(n.getNodeType().equals(frag.cat) && (frag.children.size() == 0 || n.getChildren().size() == frag.children.size())){
//			same = true;
//			for(int i = 0; i < frag.children.size(); i++){
//				if(!fragmentMatch(n.getChildren(i), frag.children.get(i))){
//					same = false;
//					break;
//				}
//			}
//		}
//		return same;
//	}
	
	public static boolean containsIgnoreCase(SimpleTree node, SimpleTree frag){
		return contains(node, frag, true);
	}

	public static boolean contains(SimpleTree node, SimpleTree frag){
		return contains(node, frag, false);
	}

	public static boolean contains(SimpleTree node, SimpleTree frag, boolean ignoreCase){
		if(fragmentMatch(node,frag, ignoreCase)) return true;

		for(int i = 0; i < node.children.size(); i++){
			if(contains(node.children.get(i), frag, ignoreCase)) return true;
		}
		return false;
	}

	private static boolean fragmentMatch(SimpleTree node, SimpleTree frag, boolean ignoreCase){
		boolean same = false;
		if((ignoreCase && node.cat.equalsIgnoreCase(frag.cat)) || (!ignoreCase && node.cat.equals(frag.cat))){
			if((frag.children.size() == 0 || node.children.size() == frag.children.size())){

				same = true;
				for(int i = 0; i < frag.children.size(); i++){
					if(!fragmentMatch(node.children.get(i), frag.children.get(i), ignoreCase)){
						same = false;
						break;
					}
				}
			}
		}
		return same;
	}

	public static int getHighestIndexTerm(TreebankNode inTree) {
		if(inTree instanceof TerminalTreebankNode){
			return ((TerminalTreebankNode) inTree).getIndex();
		}
			return getHighestIndexTerm(inTree.getChildren(inTree.getChildren().size()-1));
	}

	public static TopTreebankNode getTopNode(TreebankNode inTree) {
		TreebankNode cur = inTree;
		TopTreebankNode top = null;
		
		while(!(cur instanceof TopTreebankNode)){
			cur = cur.getParent();
		}
		top = (TopTreebankNode) cur;
		return top;
	}

	public static TopTreebankNode buildAlignedTree(JCas jcas, Parse parse, Sentence sent) throws AnalysisEngineProcessException {
		FSArray termArray = TreeUtils.getTerminals(jcas, sent);
		
		StringBuffer parseBuff = new StringBuffer();
		if(parse != null) parse.show(parseBuff);
		
		TopTreebankNode top = new TopTreebankNode(jcas, sent.getBegin(), sent.getEnd());
		top.setTreebankParse(parseBuff.toString());
		top.setTerminals(termArray);
		top.setParent(null);
		if(parse != null) recursivelyCreateStructure(jcas, top, parse, top);
	
		return top;
	}
	
	public static FSArray getTerminals(JCas jcas, Sentence sent){
		ArrayList<BaseToken> wordList = new ArrayList<BaseToken>();
		FSIterator<Annotation> iterator = jcas.getAnnotationIndex(BaseToken.type).subiterator(sent);
		while(iterator.hasNext()){
			BaseToken w = (BaseToken)iterator.next();
			if(w instanceof NewlineToken) continue;
			wordList.add(w);
		}
		
		FSArray terms = new FSArray(jcas, wordList.size());
		for(int i = 0; i < wordList.size(); i++){
			BaseToken w = wordList.get(i);
			TerminalTreebankNode ttn = new TerminalTreebankNode(jcas, w.getBegin(), w.getEnd());
			ttn.setChildren(null);
			ttn.setIndex(i);
			ttn.setTokenIndex(i);
			ttn.setLeaf(true);
			ttn.setNodeTags(null);
			if(w instanceof PunctuationToken){
				String tokStr = w.getCoveredText();
				if(tokStr.equals("(") || tokStr.equals("[")){
					ttn.setNodeValue("-LRB-");
				}else if(tokStr.equals(")") || tokStr.equals("]")){
					ttn.setNodeValue("-RRB-");
				}else if(tokStr.equals("{")){
					ttn.setNodeValue("-LCB-");
				}else if(tokStr.equals("}")){
					ttn.setNodeValue("-RCB-");
				}else{
					ttn.setNodeValue(w.getCoveredText());
				}
			}else{
				ttn.setNodeValue(w.getCoveredText());
			}
//			ttn.addToIndexes();
			terms.set(i, ttn);
		}
		
		return terms;
	}
	
	public static String getSentence(FSArray termArray){
		StringBuffer sent = new StringBuffer();
//		int offset = 0;
		
		for(int i = 0; i < termArray.size(); i++){
			TerminalTreebankNode ttn = (TerminalTreebankNode) termArray.get(i);
			String word = ttn.getNodeValue();
			word = word.replaceAll("\\s", "");
//			if(i == 0) offset = ttn.getBegin();
			/*else*/
			if(word.length() == 0) continue;
			//else
			sent.append(" ");

			sent.append(word);
		}		
		return sent.toString();
	}
	
	private static void recursivelyCreateStructure(JCas jcas, TreebankNode parent, Parse parse, TopTreebankNode root) throws AnalysisEngineProcessException{
		String[] typeParts;
		if(parse.getType().startsWith("-")){
			// check for dash at the start (for escaped types like -RRB- and so forth that cannot take function tags anyways)
			typeParts = new String[]{parse.getType()};
		}else{
			typeParts = parse.getType().split("-");
		}
		parent.setNodeType(typeParts[0]);
		parent.setNodeValue(null);
		parent.setLeaf(parse.getChildCount() == 0);
		StringArray tags = new StringArray(jcas, typeParts.length-1);
		for(int i = 1; i < typeParts.length; i++){
			tags.set(i-1, typeParts[i]);
		}
		parent.setNodeTags(tags);
		parent.setHeadIndex(parse.getHeadIndex());
		
		Parse[] subtrees = parse.getChildren();
		FSArray children = new FSArray(jcas, subtrees.length);
		
		for(int i = 0; i < subtrees.length; i++){
			Parse subtree = subtrees[i];
			if(subtree.getChildCount() == 1 && subtree.getChildren()[0].getChildCount() == 0){
				// pre-terminal case - now we can set the type (POS tag) and point the parent in the right direction
				TerminalTreebankNode term = root.getTerminals(subtree.getHeadIndex());
				term.setNodeType(subtree.getType());
				children.set(i,term);
				term.setParent(parent);
				term.addToIndexes();
			}else{
				try{
					TreebankNode child = new TreebankNode(jcas);
					child.setParent(parent);
					children.set(i, child);
					recursivelyCreateStructure(jcas, child, subtree, root);
					child.addToIndexes();
				}catch(NullPointerException e){
					System.err.println("MaxentParserWrapper Error: " + e);
					throw new AnalysisEngineProcessException();
				}
			}
		}
		// after we've built up all the children we can fill in the span of the parent.
		parent.setBegin(((TreebankNode)children.get(0)).getBegin());
		parent.setEnd(((TreebankNode)children.get(subtrees.length-1)).getEnd());
		parent.setChildren(children);
//		parent.addToIndexes();
	}

	public static void replaceChild(TreebankNode parent, TreebankNode oldTree,
			TreebankNode newTree) {
		// if parent is null that means we're already at the top -- no pointers to fix.
		if(parent != null){
			for(int i = 0; i < parent.getChildren().size(); i++){
				if(parent.getChildren(i) == oldTree){
					parent.setChildren(i, newTree);
				}
			}
		}
	}

	public static Parse ctakesTokensToOpennlpTokens(Sentence sent, FSArray termArray) {
		// based on the first part of parseLine in the opennlp libraries
		String text = sent.getCoveredText();
		Parse p = new Parse(sent.getCoveredText(), new Span(0, text.length()), AbstractBottomUpParser.INC_NODE, 0, 0);
		
		for(int i = 0; i < termArray.size(); i++){
			TerminalTreebankNode token = (TerminalTreebankNode) termArray.get(i);
			p.insert(new Parse(text, new Span(token.getBegin()-sent.getBegin(), token.getEnd()-sent.getBegin()), AbstractBottomUpParser.TOK_NODE, 0, i));
		}
		
		return p;
	}
}

