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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import edu.mayo.bmi.dictionary.BaseMetaDataHitImpl;
import edu.mayo.bmi.dictionary.MetaDataHit;

/**
 * 
 * @author Mayo Clinic
 */
public class LuceneDocumentMetaDataHitImpl extends BaseMetaDataHitImpl
        implements MetaDataHit
{
    private Document iv_doc;
    private Set iv_nameSet = new HashSet();
    private Collection iv_valCol = new ArrayList();

    public LuceneDocumentMetaDataHitImpl(Document luceneDoc)
    {
        iv_doc = luceneDoc;
        Field[] fields = iv_doc.getFields(null);
        for(int i=0; i<fields.length;i++)
         {
        	iv_nameSet.add(fields[i].name());
            iv_valCol.add(fields[i].stringValue());
        }
    }

    public String getMetaFieldValue(String metaFieldName)
    {
        return iv_doc.get(metaFieldName);
    }

    public Set getMetaFieldNames()
    {
        return iv_nameSet;
    }

    public Collection getMetaFieldValues()
    {
        return iv_valCol;
    }
}