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
package edu.mayo.bmi.dictionary.strtable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.mayo.bmi.dictionary.BaseDictionaryImpl;
import edu.mayo.bmi.dictionary.Dictionary;
import edu.mayo.bmi.dictionary.DictionaryException;
import edu.mayo.bmi.dictionary.MetaDataHit;

/**
 *
 * @author Mayo Clinic
 */
public class StringTableDictionaryImpl
    extends BaseDictionaryImpl
    implements Dictionary
{
    private StringTable iv_strTable;
    private String iv_lookupFieldName;

    public StringTableDictionaryImpl(
        StringTable strTable,
        String lookupFieldName)
    {
        iv_strTable = strTable;
        iv_lookupFieldName = lookupFieldName;
    }

    public boolean contains(String text) throws DictionaryException
    {
        if (iv_strTable.getRows(iv_lookupFieldName, text).length > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Collection getEntries(String str) throws DictionaryException
    {
        Set metaDataHitSet = new HashSet();
        StringTableRow[] strTableRows =
            iv_strTable.getRows(iv_lookupFieldName, str);
        for (int i = 0; i < strTableRows.length; i++)
        {
            MetaDataHit mdh =
                new StringTableRowMetaDataHitImpl(strTableRows[i]);
            metaDataHitSet.add(mdh);
        }

        return metaDataHitSet;
    }

}
