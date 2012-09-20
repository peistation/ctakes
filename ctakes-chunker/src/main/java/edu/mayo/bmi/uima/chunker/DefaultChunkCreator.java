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
package edu.mayo.bmi.uima.chunker;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import org.apache.ctakes.typesystem.type.syntax.Chunk;

/**
 * This chunker creator simply creates annotations of type edu.mayo.bmi.chunker.type.Chunk and
 * sets the chunkType feature of the annotation to the passed in parameter chunkType.  
 * @author Philip
 * @see org.apache.ctakes.typesystem.type.Chunk
 */
public class DefaultChunkCreator implements ChunkCreator {

	public void initialize(UimaContext annotatorContext) throws ResourceInitializationException {

	}

	public Annotation createChunk(JCas jCas, int start, int end, String chunkType) {
		Chunk chunk = new Chunk(jCas, start, end);
		chunk.setChunkType(chunkType);
		chunk.addToIndexes();
		return chunk;
	}

}
