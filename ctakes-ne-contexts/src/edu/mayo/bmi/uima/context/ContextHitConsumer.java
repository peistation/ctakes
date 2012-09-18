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
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * A context hit consumer is called by the context annotator when a context hit
 * is returned by its context analyzer.
 */

public interface ContextHitConsumer {
	/**
	 * Implementation determines how context data is stored to the CAS.
	 * 
	 * @param jcas
	 *            the UIMA view to update.
	 * @param focusAnnotation
	 *            The focus annotation.
	 * @param scope
	 *            Scope of the context hit.
	 * @param contextHit
	 *            The context hit.
	 * @throws AnnotatorProcessException
	 */
	public void consumeHit(JCas jcas, Annotation focusAnnotation, int scope, ContextHit contextHit)
			throws AnalysisEngineProcessException;
}