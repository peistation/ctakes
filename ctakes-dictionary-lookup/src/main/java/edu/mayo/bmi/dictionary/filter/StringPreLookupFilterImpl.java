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
package edu.mayo.bmi.dictionary.filter;

import java.util.Set;

/**
 *
 * @author Mayo Clinic
 */
public class StringPreLookupFilterImpl implements PreLookupFilter
{
    private Set iv_strSet;

    public StringPreLookupFilterImpl(Set strSet)
    {
        iv_strSet = strSet;
    }

    public boolean contains(String text) throws FilterException
    {
	    return iv_strSet.contains(text);
    }

}
