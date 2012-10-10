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
package org.apache.ctakes.core.ae;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import org.apache.ctakes.core.TestUtil;
import org.apache.ctakes.typesystem.type.textspan.Segment;

public class SimpleSegmentAnnotatorTests {

	@Test
    public void testSimpleSegment() throws ResourceInitializationException {
		AnalysisEngine ae = TestUtil.getAE(new File("desc/test/analysis_engine/SimpleSegmentAnnotator.xml"));
		JCas jCas = TestUtil.processAE(ae, "data/test/mother_goose/OneMistyMorning.txt");
		Segment segment = TestUtil.getFeatureStructureAtIndex(jCas, Segment.class, 0);
		assertEquals(0, segment.getBegin());
		assertEquals(42, segment.getEnd());
		assertEquals("seg1234", segment.getId());
	}
}
