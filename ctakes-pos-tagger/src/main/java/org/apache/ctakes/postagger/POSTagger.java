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
/*
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

/**
 * This class was derived directly from the example annotator provided by the Apache
 * UIMA distribution 2.2.1 in the opennlp_wrappers directory of the uimaj-examples project.
 * 
 * The following changes have been made:
 * - import of different sentence and token types.
 * - removed original comments
 * - typed the collections used in process
 * - throws an exception instead of printing out an error message.
 * 
 * Please read the README in the top-level directory of this project for further details.  
 */

package org.apache.ctakes.postagger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.postag.POSModel;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;

public class POSTagger extends JCasAnnotator_ImplBase {

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * "PosModelFile" is a required, single, string parameter that contains the
	 * file name of the part of speech tagger model. The model file name should
	 * end with ".bin.gz" or ".txt". If this is not the case, then please see
	 * resources/models/README.
	 */
	public static final String POS_MODEL_FILE_PARAM = "PosModelFile";
	private opennlp.tools.postag.POSTaggerME tagger;

	@Override
	public void initialize(UimaContext uimaContext)
			throws ResourceInitializationException {
		super.initialize(uimaContext);

		String posModelPath = null;
		InputStream fis = null;

		try {
			posModelPath = (String) uimaContext
					.getConfigParameterValue(POS_MODEL_FILE_PARAM);
			logger.info("POS tagger model file: " + posModelPath);
			fis = FileLocator.getAsStream(posModelPath);
			POSModel modelFile = new POSModel(fis);
			tagger = new opennlp.tools.postag.POSTaggerME(modelFile);
			fis.close();
		} catch (Exception e) {
			logger.info("Error loading POS tagger model: " + posModelPath);
			throw new ResourceInitializationException(e);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				throw new ResourceInitializationException(e);
			}
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		logger.info("process(JCas)");

		List<BaseToken> tokens = new ArrayList<BaseToken>();
		List<String> words = new ArrayList<String>();

		AnnotationIndex baseTokenIndex = jCas
				.getAnnotationIndex(BaseToken.type);

		FSIterator sentences = jCas.getAnnotationIndex(Sentence.type)
				.iterator();

		while (sentences.hasNext()) {
			Sentence sentence = (Sentence) sentences.next();

			tokens.clear();
			words.clear();

			FSIterator tokenIterator = baseTokenIndex.subiterator(sentence);
			while (tokenIterator.hasNext()) {
				BaseToken token = (BaseToken) tokenIterator.next();
				tokens.add(token);
				words.add(token.getCoveredText());
			}

			List<?> wordTagList = null; // List of BaseToken's
			if (words.size() > 0) {
				wordTagList = tagger.tag(words);
			}
			// else {
			// logger.info("sentence has no words = '" +
			// sentence.getCoveredText()
			// + "' at (" +sentence.getBegin() + "," + sentence.getEnd() + ")");
			// }

			try {
				for (int i = 0; i < tokens.size(); i++) {
					BaseToken token = (BaseToken) tokens.get(i);
					String posTag = (String) wordTagList.get(i);
					token.setPartOfSpeech(posTag);
				}
			} catch (IndexOutOfBoundsException e) {
				throw new AnalysisEngineProcessException(
						"sentence being tagged is: '"
								+ sentence.getCoveredText() + "'", null, e);
			}
		}
	}
}
