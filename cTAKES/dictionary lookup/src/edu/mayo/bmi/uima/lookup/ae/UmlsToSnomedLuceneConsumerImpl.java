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


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;

import edu.mayo.bmi.dictionary.DictionaryException;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.dictionary.lucene.LuceneDictionaryImpl;
import edu.mayo.bmi.uima.core.resource.FileLocator;

/**
 * Implementation that takes UMLS dictionary lookup hits and stores as NamedEntity 
 * objects only the ones that have a SNOMED synonym, by looking in a lucene index 
 * for SNOMED codes that map to the identified CUI.
 * 
 * @author Mayo Clinic
 */
public class UmlsToSnomedLuceneConsumerImpl extends UmlsToSnomedConsumerImpl implements
		LookupConsumer
{

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	//ohnlp-Bugs-3296301 limits the search results to fixed 100 records.
	// Added 'MaxListSize'
	private static int iv_maxListSize;
	private final String SNOMED_MAPPING_PRP_KEY = "snomedCodeMappingField";
	private final String CUI_MAPPING_PRP_KEY = "cuiMappingField";
	
	private LuceneDictionaryImpl snomedLikeCodesIndex;

	public UmlsToSnomedLuceneConsumerImpl(AnnotatorContext aCtx, Properties properties)
			throws Exception
	{
		this(aCtx,properties,Integer.MAX_VALUE);
	}

	// ohnlp Bugs tracker ID: 3390078 do not reload lucene index for each document, load in constructor
	public UmlsToSnomedLuceneConsumerImpl(AnnotatorContext aCtx, Properties properties, int maxListSize)
			throws Exception
	{
		super(aCtx,properties);
		iv_maxListSize = maxListSize;
		
		IndexReader indexReader;
		String indexPath = null;
		String indexDirAbsPath = null;
		try {
			// For the sample dictionary, we use the following lucene index.
			indexPath = "lookup/snomed-like_codes_sample";
			File indexDir = FileLocator.locateFile(indexPath); // TODO parameterize this
			indexDirAbsPath = indexDir.getAbsolutePath();
			
			indexReader = IndexReader.open(FSDirectory.open(indexDir)); 

			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			String lookupFieldName = props.getProperty(CUI_MAPPING_PRP_KEY);
			
			// We will lookup entries based on lookupFieldName
			snomedLikeCodesIndex = new LuceneDictionaryImpl(indexSearcher, lookupFieldName, iv_maxListSize);
			
			logger.info("Loaded Lucene index with "+ indexReader.numDocs() +" entries.");
	        
		} catch (IOException ioe) {
			
		    logger.info("Lucene index: " + indexDirAbsPath);
		    throw new DictionaryException(ioe);
		    
		}

	}


	/**
	 * Find all Snomed codes that map to the given UMLS code (CUI),
	 * by looking in a lucene index
	 * 
	 * @param umlsCode a UMLS CUI
	 * @return Set of Snomed codes that map to the given UMLS code (CUI).
	 * @see getSnomedCodes in <code>UmlsToSnomedConsumerImpl</code> for example of using a database
	 */
	protected Set getSnomedCodes(String umlsCode) throws DictionaryException
	{
		Set codeSet = new HashSet();
		
		String valueFieldName = props.getProperty(SNOMED_MAPPING_PRP_KEY);
		// Get the entries with field lookupFieldName having value umlsCode
		Collection<MetaDataHit> mdhCollection = snomedLikeCodesIndex.getEntries(umlsCode);
	        
		for (MetaDataHit mdh: mdhCollection) {
		    codeSet.add(mdh.getMetaFieldValue(valueFieldName));
		}
		
		return codeSet;

	}

}