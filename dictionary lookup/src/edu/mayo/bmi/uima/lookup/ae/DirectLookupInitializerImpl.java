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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.dictionary.DictionaryEngine;
import edu.mayo.bmi.lookup.algorithms.DirectPassThroughImpl;
import edu.mayo.bmi.lookup.algorithms.LookupAlgorithm;
import edu.mayo.bmi.lookup.phrasebuilder.PhraseBuilder;
import edu.mayo.bmi.lookup.phrasebuilder.VariantPhraseBuilderImpl;
import edu.mayo.bmi.lookup.vo.LookupToken;
import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.ContractionToken;
import edu.mayo.bmi.uima.core.type.NewlineToken;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.SymbolToken;
import edu.mayo.bmi.uima.core.type.WordToken;

/**
 * @author Mayo Clinic
 */
public class DirectLookupInitializerImpl implements LookupInitializer
{
	private final String CANONICAL_VARIANT_ATTR = "canonicalATTR";

	public DirectLookupInitializerImpl(AnnotatorContext aCtx, Properties props)
	{
		// TODO property validation could be done here
	}

	public LookupAlgorithm getLookupAlgorithm(DictionaryEngine dictEngine)
			throws AnnotatorInitializationException
	{
		// variant support
		String[] variantArr = { CANONICAL_VARIANT_ATTR };
		PhraseBuilder pb = new VariantPhraseBuilderImpl(variantArr, true);

		return new DirectPassThroughImpl(dictEngine, pb);
	}

	public Iterator getLookupTokenIterator(JCas jcas)
			throws AnnotatorInitializationException
	{
		List ltList = new ArrayList();

		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator btaItr = indexes.getAnnotationIndex(BaseToken.type)
				.iterator();
		while (btaItr.hasNext())
		{
			BaseToken bta = (BaseToken) btaItr.next();
			if (!((bta instanceof NewlineToken)
					|| (bta instanceof PunctuationToken)
					|| (bta instanceof ContractionToken)
					|| (bta instanceof SymbolToken)))
			{
				LookupToken lt = new LookupAnnotationToJCasAdapter(bta);

				if (bta instanceof WordToken)
				{
					WordToken wta = (WordToken) bta;
					String canonicalForm = wta.getCanonicalForm();
					if (canonicalForm != null)
					{
						lt.addStringAttribute(
								CANONICAL_VARIANT_ATTR,
								canonicalForm);
					}
				}

				ltList.add(lt);
			}
		}
		return ltList.iterator();
	}

	public Iterator getLookupWindowIterator(JCas jcas)
			throws AnnotatorInitializationException
	{
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		return indexes.getAnnotationIndex(Sentence.type).iterator();
	}

	public Map getContextMap(JCas jcas, int windowBegin, int windowEnd)
	{
		// not used for direct pass through algorithm, return empty map
		return new HashMap();
	}
}