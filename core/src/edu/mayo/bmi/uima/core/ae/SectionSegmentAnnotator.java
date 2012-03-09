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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.mayo.bmi.uima.core.type.textspan.Segment;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.core.util.DocumentSection;

/**
 * Creates a single segment annotation that spans the entire document. This is
 * useful for running a TAE without a CasInitializer that would normally create
 * the segment annotations.
 * 
 * @author Mayo Clinic
 */
public class SectionSegmentAnnotator extends JCasAnnotator_ImplBase {
	private String segmentId;
	private final String modelName = "sectionParser";
	private final String templateName = "parserTemplate";

	private String modelPath;
	private String templatePath;
	Logger logger = Logger.getLogger(this.getClass());

	public HashMap<Integer, DocumentSection> sections;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		modelPath = (String) aContext.getConfigParameterValue(modelName);
		templatePath = (String) aContext.getConfigParameterValue(templateName);

		try {
			logger.info("Initializing section parser...");
			File checkFile = new File(modelPath);

			if(! checkFile.exists())
				throw new FileNotFoundException();

			checkFile = new File(templatePath);
			if(! checkFile.exists())
				throw new FileNotFoundException();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Error reading parser model file/directory: " + e.getMessage());
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

		sections = sectionIdentifier( text);

		if (sections == null){
			Segment segment = new Segment(jCas);
			segment.setBegin(0);

			segment.setEnd(text.length());
			segment.setId(segmentId);
			segment.addToIndexes();
		}else
			for(Integer key : sections.keySet()){
				DocumentSection section = sections.get(key);
				Segment segment = new Segment(jCas);
				segment.setBegin(section.getStartLine());

				segment.setEnd(section.getEndLine());
				segment.setId(section.getSectionName());
				segment.addToIndexes();
			}
	}

	/**
	 * Identify the sections of a document
	 * @author andreea bodnari
	 * @param tmpFilePath
	 * @return the identified sections
	 */
	private HashMap<Integer, DocumentSection> sectionIdentifier(String text) {
		String tmpFilePath = this.storeContent(text);
		if (tmpFilePath == null) return null;

		HashMap<Integer, DocumentSection> cSections = null;

		Runtime rt = Runtime.getRuntime();
		try {
			String command = "java -jar " + this.modelPath ;
			command += " -t " + this.templatePath ;
			command += " -x " + tmpFilePath ;
			command += " " + tmpFilePath;

			Process pr = rt.exec(command);
			pr.waitFor();

			cSections = this.xmlImporter(tmpFilePath  + ".xml");

			// when done destroy the process
			pr.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error creating tmp file: " + e.getMessage());
		}	

		return cSections;
	}

	/**
	 * Write the content of the document to a temp file
	 * @author andreea bodnari
	 * @param text
	 * @return the absolute path of the file
	 */
	private String storeContent(String text) {
		try {
			File temp = File.createTempFile("sectionsRaw", ".txt");
			temp.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(text);
			out.close();

			return temp.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error creating tmp file: " + e.getMessage());
		}		

		return null;
	}

	/**
	 * @author andreea bodnari
	 * @param filePath
	 */
	private HashMap<Integer, DocumentSection> xmlImporter(String filePath){
		File givenFile = new File(filePath);

		if(! givenFile.exists()) return null;

		HashMap<Integer, DocumentSection> sections = 
				new HashMap<Integer, DocumentSection>();

		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(givenFile);
			doc.getDocumentElement().normalize();
			NodeList children = doc.getChildNodes();
			int startLine = 0;

			for (int s = 0; s < children.getLength(); s++) {
				Node fstNode = children.item(s);

				NodeList subChildren  = fstNode.getChildNodes();

				for (int s1 = 0; s1 < subChildren.getLength(); s1 ++) {
					Node sndNode = subChildren.item(s1);
					String nodeName = sndNode.getNodeName();
					String content  = sndNode.getTextContent();

					if(nodeName== null || nodeName.trim().isEmpty() || 
							content == null || content.trim().isEmpty())
						continue;
					String[] splitContent = content.split("\n");
					int endLine = startLine + splitContent.length;

					DocumentSection section = 
							new DocumentSection(startLine, endLine, content);
					section.setSectionName(nodeName);
					sections.put(startLine, section);

					startLine = endLine ;
				}

			}

		}catch(Exception e){
			e.printStackTrace();
			logger.error("Error parsing tmp file: " + e.getMessage());
		}

		return sections;
	}
}