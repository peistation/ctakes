/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
* @author Tim Miller
*/
package org.chboston.cnlp.ctakes.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.lang.english.TreebankParser;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.util.Span;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode;
import org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode;
import org.chboston.cnlp.ctakes.parser.uima.type.TreebankNode;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.NewlineToken;
import edu.mayo.bmi.uima.core.type.syntax.PunctuationToken;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;
import edu.mayo.bmi.uima.core.type.syntax.SymbolToken;
import edu.mayo.bmi.uima.core.type.syntax.WordToken;

public class MaxentParserWrapper implements ParserWrapper {

	Parser parser = null;
	private boolean useTagDictionary = true;
	private boolean useCaseSensitiveTagDictionary = true;
	private String parseStr = "";
	
	public MaxentParserWrapper(String dataDir) {
		try {
			parser = TreebankParser.getParser(dataDir, useTagDictionary, useCaseSensitiveTagDictionary, AbstractBottomUpParser.defaultBeamSize, AbstractBottomUpParser.defaultAdvancePercentage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getParseString(FSIterator tokens) {
		return parseStr;
	}

	@Override
	public void createAnnotations(JCas jcas) {
		// iterate over sentences
		FSIterator iterator = jcas.getAnnotationIndex(Sentence.type).iterator();
		// Map from indices in the parsed string to indices in the sofa
		HashMap<Integer,Integer> indexMap = new HashMap<Integer,Integer>();
		
		while(iterator.hasNext()){
			Sentence sentAnnot = (Sentence) iterator.next();
//			if(parser == null){
//				sentAnnot.setParse("Parser not initialized properly.");
//			}
			if(sentAnnot.getCoveredText().length() == 0){
				continue;
			}
			indexMap.clear();
			FSArray termArray = getTerminals(jcas, sentAnnot);
			String sentStr = getSentence(termArray, indexMap);
			Parse parse = TreebankParser.parseLine(sentStr, parser, 1)[0];
			StringBuffer parseBuff = new StringBuffer();
			parse.show(parseBuff);
			parseStr = parseBuff.toString();
			Span span = parse.getSpan();
			TopTreebankNode top = new TopTreebankNode(jcas, sentAnnot.getBegin(), sentAnnot.getEnd());
			top.setTreebankParse(parseBuff.toString());
			top.setTerminals(termArray);
			top.setParent(null);
			recursivelyCreateStructure(jcas, top, parse, top, indexMap);
		}
	}

	private void recursivelyCreateStructure(JCas jcas, TreebankNode parent, Parse parse, TopTreebankNode root, Map<Integer,Integer> imap){
		String[] typeParts = parse.getType().split("-");
		parent.setNodeType(typeParts[0]);
		parent.setNodeValue(typeParts[0]);
		parent.setLeaf(parse.getChildCount() == 0);
		StringArray tags = new StringArray(jcas, typeParts.length-1);
		for(int i = 1; i < typeParts.length; i++){
			tags.set(i-1, typeParts[i]);
		}
		parent.setNodeTags(tags);
		// This is not part of the MiPacq/SHARP type system, but it is hopefully being added. 
		parent.setHeadIndex(parse.getHeadIndex());
		
		Parse[] subtrees = parse.getChildren();
		FSArray children = new FSArray(jcas, subtrees.length);
		
		for(int i = 0; i < subtrees.length; i++){
			Parse subtree = subtrees[i];
			Span span = subtree.getSpan();
			if(subtree.getChildCount() > 0){
				try{
					TreebankNode child = new TreebankNode(jcas, root.getBegin() + imap.get(span.getStart()), root.getBegin() + imap.get(span.getEnd()));
					child.setParent(parent);
					children.set(i, child);
					recursivelyCreateStructure(jcas, child, subtree, root, imap);
				}catch(NullPointerException e){
					System.err.println("MaxentParserWrapper Error: " + e);
				}
			}else{
				TerminalTreebankNode term = root.getTerminals(subtree.getHeadIndex());
				children.set(i,term);
				term.setParent(parent);
			}
//			children.set(i, child);
		}
		parent.setChildren(children);
		parent.addToIndexes();
	}
	
	private String getSentence(FSArray termArray, Map<Integer,Integer> imap){
		StringBuffer sent = new StringBuffer();
		int offset = 0;
		
		for(int i = 0; i < termArray.size(); i++){
			TerminalTreebankNode ttn = (TerminalTreebankNode) termArray.get(i);
			String word = ttn.getNodeType();
			word = word.replaceAll("\\s", "");
			if(i == 0) offset = ttn.getBegin();
			else if(word.length() == 0) continue;
			else sent.append(" ");

			sent.append(word);
//			imap.put(sent.length()-ttn.getNodeType().length(), ttn.getBegin()-offset);
//			imap.put(sent.length(), ttn.getEnd()-offset);
			imap.put(sent.length()-word.length(), ttn.getBegin()-offset);
			imap.put(sent.length(), ttn.getEnd()-offset);
		}
		
		return sent.toString();
	}
	
	private FSArray getTerminals(JCas jcas, Sentence sent){
		ArrayList<BaseToken> wordList = new ArrayList<BaseToken>();
		FSIterator<Annotation> iterator = jcas.getAnnotationIndex(BaseToken.type).subiterator(sent);
		while(iterator.hasNext()){
			BaseToken w = (BaseToken)iterator.next();
			if(w instanceof NewlineToken) continue;
			wordList.add(w);
		}
		
		FSArray terms = new FSArray(jcas, wordList.size());
		for(int i = 0; i < wordList.size(); i++){
			BaseToken w = (BaseToken) wordList.get(i);
			TerminalTreebankNode ttn = new TerminalTreebankNode(jcas, w.getBegin(), w.getEnd());
			ttn.setChildren(null);
			ttn.setIndex(i);
			ttn.setTokenIndex(i);
			ttn.setLeaf(true);
			ttn.setNodeTags(null);
			if(w instanceof PunctuationToken){
				String tokStr = w.getCoveredText();
				if(tokStr.equals("(") || tokStr.equals("[")){
					ttn.setNodeType("-LRB-");
				}else if(tokStr.equals(")") || tokStr.equals("]")){
					ttn.setNodeType("-RRB-");
				}else if(tokStr.equals("{")){
					ttn.setNodeType("-LCB-");
				}else if(tokStr.equals("}")){
					ttn.setNodeType("-RCB-");
				}else{
					ttn.setNodeType(w.getCoveredText());
				}
			}else{
				ttn.setNodeType(w.getCoveredText());
			}
			ttn.setNodeValue(ttn.getNodeType());
			ttn.addToIndexes();
			terms.set(i, ttn);
		}
		
		return terms;
	}
}
