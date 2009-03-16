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
package edu.mayo.bmi.uima.context;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import edu.mayo.bmi.uima.core.test.TestUtil;
import edu.mayo.bmi.uima.core.type.NamedEntity;

public class NegationAnnotatorTests {

	static String unexpectedParamValueMsg = "unexpected parameter value for descriptor file test/desc/NegationAnnotator.xml for param: ";

	@Test
	public void testNegationExamples() throws ResourceInitializationException, AnalysisEngineProcessException {

		String descriptor = "test/desc/NegationAnnotator.xml";
		AnalysisEngine contextAE = TestUtil.getAE(new File(descriptor));
		UimaContext uimaContext = contextAE.getUimaContext();
		ContextAnnotator negationAnnotator = new ContextAnnotator();
		negationAnnotator.initialize(uimaContext);

		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.MAX_LEFT_SCOPE_SIZE_PARAM, new Integer(10));
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.MAX_RIGHT_SCOPE_SIZE_PARAM, new Integer(10));
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.SCOPE_ORDER_PARAM, "LEFT", 0);
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.SCOPE_ORDER_PARAM, "RIGHT", 1);
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.CONTEXT_ANALYZER_CLASS_PARAM, "edu.mayo.bmi.uima.context.negation.NegationContextAnalyzer");
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.CONTEXT_HIT_CONSUMER_CLASS_PARAM, "edu.mayo.bmi.uima.context.negation.NegationContextHitConsumer");
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.WINDOW_ANNOTATION_CLASS_PARAM, "edu.mayo.bmi.uima.core.type.Sentence");
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.FOCUS_ANNOTATION_CLASS_PARAM, "edu.mayo.bmi.uima.core.type.NamedEntity");
		TestUtil.testConfigParam(uimaContext, descriptor, ContextAnnotator.CONTEXT_ANNOTATION_CLASS_PARAM, "edu.mayo.bmi.uima.core.type.BaseToken");

		AnalysisEngine segmentTokenSentenceAE = TestUtil.getAE(new File("test/desc/SegmentTokenSentenceAggregate.xml"));

		String text;
		JCas jCas;

		text = "No evidence of ankle edema is noted.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		NamedEntity ne1 = addNE(jCas, 21, 26, "edema");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "evidence of ankle edema is noted.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 18, 23, "edema");
		negationAnnotator.process(jCas);
		assertEquals(0, ne1.getCertainty());

		text = "He denies gastric reflux symptoms, and he denies any symptoms referable to his to his genitourinary system.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 18, 24, "reflux");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "He has no history of cardiac-related problems and no family history of coronary artery disease.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 71, 94, "coronary artery disease");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "He is a lifetime nonsmoker, reports he walks for 30-minutes 4-5 times a week and has no history of hypoglycemia.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 99, 111, "hypoglycemia");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "Electrocardiogram was negative for ischemia, PVC's were less than 5 per minute.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 35, 43, "ischemia");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "He does not have diabetes.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 17, 25, "diabetes");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "She denies any loss of vision, double-vision, dysarthria, or dysphagia.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 15, 29, "loss of vision");
		NamedEntity ne2 = addNE(jCas, 31, 44, "double-vision");
		NamedEntity ne3 = addNE(jCas, 46, 56, "dysarthria");
		NamedEntity ne4 = addNE(jCas, 61, 70, "dysphagia");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());
		assertEquals(-1, ne2.getCertainty());
		assertEquals(-1, ne3.getCertainty());
		assertEquals(-1, ne4.getCertainty());
		
		text = "Her pain problem began gradually in June of 2000 without prior incident, injury, or illness.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 73, 79, "injury");
		ne2 = addNE(jCas, 84, 91, "illness");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());
		assertEquals(-1, ne2.getCertainty());

		text = "I found no evidence for cancer in this examination.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 24, 30, "cancer");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

		text = "Careful examination of the head, face, and neck reveal no skin cancers.";
		jCas = TestUtil.processAE(segmentTokenSentenceAE, text);
		ne1 = addNE(jCas, 58, 70, "skin cancers");
		negationAnnotator.process(jCas);
		assertEquals(-1, ne1.getCertainty());

	}

	private NamedEntity addNE(JCas jCas, int neBegin, int neEnd, String neText) throws ResourceInitializationException, AnalysisEngineProcessException {
		NamedEntity namedEntity = new NamedEntity(jCas, neBegin, neEnd);
		namedEntity.addToIndexes();
		assertEquals(neText, namedEntity.getCoveredText());
		return namedEntity;
	}

}
