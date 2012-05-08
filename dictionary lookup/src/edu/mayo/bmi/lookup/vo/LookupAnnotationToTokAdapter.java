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
/*
 * Created on Aug 24, 2005
 *
 */
package edu.mayo.bmi.lookup.vo;

import java.util.HashMap;
import java.util.Map;

import edu.mayo.bmi.nlp.tokenizer.Token;

/**
 * @author Mayo Clinic
 *
 */
public class LookupAnnotationToTokAdapter
        implements LookupToken, LookupAnnotation
{
        private Map iv_attrMap = new HashMap();

        private Token iv_tok;

        public LookupAnnotationToTokAdapter(Token tok)
        {
            iv_tok = tok;
        }

        public void addStringAttribute(String attrKey, String attrVal)
        {
            iv_attrMap.put(attrKey, attrVal);
        }

        public int getEndOffset()
        {
            return iv_tok.getEndOffset();
        }

        public int getLength()
        {
            return getStartOffset() - getEndOffset();
        }

        public int getStartOffset()
        {
            return iv_tok.getStartOffset();
        }

        public String getStringAttribute(String attrKey)
        {
            return (String) iv_attrMap.get(attrKey);
        }

        public String getText()
        {
            return iv_tok.getText();
        }

}
