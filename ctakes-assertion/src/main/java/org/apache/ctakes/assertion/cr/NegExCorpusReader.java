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
package org.apache.ctakes.assertion.cr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.assertion.util.AssertionConst;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;

import com.google.common.collect.Maps;

/**
 * assumes knowtator xml files are in "exported-xml" subdirectory
 * and the original plaintext files are in "text" subdirectory
 *
 */
public class NegExCorpusReader extends JCasAnnotator_ImplBase {
  static Logger LOGGER = Logger.getLogger(NegExCorpusReader.class);
  

  private static final Map<String, String> SUBJECT_KNOWTATOR_TO_UIMA_MAP;
  static {
    SUBJECT_KNOWTATOR_TO_UIMA_MAP = Maps.newHashMap();
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("C0030705", CONST.ATTR_SUBJECT_PATIENT);
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("patient", CONST.ATTR_SUBJECT_PATIENT);
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("family_member", CONST.ATTR_SUBJECT_FAMILY_MEMBER);
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("donor_family_member", CONST.ATTR_SUBJECT_DONOR_FAMILY_MEMBER);
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("donor_other", CONST.ATTR_SUBJECT_DONOR_OTHER);
    SUBJECT_KNOWTATOR_TO_UIMA_MAP.put("other", CONST.ATTR_SUBJECT_OTHER);
  }
  

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
	  //
  }
  
  
  
  
  /**
   * This main method is only for testing purposes. It runs the reader on Knowtator directories.
   * 	args[0] = "/usr/data/MiPACQ/copies-of-just-clinical-knowtator-xml-and-text/";
   * should have a child directory called "text"
   * should have a child directory called "exported-xml"
   * files in knowtator xml directory should have files that end with .xml
   */
  public static void main(String[] args) throws Exception {

	  String filename;
	  if (args.length != 0) {
		  filename = args[0];
	  } else {
		  try {
			  LOGGER.warn(String.format(
					  "usage: java %s path/to/negex/file ",
					  NegExCorpusReader.class.getName()));
		  } catch (IllegalArgumentException e) {
			  e.printStackTrace();
		  }
		  Exception e = new RuntimeException("Going to continue with default values");
		  LOGGER.warn(e.getLocalizedMessage());
		  filename = AssertionConst.NEGEX_CORPUS;
	  }

	  AnalysisEngine negexReader = AnalysisEngineFactory.createPrimitive(NegExCorpusReader.class);

	  AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
			  XWriter.class,
			  XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
			  AssertionConst.NEGEX_CORPUS_PREPROCESSED,
			  XWriter.PARAM_FILE_NAMER_CLASS_NAME,
			  CtakesFileNamer.class.getName()
			  );

	  // For each line of data in the file that contains the negex corpus, parse the line and process the data.
	  String [] lines = readNonWhiteSpaceLines(filename);
	  int n = lines.length;
	  LOGGER.info("Processing " + n + " lines from the negex file, treating each line as a document.");
	  
	  for (String data : lines) {
		  LOGGER.info("Processing line '" + data + "'.");
		  try {
			  NegExAnnotation a = new NegExAnnotation(data);
			  JCas jCas = negexReader.newJCas();
			  jCas.setDocumentText(a.sentenceText);
			  DocumentID documentID = new DocumentID(jCas);
			  documentID.setDocumentID("doc" + a.lineNumber);
			  documentID.addToIndexes();
			  IdentifiedAnnotation ia = new IdentifiedAnnotation(jCas);
			  ia.setBegin(Integer.parseInt(a.begin));
			  ia.setEnd(Integer.parseInt(a.end));
			  ia.setPolarity(Integer.parseInt(a.polarity));
			  ia.addToIndexes();
			  xWriter.process(jCas);
		  } catch (RuntimeException e) {
			  LOGGER.warn("Skipping this one because of RuntimeException");
		  } 
	  }

  }



  private static String[] readNonWhiteSpaceLines(String filename) {
	  List<String> lines = new ArrayList<String>();
	  BufferedReader br  = null;
	  try {
		  br = new BufferedReader(new FileReader(filename));
		  String line;
		  while ((line=br.readLine())!=null) {
			  if (line.trim().length()>0) {
				  lines.add(line);
			  }
		  }
	  } catch (Exception e) {
		  //
	  } finally {
		  if (br!=null)
			try {
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	  }
	  return lines.toArray(new String[0]);
	  
  }
}
