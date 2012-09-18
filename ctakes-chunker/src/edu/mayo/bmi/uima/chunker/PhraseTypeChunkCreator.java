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

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.type.syntax.ADJP;
import edu.mayo.bmi.uima.core.type.syntax.ADVP;
import edu.mayo.bmi.uima.core.type.syntax.CONJP;
import edu.mayo.bmi.uima.core.type.syntax.INTJ;
import edu.mayo.bmi.uima.core.type.syntax.LST;
import edu.mayo.bmi.uima.core.type.syntax.NP;
import edu.mayo.bmi.uima.core.type.syntax.O;
import edu.mayo.bmi.uima.core.type.syntax.PP;
import edu.mayo.bmi.uima.core.type.syntax.PRT;
import edu.mayo.bmi.uima.core.type.syntax.SBAR;
import edu.mayo.bmi.uima.core.type.syntax.UCP;
import edu.mayo.bmi.uima.core.type.syntax.VP;
import edu.mayo.bmi.uima.core.type.syntax.Chunk;


/**
 * This chunker creator creates annotations of type edu.mayo.bmi.uima.core.type.* and
 * sets the chunkType feature of the annotation to the passed in parameter chunkType. 
 * @author Philip
 * @see edu.mayo.bmi.uima.chunker.type
 */

public class PhraseTypeChunkCreator implements ChunkCreator {

	
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		
	}

	public Annotation createChunk(JCas jCas, int start, int end, String chunkType) {
		Chunk chunk;
		if(chunkType.equals("ADJP")) {
			chunk = new ADJP(jCas, start, end);
		} else if(chunkType.equals("ADVP")) {
			chunk = new ADVP(jCas, start, end);
		} else if(chunkType.equals("CONJP")) {
			chunk = new CONJP(jCas, start, end);
		} else if(chunkType.equals("INTJ")) {
			chunk = new INTJ(jCas, start, end);
		} else if(chunkType.equals("LST")) {
			chunk = new LST(jCas, start, end);
		} else if(chunkType.equals("NP")) {
			chunk = new NP(jCas, start, end);
		} else if(chunkType.equals("PP")) {
			chunk = new PP(jCas, start, end);
		} else if(chunkType.equals("PRT")) {
			chunk = new PRT(jCas, start, end);
		} else if(chunkType.equals("SBAR")) {
			chunk = new SBAR(jCas, start, end);
		} else if(chunkType.equals("UCP")) {
			chunk = new UCP(jCas, start, end);
		} else if(chunkType.equals("VP")) {
			chunk = new VP(jCas, start, end);
		} else if(chunkType.equals("O")) {
			chunk = new O(jCas, start, end);
		} else {
			chunk = new Chunk(jCas, start, end);
		}
				
		chunk.setChunkType(chunkType);
		chunk.addToIndexes();
		return chunk;
	}


}
