/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.lookup.ae;

import edu.mayo.bmi.lookup.algorithms.LookupAlgorithm;

/**
 * A container for three related classes used to lookup terms in a dictionary and process hits found.
 * <li>a lookup algorithm - a class with a <code>lookup</code> method that returns hits</li>
 * <li>a lookup initializer - a collection of methods used to initialize/control the lookup algorithm</li>
 * <li>a lookup consumer - class which processes hits found by the lookup algorithm,
 * and typically adds annotations to the CAS</li>
 * 
 * @author Mayo Clinic
 */
public class LookupSpec
{
    private LookupAlgorithm iv_lookupAlgorithm;
    private LookupInitializer iv_lookupInitializer;
    private LookupConsumer iv_lookupConsumer;

    public LookupSpec(LookupAlgorithm lookupAlgorithm,
            LookupInitializer lookupInitializer, LookupConsumer lookupConsumer)
    {
        iv_lookupAlgorithm = lookupAlgorithm;
        iv_lookupInitializer = lookupInitializer;
        iv_lookupConsumer = lookupConsumer;
    }

    public LookupAlgorithm getLookupAlgorithm()
    {
        return iv_lookupAlgorithm;
    }

    public LookupInitializer getLookupInitializer()
    {
        return iv_lookupInitializer;
    }

    public LookupConsumer getLookupConsumer()
    {
        return iv_lookupConsumer;
    }

}
