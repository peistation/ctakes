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

public class ChunkAdjusterTests {

	@Test
    public void testAdjusterExtendsChunk() throws ResourceInitializationException {
		
		// new File(String) is rooted under chunker project directory
		AnalysisEngine ae = TestUtil.getAE(new File("test/desc/ChunkAdjusterAggregate.xml"));
		// this tests the ChunkAdjuster NP PP NP extending end offset of chunk 0 
		// to match end offset of chunk 2 in the pattern NP PP NP, but with that
		// process repeated when pattern is repeated.
		JCas jCas = TestUtil.processAE(ae, "Induction of NF-KB during monocyte differentiation by HIV type 1 infection.");

		// The above should be assigned
		// NP PP NP PP NP
		
		// The ChunkAdjuster extends the first NP so it ends up like
		// NP-----> PP NP
		// Then the NP is extended again so it ends up like
		// NP----------->
		
		Chunk chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 0);
		assertEquals(0, chunk.getBegin());
		int chunkEnd = chunk.getEnd();
		//		assertEquals("Induction", chunk.getCoveredText());
		//		assertEquals("NP", chunk.getChunkType());
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 6);
		assertEquals(chunkEnd, chunk.getEnd());
		//		assertEquals("HIV type 1 infection", chunk.getCoveredText());
		//		assertEquals("NP", chunk.getChunkType());
	
	}

	
	@Test
    public void testAdjustDoesNotAlwaysExtend() throws ResourceInitializationException {
		
		// new File(String) is rooted under chunker project directory
		AnalysisEngine ae = TestUtil.getAE(new File("test/desc/ChunkAdjusterAggregate.xml"));

		// This tests that no end offsets are changed when the pattern is not matched
		JCas jCas = TestUtil.processAE(ae, "He prescribed some antibiotics.");

		// The above should be 
		// NP PP NP PP NP
		// The first NP is extended so it ends up like
		// NP-----> PP NP
		// Then the NP is extended again so it ends up like
		// NP----------->
		
		Chunk chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 0);
		assertEquals(0, chunk.getBegin());
		int chunkEnd = chunk.getEnd();
		
		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 1);
		assertTrue(chunkEnd < chunk.getEnd());
		assertTrue(chunkEnd < chunk.getBegin());
		chunkEnd = chunk.getEnd();

		chunk = TestUtil.getFeatureStructureAtIndex(jCas, Chunk.class, 2);
		assertTrue(chunkEnd < chunk.getEnd());
		assertTrue(chunkEnd < chunk.getBegin());


	}
	
}
