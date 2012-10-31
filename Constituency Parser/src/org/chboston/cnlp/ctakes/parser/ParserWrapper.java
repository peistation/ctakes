/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *     
 * @author Tim Miller
 */
package org.chboston.cnlp.ctakes.parser;

import java.util.ArrayList;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.NewlineToken;
import edu.mayo.bmi.uima.core.type.syntax.PunctuationToken;
import edu.mayo.bmi.uima.core.type.syntax.TerminalTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public abstract class ParserWrapper {
//	public abstract String getParseString(FSIterator tokens);
	
	// instead of passing it an array of tokens or even a sentence, just pass it the whole
	// document.  this allows for freedom to do parsing at whatever level it wishes, as well as to do
	// its own tokenization if necessary... (though that might be weird)
	public abstract void createAnnotations(JCas jcas);
	
	public FSArray getTerminals(JCas jcas, Sentence sent){
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
	
	public TerminalTreebankNode getLeftmostChild(TreebankNode node){
		TreebankNode cur = node;
		while(!(cur instanceof TerminalTreebankNode)){
			cur = cur.getChildren(0);
		}
		return (TerminalTreebankNode) cur;
	}

	public TerminalTreebankNode getRightmostChild(TreebankNode node){
		TreebankNode cur = node;
		while(!(cur instanceof TerminalTreebankNode)){
			cur = cur.getChildren(cur.getChildren().size());
		}
		return (TerminalTreebankNode) cur;
	}
}
