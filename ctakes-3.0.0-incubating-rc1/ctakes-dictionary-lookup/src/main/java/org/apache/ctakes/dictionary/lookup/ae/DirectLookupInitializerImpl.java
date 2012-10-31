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
package org.apache.ctakes.dictionary.lookup.ae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;


import org.apache.ctakes.dictionary.lookup.DictionaryEngine;
import org.apache.ctakes.dictionary.lookup.algorithms.DirectPassThroughImpl;
import org.apache.ctakes.dictionary.lookup.algorithms.LookupAlgorithm;
import org.apache.ctakes.dictionary.lookup.phrasebuilder.PhraseBuilder;
import org.apache.ctakes.dictionary.lookup.phrasebuilder.VariantPhraseBuilderImpl;
import org.apache.ctakes.dictionary.lookup.vo.LookupToken;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ContractionToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;

/**
 * @author Mayo Clinic
 */
public class DirectLookupInitializerImpl implements LookupInitializer
{
	private final String CANONICAL_VARIANT_ATTR = "canonicalATTR";

	public DirectLookupInitializerImpl(UimaContext aCtx, Properties props)
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