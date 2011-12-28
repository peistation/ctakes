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
package edu.mayo.bmi.uima.core.ae;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import edu.mayo.bmi.nlp.tokenizer.Token;
import edu.mayo.bmi.nlp.tokenizer.Tokenizer;
import edu.mayo.bmi.uima.core.resource.StringIntegerMapResource;
import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.Segment;
import edu.mayo.bmi.uima.core.util.ParamUtil;

/**
 * UIMA annotator that uses the Tokenizer module.
 * 
 * @author Mayo Clinic
 */
public class TokenizerAnnotator extends JTextAnnotator_ImplBase
{
    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());
  
    public static final int TOKEN_CAP_NONE = 0;
    public static final int TOKEN_CAP_FIRST_ONLY = 1;
    public static final int TOKEN_CAP_MIXED = 2;
    public static final int TOKEN_CAP_ALL = 3;

    public static final int TOKEN_NUM_POS_NONE = 0;
    public static final int TOKEN_NUM_POS_FIRST = 1;
    public static final int TOKEN_NUM_POS_MIDDLE = 2;
    public static final int TOKEN_NUM_POS_LAST = 3;

    public static final int TOKEN_NUM_TYPE_UNKNOWN = 0;
    public static final int TOKEN_NUM_TYPE_INTEGER = 1;
    public static final int TOKEN_NUM_TYPE_DECIMAL = 2;
    
	/**
	 * Value is "SegmentsToSkip".  This parameter specifies which segments to skip.  The parameter should be
	 * of type String, should be multi-valued and optional. 
	 */
	public static final String PARAM_SEGMENTS_TO_SKIP = "SegmentsToSkip";

    private final String HYPH_FREQ_TABLE_RESRC_KEY = "HyphFreqTable";

    private AnnotatorContext context;
    private Set skipSegmentsSet;

    private Tokenizer tokenizer;

    private int tokenCount = 0;

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
    }

    /**
     * Reads configuration parameters.
     */
    private void configInit() throws AnnotatorContextException
    {
        skipSegmentsSet = ParamUtil.getStringParameterValuesSet(PARAM_SEGMENTS_TO_SKIP, context); 

        int freqCutoff = ((Integer) context.getConfigParameterValue("FreqCutoff")).intValue();
        try
        {
            StringIntegerMapResource strIntMapResrc = (StringIntegerMapResource) context.getResourceObject(HYPH_FREQ_TABLE_RESRC_KEY);
            if (strIntMapResrc == null)
            {
                logger.warn("Unable to locate resource with key="
                        + HYPH_FREQ_TABLE_RESRC_KEY
                        + ".  Proceeding without hyphenation support.");
                tokenizer = new Tokenizer();
            }
            else
            {
            	logger.info("Hyphen dictionary: " + strIntMapResrc.toString());
                Map hyphMap = strIntMapResrc.getMap();
                tokenizer = new Tokenizer(hyphMap, freqCutoff);
            }
        }
        catch (Exception e)
        {
            throw new AnnotatorContextException(e);
        }
    }

    /**
     * Entry point for processing.
     */
    public void process(JCas jcas, ResultSpecification resultSpec)
            throws AnnotatorProcessException {

    	logger.info(" process(JCas, ResultSpecification)" );

        tokenCount = 0;

        JFSIndexRepository indexes = jcas.getJFSIndexRepository();
        Iterator segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();
        while (segmentItr.hasNext())
        {
            Segment sa = (Segment) segmentItr.next();
            String segmentID = sa.getId();
            if (!skipSegmentsSet.contains(segmentID))
            {
                annotateRange(jcas, sa.getBegin(), sa.getEnd());
            }
        }
    }

    /**
     * A utility method that tokenizes a range of text.
     */
    protected void annotateRange(JCas jcas, int beginPos, int endPos)
            throws AnnotatorProcessException
    {
        String text = jcas.getDocumentText().substring(beginPos, endPos);

        List tokens = null;
        try
        {
            tokens = tokenizer.tokenizeAndSort(text);
        }
        catch (Exception e)
        {
            throw new AnnotatorProcessException(e);
        }

        Iterator tokenItr = tokens.iterator();
        while (tokenItr.hasNext())
        {
            Token token = (Token) tokenItr.next();

            // convert token into JCas object
            BaseToken bta = TokenConverter.convert(
                    token,
                    jcas,
                    beginPos);

            bta.setTokenNumber(tokenCount);

            // add JCas object to CAS index
            bta.addToIndexes();

            tokenCount++;
        }
    }
}