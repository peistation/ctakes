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
package org.apache.ctakes.coreference.ae;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import org.apache.ctakes.coreference.util.AnnotationSelector;
import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.coreference.type.DemMarkable;
import org.apache.ctakes.coreference.type.NEMarkable;
import org.apache.ctakes.coreference.type.PronounMarkable;

public class MipacqMarkableCreator extends JCasAnnotator_ImplBase {

	public static int nextID = 0;
	HashSet<String> modalAdj;
	HashSet<String> cogved;
	HashSet<String> otherVerb;

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	public void initialize(UimaContext uc) throws ResourceInitializationException {
		super.initialize(uc);

		// Load modal adjectives and cognitive verbs for pleonastic patterns
		String[] ma = (String[]) uc.getConfigParameterValue("modalAdj");
		modalAdj = new HashSet<String>();
		for (String s : ma) modalAdj.add(s);
		String[] cv = (String[]) uc.getConfigParameterValue("cogved");
		cogved = new HashSet<String>();
		for (String s : cv) cogved.add(s);
		String[] ov = (String[]) uc.getConfigParameterValue("otherVerb");
		otherVerb = new HashSet<String>();
		for (String s : ov) otherVerb.add(s);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		ArrayList<Annotation> la = AnnotationSelector.selectNE(aJCas);
		for (int i = 0; i < la.size(); ++i) {
			Annotation a = la.get(i);
			NEMarkable m = new NEMarkable(aJCas, a.getBegin(), a.getEnd());
			m.setContent(a);
			m.setId(nextID + i);
			m.addToIndexes();
		}

		nextID += la.size();

		ArrayList<WordToken> lw = AnnotationSelector.selectPronoun(aJCas, modalAdj, cogved, otherVerb, logger);
		for (int i = 0; i < lw.size(); ++i) {
			WordToken t = lw.get(i);
			PronounMarkable m = new PronounMarkable(aJCas, t.getBegin(), t.getEnd());
			m.setContent(t);
			m.setId(nextID + i);
			m.addToIndexes();
		}

		nextID += lw.size();

		ArrayList<Chunk> lc = AnnotationSelector.selectDemonAndRelative(aJCas);
		for (int i = 0; i < lc.size(); ++i) {
			Chunk c = lc.get(i);
			DemMarkable m = new DemMarkable(aJCas, c.getBegin(), c.getEnd());
			m.setContent(c);
			m.setId(nextID + i);
			m.addToIndexes();
		}

		nextID += lc.size();
	}

}
