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

import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.uima.core.type.Segment;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * Creates a single segment annotation that spans the entire document. This is
 * useful for running a TAE without a CasInitializer that would normally create
 * the segment annotations.
 * uima.lookup
 * @author Mayo Clinic
 */
public class SimpleSegmentWithTagsAnnotator extends JTextAnnotator_ImplBase {
	private String segmentId;

	public void initialize(AnnotatorContext aContext) throws AnnotatorConfigurationException,
			AnnotatorInitializationException {
		super.initialize(aContext);

		try {
			segmentId = (String) aContext.getConfigParameterValue("SegmentID");
			if (segmentId == null) {
				segmentId = "SIMPLE_SEGMENT";
			}
		} catch (AnnotatorContextException ace) {
			throw new AnnotatorConfigurationException(ace);
		}
	}

    /**
     * Entry point for processing.
     */
    public void process(JCas jcas, ResultSpecification resultSpec)
            throws AnnotatorProcessException
    {
        //sa.setBegin(0);
        String text = jcas.getDocumentText();
        if(text == null)
        {
         	System.out.println("text == null in Segmentannotator");
        	String docID = DocumentIDAnnotationUtil.getDocumentID(jcas);
        	System.out.println(" \t\tdocID="+docID);
        }
        if (segmentId.compareTo("parseSectionTag")!=0){
            // If the default segment ID or a segment ID other than "parseSectionTag",
            // create 1 segment using the given segment ID.
        	Segment sa = new Segment(jcas);
           	sa.setBegin(0);
        	sa.setEnd(jcas.getDocumentText().length());
        	sa.setId(segmentId);
        	sa.addToIndexes();
        } else { // is "parseSectionTag", so search for the segment markers and annotate
        	BufferedReader fileReader = new BufferedReader(new StringReader(text));

			int charNum = 0;
			int charPos = 0;
			char[] sectIdArr = new char[5];
			int endStartSegment = 0;
			int beginEndSegment = 0;

			try {
				while ((charNum = fileReader.read()) != -1) {

					String segId = "";

					if (charNum == '[') {
						charPos++;
						if (((charNum = fileReader.read()) == 's')
								|| (charNum == 'e') || (charNum == 'h')) {
							charPos++;
							if (((charNum = fileReader.read()) == 't')
									|| (charNum == 'n') || (charNum =='e')) {
								charPos++;
								if (((charNum = fileReader.read()) == 'a')
										|| (charNum == 'd')) {
									charPos++;
									
									if ((charNum = fileReader.read()) == 'r') {
										endStartSegment = charPos + 24;
										fileReader.skip(14);
										
										fileReader.read(sectIdArr, 0, 5);
										charPos = charPos +19;

									} else if (charNum == ' ') {
										beginEndSegment = charPos - 5;
										Segment sa = new Segment(jcas);
										sa.setBegin(endStartSegment);
										sa.setEnd(beginEndSegment);
										sa.setId(segId.copyValueOf(sectIdArr));
										for (int i = 0; i < sectIdArr.length; i++)
											sectIdArr[i] = ' ';
										sa.addToIndexes();
									} else if (charNum == 'd') { // stand alone header
										Segment sa = new Segment(jcas);
										sa.setBegin(64);
										sa.setEnd(64);
										sa.setId("head");
										sa.addToIndexes();
									}
									charPos++;
								}
							}
						}
					} else
						charPos++;
				}
			} catch (Exception e) {
				throw new AnnotatorProcessException(e);
			}
        }
        

    }
}