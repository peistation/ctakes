/*
 * Copyright: (c) 2012   Mayo Foundation for Medical Education and 
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
package edu.mayo.bmi.uima.attributes;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.attributes.generic.GenericAttributeClassifier;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * <br>
* This class provides a basic generic attribute extraction technique based on dependency parses.
 * <p>
 * 
 * 
 */

public class GenericAttributeAnalysisEngine extends JCasAnnotator_ImplBase {

	public Logger logger = Logger.getLogger(getClass().getName());
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (IdentifiedAnnotation mention : JCasUtil.select(jCas, IdentifiedAnnotation.class)) {

				Boolean oldgeneric = mention.getGeneric();
				mention.setGeneric( GenericAttributeClassifier.getGeneric(jCas, mention) );
//				System.err.println("Word:"+mention.getCoveredText()+", overwrote generic "+oldgeneric+" with "+mention.getGeneric());
		}
		
		
	}
}
