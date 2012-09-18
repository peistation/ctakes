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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.mayo.bmi.dictionary.DictionaryEngine;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.phrasebuilder.PhraseBuilder;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.lookup.vo.LookupToken;

/**
 * Each individual LookupToken is referenced against a Dictionary.  
 * 
 * @author Mayo Clinic
 */
public class DirectPassThroughImpl implements LookupAlgorithm
{
    private DictionaryEngine iv_dictEngine;
    private PhraseBuilder iv_phrBuilder;

    /**
     * Constructor
     * @param dictEngine
     * @param phraseBuilder
     */
    public DirectPassThroughImpl(DictionaryEngine dictEngine,
            PhraseBuilder phraseBuilder)
    {
        iv_dictEngine = dictEngine;
        iv_phrBuilder = phraseBuilder;
    }

    public Collection lookup(List ltList, Map ctxMap) throws Exception
    {
        List lhList = new ArrayList();
        for (int tokenIdx = 0; tokenIdx < ltList.size(); tokenIdx++)
        {
            LookupToken lt = (LookupToken) ltList.get(tokenIdx);

            List singleLtList = new ArrayList();
            singleLtList.add(lt);

            String[] strArr = iv_phrBuilder.getPhrases(singleLtList);
            Collection mdhCol = getHits(strArr);

            if ((mdhCol != null) && (mdhCol.size() > 0))
            {
                Iterator mdhMatchItr = mdhCol.iterator();
                while (mdhMatchItr.hasNext())
                {
                    MetaDataHit mdh = (MetaDataHit) mdhMatchItr.next();
                    LookupHit lh = new LookupHit(mdh, lt.getStartOffset(), lt
                            .getEndOffset());
                    lhList.add(lh);
                }
            }
        }
        return lhList;
    }

    private Collection getHits(String[] phrases) throws Exception
    {
        Collection mdhCol = new ArrayList();
        for (int i = 0; i < phrases.length; i++)
        {
            Collection curMdhCol = iv_dictEngine.metaLookup(phrases[i]);
            if (curMdhCol.size() > 0)
            {
                mdhCol.addAll(curMdhCol);
            }
        }
        return mdhCol;
    }

}