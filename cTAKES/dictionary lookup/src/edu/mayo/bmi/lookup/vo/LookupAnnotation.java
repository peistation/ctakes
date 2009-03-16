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
package edu.mayo.bmi.lookup.vo;

/**
 * Value object that models a text annotation.
 * 
 * @author Mayo Clinic
 */
public interface LookupAnnotation
{
    /**
     * Gets the start offset.
     * 
     * @return
     */
    public int getStartOffset();

    /**
     * Gets the end offset.
     * 
     * @return
     */
    public int getEndOffset();

    /**
     * Gets the length of this annotation based on offsets.
     * 
     * @return
     */
    public int getLength();

    /**
     * Gets the text.
     * 
     * @return
     */
    public String getText();
    
    /**
     * Adds an attribute that may be used for filtering.
     * 
     * @param attrKey
     * @param attrVal
     */
    public void addStringAttribute(String attrKey, String attrVal);

    /**
     * Gets an attribute.
     * 
     * @param attrKey
     * @return
     */
    public String getStringAttribute(String attrKey);    
}