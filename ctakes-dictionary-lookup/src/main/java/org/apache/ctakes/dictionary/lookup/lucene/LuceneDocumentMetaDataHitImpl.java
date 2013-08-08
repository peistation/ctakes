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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.ctakes.dictionary.lookup.BaseMetaDataHitImpl;
import org.apache.ctakes.dictionary.lookup.MetaDataHit;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;


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

        List<Fieldable> fieldEnumList = iv_doc.getFields();
        
        ListIterator<Fieldable> fieldEnum = fieldEnumList.listIterator();
        while (fieldEnum.hasNext())
        {
            Field f = (Field) fieldEnum.next();

            iv_nameSet.add(f.name());
            iv_valCol.add(f.stringValue());
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