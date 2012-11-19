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
package org.apache.ctakes.dictionary.lookup.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.ctakes.dictionary.lookup.BaseDictionaryImpl;
import org.apache.ctakes.dictionary.lookup.Dictionary;
import org.apache.ctakes.dictionary.lookup.DictionaryException;
import org.apache.ctakes.dictionary.lookup.MetaDataHit;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;


/**
 *
 * @author Mayo Clinic
 */
public class LuceneDictionaryImpl extends BaseDictionaryImpl implements Dictionary
{
    private IndexSearcher iv_searcher;
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
    public LuceneDictionaryImpl(IndexSearcher searcher, String lookupFieldName)
    {
	this(searcher, lookupFieldName, Integer.MAX_VALUE);

	// TODO Only take perfect matches?
    }
    
    /**
     * 
     * Constructor
     *
     */
    public LuceneDictionaryImpl(IndexSearcher searcher, String lookupFieldName, int maxListHits)
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
    		Query q = null; 
    		TopDocs topDoc = null;
    		if (str.indexOf('-') == -1) {
    			q = new TermQuery(new Term(iv_lookupFieldName, str));
    			topDoc = iv_searcher.search(q, iv_maxHits);
    		}
    		else {  // needed the KeyworkAnalyzer for situations where the hypen was included in the f-word
    			QueryParser query = new QueryParser(Version.LUCENE_40, iv_lookupFieldName, new KeywordAnalyzer());
    			try {
   				 	//CTAKES-63 - I believe all of the chars in the str token should be escaped to avoid issues such as a token ending with ']'
					//topDoc = iv_searcher.search(query.parse(str.replace('-', ' ')), iv_maxHits);
   				 	String escaped = QueryParserBase.escape(str.replace('-', ' '));
					 topDoc = iv_searcher.search(query.parse(escaped), iv_maxHits);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		if (iv_maxHits==0) {
    			iv_maxHits=Integer.MAX_VALUE;
    			iv_logger.warn("iv_maxHits was 0, using Integer.MAX_VALUE instead");
    		}
    		
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
