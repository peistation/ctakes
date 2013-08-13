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
package edu.mayo.bmi.lookup.algorithms;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Generic interface to specific lookup algorithm implementations.
 * 
 * @author Mayo Clinic
 */
public interface LookupAlgorithm
{
    /**
     * Lookup the given text specified via LookupToken objects. Any hits will be
     * returned as a collection of LookupHit objects.
     * 
     * @param lookupTokenList
     *            List of LookupTokens, must be sorted.
     * @param contextMap
     *            Map where key=Impl specific String object and value=List of
     *            LookupAnnotation objects
     * @return Collection of LookupHits.
     * @throws Exception
     */
    public Collection lookup(List lookupTokenList, Map contextMap)
            throws Exception;
}
