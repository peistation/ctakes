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
package edu.mayo.bmi.uima.lvg.ae;

import edu.mayo.bmi.uima.lvg.resource.LvgCmdApiResource;
import gov.nih.nlm.nls.lvg.Api.LvgCmdApi;
import gov.nih.nlm.nls.lvg.Api.LvgLexItemApi;
import gov.nih.nlm.nls.lvg.Lib.Category;
import gov.nih.nlm.nls.lvg.Lib.LexItem;

import edu.mayo.bmi.uima.core.type.Lemma;
import edu.mayo.bmi.uima.core.type.Segment;
import edu.mayo.bmi.uima.core.type.WordToken;
import edu.mayo.bmi.uima.core.util.ListFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

/**
 * UIMA annotator that uses the UMLS LVG package to find the canonical form of
 * WordTokens. The package is also used to find one or more lemmas for a given
 * WordToken along with its associated part of speech.
 * 
 * @author Mayo Clinic
 * 
 * to do:  what effect does using the cache have on words that may be misspelled.  It seems that if you 
 * automatically normalize a word from the cache, this may be bad if it is misspelled in the case where 
 * the misspelling is a word in the lexicon.
 */
public class LvgAnnotator extends JTextAnnotator_ImplBase
{
	/**
	 * Value is "PostLemmas".  This parameter determines whether the feature lemmaEntries will be populated for word annotations.  
	 */
	public static final String PARAM_POST_LEMMAS = "PostLemmas";
	/**
	 * Value is "UseLemmaCache".  This parameter determines whether a cache will be used to improve performance of setting lemma entries.
	 */
	public static final String PARAM_USE_LEMMA_CACHE = "UseLemmaCache";
	/**
	 * Value is "LemmaCacheFileLocation".  This parameter determines where the lemma cache is located.
	 */
	public static final String PARAM_LEMMA_CACHE_FILE_LOCATION = "LemmaCacheFileLocation";
	/**
	 * Value is "LemmaCacheFrequencyCutoff".  This parameter sets a threshold for the frequency of a lemma to be loaded into the cache. 
	 */
	public static final String PARAM_LEMMA_CACHE_FREQUENCY_CUTOFF = "LemmaCacheFrequencyCutoff";
	
    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());

    private final String LVGCMDAPI_RESRC_KEY = "LvgCmdApi";

    private LvgCmdApi lvgCmd;
    
    private LvgLexItemApi lvgLexItem;

    private AnnotatorContext context;

    private boolean useSegments;

    private Set skipSegmentsSet;

    private boolean useCmdCache;
    private String cmdCacheFileLocation;
    private int cmdCacheFreqCutoff;
    
    private Map xeroxTreebankMap;

    private boolean postLemmas;
    private boolean useLemmaCache;
    private String lemmaCacheFileLocation;
    private int lemmaCacheFreqCutoff;

    // key = word, value = canonical word
    private Map normCacheMap;
    
    // key = word, value = Set of Lemma objects
    private Map lemmaCacheMap;
    

    private Set exclusionSet;

    /**
     * Performs initialization logic. This implementation just reads values for
     * the configuration parameters.
     * 
     * @see org.apache.uima.analysis_engine.annotator.BaseAnnotator#initialize(AnnotatorContext)
     */
    public void initialize(AnnotatorContext aContext)
            throws AnnotatorConfigurationException,
            AnnotatorInitializationException
    {
        super.initialize(aContext);

        context = aContext;
        try
        {
            configInit();
        }
        catch (AnnotatorContextException ace)
        {
            throw new AnnotatorConfigurationException(ace);
        }

        try
        {
        	LvgCmdApiResource lvgResource = (LvgCmdApiResource) context.getResourceObject(LVGCMDAPI_RESRC_KEY);
            
        	if (lvgResource == null) throw new AnnotatorInitializationException(new Exception("Unable to locate resource with key="+ LVGCMDAPI_RESRC_KEY+"."));

            lvgCmd = lvgResource.getLvg();
            
            if (useCmdCache)
            {
                logger.info("Loading Cmd cache=" + cmdCacheFileLocation);
                loadCmdCacheFile(cmdCacheFileLocation);
                logger.info("Loaded " + normCacheMap.size() + " entries");
            }
            
            if(postLemmas)
            {
            	lvgLexItem = lvgResource.getLvgLex();
            	if (useLemmaCache)
	            {
	                logger.info("Loading Lemma cache="+ lemmaCacheFileLocation);
	                loadLemmaCacheFile(lemmaCacheFileLocation);
	                logger.info("Loaded " + lemmaCacheMap.size() + " entries");
	            }
            }

        }
        catch (Exception e)
        {
            throw new AnnotatorConfigurationException(e);
        }
    }

    /**
     * Sets configuration parameters with values from the descriptor.
     */
    private void configInit() throws AnnotatorContextException
    {
    	useSegments = ((Boolean) context.getConfigParameterValue("UseSegments")).booleanValue();
        String[] skipSegmentIDs = (String[]) context.getConfigParameterValue("SegmentsToSkip");
        skipSegmentsSet = new HashSet();
        for (int i = 0; i < skipSegmentIDs.length; i++)
        {
            skipSegmentsSet.add(skipSegmentIDs[i]);
        }

        // Load Xerox Treebank tagset map
        String xtMaps[] = (String[]) context.getConfigParameterValue("XeroxTreebankMap");
        xeroxTreebankMap = new HashMap();
        for (int i = 0; i < xtMaps.length; i++)
        {
            StringTokenizer tokenizer = new StringTokenizer(xtMaps[i], "|");
            if (tokenizer.countTokens() == 2)
            {
                String xTag = tokenizer.nextToken();
                String tTag = tokenizer.nextToken();
                xeroxTreebankMap.put(xTag, tTag);
            }
        }

        
        useCmdCache = ((Boolean) context.getConfigParameterValue("UseCmdCache")).booleanValue();

        cmdCacheFileLocation = (String) context.getConfigParameterValue("CmdCacheFileLocation");

        cmdCacheFreqCutoff = ((Integer) context.getConfigParameterValue("CmdCacheFrequencyCutoff")).intValue();

        
        String[] wordsToExclude = (String[]) context.getConfigParameterValue("ExclusionSet");
        exclusionSet = new HashSet();
        for (int i = 0; i < wordsToExclude.length; i++)
        {
            exclusionSet.add(wordsToExclude[i]);
        }
        
    	Boolean bPostLemmas = (Boolean) context.getConfigParameterValue(PARAM_POST_LEMMAS); 
    	postLemmas = bPostLemmas == null ? false : bPostLemmas.booleanValue(); 
    	if(postLemmas)
    	{
            Boolean useLemmaCache = (Boolean) context.getConfigParameterValue(PARAM_USE_LEMMA_CACHE);
    		useLemmaCache = useLemmaCache == null ? false : useLemmaCache.booleanValue();
    		if(useLemmaCache)
    		{
    			lemmaCacheFileLocation = (String) context.getConfigParameterValue(PARAM_LEMMA_CACHE_FILE_LOCATION);
    			if(lemmaCacheFileLocation == null)
    				throw new AnnotatorContextException(new Exception("Parameter for "+PARAM_LEMMA_CACHE_FILE_LOCATION+" was not set."));
    			Integer lemmaCacheFreqCutoff = (Integer) context.getConfigParameterValue(PARAM_LEMMA_CACHE_FREQUENCY_CUTOFF); 
    			if(lemmaCacheFreqCutoff == null)
    				lemmaCacheFreqCutoff = 20;
    			else
    				lemmaCacheFreqCutoff = lemmaCacheFreqCutoff.intValue();
    		}
    	}
    }


    /**
     * Invokes this annotator's analysis logic.
     */
    public void process(JCas jcas, ResultSpecification resultSpec)
    	throws AnnotatorProcessException {

    	logger.info(" process(JCas, ResultSpecification)");
    	
        String text = jcas.getDocumentText();

        try
        {
            if (useSegments)
            {
            	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
                Iterator segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();
                while (segmentItr.hasNext())
                {
                    Segment segmentAnnotation = (Segment) segmentItr.next();
                    String segmentID = segmentAnnotation.getId();
                    
                	if (!skipSegmentsSet.contains(segmentID))
                    {
                        int start = segmentAnnotation.getBegin();
                        int end = segmentAnnotation.getEnd();
                        annotateRange(jcas, text, start, end, resultSpec);
                    }
                }
            }
            else
            {
                // annotate over full doc text
                annotateRange(jcas, text, 0, text.length(), resultSpec);
            }
        }
        catch (Exception e)
        {
            throw new AnnotatorProcessException(e);
        }

    }

    /**
     * A utility method that annotates a given range.
     */
    protected void annotateRange(
            JCas jcas,
            String text,
            int rangeBegin,
            int rangeEnd,
            ResultSpecification resultSpec) throws AnnotatorContextException
    {
    	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
        Iterator wordItr = indexes.getAnnotationIndex(WordToken.type).iterator();
        while (wordItr.hasNext())
        {
        	WordToken wordAnnotation = (WordToken) wordItr.next();
        	if(wordAnnotation.getBegin() >= rangeBegin &&
        			wordAnnotation.getEnd() <= rangeEnd)
        	{
                String word = text.substring(wordAnnotation.getBegin(), wordAnnotation.getEnd());

                // if the original word was misspelled, use the spell correction
                String suggestion = wordAnnotation.getSuggestion();
                
                if ((suggestion != null) && (suggestion.length() > 0))
                {
                    word = suggestion;
                }

                // skip past words that are part of the exclusion set
                if (exclusionSet.contains(word)) continue;
                    
                setCanonicalForm(wordAnnotation, word);
                if(postLemmas)
                	setLemma(wordAnnotation, word, jcas);
            }
        }
    }

    private void setCanonicalForm(WordToken wordAnnotation, String word) throws AnnotatorContextException
    {
        // apply LVG processing to get canonical form
        String canonicalForm = null;
        if (useCmdCache)
        {
            canonicalForm = (String) normCacheMap.get(word);
            if (canonicalForm == null)
            {
            	// logger.info("["+ word+ "] was not found in LVG norm cache.");
            }
        }

        // only apply LVG processing if not found in cache first
        if (canonicalForm == null)
        {
            try
            {
                String out = lvgCmd.MutateToString(word);

                String[] output = out.split("\\|");

                if ((output != null)
                        && (output.length >= 2)
                        && (!output[1].matches("No Output")))
                {
                    canonicalForm = output[1];
                }
            }
            catch (Exception e)
            {
                throw new AnnotatorContextException(e);
            }

        }

        if (canonicalForm != null)
        {
            wordAnnotation.setCanonicalForm(canonicalForm);
        }
    }

    
    private void setLemma(WordToken wordAnnotation, String word, JCas jcas) throws AnnotatorContextException
    {
        // apply LVG processing to get lemmas
        // key = lemma string, value = Set of POS tags
        Map lemmaMap = null;

        if (useLemmaCache)
        {
            Set lemmaSet = (Set) lemmaCacheMap.get(word);
            if (lemmaSet == null)
            {
                // logger.info("["+ word+ "] was not found in LVG lemma cache.");
            }
            else
            {
                lemmaMap = new HashMap();
                Iterator lemmaItr = lemmaSet.iterator();
                while (lemmaItr.hasNext())
                {
                    LemmaLocalClass l = (LemmaLocalClass) lemmaItr.next();
                    lemmaMap.put(l.word, l.posSet);
                }
            }
        }

        if (lemmaMap == null)
        {
            lemmaMap = new HashMap();
            try
            {
                Vector lexItems = lvgLexItem.MutateLexItem(word);
                Iterator lexItemItr = lexItems.iterator();
                while (lexItemItr.hasNext())
                {
                    LexItem li = (LexItem) lexItemItr.next();

                    Category c = li.GetTargetCategory();
                    String lemmaStr = li.GetTargetTerm();
                    long[] bitValues = Category.ToValuesArray(c.GetValue());
                    for (int i = 0; i < bitValues.length; i++)
                    {
                        // note that POS is Xerox tagset
                        String lemmaPos = Category.ToName(bitValues[i]);
                        // convert Xerox tagset to PennTreebank tagset
                        String treebankTag = (String) xeroxTreebankMap.get(lemmaPos);
                        if (treebankTag != null)
                        {
                            Set posSet = null;
                            if (lemmaMap.containsKey(lemmaStr))
                            {
                                posSet = (Set) lemmaMap.get(lemmaStr);
                            }
                            else
                            {
                                posSet = new HashSet();
                            }
                            posSet.add(treebankTag);
                            lemmaMap.put(lemmaStr, posSet);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new AnnotatorContextException(e);
            }
        }

        // add lemma information to CAS
        // FSArray lemmas = new FSArray(jcas, lemmaMap.keySet().size());
        Collection lemmas = new ArrayList(lemmaMap.keySet().size());
        

        Iterator lemmaStrItr = lemmaMap.keySet().iterator();
        while (lemmaStrItr.hasNext())
        {
        	String form = (String) lemmaStrItr.next();
        	Set posTagSet = (Set) lemmaMap.get(form);
        	Iterator posTagItr = posTagSet.iterator();
        	while(posTagItr.hasNext()) {
        		String pos = (String) posTagItr.next(); // part of speech
        		Lemma lemma = new Lemma(jcas); 
            	lemma.setKey(form);
            	lemma.setPosTag(pos);
            	lemmas.add(lemma);
        	}
        }
        Lemma[] lemmaArray = (Lemma[]) lemmas.toArray(new Lemma[lemmas.size()]);
    	FSList fsList = ListFactory.buildList(jcas, lemmaArray);
    	wordAnnotation.setLemmaEntries(fsList);
    }
    
    /**
     * Helper method that loads a Norm cache file.
     * 
     * @param location
     */
    private void loadCmdCacheFile(String cpLocation)
            throws FileNotFoundException, IOException
    {
        InputStream inStream = getClass().getResourceAsStream(cpLocation);
        if (inStream == null)
        {
            throw new FileNotFoundException("Unable to find: " + cpLocation);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        // initialize map
        normCacheMap = new HashMap();

        String line = br.readLine();
        while (line != null)
        {
            StringTokenizer st = new StringTokenizer(line, "|");
            if (st.countTokens() == 7)
            {
                int freq = Integer.parseInt(st.nextToken());
                if (freq > cmdCacheFreqCutoff)
                {
                    String origWord = st.nextToken();
                    String normWord = st.nextToken();
                    if (!normCacheMap.containsKey(origWord))
                    {
                        // if there are duplicates, then only have the first
                        // occurrence in the map
                        normCacheMap.put(origWord, normWord);
                    }
                }
                else {
                    logger.debug("Discarding norm cache line due to frequency cutoff: "
                                    + line);
                }
            }
            else {
                logger.warn("Invalid LVG norm cache " + "line: " + line);
            }
            line = br.readLine();
        }
    }
    
    /**
     * Helper method that loads a Lemma cache file.
     * 
     * @param location
     */
    private void loadLemmaCacheFile(String cpLocation)
            throws FileNotFoundException, IOException
    {
        InputStream inStream = getClass().getResourceAsStream(cpLocation);
        if (inStream == null)
        {
            throw new FileNotFoundException("Unable to find: " + cpLocation);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        // initialize map
        lemmaCacheMap = new HashMap();

        String line = br.readLine();
        while (line != null)
        {
            StringTokenizer st = new StringTokenizer(line, "|");
            if (st.countTokens() == 4) //JZ: changed from 7 to 4 as used a new dictionary
            {
                int freq = Integer.parseInt(st.nextToken());
                if (freq > lemmaCacheFreqCutoff)
                {
                    String origWord = st.nextToken();
                    String lemmaWord = st.nextToken();
                    String combinedCategories = st.nextToken();

                    // strip < and > chars
                    combinedCategories = combinedCategories.substring(1, combinedCategories.length() - 1);

                    // construct Lemma object
                    LemmaLocalClass l = new LemmaLocalClass();
                    l.word = lemmaWord;
                    l.posSet = new HashSet();
                    long bitVector = Category.ToValue(combinedCategories);
                    long[] bitValues = Category.ToValuesArray(bitVector);
                    for (int i = 0; i < bitValues.length; i++)
                    {
                        String pos = Category.ToName(bitValues[i]);
                        // convert Xerox tag into Treebank
                        String treebankTag = (String) xeroxTreebankMap.get(pos);
                        if (treebankTag != null)
                        {
                            l.posSet.add(treebankTag);
                        }
                    }

                    // add Lemma to cache map
                    Set lemmaSet = null;
                    if (!lemmaCacheMap.containsKey(origWord))
                    {
                        lemmaSet = new HashSet();
                    }
                    else
                    {
                        lemmaSet = (Set) lemmaCacheMap.get(origWord);
                    }
                    lemmaSet.add(l);
                    lemmaCacheMap.put(origWord, lemmaSet);
                }
                else
                {
                    logger.debug("Discarding lemma cache line due to frequency cutoff: "+ line);
                }
            }
            else
            {
                logger.warn("Invalid LVG lemma cache " + "line: " + line);
            }
            line = br.readLine();
        }
    }

    /**
     * Basic class to group a lemma word with its various parts of speech.
     * 
     * @author Mayo Clinic
     */
    class LemmaLocalClass
    {
        public String word;

        public Set posSet;
    }
    
}