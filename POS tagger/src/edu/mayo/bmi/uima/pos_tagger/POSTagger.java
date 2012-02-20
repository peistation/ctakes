/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
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
 * - added TAG_DICIONARY_PARAM
 * - added CASE_SENSITIVE_PARAM
 * - typed the collections used in process
 * - throws an exception instead of printing out an error message.
 * 
 * Please read the README in the top-level directory of this project for further details.  
 */

package edu.mayo.bmi.uima.pos_tagger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.TagDictionary;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.resource.FileLocator;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

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

	/**
	 * "TagDictionary" is an optional, single, string parameter that contains
	 * the file name of the part-of-speech tag dictionary. For relevant
	 * discussion of the difference between a "tag dictionary" and a
	 * "dictionary" see:
	 * <p><ul>
	 * <li>
	 * <a href="https://sourceforge.net/forum/forum.php?thread_id=1720863&forum_id=9943">PosTagger - with/without dictionaries? and ..</a>
	 * <li> 
	 * </li>
	 * <a href="https://sourceforge.net/forum/forum.php?thread_id=1894043&forum_id=9943">Create a new dict file</a>
	 * </li>
	 * </ul>
	 * For information about how to create a TagDictionary, please see the
	 * README in top-level directory of this project.
	 * 
	 * @see TagDictionary
	 * @see POSDictionary
	 */
	public static final String TAG_DICTIONARY_PARAM = "TagDictionary";

	/**
	 * "CaseSensitive" is a required, single, boolean parameter that specifies
	 * how to access entries in the tag dictionary. If you give the value
	 * "false", then you should really have a tag dictionary that is also case
	 * insensitive. Please see the README in top-level directory of this project
	 * for details on how to create a case insensitive tag dictionary.
	 * 
	 * <br>
	 * This parameter has no effect if no tag dictionary is provided but is
	 * required if a tag dictionary is provided.
	 * 
	 * @see POSDictionary#POSDictionary(String, boolean)
	 * 
	 */
	public static final String CASE_SENSITIVE_PARAM = "CaseSensitive";

	private PosTagger tagger;

	public void initialize(UimaContext uimaContext)	throws ResourceInitializationException {
		super.initialize(uimaContext);

		String posModelPath = null;

		try {
			posModelPath = (String) uimaContext.getConfigParameterValue(POS_MODEL_FILE_PARAM);
			File posModelFile = FileLocator.locateFile(posModelPath);
			String modelFileAbsPath = posModelFile.getAbsolutePath();
			logger.info("POS tagger model file: " + modelFileAbsPath);

			boolean caseSensitive = (Boolean) uimaContext.getConfigParameterValue(CASE_SENSITIVE_PARAM);
			String tagDictionaryPath = (String) uimaContext.getConfigParameterValue(TAG_DICTIONARY_PARAM);

			TagDictionary tagDictionary = null;
			if (tagDictionaryPath != null && !tagDictionaryPath.trim().equals("")) {
				File tagDictFile = FileLocator.locateFile(tagDictionaryPath);
				String tagDictFileAbsPath = tagDictFile.getAbsolutePath();
				logger.info("POS tagger tag-dictionary: " + tagDictFileAbsPath);

				tagDictionary = new POSDictionary(tagDictFileAbsPath, caseSensitive);
			} else {
				logger.info("No POS tagger tag-dictionary.");
			}

			tagger = new PosTagger(modelFileAbsPath, tagDictionary);

		} catch (Exception e) {
			logger.info("POS tagger model: " + posModelPath);
			throw new ResourceInitializationException(e);
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		logger.info("POSTaggerAnnotator process(JCas)");

		List<BaseToken> tokens = new ArrayList<BaseToken>();
		List<String> words = new ArrayList<String>();

		AnnotationIndex baseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);

		FSIterator sentences = jCas.getAnnotationIndex(Sentence.type).iterator();

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
			// 	logger.info("sentence has no words = '" + sentence.getCoveredText()
			// 		+ "' at (" +sentence.getBegin() + "," + sentence.getEnd() + ")");
			// }

			try {
				for (int i = 0; i < tokens.size(); i++) {
					BaseToken token = (BaseToken) tokens.get(i);
					String posTag = (String) wordTagList.get(i);
					token.setPartOfSpeech(posTag);
				}
			} catch (IndexOutOfBoundsException e) {
				throw new AnalysisEngineProcessException(
						"sentence being tagged is: '" + sentence.getCoveredText() + "'", null, e);
			}
		}
	}
}
