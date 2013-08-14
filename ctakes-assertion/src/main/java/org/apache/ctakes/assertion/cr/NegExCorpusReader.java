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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.assertion.util.AssertionConst;
import org.apache.ctakes.core.knowtator.KnowtatorAnnotation;
import org.apache.ctakes.core.knowtator.KnowtatorXMLParser;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.core.util.SHARPKnowtatorXMLDefaults;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.BodyLaterality;
import org.apache.ctakes.typesystem.type.refsem.BodySide;
import org.apache.ctakes.typesystem.type.refsem.Course;
import org.apache.ctakes.typesystem.type.refsem.Date;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.refsem.LabReferenceRange;
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.refsem.MedicationDosage;
import org.apache.ctakes.typesystem.type.refsem.MedicationDuration;
import org.apache.ctakes.typesystem.type.refsem.MedicationForm;
import org.apache.ctakes.typesystem.type.refsem.MedicationFrequency;
import org.apache.ctakes.typesystem.type.refsem.MedicationRoute;
import org.apache.ctakes.typesystem.type.refsem.MedicationStatusChange;
import org.apache.ctakes.typesystem.type.refsem.MedicationStrength;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.ProcedureDevice;
import org.apache.ctakes.typesystem.type.refsem.ProcedureMethod;
import org.apache.ctakes.typesystem.type.refsem.Severity;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.relation.AffectsTextRelation;
import org.apache.ctakes.typesystem.type.relation.AspectualTextRelation;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.ComplicatesDisruptsTextRelation;
import org.apache.ctakes.typesystem.type.relation.DegreeOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.LocationOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.ManagesTreatsTextRelation;
import org.apache.ctakes.typesystem.type.relation.ManifestationOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.relation.ResultOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.TemporalTextRelation;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;
import org.apache.ctakes.typesystem.type.textsem.BodyLateralityModifier;
import org.apache.ctakes.typesystem.type.textsem.BodySideModifier;
import org.apache.ctakes.typesystem.type.textsem.ConditionalModifier;
import org.apache.ctakes.typesystem.type.textsem.CourseModifier;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.GenericModifier;
import org.apache.ctakes.typesystem.type.textsem.HistoryOfModifier;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabEstimatedModifier;
import org.apache.ctakes.typesystem.type.textsem.LabInterpretationModifier;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.LabReferenceRangeModifier;
import org.apache.ctakes.typesystem.type.textsem.LabValueModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationAllergyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDosageModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDurationModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFormModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFrequencyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationRouteModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStatusChangeModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStrengthModifier;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.PolarityModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureDeviceModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMethodModifier;
import org.apache.ctakes.typesystem.type.textsem.SeverityModifier;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.ctakes.typesystem.type.textsem.SubjectModifier;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textsem.UncertaintyModifier;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.jdom2.JDOMException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * assumes knowtator xml files are in "exported-xml" subdirectory
 * and the original plaintext files are in "text" subdirectory
 *
 */
public class NegExCorpusReader extends JCasAnnotator_ImplBase {
  static Logger LOGGER = Logger.getLogger(NegExCorpusReader.class);
  
  public static final String PARAM_TEXT_DIRECTORY = "TextDirectory";
  @ConfigurationParameter(
      name = PARAM_TEXT_DIRECTORY,
      description = "directory containing the text files (if DocumentIDs are just filenames); "
          + "defaults to assuming that DocumentIDs are full file paths")
  private File textDirectory;
  
  public static final String PARAM_SET_DEFAULTS = "SetDefaults";
  @ConfigurationParameter(
      name = PARAM_SET_DEFAULTS,
      description = "whether or not to set default attribute values if no annotation is present")
  private boolean setDefaults;

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
  
  /**
   * Get the URI that the text in this class was loaded from
   */
  protected URI getTextURI(JCas jCas) throws AnalysisEngineProcessException {

	  String textPath = JCasUtil.selectSingle(jCas, DocumentID.class).getDocumentID();
	  if (this.textDirectory != null) {
		  textPath = this.textDirectory + File.separator +  textPath;
	  }

	  URI uri;
	  try {
		  uri = new URI(textPath);
	  } catch (URISyntaxException e) {
		  throw new AnalysisEngineProcessException(e);
	  }

	  //LOGGER.info("textPath = " + textPath);
	  //LOGGER.info("uri = " + uri);
	  
	  
	  
	  
	  //File tmpFile = new File(textPath); // Note this does not work with something like "file:/C:/usr/data/MiPACQ/1/xml/0054074073-0.xml"
	  //LOGGER.info("tmpFile = " + tmpFile);
	  //URI answer = tmpFile.toURI();
	  //LOGGER.info("answer = " + answer);

	  return uri;

  }
  


/**
   * Returns the names of the annotators in the Knowtator files that represent the gold standard
   */
  protected static String[] getAnnotatorNames() {
    return new String[] { "cTAKES , Mayo Clinic", "CU annotator ,", "consensus set annotator team" , "cons annotator team", "cons team", "team" }; // these three are what are used by MiPACQ gold standard
  }
  

  private static List<String> getDiseaseDisorderKnowtatorClasses() {
	  return Arrays.asList(new String [] {"Disorders"}); 
  }
  
  
  private static List<String> getSignSymptomKnowtatorClasses() {
	  return Arrays.asList(new String [] {"Sign_Symptom", "Finding"}); 
  }
  
  private static List<String> getProcedureKnowtatorClasses() {
	  return Arrays.asList(new String [] {
			  "Diagnostic_procedure",
			  "Laboratory_procedure",
			  "Procedures",
			  "Therapeutic_or_preventive_procedure",
			  "Intervention",
			  "Health_care_activity",
			  "Research_activity"}); 
  }
  
  private static List<String> getMedicationKnowtatorClasses() {
	  return Arrays.asList(new String [] {"Chemicals_and_drugs", "Pharmacologic_substance"}); 
  }
  
  private static List<String> getAnatomyKnowtatorClasses() {
	  return Arrays.asList(new String [] {"Anatomy"}); 
  }


  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
	  //
  }
  
  static String format(Annotation ann) {
    String result;
    if (ann.getEnd() == Integer.MIN_VALUE || ann.getBegin() == Integer.MAX_VALUE) {
      result = "<no-spanned-text>";
    } else {
      result = String.format("\"%s\"[%d,%d]", ann.getCoveredText(), ann.getBegin(), ann.getEnd());
    }
    return String.format("%s(%s)", ann.getClass().getSimpleName(), result);
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
