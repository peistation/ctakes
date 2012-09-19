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
package edu.mayo.bmi.uima.core.cc;

import org.apache.uima.collection.CasConsumer;

/**
 * Extends the CasConsumer interface to provide a method for getting
 * output in XML form.  A regular CasConsumer is "terminal" in the sense
 * that its output does not get returned to object that initiated the
 * CasConsumer.  This interface allows for "non-terminal" behavior so that
 * the output can be returned.
 *  
 * @author Mayo Clinic
 */
public interface NonTerminalConsumer extends CasConsumer
{
    /**
     * Gets the generated output from a CasConsumer in XML form.
     * @return Output xml in String form.
     */
    public String getOutputXml();
}