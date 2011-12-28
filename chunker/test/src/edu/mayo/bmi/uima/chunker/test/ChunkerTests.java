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
package edu.mayo.bmi.uima.chunker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import edu.mayo.bmi.uima.chunker.PhraseTypeChunkCreator;
import edu.mayo.bmi.uima.chunker.type.ADJP;
import edu.mayo.bmi.uima.chunker.type.ADVP;
import edu.mayo.bmi.uima.chunker.type.CONJP;
import edu.mayo.bmi.uima.chunker.type.INTJ;
import edu.mayo.bmi.uima.chunker.type.LST;
import edu.mayo.bmi.uima.chunker.type.NP;
import edu.mayo.bmi.uima.chunker.type.PP;
import edu.mayo.bmi.uima.chunker.type.PRT;
import edu.mayo.bmi.uima.chunker.type.SBAR;
import edu.mayo.bmi.uima.chunker.type.UCP;
import edu.mayo.bmi.uima.chunker.type.VP;
import edu.mayo.bmi.uima.core.test.TestUtil;
import edu.mayo.bmi.uima.chunker.type.Chunk;

public class ChunkerTests {

	@Test
    public void testDefaultChunker() throws ResourceInitializationException {
		
		// new File(String) is rooted under chunker project directory
		AnalysisEngine ae = TestUtil.getAE(new File("test/desc/ChunkerAggregate.xml"));
		//this tests the tagging model with no tag dictionary
		JCas jCas = TestUtil.processAE(ae, "Induction of NF-KB during monocyte differentiation by HIV type 1 infection.");

		Chunk chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 0);
		assertEquals(0, chunk.getBegin());
		assertEquals(9, chunk.getEnd());
		assertEquals("Induction", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 1);
		assertEquals("of", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 2);
		assertEquals("NF-KB", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 3);
		assertEquals("during", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 4);
		assertEquals("monocyte differentiation", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 5);
		assertEquals("by", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 6);
		assertEquals("HIV type 1 infection", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());

	}
	
	@Test
    public void testPhraseTypeChunker() throws ResourceInitializationException {
		
		AnalysisEngine ae = TestUtil.getAE(new File("test/desc/PhraseTypeChunkerAggregate.xml"));
		//this tests the tagging model with no tag dictionary
		JCas jCas = TestUtil.processAE(ae, "Induction of NF-KB during monocyte differentiation by HIV type 1 infection.");

		Chunk chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 0);
		assertEquals(0, chunk.getBegin());
		assertEquals(9, chunk.getEnd());
		assertEquals("Induction", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());
		assertTrue(chunk instanceof NP);
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 1);
		assertEquals("of", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());
		assertTrue(chunk instanceof PP);
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 2);
		assertEquals("NF-KB", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());
		assertTrue(chunk instanceof NP);

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 3);
		assertEquals("during", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());
		assertTrue(chunk instanceof PP);

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 4);
		assertEquals("monocyte differentiation", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());
		assertTrue(chunk instanceof NP);

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 5);
		assertEquals("by", chunk.getCoveredText());
		assertEquals("PP", chunk.getChunkType());
		assertTrue(chunk instanceof PP);

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 6);
		assertEquals("HIV type 1 infection", chunk.getCoveredText());
		assertEquals("NP", chunk.getChunkType());
		assertTrue(chunk instanceof NP);

		PhraseTypeChunkCreator chunkCreator = new PhraseTypeChunkCreator();
		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "ADJP");
		assertEquals("ADJP", chunk.getChunkType());
		assertTrue(chunk instanceof ADJP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "ADVP");
		assertEquals("ADVP", chunk.getChunkType());
		assertTrue(chunk instanceof ADVP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "CONJP");
		assertEquals("CONJP", chunk.getChunkType());
		assertTrue(chunk instanceof CONJP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "INTJ");
		assertEquals("INTJ", chunk.getChunkType());
		assertTrue(chunk instanceof INTJ);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "LST");
		assertEquals("LST", chunk.getChunkType());
		assertTrue(chunk instanceof LST);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "NP");
		assertEquals("NP", chunk.getChunkType());
		assertTrue(chunk instanceof NP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "PP");
		assertEquals("PP", chunk.getChunkType());
		assertTrue(chunk instanceof PP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "PRT");
		assertEquals("PRT", chunk.getChunkType());
		assertTrue(chunk instanceof PRT);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "SBAR");
		assertEquals("SBAR", chunk.getChunkType());
		assertTrue(chunk instanceof SBAR);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "UCP");
		assertEquals("UCP", chunk.getChunkType());
		assertTrue(chunk instanceof UCP);

		chunk = (Chunk) chunkCreator.createChunk(jCas, 0, 5, "VP");
		assertEquals("VP", chunk.getChunkType());
		assertTrue(chunk instanceof VP);

	}

}
