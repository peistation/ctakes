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
package edu.mayo.bmi.uima.core.ae.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import edu.mayo.bmi.uima.core.test.TestUtil;
import edu.mayo.bmi.uima.core.type.textspan.Segment;

public class SimpleSegmentAnnotatorTests {

	@Test
    public void testSimpleSegment() throws ResourceInitializationException {
		AnalysisEngine ae = TestUtil.getAE(new File("desc/test/analysis_engine/SimpleSegmentAnnotator.xml"));
		JCas jCas = TestUtil.processAE(ae, "src/test/resources/data/test/mother_goose/OneMistyMoistyMorning.txt");
		Segment segment = TestUtil.getFeatureStructureAtIndex(jCas, Segment.class, 0);
		assertEquals(0, segment.getBegin());
		assertEquals(67, segment.getEnd());
		assertEquals("seg1234", segment.getId());
	}
}
