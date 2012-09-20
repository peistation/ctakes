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
package edu.mayo.bmi.lookup.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.mayo.bmi.dictionary.MetaDataHit;

/**
 * Represents a single hit found by a lookup algorithm. Meta data returned by
 * the Dictionary is also encapsulated here as well.
 * 
 * @author Mayo Clinic
 */
public class LookupHit
{
    private int iv_startOffset;
    private int iv_endOffset;
    private MetaDataHit iv_mdh;

    public LookupHit(MetaDataHit mdh, int startOffset, int endOffset)
    {
        iv_mdh = mdh;
        iv_startOffset = startOffset;
        iv_endOffset = endOffset;
    }

    public int getEndOffset()
    {
        return iv_endOffset;
    }

    public MetaDataHit getDictMetaDataHit()
    {
        return iv_mdh;
    }

    public int getStartOffset()
    {
        return iv_startOffset;
    }

    /**
     * Override default equals method. Two LookupHit objects are equal if their
     * offsets match and their MetaDataHit objects are equal.
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof LookupHit)
        {
            LookupHit lh = (LookupHit) obj;
            if ((iv_startOffset == lh.getStartOffset())
                    && (iv_endOffset == lh.getEndOffset()))
            {
                return iv_mdh.equals(lh.getDictMetaDataHit());
            }
        }
        return false;
    }
    
    /**
     * Generates a unique key based on this objects values.
     * @return
     */
    public String getUniqueKey()
    {
        StringBuffer key = new StringBuffer();
        
        key.append("s=");
        key.append(iv_startOffset);
        key.append("/");
        key.append("e=");
        key.append(iv_endOffset);
        key.append("/");

        List mfNameList = new ArrayList(iv_mdh.getMetaFieldNames());
        Collections.sort(mfNameList);
        Iterator mfNameItr = mfNameList.iterator();
        while (mfNameItr.hasNext())
        {
            String mfName = (String)mfNameItr.next();
            String mfValue = iv_mdh.getMetaFieldValue(mfName);
            key.append(mfName);
            key.append("=");
            key.append(mfValue);
            key.append(",");
        }
        
        return key.toString();        
    }
}