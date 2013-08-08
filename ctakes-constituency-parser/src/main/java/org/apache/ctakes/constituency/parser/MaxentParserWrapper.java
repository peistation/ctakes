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
package org.apache.ctakes.constituency.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.parser.chunking.Parser;
import opennlp.tools.util.Span;

import org.apache.ctakes.constituency.parser.util.TreeUtils;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.syntax.TerminalTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;

public class MaxentParserWrapper implements ParserWrapper {

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

	@Override
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
	public void createAnnotations(JCas jcas) throws AnalysisEngineProcessException {
		String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
		logger.info("Started processing: " + docId);
		// iterate over sentences
		FSIterator iterator = jcas.getAnnotationIndex(Sentence.type).iterator();
		Parse parse = null;
		
		while(iterator.hasNext()){
			Sentence sentAnnot = (Sentence) iterator.next();
			if(sentAnnot.getCoveredText().length() == 0){
				continue;
			}
			FSArray termArray = TreeUtils.getTerminals(jcas, sentAnnot);
			Parse inputTokens = TreeUtils.ctakesTokensToOpennlpTokens(sentAnnot, termArray);
			String sentStr = TreeUtils.getSentence(termArray);
			if(sentStr.length() == 0){
				parse = null;
			}else{
				parse = parser.parse(inputTokens);
			}
			TopTreebankNode top = TreeUtils.buildAlignedTree(jcas, parse, sentAnnot);
			top.addToIndexes();
		}
//		logger.info("Done parsing: " + docId);
	}


}
