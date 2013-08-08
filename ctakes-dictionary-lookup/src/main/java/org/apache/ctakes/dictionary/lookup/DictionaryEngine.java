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
package org.apache.ctakes.dictionary.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.dictionary.lookup.filter.CollectionFilter;
import org.apache.ctakes.dictionary.lookup.filter.FilterException;
import org.apache.ctakes.dictionary.lookup.filter.PostLookupFilter;
import org.apache.ctakes.dictionary.lookup.filter.PreLookupFilter;


/**
 * The engine adds additional functionality to executing a dictionary.
 * <ol>
 * <li>Pre-lookup filtering. Filters the input text.</li>
 * <li>Post-lookup filtering. Filters individual dictionary hits based on
 * metadata.</li>
 * <li>Collection filtering. Filters the collection of dictionary hits based on
 * metadata and/or collection information.</li>
 * <li>Caching. Caches dictionary hits.</li>
 * <li>Case Sensitivity. Keeps case or makes case irrelevant to lookup.</li>
 * </ol>
 * 
 * @author Mayo Clinic
 */
public class DictionaryEngine
{
    private Dictionary iv_dict;

    // use Lists to store filters to maintain order
    private List iv_preLookupFilterList = new ArrayList();
    private List iv_postLookupFilterList = new ArrayList();
    private List iv_collectionFilterList = new ArrayList();

    // cache objs
    // key = String text, value = Boolean
    private Map iv_binaryLookupCacheMap = new HashMap();
    // key = String text, value = Collection of MetaDataHits
    private Map iv_metaLookupCacheMap = new HashMap();

    private boolean iv_keepCase = false;

    /**
     * 
     * Constructor
     * 
     * @param dict
     *            Dictionary to use for lookup operations.
     * @param keepCase
     *            Determines whether to keep character Upper or Lower casing.
     *            False indicates that casing will be ignored by lower casing
     *            all lookups.
     */
    public DictionaryEngine(Dictionary dict, boolean keepCase)
    {
        iv_dict = dict;
        iv_keepCase = keepCase;
    }

    /**
     * Adds a Pre-lookup filter to the engine. Filters will be applied in the
     * order of addition.
     * 
     * @param plf
     *            PreLookupFilter to add.
     */
    public void addPreLookupFilter(PreLookupFilter plf)
    {
        iv_preLookupFilterList.add(plf);
    }

    /**
     * Adds a Post-lookup filter to the engine. Filters will be applied in the
     * order of addition.
     * 
     * @param plf
     *            PostLookupFilter to add.
     */
    public void addPostLookupFilter(PostLookupFilter plf)
    {
        iv_postLookupFilterList.add(plf);
    }

    /**
     * Adds a Collection filter to the engine. Filters will be applied in the
     * order of addition. Filter will be applied after PostLookupFiltering.
     * 
     * @param cf
     *            CollectionFilter to add.
     */
    public void addCollectionFilter(CollectionFilter cf)
    {
        iv_collectionFilterList.add(cf);
    }

    /**
     * Adds an entry to the dictionary lookup cache. The given text will be
     * cached immediately when this method is invoked. This cache does not
     * expire.
     * 
     * @param text
     */
    public void addCacheEntry(String text)
            throws DictionaryException, FilterException
    {
        if (!iv_keepCase)
        {
            text = text.toLowerCase();
        }

        boolean isHit = binaryLookup(text);
        iv_binaryLookupCacheMap.put(text, new Boolean(isHit));

        Collection c = metaLookup(text);
        iv_metaLookupCacheMap.put(text, c);
    }

    /**
     * Gets a collection of MetaDataHits from the Dictionary based on the input
     * text. Both Pre-lookup and Post-lookup filtering are applied.
     * 
     * @param text
     *            The input text.
     * @return Collection of MetaDataHit objects
     * @throws DictionaryException
     * @throws FilterException
     */
    public Collection metaLookup(String text)
            throws DictionaryException, FilterException
    {
        if (!iv_keepCase)
        {
            text = text.toLowerCase();
        }

        // apply pre-filtering
        if (isFilteredByPreLookup(text))
        {
            // return empty Collection
            return new HashSet();
        }

        // not part of filter
        Collection metaDataHitCol = null;
        boolean isCached = iv_metaLookupCacheMap.containsKey(text);
        if (isCached)
        {
            metaDataHitCol = (Collection) iv_metaLookupCacheMap.get(text);
        }
        else
        {
            // not part of cache, go ahead and do lookup
            metaDataHitCol = iv_dict.getEntries(text);
        }

        // apply post-filtering
        if (iv_postLookupFilterList.size() > 0)
        {
            Set mdhRemovalSet = new HashSet();
            Iterator mdhItr = metaDataHitCol.iterator();
            while (mdhItr.hasNext())
            {
                MetaDataHit mdh = (MetaDataHit) mdhItr.next();
                Iterator plfItr = iv_postLookupFilterList.iterator();
                while (plfItr.hasNext())
                {
                    PostLookupFilter plf = (PostLookupFilter) plfItr.next();
                    if (plf.contains(mdh))
                    {
                        mdhRemovalSet.add(mdh);
                    }
                }
            }
            metaDataHitCol.removeAll(mdhRemovalSet);
        }

        // apply collection filtering
        if (iv_collectionFilterList.size() > 0)
        {
            Iterator cfItr = iv_collectionFilterList.iterator();
            while (cfItr.hasNext())
            {
                CollectionFilter cf = (CollectionFilter) cfItr.next();
                metaDataHitCol = cf.applyFilter(metaDataHitCol);
            }
        }

        return metaDataHitCol;
    }

    /**
     * Determines whether the input text is contained by the Dictionary. Only
     * pre-lookup filtering is applied.
     * 
     * @param text
     *            The input text.
     * @return true if contained by Dictionary, false otherwise
     * @throws DictionaryException
     * @throws FilterException
     */
    public boolean binaryLookup(String text)
            throws DictionaryException, FilterException
    {
        if (!iv_keepCase)
        {
            text = text.toLowerCase();
        }

        // apply pre-filtering
        if (isFilteredByPreLookup(text))
        {
            return false;
        }

        // not part of filter, go ahead and do lookup

        Boolean isHit = null;
        boolean isCached = iv_binaryLookupCacheMap.containsKey(text);
        if (isCached)
        {
            isHit = (Boolean) iv_binaryLookupCacheMap.get(text);
        }
        else
        {
            // not part of cache, go ahead and do lookup
            isHit = new Boolean(iv_dict.contains(text));
        }

        return isHit.booleanValue();
    }

    /**
     * Helper method that applies Pre-lookup filtering to the input text.
     * 
     * @param text
     *            The input text to be filtered.
     * @return true if filtered, false otherwise
     * @throws FilterException
     */
    private boolean isFilteredByPreLookup(String text) throws FilterException
    {
        if (iv_preLookupFilterList.size() > 0)
        {
            Iterator plfItr = iv_preLookupFilterList.iterator();
            while (plfItr.hasNext())
            {
                PreLookupFilter plf = (PreLookupFilter) plfItr.next();
                if (plf.contains(text))
                {
                    // text is part of filter
                    return true;
                }
            }
        }
        return false;
    }
}