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
package edu.mayo.bmi.uima.adjuster;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.type.syntax.Chunk;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

/**
 * UIMA annotator that uses a pattern and a rule about that pattern to adjust
 * certain annotations.
 * 
 * The original reason for this annotator is to extend NP annotations to include
 * prepositional phrases so that for the pattern NP PP NP, named entities that
 * includes a word(s) from each of those NPs is found.
 * 
 * Searches for the pattern within each Sentence. The end offset of the first
 * chunk in the pattern is extended to match the end offset of the last chunk in
 * the pattern.
 * 
 * Note the pattern is applied repeatedly so that a sentence of NP PP NP PP NP
 * results in only the first NP being extended all the way to the last NP in
 * that sentence. This prevents NP annotations from only partially overlapping
 * other NP annotations.
 * 
 * This annotator is written to be able to handle more general cases than NP PP
 * NP.
 * 
 * 
 */
public class ChunkAdjuster extends JCasAnnotator_ImplBase {
	/**
	 * The pattern of chunks that trigger an adjustment.
	 * 
	 */
	public static final String PARAM_CHUNK_PATTERN = "ChunkPattern";

	/**
	 * The index of the token (within the pattern) to extend the end offset to
	 * include. E.g. is 2 to extend the first NP to include the last NP in NP PP
	 * NP.
	 */
	public static final String PARAM_EXTEND_TO_INCLUDE_TOKEN = "IndexOfTokenToInclude";

	// TODO Consider adding a parameter for the type of annotation to look for
	// pattern within, instead of always Sentence

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	private UimaContext context;

	private String[] chunksTypesInPattern;

	private int indexOfTokenToInclude;

	/**
	 * Performs initialization logic. This implementation just reads values for
	 * the configuration parameters. This method is not invoked for every
	 * document processed.
	 * 
	 * @see org.apache.uima.analysis_engine.annotator.BaseAnnotator#initialize(AnnotatorContext)
	 */
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {

		super.initialize(aContext);

		context = aContext;

		configInit();

	}

	/**
	 * Sets configuration parameters with values from the descriptor.
	 */
	private void configInit() throws ResourceInitializationException {

		// populate the HashSet of words that we will ignore when pattern
		// matching
		chunksTypesInPattern = (String[]) context
				.getConfigParameterValue(PARAM_CHUNK_PATTERN);
		indexOfTokenToInclude = ((Integer) context
				.getConfigParameterValue(PARAM_EXTEND_TO_INCLUDE_TOKEN))
				.intValue();

		// TODO Consider validating values in pattern to type system

		if (indexOfTokenToInclude < 0
				|| indexOfTokenToInclude >= chunksTypesInPattern.length) {
			// "The value "{0}" is not valid for the {1} parameter."
			String msgArgs[] = { Integer.toString(indexOfTokenToInclude),
					PARAM_EXTEND_TO_INCLUDE_TOKEN };
			throw new ResourceInitializationException(
					AnnotatorConfigurationException.PARAMETER_NOT_VALID,
					msgArgs);
		}

	}

	/**
	 * Invokes this annotator's analysis logic. Invoked for each document
	 * processed. For each Sentence, look for the pattern, and adjust a chunk if
	 * the pattern is found.
	 */
	public void process(JCas jcas)
			throws AnalysisEngineProcessException {

		logger.info(" process(JCas)");

		String text = jcas.getDocumentText();

		try {
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
			FSIterator sentenceItr = indexes.getAnnotationIndex(Sentence.type)
					.iterator();
			while (sentenceItr.hasNext()) {
				Sentence sentence = (Sentence) sentenceItr.next();
				int start = sentence.getBegin();
				int end = sentence.getEnd();
				annotateRange(jcas, text, start, end);
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * A utility method that annotates a given range.
	 */
	protected void annotateRange(JCas jcas, String text, int rangeBegin,
			int rangeEnd)
			throws AnalysisEngineProcessException {

		JFSIndexRepository indexes = jcas.getJFSIndexRepository();

		// logger.info("Adjuster: from " + rangeBegin + " to " + rangeEnd);

		// Find the Chunks in this Sentence
		// For each Chunk, there is a corresponding more specific such as NP,
		// PP, etc
		FSIterator chunkItr = indexes.getAnnotationIndex(Chunk.type).iterator();
		ArrayList<Chunk> list = new ArrayList<Chunk>();
		while (chunkItr.hasNext()) {
			Chunk baseChunk = (Chunk) chunkItr.next();
			if (baseChunk.getBegin() >= rangeBegin
					&& baseChunk.getEnd() <= rangeEnd) {
				list.add(baseChunk);
			}
		}

		// For each chunk in the Sentence, see if the chunk is the start of a
		// matching pattern
		// If so, extend the end offset of the <code>i</code> +
		// <code>indexOfTokenToInclude</code>
		for (int i = 0; i < list.size(); i++) {

			boolean matches = true;
			Chunk chunk = list.get(i);

			while (matches == true) {
				matches = compareToPattern(list, i);
				if (matches) {
					extendChunk(chunk, list.get(i + indexOfTokenToInclude)
							.getEnd());
					removeEnvelopedChunks(list, i); // to check again on next
													// iteration of while loop
				}
			}
		}

	}

	/**
	 * Remove from our local list of chunks the chunks that have been enveloped.
	 * This allows the rule to be applied again.
	 * 
	 */
	private void removeEnvelopedChunks(ArrayList<Chunk> list, int i) {
		for (int j = 0; j < indexOfTokenToInclude; j++) {
			Chunk chunk = list.remove(i + 1);
			if (false)
				logger.info("removed '" + chunk.getCoveredText() + "'");
		}
	}

	/**
	 * Compares the chunks at index i to the 1st element on the pattern, i+1 to
	 * the 2nd element, etc and returns true if the chunks starting at i fit the
	 * pattern
	 * 
	 * @param list
	 *            the list of chunks
	 * @param i
	 *            the position within the list to compare to the pattern
	 * @return true if the pattern is matched by the chunks starting with
	 *         element <code>i</code> in the list. Note if there aren't enough
	 *         chunks in the list starting at i to match the pattern, returns
	 *         false.
	 * @throws AnnotatorProcessException
	 */
	private boolean compareToPattern(ArrayList<Chunk> list, int i)
			throws AnalysisEngineProcessException {

		boolean match = true;
		int len = list.size();
		for (int j = 0; j < chunksTypesInPattern.length; j++) {
			if (i + j >= len
					|| !list.get(i + j).getChunkType()
							.equals(chunksTypesInPattern[j])) {
				match = false; // some part of pattern doesn't match chunks
								// starting at i
				break;
			}
		}

		return match;

	}

	/**
	 * Update the end value for the chunk to have the new value
	 * 
	 * @param chunk
	 *            The chunk to update
	 * @param newEnd
	 *            The new end value for the chunk.
	 * @return The updated Chunk
	 * @throws AnnotatorProcessException
	 */
	private Chunk extendChunk(Chunk chunk, int newEnd)
			throws AnalysisEngineProcessException {

		if (newEnd < chunk.getBegin()) {
			Exception e;
			e = new Exception("New end offset (" + newEnd
					+ ") < begin offset (" + chunk.getBegin() + ").");
			throw new AnalysisEngineProcessException(e);
		}
		// logger.info("Extending chunk end from " +chunk.getEnd()+ " to " +
		// newEnd + ".");
		// logger.info("For chunk " + chunk.getChunkType());
		// logger.info(" text =      '" + chunk.getCoveredText() + "'.");
		chunk.setEnd(newEnd);
		// logger.info(" new text =  '" + chunk.getCoveredText() + "'.");
		return chunk;

	}

}
