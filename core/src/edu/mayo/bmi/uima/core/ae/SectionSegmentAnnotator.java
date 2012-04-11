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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.resource.FileResource;
import edu.mayo.bmi.uima.core.type.textspan.Segment;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.core.util.DocumentSection;
import findstruct.Section;
import findstruct.StructFinder;

/**
 * Creates a single segment annotation that spans the entire document. This is
 * useful for running a TAE without a CasInitializer that would normally create
 * the segment annotations.
 * 
 * @author Mayo Clinic
 */
public class SectionSegmentAnnotator extends JCasAnnotator_ImplBase {
	private String segmentId;
	private final String templateName = "parserTemplate";
	private StructFinder structureFinder;
	private InputStream templateContent;

	private File templateFile= null;
	private String templatePath;
	Logger logger = Logger.getLogger(this.getClass());

	public HashMap<Integer, DocumentSection> sections;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		structureFinder = new StructFinder();

		try {
			templateFile = ((FileResource)aContext.getResourceObject("template")).getFile();
		}catch(Exception e ){
			logger.error("Error reading template file: " + e.getMessage());
		}

		segmentId = (String) aContext.getConfigParameterValue("SegmentID");
		if (segmentId == null) {
			segmentId = "SIMPLE_SEGMENT";
		}
	}

	/**
	 * Entry point for processing.
	 * Identify all the sections of the medical record
	 * @param jCas
	 * @author andreea bodnari
	 */
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		String text = jCas.getDocumentText();

		if (text == null) {
			String docId = DocumentIDAnnotationUtil.getDocumentID(jCas);
			throw new AnalysisEngineProcessException("text is null for docId="
					+ docId, null);
		}
		
		ArrayList<Integer> line2char = new ArrayList<Integer>();
		String[] lines = text.split("\n");
		int charNum = 0;
		for(String line : lines){
			line2char.add(charNum);
			charNum += line.length()+1;
		}

		try {
			templateContent = new FileInputStream(templateFile);
			ArrayList<Section> foundSections = 
				structureFinder.execute(text, templateContent);

			for (Section sct : foundSections) {
				String nodeName = sct.getHeader();
				String content  = sct.getContent();

				if(nodeName== null || nodeName.trim().isEmpty() || 
						content == null || content.trim().isEmpty())
					continue;

				//			String[] splitContent = content.split("\n");
				//			int endLine = startLine + splitContent.length;

				int index = text.indexOf(content);

				Segment segment = new Segment(jCas);
				segment.setBegin(index);
				segment.setEnd(index+content.length());
				segment.setId(sct.getHeader());
				segment.addToIndexes();

				//			DocumentSection section = 
				//					new DocumentSection(startLine, endLine, content);
				//			section.setSectionName(nodeName);
				//			sections.put(startLine, section);
				//
				//			startLine = endLine ;
			}		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Segment seg = new Segment(jCas);
			seg.setBegin(0);
			seg.setEnd(text.length());
			seg.setId(segmentId);
			seg.addToIndexes();
		}

//		sections = sectionIdentifier( text);
//
//		if (sections == null){
//			Segment segment = new Segment(jCas);
//			segment.setBegin(0);
//
//			segment.setEnd(text.length());
//			segment.setId(segmentId);
//			segment.addToIndexes();
//		}else
//			for(Integer key : sections.keySet()){
//				DocumentSection section = sections.get(key);
//				Segment segment = new Segment(jCas);
//				segment.setBegin(line2char.get(section.getStartLine()));
//
//				segment.setEnd(line2char.get(section.getEndLine()));
//				segment.setId(section.getSectionName());
//				segment.addToIndexes();
//			}
	}

	/**
	 * Identify the sections of a document
	 * @author andreea bodnari
	 * @param tmpFilePath
	 * @return the identified sections
	 */
//	private HashMap<Integer, DocumentSection> sectionIdentifier(String text) {
//
//		HashMap<Integer, DocumentSection> cSections = 
//				new HashMap<Integer, DocumentSection>();
//
//		try {
//			templateContent = new FileInputStream(templateFile);
//			ArrayList<Section> foundSections = 
//					structureFinder.execute(text, templateContent);
//
////			this.addSections(foundSections);
//			for(Section sct : foundSections){
//				String nodeName = sct.getHeader();
//				String content  = sct.getContent();
//
//				if(nodeName== null || nodeName.trim().isEmpty() || 
//						content == null || content.trim().isEmpty())
//					continue;
//
//				int index = text.indexOf(content);
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("Error finding sections: " + e.getMessage());
//		}	
//
//		return cSections;
//	}

	/**
	 * @author andreea bodnari
	 * @param foundSections
	 */
//	private void addSections(ArrayList<Section> foundSections){
//
//		HashMap<Integer, DocumentSection> sections = 
//				new HashMap<Integer, DocumentSection>();
//
//		try{
//
//			int startLine = 0;
//
//			for (Section sct : foundSections) {
//				String nodeName = sct.getHeader();
//				String content  = sct.getContent();
//
//				if(nodeName== null || nodeName.trim().isEmpty() || 
//						content == null || content.trim().isEmpty())
//					continue;
//
////				String[] splitContent = content.split("\n");
////				int endLine = startLine + splitContent.length;
//				
//				int index = 
//				DocumentSection section = 
//						new DocumentSection(startLine, endLine, content);
//				section.setSectionName(nodeName);
//				sections.put(startLine, section);
//
//				startLine = endLine ;
//			}
//
//		}catch(Exception e){
//			e.printStackTrace();
//			logger.error("Error parsing tmp file: " + e.getMessage());
//		}
//
//		return sections;
//	}
}