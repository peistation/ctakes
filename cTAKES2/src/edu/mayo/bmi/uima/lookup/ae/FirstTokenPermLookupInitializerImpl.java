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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.dictionary.DictionaryEngine;
import edu.mayo.bmi.lookup.algorithms.FirstTokenPermutationImpl;
import edu.mayo.bmi.lookup.algorithms.LookupAlgorithm;
import edu.mayo.bmi.lookup.phrasebuilder.PhraseBuilder;
import edu.mayo.bmi.lookup.phrasebuilder.VariantPhraseBuilderImpl;
import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.ContractionToken;
import edu.mayo.bmi.uima.core.type.NewlineToken;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.SymbolToken;
import edu.mayo.bmi.uima.core.type.WordToken;
import edu.mayo.bmi.uima.core.util.JCasUtil;
import edu.mayo.bmi.lookup.vo.LookupToken;

/**
 * @author Mayo Clinic
 */
public class FirstTokenPermLookupInitializerImpl implements LookupInitializer
{
	// LOG4J logger based on class name
	private Logger iv_logger = Logger.getLogger(getClass().getName());

	// properties for firstWordPermutation algorithm
	private final String TEXT_MFS_PRP_KEY = "textMetaFields";
	private final String MAX_P_LEVEL_PRP_KEY = "maxPermutationLevel";
	private final String WINDOW_ANNOT_PRP_KEY = "windowAnnotations";
	private final String EXC_TAGS_PRP_KEY = "exclusionTags"; // optional

	private final String CANONICAL_VARIANT_ATTR = "canonicalATTR";

	Properties iv_props;

	// array of JCas window annotation type values
	private int[] iv_annotTypeArr;

	// set of exclusion POS tags (lower cased)
	private Set iv_exclusionTagSet = null;

	public FirstTokenPermLookupInitializerImpl(AnnotatorContext aCtx,
			Properties props) throws ClassNotFoundException,
			IllegalAccessException, NoSuchFieldException
	{
		// TODO property validation could be done here
		iv_props = props;

		// optional context window annotations
		String windowAnnots = iv_props.getProperty(WINDOW_ANNOT_PRP_KEY);
		if (windowAnnots != null)
		{
			String[] windowAnnotArr = windowAnnots.split("\\|");
			iv_annotTypeArr = new int[windowAnnotArr.length];
			for (int i = 0; i < windowAnnotArr.length; i++)
			{
				iv_annotTypeArr[i] = JCasUtil.getType(windowAnnotArr[i]);
			}
		}

		// optional exclusion POS tags
		String tagStr = iv_props.getProperty(EXC_TAGS_PRP_KEY);
		if (tagStr != null)
		{
			iv_exclusionTagSet = new HashSet();
			String[] tagArr = tagStr.split(",");
			for (int i = 0; i < tagArr.length; i++)
			{
				iv_exclusionTagSet.add(tagArr[i].toLowerCase());
			}
			iv_logger.info("Exclusion tagset loaded: " + iv_exclusionTagSet);
		}
	}

	public LookupAlgorithm getLookupAlgorithm(DictionaryEngine dictEngine)
			throws AnnotatorInitializationException
	{
		// variant support
		String[] variantArr = { CANONICAL_VARIANT_ATTR };
		PhraseBuilder pb = new VariantPhraseBuilderImpl(variantArr, true);

		String textMetaFields = iv_props.getProperty(TEXT_MFS_PRP_KEY);
		String[] textMetaFieldNameArr;
		if(textMetaFields == null) 
			textMetaFieldNameArr = new String[]{};
		else
			textMetaFieldNameArr = textMetaFields.split("\\|");
        
		
		int maxPermutationLevel = Integer.parseInt(iv_props.getProperty(MAX_P_LEVEL_PRP_KEY));

		return new FirstTokenPermutationImpl(dictEngine,
				pb,
				textMetaFieldNameArr,
				maxPermutationLevel);
	}

	private boolean isTagExcluded(String tag)
	{
		if ((iv_exclusionTagSet == null) || (tag == null))
		{
			return false;
		}

		return iv_exclusionTagSet.contains(tag.toLowerCase());
	}

	public Iterator getLookupTokenIterator(JCas jcas)
			throws AnnotatorInitializationException
	{
		List ltList = new ArrayList();

		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator btaItr = indexes.getAnnotationIndex(BaseToken.type).iterator();
		while (btaItr.hasNext())
		{
			BaseToken bta = (BaseToken) btaItr.next();
			if (!((bta instanceof NewlineToken)
					|| (bta instanceof PunctuationToken)
					|| (bta instanceof ContractionToken)
					|| (bta instanceof SymbolToken)))
			{
				LookupToken lt = new LookupAnnotationToJCasAdapter(bta);

				// POS exclusion logic for first word lookup
				if (isTagExcluded(bta.getPartOfSpeech()))
				{
					lt.addStringAttribute(
							FirstTokenPermutationImpl.LT_KEY_USE_FOR_LOOKUP,
							"false");
				}
				else
				{
					lt.addStringAttribute(
							FirstTokenPermutationImpl.LT_KEY_USE_FOR_LOOKUP,
							"true");
				}

				if (bta instanceof WordToken)
				{
					WordToken wta = (WordToken) bta;
					String canonicalForm = wta.getCanonicalForm();
					if (canonicalForm != null)
					{
						lt.addStringAttribute(CANONICAL_VARIANT_ATTR, canonicalForm);
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
		try
		{
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
			String objClassName = iv_props.getProperty(WINDOW_ANNOT_PRP_KEY);
			int windowType = JCasUtil.getType(objClassName);
			return indexes.getAnnotationIndex(windowType).iterator();
		}
		catch (Exception e)
		{
			throw new AnnotatorInitializationException(e);
		}
	}

	public Map getContextMap(JCas jcas, int windowBegin, int windowEnd)
			throws AnnotatorInitializationException
	{
		if (iv_annotTypeArr != null)
		{
			List list = new ArrayList();

			// algorithm depends on a window for permutations
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
			for (int i = 0; i < iv_annotTypeArr.length; i++)
			{
				Iterator itr = indexes.getAnnotationIndex(iv_annotTypeArr[i])
						.iterator();
				list.addAll(constrainToWindow(windowBegin, windowEnd, itr));
			}

			Map m = new HashMap();
			m.put(FirstTokenPermutationImpl.CTX_KEY_WINDOW_ANNOTATIONS, list);
			return m;
		}
		else
		{
			return new HashMap();
		}
	}

	/**
	 * Gets a list of LookupAnnotation objects within the specified window.
	 * 
	 * @param annotItr
	 * @return
	 */
	private List constrainToWindow(int begin, int end, Iterator annotItr)
	{
		List list = new ArrayList();

		while (annotItr.hasNext())
		{
			Annotation annot = (Annotation) annotItr.next();

			// only consider if it's within the window
			if ((annot.getBegin() >= begin) && (annot.getEnd() <= end))
			{
				list.add(new LookupAnnotationToJCasAdapter(annot));
			}
		}
		return list;
	}
}