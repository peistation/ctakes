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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.context.type.ContextAnnotation;


public abstract class NamedEntityContextHitConsumer implements ContextHitConsumer {
	public abstract void consumeHit(JCas jcas, Annotation focusAnnot, int scope, ContextHit ctxHit)
			throws AnalysisEngineProcessException;

	protected ContextAnnotation createContextAnnot(JCas jcas, Annotation focusAnnot, int scope, ContextHit ctxHit) {
		ContextAnnotation ctxAnnot = new ContextAnnotation(jcas);
		ctxAnnot.setBegin(ctxHit.getStartOffset());
		ctxAnnot.setEnd(ctxHit.getEndOffset());
		if (scope == ContextAnnotator.LEFT_SCOPE) {
			ctxAnnot.setScope("LEFT");
		} else if (scope == ContextAnnotator.MIDDLE_SCOPE) {
			ctxAnnot.setScope("MIDDLE");
		} else if (scope == ContextAnnotator.RIGHT_SCOPE) {
			ctxAnnot.setScope("RIGHT");
		}
		ctxAnnot.setFocusText(focusAnnot.getCoveredText());
		return ctxAnnot;
	}

}
