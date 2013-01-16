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


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.log4j.Logger;
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
 * @author matt coarr
 *
 */
public class GoldEntityAndAttributeReaderPipelineForSeedCorpus {
	
	static final Logger logger = Logger.getLogger(GoldEntityAndAttributeReaderPipelineForSeedCorpus.class.getName());

	public static void main(String[] args) throws UIMAException, IOException {
		
		logger.warn("This should be run with one command-line argument that is the parent UMLS_CEM directory.");
		logger.warn("Also, make sure each ss1_batch* directory has both a Knowtator/text directory and a Knowtator_XML directory (not the underscore in the xml directory, not a space)");
		
		if (args.length != 1)
		{
			System.out.println("Requires one parameter that is the UMLS_CEM main directory (e.g. the \"Seed_Corpus/Mayo/UMLS_CEM\" or \"Seattle Group Health/UMLS_CEM\"). The path should be fully specified.");
		}
		
		String parentDirectoryString = args[0];
		//String parentDirectoryString = "/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/Seattle Group Health/UMLS_CEM";
		logger.info("parent directory: " + parentDirectoryString);
		File parentDirectory = new File(parentDirectoryString);
		if (!parentDirectory.exists())
		{
			logger.fatal("parent directory does not exist! exiting!");
			return;
		}
		
		File batchDirectories[] = parentDirectory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		for (File currentBatchDirectory : batchDirectories)
		{
			
			logger.info("current batch directory: " + currentBatchDirectory.getName());
			
			if (!currentBatchDirectory.exists())
			{
				logger.fatal(String.format("current batch directory does not exist! exiting! [\"%s\"]", currentBatchDirectory.toString()));
				continue;
			}
			
			File knowtatorDirectory = new File(currentBatchDirectory, "Knowtator");
			File textDirectory = new File(knowtatorDirectory, "text");
			File xmlDirectory = new File(currentBatchDirectory, "Knowtator_XML");
			File xmiDirectory = new File(currentBatchDirectory, "Knowtator_XMI");
			
			if (!knowtatorDirectory.isDirectory() ||
					!textDirectory.isDirectory() ||
					!xmlDirectory.isDirectory())
			{
				logger.error("one of the directories does not exist! skipping...");
				continue;
			}
			
			if (!xmiDirectory.isDirectory())
			{
				xmiDirectory.mkdir();
			}
				
			
			TypeSystemDescription typeSystemDescription = 
					// use the uimafit method of finding available type system
					// descriptor via META-INF/org.uimafit/types.txt 
					// (found in ctakes-type-system/src/main/resources)
				TypeSystemDescriptionFactory.createTypeSystemDescription();
			
			CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
					FilesInDirectoryCollectionReader.class,
					typeSystemDescription,
					"InputDirectory",
					//"/Users/m081914/work/data/sharp/Seed Corpus/Mayo/UMLS_CEM/ss1_batch04/Knowtator/text"
					//"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/text"
					textDirectory.toString()
					);
			
//			AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
//					GoldEntityAndAttributeReader.class,
//					typeSystemDescription,
//					"InputDirectory",
//					//"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/knowtator/"
//					xmlDirectory.toString() + "/"
//					);
			AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
					SHARPKnowtatorXMLReader.class,
					typeSystemDescription,
					"TextURI",
					//"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/knowtator/"
					textDirectory.toString() + "/"
			);
			
	    AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
	        XWriter.class,
	        typeSystemDescription,
	        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
//	        "/Users/m081914/work/sharpattr/ctakes/ctakes-assertion/data/output",
	        // "/work/medfacts/sharp/data/2012-10-09_full_data_set/batch02"
	        //"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/xmi",
	        xmiDirectory.toString(),
	        XWriter.PARAM_FILE_NAMER_CLASS_NAME,
	        CtakesFileNamer.class.getName()
	        );
	    
			SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter);
		}
		
		logger.info("Finished!");
		
	}
}
