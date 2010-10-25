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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.resource.LuceneIndexReaderResource;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.OntologyConcept;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;

/**
 * Implementation that takes Rxnorm dictionary lookup hits and stores only the
 * ones that are also present in the Orange Book.
 * 
 * @author Mayo Clinic
 */
public class OrangeBookFilterConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer
{
	// LOG4J logger based on class name
	private Logger iv_logger = Logger.getLogger(getClass().getName());

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";

	private final String LUCENE_FILTER_RESRC_KEY_PRP_KEY = "luceneFilterExtResrcKey";

	private Properties iv_props;

	private Searcher iv_searcher;

	public OrangeBookFilterConsumerImpl(AnnotatorContext aCtx, Properties props)
			throws Exception
	{
		// TODO property validation could be done here
		iv_props = props;

		String resrcName = iv_props.getProperty(LUCENE_FILTER_RESRC_KEY_PRP_KEY);
		LuceneIndexReaderResource resrc = (LuceneIndexReaderResource) aCtx.getResourceObject(resrcName);
		iv_searcher = new IndexSearcher(resrc.getIndexReader());
	}

	public void consumeHits(JCas jcas, Iterator lhItr)
			throws AnnotatorProcessException
	{
		Iterator hitsByOffsetItr = organizeByOffset(lhItr);
		while (hitsByOffsetItr.hasNext())
		{
			Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();

			// iterate over the LookupHit objects
			// code is only valid if the covered text is also present in the
			// filter
			Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
			int neBegin = -1;
			int neEnd = -1;
			Collection validCodeCol = new HashSet();
			while (lhAtOffsetItr.hasNext())
			{
				LookupHit lh = (LookupHit) lhAtOffsetItr.next();
				neBegin = lh.getStartOffset();
				neEnd = lh.getEndOffset();

				String text = jcas.getDocumentText().substring(
						lh.getStartOffset(),
						lh.getEndOffset());
				text = text.trim().toLowerCase();

				MetaDataHit mdh = lh.getDictMetaDataHit();
				String code = mdh.getMetaFieldValue(iv_props.getProperty(CODE_MF_PRP_KEY));

				if (isValid("trade_name", text) || isValid("ingredient", text))
				{
					validCodeCol.add(code);
				}
				else
				{
					iv_logger.warn("Filtered out: "+text);
				}
			}

			if (validCodeCol.size() > 0)
			{
				FSArray ocArr = createOntologyConceptArr(jcas, validCodeCol);
				NamedEntity neAnnot = new NamedEntity(jcas);
				neAnnot.setTypeID(TypeSystemConst.NE_TYPE_ID_DRUG);
				neAnnot.setBegin(neBegin);
				neAnnot.setEnd(neEnd);
				neAnnot.setDiscoveryTechnique(TypeSystemConst.NE_DISCOVERY_TECH_DICT_LOOKUP);
				neAnnot.setOntologyConceptArr(ocArr);
				neAnnot.addToIndexes();
			}
		}
	}

	/**
	 * For each valid code, a corresponding JCas OntologyConcept object is
	 * created and stored in a FSArray.
	 * 
	 * @param jcas
	 * @param validCodeCol
	 * @return
	 */
	private FSArray createOntologyConceptArr(JCas jcas, Collection validCodeCol)
	{
		FSArray ocArr = new FSArray(jcas, validCodeCol.size());
		int ocArrIdx = 0;
		Iterator validCodeItr = validCodeCol.iterator();
		while (validCodeItr.hasNext())
		{
			String validCode = (String) validCodeItr.next();
			OntologyConcept oc = new OntologyConcept(jcas);
			oc.setCode(validCode);
			oc.setCodingScheme(iv_props.getProperty(CODING_SCHEME_PRP_KEY));

			ocArr.set(ocArrIdx, oc);
			ocArrIdx++;
		}
		return ocArr;
	}

	private boolean isValid(String fieldName, String str)
			throws AnnotatorProcessException
	{
		try
		{
			Query q = new TermQuery(new Term(fieldName, str));
			Hits hits = iv_searcher.search(q);

			if (hits != null)
			{
				for (int i = 0; i < hits.length(); i++)
				{
					if (hits.score(i) == 1)
					{
						return true;
					}
				}
			}
			return false;
		}
		catch (Exception e)
		{
			throw new AnnotatorProcessException(e);
		}
	}
}