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

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.OntologyConcept;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;

/**
 * @author Mayo Clinic
 */
public class NamedEntityLookupConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer
{

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";

	private Properties iv_props;

	public NamedEntityLookupConsumerImpl(AnnotatorContext aCtx, Properties props)
	{
		// TODO property validation could be done here
		iv_props = props;
	}

	public void consumeHits(JCas jcas, Iterator lhItr)
			throws AnnotatorProcessException
	{
		Iterator hitsByOffsetItr = organizeByOffset(lhItr);
		while (hitsByOffsetItr.hasNext())
		{
			Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();

			FSArray ocArr = new FSArray(jcas, hitsAtOffsetCol.size());
			int ocArrIdx = 0;

			// iterate over the LookupHit objects and create
			// a corresponding JCas OntologyConcept object that will
			// be placed in a FSArray
			Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
			int neBegin = -1;
			int neEnd = -1;
			while (lhAtOffsetItr.hasNext())
			{
				LookupHit lh = (LookupHit) lhAtOffsetItr.next();
				neBegin = lh.getStartOffset();
				neEnd = lh.getEndOffset();

				MetaDataHit mdh = lh.getDictMetaDataHit();

				OntologyConcept oc = new OntologyConcept(jcas);
				oc.setCode(mdh.getMetaFieldValue(iv_props.getProperty(CODE_MF_PRP_KEY)));
				oc.setCodingScheme(iv_props.getProperty(CODING_SCHEME_PRP_KEY));

				ocArr.set(ocArrIdx, oc);
				ocArrIdx++;
			}

			NamedEntity neAnnot = new NamedEntity(jcas);
			neAnnot.setBegin(neBegin);
			neAnnot.setEnd(neEnd);
			neAnnot.setDiscoveryTechnique(TypeSystemConst.NE_DISCOVERY_TECH_DICT_LOOKUP);
			neAnnot.setOntologyConceptArr(ocArr);
			neAnnot.addToIndexes();
		}
	}
}