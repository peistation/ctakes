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
package edu.mayo.bmi.uima.chunker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.chunker.ChunkerModel;
// import opennlp.tools.lang.english.TreebankChunker; // no longer part of OpenNLP as of 1.5

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

/**
 * This class provides a UIMA wrapper for the OpenNLP
 * opennlp.tools.chunker.Chunker class. This wrapper can generate chunks of any
 * kind as specified by the chunker model and the chunk creator.  
 */

public class Chunker extends JCasAnnotator_ImplBase {

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());


	/**
	 * "ChunkerModel" is a required, single, string parameter that contains the
	 * file name of the chunker model. The model file name should
	 * end with ".bin.gz" or ".txt". If this is not the case, then please see
	 * resources/models/README.
	 */
	public static final String CHUNKER_MODEL_FILE_PARAM = "ChunkerModelFile";

	/**
	 * "ChunkCreatorClass" is a required, single, string parameter that
	 * specifies the chunker creator class to instantiate. A chunker creator
	 * determines how chunk annotations are created.
	 * 
	 * @see ChunkCreator
	 * @see DefaultChunkCreator
	 * @see PhraseTypeChunkCreator
	 */
	public static final String CHUNKER_CREATOR_CLASS_PARAM = "ChunkCreatorClass";

	private opennlp.tools.chunker.Chunker chunker;

	ChunkCreator chunkerCreator;


	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		String chunkerModelPath = null;
		try {
			chunkerModelPath = (String) uimaContext.getConfigParameterValue(CHUNKER_MODEL_FILE_PARAM);
			File chunkerModelFile = FileLocator.locateFile(chunkerModelPath);
			InputStream fis = new FileInputStream(chunkerModelFile);
			ChunkerModel model = new ChunkerModel(fis);
			String chunkerModelAbsPath = chunkerModelFile.getAbsolutePath();
			logger.info("Chunker model file: " + chunkerModelAbsPath); 
									
			chunker = new opennlp.tools.chunker.ChunkerME(model);

			String chunkerCreatorClassName = (String) uimaContext.getConfigParameterValue(CHUNKER_CREATOR_CLASS_PARAM);

			chunkerCreator = (ChunkCreator) Class.forName(chunkerCreatorClassName).newInstance();
			chunkerCreator.initialize(uimaContext);

		} catch (Exception e) {
			logger.info("Chunker model: " + chunkerModelPath); 
			throw new ResourceInitializationException(e);
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		logger.info(" process(JCas)");

		List<BaseToken> tokens = new ArrayList<BaseToken>();

		AnnotationIndex baseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);
		FSIterator sentences = jCas.getAnnotationIndex(Sentence.type).iterator();

		while (sentences.hasNext()) {
			Sentence sentence = (Sentence) sentences.next();

			tokens.clear();

			FSIterator tokenIterator = baseTokenIndex.subiterator(sentence);
			while (tokenIterator.hasNext()) {
				BaseToken token = (BaseToken) tokenIterator.next();
				tokens.add(token);
			}

			String[] words = new String[tokens.size()];
			String[] tags = new String[tokens.size()];
			for (int i = 0; i < tokens.size(); i++) {
				words[i] = tokens.get(i).getCoveredText();
				tags[i] = tokens.get(i).getPartOfSpeech();
			}

			String[] chunks = chunker.chunk(words, tags);

			int chunkBegin = 0;
			String chunkType = "";
			int chunkEnd;

			// The logic below may seem to be oversimplified. For example, it
			// does not handle
			// cases where you might see a O I-NP O. However, such sequences
			// should never be
			// generated because they are restricted by
			// TreebankChunker.validOutcome()
			// This code was directly modified from TreebankChunker.main()
			for (int i = 0; i < chunks.length; i++) {
			    
			    if (i > 0 && !chunks[i].startsWith("I-")) { // && !chunks[i - 1].equals("O")) {
				chunkEnd = tokens.get(i - 1).getEnd();
				chunkerCreator.createChunk(jCas, chunkBegin, chunkEnd, chunkType);
			    }
			    
			    if (chunks[i].startsWith("B-")) {
				chunkBegin = tokens.get(i).getBegin();
				chunkType = chunks[i].substring(2);
			    } else if (chunks[i].equals("O")) { // O found  (her_PRP$ ear_O)
				chunkBegin = tokens.get(i).getBegin();
				chunkType = chunks[i];
			    
			    }
			}
			if (chunks.length > 0 && !chunks[chunks.length - 1].equals("O")) {
				chunkEnd = tokens.get(chunks.length - 1).getEnd();
				chunkerCreator.createChunk(jCas, chunkBegin, chunkEnd, chunkType);
			}
		}
	}
}
