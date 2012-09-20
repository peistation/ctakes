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
package edu.mayo.bmi.uima.lookup.ae;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.ctakes.typesystem.type.constants.CONST;

/**
 * @author Mayo Clinic
 */
public class NamedEntityLookupConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer
{

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";

	private final String TYPE_ID_FIELD = "typeIdField";
	
	private Properties iv_props;
	
	private static int iv_maxSize;

	public NamedEntityLookupConsumerImpl(UimaContext aCtx, Properties props, int maxListSize)
	{
		// TODO property validation could be done here
		iv_props = props;
		iv_maxSize = maxListSize;
	}
	public NamedEntityLookupConsumerImpl(UimaContext aCtx, Properties props)
	{
		// TODO property validation could be done here
		iv_props = props;
	}

	private int countUniqueCodes(Collection hitsAtOffset) {
		Iterator lhAtOffsetItr = hitsAtOffset.iterator();
		Set<String> codes = new HashSet<String>();
		while (lhAtOffsetItr.hasNext())
		{
			LookupHit lh = (LookupHit) lhAtOffsetItr.next();

			MetaDataHit mdh = lh.getDictMetaDataHit();

			String code = mdh.getMetaFieldValue(iv_props.getProperty(CODE_MF_PRP_KEY));
			if (codes.contains(code)) {
				// don't create a second entry in the array for a code already seen, including null 
			} else {
				
				codes.add(code);

			}
		}
		
		return codes.size();
	}

	
	public void consumeHits(JCas jcas, Iterator lhItr)
			throws AnalysisEngineProcessException
	{

		String typeId = null;
		Iterator hitsByOffsetItr = organizeByOffset(lhItr);
		while (hitsByOffsetItr.hasNext())
		{
			Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();

			
			FSArray ocArr = new FSArray(jcas, countUniqueCodes(hitsAtOffsetCol));
			
			int ocArrIdx = 0;

			// iterate over the LookupHit objects and create
			// a corresponding JCas OntologyConcept object that will
			// be placed in a FSArray
			Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
			int neBegin = -1;
			int neEnd = -1;
			Set<String> codes = new HashSet<String>();
			while (lhAtOffsetItr.hasNext())
			{
				LookupHit lh = (LookupHit) lhAtOffsetItr.next();
				neBegin = lh.getStartOffset();
				neEnd = lh.getEndOffset();

				MetaDataHit mdh = lh.getDictMetaDataHit();

				String code = mdh.getMetaFieldValue(iv_props.getProperty(CODE_MF_PRP_KEY));
				if (codes.contains(code)) {
					// don't create a second entry in the array for a code already seen, including null 
				} else {
					
					OntologyConcept oc = new OntologyConcept(jcas);
					oc.setCode(code);
					oc.setCodingScheme(iv_props.getProperty(CODING_SCHEME_PRP_KEY));

					if(iv_props.getProperty(TYPE_ID_FIELD) != null) {
						typeId = iv_props.getProperty(TYPE_ID_FIELD);//mdh.getMetaFieldValue(iv_props.getProperty(TYPE_ID_FIELD));
					}
					
					ocArr.set(ocArrIdx, oc);
					ocArrIdx++;

					codes.add(code);

				}
			}

			int tid=CONST.NE_TYPE_ID_UNKNOWN;
			if(typeId != null){
				try { 
					tid = Integer.parseInt(typeId);
				} catch ( NumberFormatException nfe ) {
					tid = CONST.NE_TYPE_ID_UNKNOWN;
				}
				
			}

			IdentifiedAnnotation neAnnot;
			if (tid == CONST.NE_TYPE_ID_DRUG || tid == CONST.NE_TYPE_ID_UNKNOWN) {
				neAnnot = new MedicationEventMention(jcas);	
			} else {
				neAnnot = new EntityMention(jcas);	
			
			}
			
			neAnnot.setBegin(neBegin);
			neAnnot.setEnd(neEnd);
			neAnnot.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_DICT_LOOKUP);
			neAnnot.setOntologyConceptArr(ocArr);
			neAnnot.setTypeID(tid);
			neAnnot.addToIndexes();			
		}
	}
}