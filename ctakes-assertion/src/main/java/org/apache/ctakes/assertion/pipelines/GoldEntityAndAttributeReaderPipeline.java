/*
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
package org.apache.ctakes.assertion.pipelines;


import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

import org.apache.ctakes.assertion.cr.GoldEntityAndAttributeReader;
import org.apache.ctakes.core.ae.SHARPKnowtatorXMLReader;
import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;

/**
 * 
 * A class for testing the reader for the gold standard relation data. 
 * Currently this class runs the reader and saves the resulting annotations as xmi files.
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityAndAttributeReaderPipeline {

	public static void main(String[] args) throws UIMAException, IOException {
		
		TypeSystemDescription typeSystemDescription = 
				// use the uimafit method of finding available type system
				// descriptor via META-INF/org.uimafit/types.txt 
				// (found in ctakes-type-system/src/main/resources)
			TypeSystemDescriptionFactory.createTypeSystemDescription();
		
		CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
				FilesInDirectoryCollectionReader.class,
				typeSystemDescription,
				"InputDirectory",
//				"/Users/m081914/work/data/sharp/Seed_Corpus/Mayo/UMLS_CEM/ss1_batch10/Knowtator/text"
				"/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/sharp_data/one/Knowtator/text"
				//"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/text"
				);
		
//		AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
//				GoldEntityAndAttributeReader.class,
//				typeSystemDescription,
//				"InputDirectory",
////				"/Users/m081914/work/data/sharp/Seed_Corpus/Mayo/UMLS_CEM/ss1_batch10/Knowtator XML/");
//				"/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/sharp_data/one/xml/");
////				"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/knowtator/");

		AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
				SHARPKnowtatorXMLReader.class,
				typeSystemDescription,
				"KnowtatorURI",
				"/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/sharp_data/one/Knowtator_XML",
				"TextURI",
				"/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/sharp_data/one/Knowtator/text");
		
		AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
        XWriter.class,
        typeSystemDescription,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
//        "/Users/m081914/work/data/sharp/Seed_Corpus/Mayo/UMLS_CEM/ss1_batch10/Knowtator XMI/",
    	"/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/sharp_data/one/Knowtator_XMI/",
        // "/work/medfacts/sharp/data/2012-10-09_full_data_set/batch02",
//        "/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/xmi",
        XWriter.PARAM_FILE_NAMER_CLASS_NAME,
        CtakesFileNamer.class.getName()
        );
    
		SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter);
	}
}
