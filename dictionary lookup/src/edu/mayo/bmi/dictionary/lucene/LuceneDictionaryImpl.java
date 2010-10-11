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
	private int maxHits = 100;
    /**
     * 
     * Constructor
     *
     */
    public LuceneDictionaryImpl(Searcher searcher, String lookupFieldName)
    {
        iv_searcher = searcher;
        iv_lookupFieldName = lookupFieldName;

        // TODO Only take perfect matches?
    }

    public Collection getEntries(String str) throws DictionaryException
    {
    	Set metaDataHitSet = new HashSet();

    	try
    	{
    		Query q = new TermQuery(new Term(iv_lookupFieldName, str));
    		TopDocs topDoc = iv_searcher.search(q, maxHits);
    		ScoreDoc[] hits = topDoc.scoreDocs;
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

            TopDocs topDoc = iv_searcher.search(q, maxHits);
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
