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
package edu.mayo.bmi.lookup.phrasebuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.mayo.bmi.lookup.vo.LookupToken;

/**
 * Builds phrases based on various variants of a LookupToken. For instance, a
 * single LookupToken may have a spell corrected variant, abbreviation expansion
 * variant, etc...
 * 
 * @author Mayo Clinic
 */
public class VariantPhraseBuilderImpl implements PhraseBuilder
{
    private List iv_textExtractorList;

    /**
     * Constructor
     * 
     * @param variantAttrNames
     *            Key names of the variant attributes attached to the
     *            LookupToken objects.
     * @param useOriginalText
     *            flag that determines whether to use the original text or not.
     */
    public VariantPhraseBuilderImpl(String[] variantAttrNames,
            boolean useOriginalText)
    {
        iv_textExtractorList = new ArrayList();

        if (useOriginalText)
        {
            // use original text as a variant
            iv_textExtractorList.add(new OriginalTextImpl());
        }

        // add variants
        for (int i = 0; i < variantAttrNames.length; i++)
        {
            iv_textExtractorList.add(new AttributeTextImpl(variantAttrNames[i]));
        }
    }

    public String[] getPhrases(List lookupTokenList)
    {
        Set phraseSet = new HashSet();
        Iterator teItr = iv_textExtractorList.iterator();
        while (teItr.hasNext())
        {
            TextExtractor te = (TextExtractor) teItr.next();

            StringBuffer sb = new StringBuffer();
            LookupToken previousLt = null;
            Iterator ltItr = lookupTokenList.iterator();
            while (ltItr.hasNext())
            {
                LookupToken lt = (LookupToken) ltItr.next();
                String variant = te.getText(lt);

                if (variant == null)
                {
                    variant = lt.getText();
                }

                if (previousLt != null)
                {
                    // check delta between previous token and current token
                    // this delta represents whitespace between tokens
                    if (previousLt.getEndOffset() != lt.getStartOffset())
                    {
                        // insert whitespace
                        sb.append(' ');
                    }
                }

                sb.append(variant);

                previousLt = lt;
            }
            String phrase = sb.toString().trim();
            phraseSet.add(phrase);
        }

        String[] phraseArr = new String[phraseSet.size()];
        phraseSet.toArray(phraseArr);

        return phraseArr;
    }

    /**
     * Common interface to extract text from a LookupToken.
     * 
     * @author Mayo Clinic
     */
    private interface TextExtractor
    {
        public String getText(LookupToken lt);
    }

    /**
     * Implementation that extracts text from the original text of a
     * LookupToken.
     * 
     * @author Mayo Clinic
     */
    class OriginalTextImpl implements TextExtractor
    {
        public String getText(LookupToken lt)
        {
            return lt.getText();
        }
    }

    /**
     * Implementation that extracts text from an attribute of a LookupToken.
     * 
     * @author Mayo Clinic
     */
    class AttributeTextImpl implements TextExtractor
    {
        private String iv_varAttrName;

        /**
         * Constructor
         * 
         * @param varAttrName
         */
        public AttributeTextImpl(String varAttrName)
        {
            iv_varAttrName = varAttrName;
        }

        public String getText(LookupToken lt)
        {
            return lt.getStringAttribute(iv_varAttrName);
        }
    }
}
