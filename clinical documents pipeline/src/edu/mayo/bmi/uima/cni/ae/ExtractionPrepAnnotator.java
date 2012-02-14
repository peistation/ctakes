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
package edu.mayo.bmi.uima.cni.ae;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.WordToken;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textspan.Segment;
import edu.mayo.bmi.uima.core.type.util.Pair;
import edu.mayo.bmi.uima.core.type.util.Pairs;

/**
 * UIMA annotator that prepares the CAS for output - performs
 * some (final) updates to the CAS
 * 
 * @author Mayo Clinic
 */
public class ExtractionPrepAnnotator extends JCasAnnotator_ImplBase {
	private String iv_annotVerPropKey;
	private int iv_annotVer;

	/**
	 * Method invoked by UIMA framework to initialize this annotator
	 */
	public void initialize(UimaContext aCtx)
			throws ResourceInitializationException {
		
		super.initialize(aCtx);

		try {
			iv_annotVer = ((Integer) aCtx.getConfigParameterValue("AnnotationVersion")).intValue();
			iv_annotVerPropKey = (String) aCtx.getConfigParameterValue("AnnotationVersionPropKey");
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	
	}

	/**
	 * Method invoked by UIMA framework to process a document
	 */
	public void process(JCas jcas)
			throws AnalysisEngineProcessException {
		generateUidValues(jcas);
		generateTokenNormForms(jcas);
		assignNamedEntityFeats(jcas);
		storeAnnotationVersion(jcas);
	}

	
	/**
	 * Stores annotation version as a property JCas object.
	 * 
	 * @param jcas
	 */
	private void storeAnnotationVersion(JCas jcas) {
		Iterator itr = jcas.getJFSIndexRepository().getAnnotationIndex(Pairs.type).iterator();
		if (itr == null || !itr.hasNext())
			return;

		Pairs props = (Pairs) itr.next(); 

		// create a new property array that is one item bigger
		FSArray propArr = props.getPairs();
		FSArray newPropArr = new FSArray(jcas, propArr.size() + 1);
		for (int i = 0; i < propArr.size(); i++) {
			newPropArr.set(i, propArr.get(i));
		}

		Pair annotVerProp = new Pair(jcas);    		
		annotVerProp.setAttribute(iv_annotVerPropKey);
		annotVerProp.setValue(String.valueOf(iv_annotVer));

		// add annotation version prop as last item in array
		newPropArr.set(newPropArr.size() - 1, annotVerProp);
		props.setPairs(newPropArr);
	}

	/**
	 * Generates UID values for all IdentifiedAnnotation objects.
	 * This is just a numeric identifier, assigned sequentially.
	 */
	private void generateUidValues(JCas jcas) {
		int uid = 0;
		Iterator itr = jcas.getJFSIndexRepository().getAnnotationIndex(
				IdentifiedAnnotation.type).iterator();
		while (itr.hasNext()) {
			IdentifiedAnnotation idAnnot = (IdentifiedAnnotation) itr.next();
			idAnnot.setId(uid);
			uid++;
		}
	}

	/**
	 * Generates normalized form for each token annotation.
	 * Considers whether it is a <code>WordToken</code> with a canonical form
	 */
	private void generateTokenNormForms(JCas jcas) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();

		// Determine and set the normalized form for each <code>BaseToken</code>
		Iterator btaItr = indexes.getAnnotationIndex(BaseToken.type).iterator();
		while (btaItr.hasNext()) {
			BaseToken bta = (BaseToken) btaItr.next();
			String normForm = null;
			if (!(bta instanceof WordToken)) {
				normForm = bta.getCoveredText();
			} else {
				WordToken wta = (WordToken) bta;
				String canonicalForm = wta.getCanonicalForm();


				// The norm form is the canonical form, if there is one
				// Otherwise the norm form is the token's text.
				if ((canonicalForm != null)	&& (canonicalForm.length() > 0)) {
					normForm = canonicalForm;
				} else {
					normForm = wta.getCoveredText();
				}
			}
			bta.setNormalizedForm(normForm);
		}
	}

	/**
	 * Assigns OID and segmentID values to NamedEntities
	 */
	private void assignNamedEntityFeats(JCas jcas) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		// Set keySet = new HashSet();
		// List dupList = new ArrayList();

		Set segmentSet = new HashSet();
		Iterator segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();
		while (segmentItr.hasNext()) {
			segmentSet.add(segmentItr.next());
		}

		// For each NE, assign segment ID and assign ontology concept OIDs if applicable
		Iterator neItr = indexes.getAnnotationIndex(IdentifiedAnnotation.type).iterator();
		while (neItr.hasNext()) {
			
			IdentifiedAnnotation neAnnot = (IdentifiedAnnotation) neItr.next();

			// assign segment ID
			Iterator segItr = segmentSet.iterator();
			while (segItr.hasNext()) {
				Segment seg = (Segment) segItr.next();
				// see if NE is inside this segment
				if ((neAnnot.getBegin() >= seg.getBegin())
						&& (neAnnot.getEnd() <= seg.getEnd())) {
					// found segment for this NE
					neAnnot.setSegmentID(seg.getId());
					break;
				}
			}

			// assign ontology concept OID values
			FSArray ocArr = neAnnot.getOntologyConceptArr();
			if (ocArr != null) {
				for (int i = 0; i < ocArr.size(); i++) {
					OntologyConcept oc = (OntologyConcept) ocArr.get(i);
					String code = oc.getCode();
					String scheme = oc.getCodingScheme();

					StringBuffer oid = new StringBuffer();
					oid.append(code);
					oid.append("#");
					oid.append(scheme);
					oc.setOid(oid.toString());
				}
			}
		}
	}
}