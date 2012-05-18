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
package edu.mayo.bmi.dictionary;

import java.util.Comparator;

/**
 * Allows two MetaDataHit objects to be compared based on a specific metadata
 * value that they both contain.
 * 
 * @author Mayo Clinic
 */
public class MetaDataHitComparator implements Comparator, MetaDataHitConst
{
    private int iv_type;
    private String iv_metaFieldName;
    private boolean iv_sortAscending;

    /**
     * Constructor
     * @param metaFieldName
     * @param type
     * @param sortAscending
     */
    public MetaDataHitComparator(String metaFieldName, int type, boolean sortAscending)
    {
        iv_metaFieldName = metaFieldName;
        iv_type = type;
        iv_sortAscending = sortAscending;
    }

    /**
     * Implementation
     */
    public int compare(Object o1, Object o2)
    {
        MetaDataHit mdh1 = (MetaDataHit) o1;
        MetaDataHit mdh2 = (MetaDataHit) o2;

        String mdv1 = mdh1.getMetaFieldValue(iv_metaFieldName);
        String mdv2 = mdh2.getMetaFieldValue(iv_metaFieldName);

        int comparison;
        switch (iv_type)
        {
        case INTEGER_TYPE:
            Integer int1 = new Integer(mdv1);
            Integer int2 = new Integer(mdv2);
            comparison = int1.compareTo(int2);
            break;
        case FLOAT_TYPE:
            Float float1 = new Float(mdv1);
            Float float2 = new Float(mdv2);
            comparison = float1.compareTo(float2);
            break;
        default:
            comparison = mdv1.compareTo(mdv2);
        	break;
        }
        
        if (iv_sortAscending)
        {
            return comparison;
        }
        else
        {
            return comparison * -1;
        }
    }
}