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
package edu.mayo.bmi.dictionary.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Truncates objects in the collection. The collection is first sorted and then
 * the top N objects are retained. The rest of the objects are truncated.
 * 
 * @author Mayo Clinic
 */
public class TruncateCollectionFilterImpl implements CollectionFilter
{
    private int iv_mdhCount;
    private Comparator iv_mdhComparator;

    /**
     * Constructor
     * 
     * @param mdhComparator
     *            Comparator for sorting MetaDataHit objects.
     * @param mdhCount
     *            Number of objects (N) to retain.
     */
    public TruncateCollectionFilterImpl(Comparator mdhComparator, int mdhCount)
    {
        iv_mdhComparator = mdhComparator;
        iv_mdhCount = mdhCount;
    }

    /**
     * Implementation
     */
    public Collection applyFilter(Collection metaDataHitCol) throws FilterException
    {
        if (iv_mdhCount >= metaDataHitCol.size())
        {
            return metaDataHitCol;
        }
        
        List mdhList = new ArrayList(metaDataHitCol);

        // sort the hits
        Collections.sort(mdhList, iv_mdhComparator);

        // truncate off the hits
        mdhList.subList(iv_mdhCount, mdhList.size()).clear();
        
        return mdhList;
    }
}