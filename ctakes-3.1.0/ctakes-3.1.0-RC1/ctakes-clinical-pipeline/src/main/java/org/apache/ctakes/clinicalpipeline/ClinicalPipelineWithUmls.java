package org.apache.ctakes.clinicalpipeline;
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


import java.io.File;
import java.util.Date;

import org.apache.ctakes.assertion.util.AssertionConst;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.util.cr.FilesCollectionReader;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Run the plaintext clinical pipeline, using the dictionary of terms from UMLS.
 * Note you must have the UMLS password supplied in some way - see the 
 * User or Developer Guide for information on options for doing that.
 * Also note you need to have the UMLS dictionaries available (they 
 * are separate download from Apache cTAKES itself due to licensing).
 * 
 * Input and output directory names are taken from {@link AssertionConst} 
 * 
 */
public class ClinicalPipelineWithUmls {

	public File inputDirectory;

	public static void main(String[] args) throws Exception {

	    System.out.println("Started " + ClinicalPipelineWithUmls.class.getCanonicalName() + " at " + new Date());

		//String documentText = "Text of document to test goes here, such as the following. No edema, some soreness, denies pain.";
		//InputStream inStream = InputStreamCollectionReader.convertToByteArrayInputStream(documentText);
		//CollectionReader collectionReader = InputStreamCollectionReader.getCollectionReader(inStream);
		
		CollectionReader collectionReader = FilesCollectionReader.getCollectionReader(AssertionConst.CORPUS_WO_GOLD_STD_TO_RUN_THROUGH_CTAKES);

		System.out.println("Reading from directory: " + AssertionConst.CORPUS_WO_GOLD_STD_TO_RUN_THROUGH_CTAKES);
		System.out.println("Outputting to directory: " + AssertionConst.evalOutputDir);
		
		AnalysisEngineDescription pipelineIncludingUmlsDictionaries = AnalysisEngineFactory.createAnalysisEngineDescription(
				"desc/analysis_engine/AggregatePlaintextUMLSProcessor");

		AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
				XWriter.class,
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
				AssertionConst.evalOutputDir,
	            XWriter.PARAM_XML_SCHEME_NAME,
	            XWriter.XMI,
	            XWriter.PARAM_FILE_NAMER_CLASS_NAME,
	            CtakesFileNamer.class.getName());

		SimplePipeline.runPipeline(collectionReader, pipelineIncludingUmlsDictionaries, xWriter);
		
	    System.out.println("Done at " + new Date());
	}


}
