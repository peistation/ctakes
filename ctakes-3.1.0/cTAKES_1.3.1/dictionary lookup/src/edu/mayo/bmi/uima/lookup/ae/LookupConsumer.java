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
package edu.mayo.bmi.uima.lookup.ae;

import java.util.Iterator;

import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.JCas;

/**
 * Defines how to consume the lookup hits produced by the LookupAnnotator.
 * 
 * NOTE: Constructor for implementation will be passed 2 arguments
 * 1.) AnnotatorContext for accessing external resources
 * 2.) Properties object for customization purposes.
 * 
 * @author Mayo Clinic
 */
public interface LookupConsumer
{
	/**
	 * Consumes the hits produced by the LookupAnnotator. This typically means
	 * iterating over the hits and storing what's necessary to the JCas
	 * @param jcas
	 *            CAS for storing data
	 * @param lookupHitItr
	 *            Iterator over LookupHit objects. These objects contain data
	 *            about the annotation span plus any associated metadata.
	 * @throws AnnotatorProcessException
	 */
	public void consumeHits(JCas jcas, Iterator lookupHitItr)
			throws AnnotatorProcessException;
}
