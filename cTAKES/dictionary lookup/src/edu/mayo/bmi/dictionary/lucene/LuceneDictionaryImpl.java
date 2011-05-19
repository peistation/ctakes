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
package edu.mayo.bmi.dictionary.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import edu.mayo.bmi.dictionary.BaseDictionaryImpl;
import edu.mayo.bmi.dictionary.Dictionary;
import edu.mayo.bmi.dictionary.DictionaryException;
import edu.mayo.bmi.dictionary.MetaDataHit;

/**
 *
 * @author Mayo Clinic
 */
public class LuceneDictionaryImpl extends BaseDictionaryImpl implements Dictionary
{
    private Searcher iv_searcher;
    private String iv_lookupFieldName;
	//ohnlp-Bugs-3296301 limits the search results to fixed 100 records.
	private int iv_maxHits;
	// LOG4J logger based on class name
	private Logger iv_logger = Logger.getLogger(getClass().getName());
    /**
     * 
     * Constructor
     *
     */
    public LuceneDictionaryImpl(Searcher searcher, String lookupFieldName, int maxListHits)
    {
        iv_searcher = searcher;
        iv_lookupFieldName = lookupFieldName;
     // Added 'maxListHits'
        iv_maxHits = maxListHits;
        // TODO Only take perfect matches?
    }

    public Collection getEntries(String str) throws DictionaryException
    {
    	Set metaDataHitSet = new HashSet();

    	try
    	{
    		Query q = new TermQuery(new Term(iv_lookupFieldName, str));
    		TopDocs topDoc = iv_searcher.search(q, iv_maxHits);
    		ScoreDoc[] hits = topDoc.scoreDocs;
    		if (hits.length == iv_maxHits) {
    			iv_logger.warn("'iv_maxHits' equals the list length returned by the lucene query (" + hits.length+").");
    			iv_logger.warn("You may want to consider setting a higher value, since there may be more entries not being returned in the event greater than " +iv_maxHits +" exist.");
    		}
    		for (int i = 0; i < hits.length; i++) {
    			int docId = hits[i].doc;
    			Document luceneDoc = iv_searcher.doc(docId);
    			MetaDataHit mdh = new LuceneDocumentMetaDataHitImpl(luceneDoc);
    			metaDataHitSet.add(mdh);
    		}

    		return metaDataHitSet;
    	}
    	catch (IOException ioe)
    	{
    		throw new DictionaryException(ioe);
    	}
    }

    public boolean contains(String str) throws DictionaryException
    {
        try
        {
            Query q = new TermQuery(new Term(iv_lookupFieldName, str));

            TopDocs topDoc = iv_searcher.search(q, iv_maxHits);
            ScoreDoc[] hits = topDoc.scoreDocs;
            if ((hits != null) && (hits.length > 0))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (IOException ioe)
        {
            throw new DictionaryException(ioe);
        }

    }
}
