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
package edu.mayo.bmi.uima.lookup.ae;

import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;

/**
 * LookupConsumer implementation that outputs debug info to the log about each LookupHit.
 * 
 * @author Mayo Clinic 
 * 
 */
public class DebugConsumerImpl implements LookupConsumer
{
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    public DebugConsumerImpl(AnnotatorContext aCtx, Properties props)
    {        
    }
    
    public void consumeHits(JCas jcas, Iterator lookupHitItr)
            throws AnnotatorProcessException
    {
        while (lookupHitItr.hasNext())
        {
            LookupHit lh = (LookupHit) lookupHitItr.next();
            int begin = lh.getStartOffset();
            int end = lh.getEndOffset();
            String coveredText = jcas.getDocumentText().substring(begin, end);            
            iv_logger.info("LookupHit offsets=("+begin+","+end+")\tcoveredText="+coveredText);
            
            MetaDataHit mdh = lh.getDictMetaDataHit();
            Iterator nameItr = mdh.getMetaFieldNames().iterator();
            while (nameItr.hasNext())
            {
                String mfName = (String)nameItr.next();
                String mfValue = mdh.getMetaFieldValue(mfName);
                iv_logger.info("\tmetafield="+mfName+"\tvalue="+mfValue);
            }
        }
    }

}
