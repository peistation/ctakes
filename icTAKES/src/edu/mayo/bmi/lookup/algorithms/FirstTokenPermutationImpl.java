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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.mayo.bmi.dictionary.DictionaryEngine;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.phrasebuilder.PhraseBuilder;
import edu.mayo.bmi.lookup.vo.LookupAnnotation;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.lookup.vo.LookupToken;

/**
 * <b>OVERVIEW: </b> Each LookupToken is fed into a "first token" Dictionary. A
 * hit indicates an anchor and the window around this anchor is based on
 * context. This hit also contains all the presentations from the Dictionary
 * where the "first token" is contained.
 * 
 * The window is determined by finding the largest overlapping context window
 * annotation. Permutations of LookupTokens found within this window are used to
 * match against the presentations found earlier. If context window annotations
 * are not provided, a fixed window is used based on the specified max
 * permutation level.
 * 
 * <b>OPTIONAL CONTEXT: </b> context window annotations
 * 
 * @author Mayo Clinic
 */
public class FirstTokenPermutationImpl implements LookupAlgorithm
{
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    /**
     * Key value for context map. Value is expected to be a List of
     * LookupAnnotation objects in sorted order.
     */
    public static final String CTX_KEY_WINDOW_ANNOTATIONS = "WINDOW_ANNOTATIONS";

    /**
     * Key value for LookupToken attribute. Value is expected to be either TRUE
     * or FALSE. This indicates whether to use this token for a "first token"
     * lookup or not. This is optional.
     */
    public static final String LT_KEY_USE_FOR_LOOKUP = "USE_FOR_LOOKUP";

    private DictionaryEngine iv_firstTokenDictEngine;
    private PhraseBuilder iv_phrBuilder;

    private int iv_maxPermutationLevel;
    // key = level Integer, value = Permutation list
    private Map iv_permCacheMap = new HashMap();

    private String[] iv_textMetaFieldNames;

    /**
     * Constructor
     * 
     * @param firstTokenDictEngine
     *            Dictionary that is indexed against first tokens.
     * @param phraseBuilder
     *            Builds phrases to match against Dictionary.
     * @param textMetaFieldNames
     *            MetaFieldNames used to extract presentations.
     * @param maxPermutationLevel
     *            Max permutation Level allowed.
     */
    public FirstTokenPermutationImpl(DictionaryEngine firstTokenDictEngine,
            PhraseBuilder phraseBuilder, String textMetaFieldNames[],
            int maxPermutationLevel)
    {
        iv_firstTokenDictEngine = firstTokenDictEngine;
        iv_phrBuilder = phraseBuilder;
        iv_textMetaFieldNames = textMetaFieldNames;

        iv_maxPermutationLevel = maxPermutationLevel;
        for (int i = 0; i <= maxPermutationLevel; i++)
        {
            Integer level = new Integer(i);
            List permList = PermutationUtil.getPermutationList(i);
            iv_permCacheMap.put(level, permList);
        }
    }

    /**
     * Implementation of algorithm.
     */
    public Collection lookup(List ltList, Map ctxMap) throws Exception
    {
        // setup optional window context data
        boolean useWindowAnnots = false;
        List wAnnotList = getWindowAnnotations(ctxMap);
        if (wAnnotList.size() > 0)
        {
            useWindowAnnots = true;
        }
        Map wStartOffsetMap = getStartOffsetMap(wAnnotList, true);
        Map wEndOffsetMap = getEndOffsetMap(wAnnotList, true);

        Map ltListIndexMap = getListIndexMap(ltList);
        Map ltStartOffsetMap = getStartOffsetMap(ltList, true);
        Map ltEndOffsetMap = getEndOffsetMap(ltList, true);

        List lhList = new ArrayList();
        for (int ltIdx = 0; ltIdx < ltList.size(); ltIdx++)
        {
            LookupToken lt = (LookupToken) ltList.get(ltIdx);

            Boolean useForLookup = Boolean.valueOf(lt.getStringAttribute(LT_KEY_USE_FOR_LOOKUP));

            if ((useForLookup == null) || (useForLookup.booleanValue()))
            {
                Collection mdhCol = getFirstTokenHits(lt);

                if ((mdhCol != null) && (mdhCol.size() > 0))
                {
                    int wEndOffset = -1;
                    if (useWindowAnnots)
                    {
                        // get the largest overlapping window annotation
                        LookupAnnotation wAnnot = getLargestWindowAnnotation(
                                ltIdx,
                                lt,
                                ltStartOffsetMap,
                                ltEndOffsetMap,
                                ltListIndexMap,
                                wStartOffsetMap,
                                wEndOffsetMap);
                        if (wAnnot != null)
                        {
                            wEndOffset = wAnnot.getEndOffset();
                        }
                    }
                    if (wEndOffset == -1)
                    {
                        iv_logger.debug("Window size set to max perm level.");
                        wEndOffset = getFixedWindowEndOffset(ltIdx, lt, ltList);
                    }

                    List endLookupTokenList = getLookupTokenList(
                            wEndOffset,
                            ltEndOffsetMap,
                            false);
                    LookupToken endLookupToken = (LookupToken) endLookupTokenList.get(endLookupTokenList.size() - 1);

                    int startTokenIdx = ltIdx;
                    int endTokenIdx = ((Integer) ltListIndexMap.get(endLookupToken)).intValue();

                    // list of LookupToken objects bound by the window
                    List wLookupTokenList = ltList.subList(
                            startTokenIdx,
                            endTokenIdx + 1);

                    // use permutation algorithm to find any hits inside the window
                    Collection lhCol = getLookupHits(
                            mdhCol,
                            wLookupTokenList,
                            new Integer(ltIdx - startTokenIdx));

                    lhList.addAll(lhCol);
                }
            }
        }

        return lhList;
    }

    private Collection getLookupHits(
            Collection mdhCol,
            List wLookupTokenList,
            Integer firstTokenIndex) throws Exception
    {
        if ((wLookupTokenList.size() - 1) > iv_maxPermutationLevel)
        {
            iv_logger.debug("Beyond permutation cache size.");
            return new ArrayList();
        }

        // build a list of index values (excludes index of first token)
        List idxList = new ArrayList();
        for (int i = 0; i < wLookupTokenList.size(); i++)
        {
            if (i != firstTokenIndex.intValue())
            {
                idxList.add(new Integer(i));
            }
        }

        Collection permCol = (Collection) iv_permCacheMap.get(new Integer(
                idxList.size()));

        List lhList = new ArrayList();

        Map mdhMap = new HashMap();
        Iterator mdhItr = mdhCol.iterator();
        while (mdhItr.hasNext())
        {
            MetaDataHit mdh = (MetaDataHit) mdhItr.next();
            for (int i = 0; i < iv_textMetaFieldNames.length; i++)
            {
                String text = mdh.getMetaFieldValue(iv_textMetaFieldNames[i]);
                if (text != null)
                {
                    text = text.toLowerCase();
                    Set mdhSet = (Set) mdhMap.get(text);
                    if (mdhSet == null)
                    {
                        mdhSet = new HashSet();
                    }
                    mdhSet.add(mdh);
                    mdhMap.put(text, mdhSet);
                }
                else
                {
                    if (iv_logger.isDebugEnabled())
                    {
                        iv_logger.debug("MetaField "
                                + iv_textMetaFieldNames[i]
                                + " contains no data.");
                    }
                }
            }
        }

        LookupToken firstWordLookupToken = (LookupToken) wLookupTokenList.get(firstTokenIndex.intValue());

        Iterator permItr = permCol.iterator();
        while (permItr.hasNext())
        {
            // convert permutation idx back into LookupTokens
            List tempList = new ArrayList();
            List permutation = (List) permItr.next();
            Iterator idxItr = permutation.iterator();
            while (idxItr.hasNext())
            {
                int idx = ((Integer) idxItr.next()).intValue();
                if (idx <= firstTokenIndex.intValue())
                {
                    idx--;
                }
                LookupToken lt = (LookupToken) wLookupTokenList.get(idx);
                tempList.add(lt);
            }

            List singleTokenList = new ArrayList();
            singleTokenList.add(firstWordLookupToken);
            String[] fwPerms = iv_phrBuilder.getPhrases(singleTokenList);

            String[] phrArr = iv_phrBuilder.getPhrases(tempList);
            for (int i = 0; i < phrArr.length; i++)
            {
                for (int fwPermIdx = 0; fwPermIdx < fwPerms.length; fwPermIdx++)
                {
                    StringBuffer phraseSB = new StringBuffer();
                    phraseSB.append(fwPerms[fwPermIdx]);
                    phraseSB.append(' ');
                    phraseSB.append(phrArr[i]);
                    String phrase = phraseSB.toString().trim().toLowerCase();
                    Set mdhSet = (Set) mdhMap.get(phrase);
                    if (mdhSet != null)
                    {
                        Iterator mdhIterator = mdhSet.iterator();
                        while (mdhIterator.hasNext())
                        {
                            MetaDataHit mdh = (MetaDataHit) mdhIterator.next();
                            // figure out start and end offsets
                            Collections.sort(permutation);

                            int startOffset;
                            if (permutation.size() > 0)
                            {
                                int firstIdx = ((Integer) permutation.get(0)).intValue();
                                if (firstIdx <= firstTokenIndex.intValue())
                                {
                                    firstIdx--;
                                }
                                LookupToken lt = (LookupToken) wLookupTokenList.get(firstIdx);
                                if (lt.getStartOffset() < firstWordLookupToken.getStartOffset())
                                {
                                    startOffset = lt.getStartOffset();
                                }
                                else
                                {
                                    startOffset = firstWordLookupToken.getStartOffset();
                                }
                            }
                            else
                            {
                                startOffset = firstWordLookupToken.getStartOffset();
                            }

                            int endOffset;
                            if (permutation.size() > 0)
                            {
                                int lastIdx = ((Integer) permutation.get(permutation.size() - 1)).intValue();
                                if (lastIdx <= firstTokenIndex.intValue())
                                {
                                    lastIdx--;
                                }
                                LookupToken lt = (LookupToken) wLookupTokenList.get(lastIdx);
                                if (lt.getEndOffset() > firstWordLookupToken.getEndOffset())
                                {
                                    endOffset = lt.getEndOffset();
                                }
                                else
                                {
                                    endOffset = firstWordLookupToken.getEndOffset();
                                }
                            }
                            else
                            {
                                endOffset = firstWordLookupToken.getEndOffset();
                            }

                            LookupHit lh = new LookupHit(
                                    mdh,
                                    startOffset,
                                    endOffset);

                            lhList.add(lh);
                        }
                    }
                }
            }
        }
        return lhList;
    }

    /**
     * Extracts the list of LookupAnnotation objects representing noun phrases
     * from the context map.
     * 
     * @param contextMap
     * @return
     */
    private List getWindowAnnotations(Map contextMap)
    {
        List list = (List) contextMap.get(CTX_KEY_WINDOW_ANNOTATIONS);
        if ((list == null) || (list.size() == 0))
        {
            iv_logger.debug("No context window annotations.");
            return new ArrayList();
        }
        return list;
    }

    /**
     * Determines the number of ListTokens are contained within the specified
     * start and end offsets;
     * 
     * @param ltStartOffsetMap
     * @param ltEndOffsetMap
     * @param ltListIndexMap
     * @param startOffset
     * @param endOffset
     * @return
     */
    private int getNumberOfListTokens(
            Map ltStartOffsetMap,
            Map ltEndOffsetMap,
            Map ltListIndexMap,
            int startOffset,
            int endOffset)
    {
        List startLookupTokenList = getLookupTokenList(
                startOffset,
                ltStartOffsetMap,
                true);
        List endLookupTokenList = getLookupTokenList(
                endOffset,
                ltEndOffsetMap,
                false);

        if ((startLookupTokenList == null) || (endLookupTokenList == null))
        {
            iv_logger.debug("Invalid window:" + startOffset + "," + endOffset);
            return -1;
        }
        LookupToken startLookupToken = (LookupToken) startLookupTokenList.get(0);
        Integer startIdx = (Integer) ltListIndexMap.get(startLookupToken);

        LookupToken endLookupToken = (LookupToken) endLookupTokenList.get(endLookupTokenList.size() - 1);
        Integer endIdx = (Integer) ltListIndexMap.get(endLookupToken);

        return endIdx.intValue() - startIdx.intValue() + 1;
    }

    /**
     * Attempts to get a list of LookupToken objects at the specified offset. If
     * there are none, this method attempts to try nearby offsets based on the
     * traversal direction.
     * 
     * @param offset
     * @param ltOffsetMap
     * @param traverseRight
     * @return
     */
    private List getLookupTokenList(
            int offset,
            Map ltOffsetMap,
            boolean traverseRight)
    {
        // first attempt the original offset, which will be the case most of the
        // time
        List lookupTokenList = (List) ltOffsetMap.get(new Integer(offset));
        if (lookupTokenList != null)
        {
            return lookupTokenList;
        }
        else
        {
            // otherwise traverse some nearby offsets and attempt to find a
            // token

            // TODO hardcoded max offset window is 10 char
            final int offsetWindow = 10;

            // build list of offsets to try
            List offsetList = new ArrayList();
            if (traverseRight)
            {
                int max = offset + offsetWindow;
                for (int i = offset; i <= max; i++)
                {
                    offsetList.add(new Integer(i));
                }
            }
            else
            {
                int min = offset - offsetWindow;
                for (int i = offset; i >= min; i--)
                {
                    offsetList.add(new Integer(i));
                }
            }

            Iterator offsetItr = offsetList.iterator();
            while (offsetItr.hasNext())
            {
                Integer tempOffset = (Integer) offsetItr.next();
                lookupTokenList = (List) ltOffsetMap.get(tempOffset);
                if (lookupTokenList != null)
                {
                    return lookupTokenList;
                }
            }
        }
        // no tokens in window
        return null;
    }

    /**
     * Determines the largest overlapping window annotation for the specified
     * LookupToken.
     * 
     * @param lt
     * @param wStartOffsetMap
     * @param wEndOffsetMap
     * @return
     */
    private LookupAnnotation getLargestWindowAnnotation(
            int tokenIdx,
            LookupToken lt,
            Map ltStartOffsetMap,
            Map ltEndOffsetMap,
            Map ltListIndexMap,
            Map wStartOffsetMap,
            Map wEndOffsetMap)
    {
        Set startCandidateSet = new HashSet();
        Set endCandidateSet = new HashSet();

        Iterator startItr = wStartOffsetMap.keySet().iterator();
        while (startItr.hasNext())
        {
            Integer startOffset = (Integer) startItr.next();
            if (startOffset.intValue() <= lt.getStartOffset())
            {
                List wAnnotList = (List) wStartOffsetMap.get(startOffset);
                startCandidateSet.addAll(wAnnotList);
            }
        }

        Iterator endItr = wEndOffsetMap.keySet().iterator();
        while (endItr.hasNext())
        {
            Integer endOffset = (Integer) endItr.next();
            if (endOffset.intValue() >= lt.getEndOffset())
            {
                List wAnnotList = (List) wEndOffsetMap.get(endOffset);
                endCandidateSet.addAll(wAnnotList);
            }
        }

        // union to get window annotations that are overlapping with LookupToken
        startCandidateSet.retainAll(endCandidateSet);

        // find largest overlapping window annotation
        LookupAnnotation largestWindowAnnot = null;
        Iterator laItr = startCandidateSet.iterator();
        while (laItr.hasNext())
        {
            LookupAnnotation tempLookupAnnot = (LookupAnnotation) laItr.next();
            if ((largestWindowAnnot == null)
                    || (tempLookupAnnot.getLength() > largestWindowAnnot.getLength()))
            {
                // now see if we can handle the size of this window (permutation
                // wise)
                int ltCount = getNumberOfListTokens(
                        ltStartOffsetMap,
                        ltEndOffsetMap,
                        ltListIndexMap,
                        tempLookupAnnot.getStartOffset(),
                        tempLookupAnnot.getEndOffset());

                if ((ltCount <= iv_maxPermutationLevel) && (ltCount > 0))
                {
                    largestWindowAnnot = tempLookupAnnot;
                }
                else
                {
                    if (iv_logger.isDebugEnabled())
                    {
                        iv_logger.debug("Window size of "
                                + ltCount
                                + " exceeds the max permutation level of "
                                + iv_maxPermutationLevel
                                + ".");
                    }
                }
            }
        }

        return largestWindowAnnot;
    }

    private int getFixedWindowEndOffset(
            int tokenIdx,
            LookupToken lt,
            List ltList)
    {
        int fixedEndOffset = 0;

        for (int i = tokenIdx; (i < tokenIdx + iv_maxPermutationLevel)
                && (i < ltList.size()); i++)
        {
            LookupToken tempLookupToken = (LookupToken) ltList.get(i);
            if (tempLookupToken != null)
            {
                fixedEndOffset = tempLookupToken.getEndOffset();
            }
        }
        return fixedEndOffset;
    }

    /**
     * Creates a map that binds an object from a list to its index position.
     * 
     * @param list
     * @return
     */
    private Map getListIndexMap(List list)
    {
        Map m = new HashMap();

        for (int i = 0; i < list.size(); i++)
        {
            Integer index = new Integer(i);
            m.put(list.get(i), index);
        }

        return m;
    }

    /**
     * Creates a map that uses the start offset to index the LookupAnnotation
     * objects. If multiple LookupAnnotations can exist at the same start
     * offset, then hasMultiples=true and the values with be a List of
     * LookupAnnotation objects at that offset.
     * 
     * @param lookupAnnotList
     * @param hasMultiples
     * @return
     */
    private Map getStartOffsetMap(List lookupAnnotList, boolean hasMultiples)
    {
        Map m = new HashMap();

        Iterator laItr = lookupAnnotList.iterator();
        while (laItr.hasNext())
        {
            LookupAnnotation la = (LookupAnnotation) laItr.next();
            Integer key = new Integer(la.getStartOffset());
            if (hasMultiples)
            {
                List list = (List) m.get(key);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add(la);
                m.put(key, list);
            }
            else
            {
                m.put(key, la);
            }
        }

        return m;
    }

    /**
     * Creates a map that uses the end offset to index the LookupAnnotation
     * objects. If multiple LookupAnnotations can exist at the end start offset,
     * then hasMultiples=true and the values with be a List of LookupAnnotation
     * objects at that offset.
     * 
     * @param lookupAnnotList
     * @param hasMultiples
     * @return
     */
    private Map getEndOffsetMap(List lookupAnnotList, boolean hasMultiples)
    {
        Map m = new HashMap();

        Iterator laItr = lookupAnnotList.iterator();
        while (laItr.hasNext())
        {
            LookupAnnotation la = (LookupAnnotation) laItr.next();
            Integer key = new Integer(la.getEndOffset());
            if (hasMultiples)
            {
                List list = (List) m.get(key);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add(la);
                m.put(key, list);
            }
            else
            {
                m.put(key, la);
            }
        }

        return m;
    }

    /**
     * Gets the hits for the specified LookupToken. This uses the first token Dictionary.
     * 
     * @param firstLookupToken
     * @return
     * @throws Exception
     */
    private Collection getFirstTokenHits(LookupToken firstLookupToken)
            throws Exception
    {
        List singleLtList = new ArrayList();
        singleLtList.add(firstLookupToken);

        String[] phrases = iv_phrBuilder.getPhrases(singleLtList);

        Collection mdhCol = new ArrayList();
        for (int i = 0; i < phrases.length; i++)
        {
            Collection curMdhCol = iv_firstTokenDictEngine.metaLookup(phrases[i]);

            if (curMdhCol.size() > 0)
            {
                mdhCol.addAll(curMdhCol);
            }
        }
        return mdhCol;
    }
}