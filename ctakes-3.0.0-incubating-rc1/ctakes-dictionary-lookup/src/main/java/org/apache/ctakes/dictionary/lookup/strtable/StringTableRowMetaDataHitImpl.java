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
package org.apache.ctakes.dictionary.lookup.strtable;

import java.util.Collection;
import java.util.Set;

import org.apache.ctakes.dictionary.lookup.BaseMetaDataHitImpl;
import org.apache.ctakes.dictionary.lookup.MetaDataHit;


/**
 * 
 * @author Mayo Clinic
 */
public class StringTableRowMetaDataHitImpl extends BaseMetaDataHitImpl
        implements MetaDataHit
{
    private StringTableRow iv_strTableRow;

    public StringTableRowMetaDataHitImpl(StringTableRow strTableRow)
    {
        iv_strTableRow = strTableRow;
    }

    public String getMetaFieldValue(String metaFieldName)
    {
        return iv_strTableRow.getFieldValue(metaFieldName);
    }

    public Set getMetaFieldNames()
    {
        return iv_strTableRow.getNames();
    }

    public Collection getMetaFieldValues()
    {
        return iv_strTableRow.getValues();
    }
}