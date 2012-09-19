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

import java.util.Collection;

/**
 *
 * @author Mayo Clinic
 */
public interface Dictionary
{
    /**
     * Tells the Dictionary to retain specific bits of metadata for each
     * entry in the Dictionary.
     * @param metaFieldName Name specific to Dictionary implementation.
     */
    public void retainMetaData(String metaFieldName);

    /**
     * Determines whether the Dictionary contains the specified input text.
     * @param text The input text.
     * @return true if Dictionary contains input text, false otherwise.
     * @throws DictionaryException
     */
    public boolean contains(String text) throws DictionaryException;

    /**
     * Gets any meta data entries associated with the specified input text.
     * @param text The input text.
     * @return Collection of MetaDataHit objects.
     * @throws DictionaryException
     */
    public Collection getEntries(String text) throws DictionaryException;
}
