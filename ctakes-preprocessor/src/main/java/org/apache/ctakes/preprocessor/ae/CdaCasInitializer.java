/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.preprocessor.ae;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;


import org.apache.ctakes.core.ci.HyphenTextModifierImpl;
import org.apache.ctakes.core.ci.TextModification;
import org.apache.ctakes.core.ci.TextModifier;
import org.apache.ctakes.core.resource.FileResource;
import org.apache.ctakes.preprocessor.ClinicalNotePreProcessor;
import org.apache.ctakes.preprocessor.DocumentMetaData;
import org.apache.ctakes.preprocessor.PreProcessor;
import org.apache.ctakes.preprocessor.SegmentMetaData;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.util.Pair;
import org.apache.ctakes.typesystem.type.util.Pairs;


/**
 * Bootstraps the CAS by:
 * <ol>
 * <li>Transforms document's original CDA text into plain text,
 * inserting section (segment) markers into text .</li>
 * <li>Transformation also inserts hyphens into words that should be hyphenated</li>
 * <li>Stores the resulting text in a new View (which has its own Sofa)</li>
 * <li>Detects sections and adds Segment (aka section) annotations </li>
 * <li>Extracts document level data and stores in CAS as Property annotations.</li>
 * </ol>
 * 
 */
public class CdaCasInitializer extends JCasAnnotator_ImplBase
{
    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());

    private File dtdFile;
    private Boolean includeSectionMarkers;

    private TextModifier tm;
    
    
    private UimaContext uimaContext; 
    
	public void initialize(UimaContext aCtx) throws ResourceInitializationException {
		
		super.initialize(aCtx);
		
		uimaContext = aCtx;
		initialize();

	}
    
    
    public void initialize() throws ResourceInitializationException
    {
    	// TODO Consider using a parameter for includeSectionMarkers
        //includeSectionMarkers = (Boolean) getConfigParameterValue("IncludeSectionMarkers");
    	includeSectionMarkers = new Boolean(false);

    	// TODO Consider using a parameter for hyphWindow/HyphenDetectionWindow
        //int hyphWindow = ((Integer) getConfigParameterValue("HyphenDetectionWindow")).intValue();
        int hyphWindow = 3;

        try {
            FileResource hyphResrc = (FileResource) uimaContext.getResourceObject("HyphenDictionary");
            File hyphFile = hyphResrc.getFile();
        	logger.info("Hyphen dictionary: " + hyphFile.getAbsolutePath());

            tm = new HyphenTextModifierImpl(
                    hyphFile.getAbsolutePath(),
                    hyphWindow);

            FileResource dtdResrc = (FileResource) uimaContext.getResourceObject("DTD");
            dtdFile = dtdResrc.getFile();
        	logger.info("DTD: " + dtdFile.getAbsolutePath());
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    

    /**
     * Apply text modifier to the text 
     * TODO - move this to <code>TextModifier</code> and take a <code>Logger</code>
     * 		See <code>HyphenTextModifierImpl</code>
     * @param sb
     * @return
     */
    private void applyTextModifier(String text, StringBuffer sb) throws Exception {
        TextModification[] textModArr = tm.modify(text);
        for (int i = 0; i < textModArr.length; i++) {

        	TextModification textMod = textModArr[i];
            
            if ((textMod.getOrigStartOffset() != textMod.getNewStartOffset())
                    || (textMod.getOrigEndOffset() != textMod.getNewEndOffset())) {
                logger.warn("UNSUPPORTED: TextModification with offset changes.");
            }
            else {
            	sb.replace(textMod.getOrigStartOffset(), 
        				textMod.getOrigEndOffset(), 
        				textMod.getNewText());
            }
        }  
    }
    
    
	public void process(JCas jcas) throws AnalysisEngineProcessException {

    	logger.info(" process(JCas)");
		
		String originalText = null;
	    DocumentMetaData dmd;

        try {
            
        	JCas originalView = jcas.getView("_InitialView");
        	originalText = originalView.getSofaDataString();

            PreProcessor pp = new ClinicalNotePreProcessor(
                    dtdFile,
                    includeSectionMarkers.booleanValue());
            dmd = pp.process(originalText);

            String text = dmd.getText();
            StringBuffer sb = new StringBuffer(text);

            applyTextModifier(text, sb); 
            
            // Create a view (and its Sofa) to hold the plain text version of
            // the CDA document
            JCas plaintextView = jcas.createView("plaintext");           
            plaintextView.setDocumentText(sb.toString());
            
            // Add section (segment) annotations
            Iterator<String> segmentItr = (Iterator<String>)dmd.getSegmentIdentifiers().iterator();
            while (segmentItr.hasNext()) 
            {
                String segmentID = (String) segmentItr.next();
                SegmentMetaData smd = dmd.getSegment(segmentID);

                Segment sa = new Segment(plaintextView);
                sa.setBegin(smd.span.start);
                sa.setEnd(smd.span.end);
                sa.setId(smd.id);

                sa.addToIndexes();
            }
            
            // Store meta data about the document
            Pairs propAnnot = new Pairs(plaintextView); 
            Map metaDataMap = dmd.getMetaData();
            
            String docID = (String)metaDataMap.get(ClinicalNotePreProcessor.MD_KEY_DOC_ID);
        	if (docID!=null) {
            	DocumentID newDocId = new DocumentID(plaintextView);
            	newDocId.setDocumentID(docID);
            	newDocId.addToIndexes();
        	
        	}
            
            FSArray fsArr = new FSArray(plaintextView, metaDataMap.size());
            Iterator keyItr = metaDataMap.keySet().iterator();
            int pos = 0;
            while (keyItr.hasNext()) {

                String key = (String) keyItr.next();
                Object value = metaDataMap.get(key);

                if (value instanceof String) {
                    Pair prop = new Pair(plaintextView);               
                    prop.setAttribute(key);
                    prop.setValue((String) value);
                    fsArr.set(pos++, prop);
                }
                else if (value instanceof HashSet) {
                }

            }

            propAnnot.setPairs(fsArr);
            propAnnot.addToIndexes();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

}
