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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import opennlp.tools.lang.english.TreebankParser; // no longer part of OpenNLP as of 1.5
import opennlp.model.AbstractModel;
import opennlp.model.MaxentModel;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.ChunkContextGenerator;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserChunkerSequenceValidator;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.parser.chunking.Parser;
import opennlp.tools.parser.lang.en.HeadRules;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.util.Span;

import org.apache.log4j.Logger;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.NewlineToken;
import edu.mayo.bmi.uima.core.type.syntax.PunctuationToken;
import edu.mayo.bmi.uima.core.type.syntax.TerminalTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TopTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

public class MaxentParserWrapper extends ParserWrapper {

	Parser parser = null;
	private String parseStr = "";
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public MaxentParserWrapper(String dataDir) {
		try {
			File d = new File(dataDir);
			
			if (!d.isDirectory()) {
				FileInputStream fis = new FileInputStream(d);
				ParserModel model = new ParserModel(fis);
				parser = new Parser(model, AbstractBottomUpParser.defaultBeamSize, AbstractBottomUpParser.defaultAdvancePercentage);
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getParseString(FSIterator tokens) {
		return parseStr;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.chboston.cnlp.ctakes.parser.ParserWrapper#createAnnotations(org.apache.uima.jcas.JCas)
	 * FIXME - Does not handle the case where a sentence is only numbers. This can happen at the end of a note
	 * after "real" sentences are done where a line is just a string of numbers (looks like a ZIP code).
	 * For some reason the built-in tokenizer does not like that.
	 */
	@Override
	public void createAnnotations(JCas jcas) {
		String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
		logger.info("Started processing: " + docId);
		// iterate over sentences
		FSIterator iterator = jcas.getAnnotationIndex(Sentence.type).iterator();
		// Map from indices in the parsed string to indices in the sofa
		HashMap<Integer,Integer> indexMap = new HashMap<Integer,Integer>();
		Parse parse = null;
		
		while(iterator.hasNext()){
			Sentence sentAnnot = (Sentence) iterator.next();
			if(sentAnnot.getCoveredText().length() == 0){
				continue;
			}
			indexMap.clear();
			FSArray termArray = getTerminals(jcas, sentAnnot);
			String sentStr = getSentence(termArray, indexMap);
			StringBuffer parseBuff = new StringBuffer();
			if(sentStr.length() == 0){
				parseBuff.append("");
				parse = null;
			}else{
				parse = ParserTool.parseLine(sentStr, parser, 1)[0];
				parse.show(parseBuff);
			}
			parseStr = parseBuff.toString();
			TopTreebankNode top = new TopTreebankNode(jcas, sentAnnot.getBegin(), sentAnnot.getEnd());
			top.setTreebankParse(parseBuff.toString());
			top.setTerminals(termArray);
			top.setParent(null);
			if(parse != null) recursivelyCreateStructure(jcas, top, parse, top, indexMap);
		}
		logger.info("Done parsing: " + docId);
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
	
}
