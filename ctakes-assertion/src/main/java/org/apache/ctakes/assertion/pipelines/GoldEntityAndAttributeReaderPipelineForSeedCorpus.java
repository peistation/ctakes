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
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

import org.apache.ctakes.assertion.cr.GoldEntityAndAttributeReader;
import org.apache.ctakes.assertion.cr.I2B2Challenge2010CollectionReader;
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

		File parentDirectory = new File(parentDirectoryString);
		readSharpUmlsCem(parentDirectory);
		
	}

	public static void readSharpUmlsCem(File parentDirectory) throws ResourceInitializationException, UIMAException, IOException {
		readSharpUmlsCem(parentDirectory, null, null, null);
	}
	
	public static void readSharpUmlsCem(File parentDirectory, File trainDirectory, File testDirectory, File devDirectory)
			throws ResourceInitializationException, UIMAException, IOException {
//		logger.info("parent directory: " + parentDirectoryString);
//		File parentDirectory = new File(parentDirectoryString);
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
			// train set uses this naming convention
			File xmlDirectory = new File(currentBatchDirectory, "Knowtator_XML");
			File xmiDirectory = new File(currentBatchDirectory, "Knowtator_XMI");
			// dev and test sets use this naming convention
			if (!xmlDirectory.exists()) {
				xmlDirectory = new File(currentBatchDirectory, "Knowtator XML");
				xmiDirectory = new File(currentBatchDirectory, "Knowtator XMI");
			}
			
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
			
			AggregateBuilder aggregate = new AggregateBuilder();
			
			CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
					FilesInDirectoryCollectionReader.class,
					typeSystemDescription,
					"InputDirectory",
					textDirectory.toString()
					);
			
			// read the UMLS_CEM data from Knowtator
			AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
					SHARPKnowtatorXMLReader.class,
					typeSystemDescription,
					"TextDirectory", // 3/13/13 halgrim changed from "TextURI" trying to work with new SHARPKnowtatorXMLReader.java
					//"/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/sandbox/batch02_mayo/knowtator/"
					textDirectory.toString() + "/"
			);
			aggregate.add(goldAnnotator);

			// write just the XMI version of what's in Knowtator UMLS_CEM
			AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
					XWriter.class,
					typeSystemDescription,
					XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
					xmiDirectory.toString(),
					XWriter.PARAM_FILE_NAMER_CLASS_NAME,
					CtakesFileNamer.class.getName()
			);
			aggregate.add(xWriter);

			// fill in other values that are necessary for preprocessing
			AnalysisEngineDescription preprocessAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(
					"desc/analysis_engine/AttributeDiscoveryPreprocessor"
					);
			aggregate.add(preprocessAnnotator);
			
			if (trainDirectory!=null && testDirectory!=null && devDirectory!=null) {
				File subcorpusDirectory;
				switch (SharpCorpusSplit.split(currentBatchDirectory)) {
				case TRAIN: 
					subcorpusDirectory = trainDirectory;
					break;
				case TEST:
					subcorpusDirectory = testDirectory;
					break;
				case DEV:
					subcorpusDirectory = devDirectory;
					break;
				case CROSSVAL:
					subcorpusDirectory = trainDirectory;
					break;
				default:
					subcorpusDirectory = trainDirectory;
					break;
				}
				AnalysisEngineDescription xWriter2 = AnalysisEngineFactory.createPrimitiveDescription(
						XWriter.class,
						typeSystemDescription,
						XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
						subcorpusDirectory,
						XWriter.PARAM_FILE_NAMER_CLASS_NAME,
						CtakesFileNamer.class.getName()
				);
				aggregate.add(xWriter2);
//				SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter, xWriter2);
			}

			SimplePipeline.runPipeline(collectionReader, aggregate.createAggregateDescription());
		}

		logger.info("Finished!");
	}
	
	public static void readI2B2Challenge2010(File parentDirectory, File trainDirectory)
	throws ResourceInitializationException, UIMAException, IOException {

		TypeSystemDescription typeSystemDescription = 
			// use the uimafit method of finding available type system
			// descriptor via META-INF/org.uimafit/types.txt 
			// (found in ctakes-type-system/src/main/resources)
			TypeSystemDescriptionFactory.createTypeSystemDescription();

		AggregateBuilder aggregate = new AggregateBuilder();

		CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
				I2B2Challenge2010CollectionReader.class,
				typeSystemDescription,
				"inputDir",
				parentDirectory
		);

		// fill in other values that are necessary for preprocessing
		AnalysisEngineDescription preprocessAnnotator = AnalysisEngineFactory.createAnalysisEngineDescription(
				"desc/analysis_engine/AttributeDiscoveryPreprocessor"
		);
		aggregate.add(preprocessAnnotator);

		if (trainDirectory!=null) {
			AnalysisEngineDescription xWriter2 = AnalysisEngineFactory.createPrimitiveDescription(
					XWriter.class,
					typeSystemDescription,
					XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
					trainDirectory,
					XWriter.PARAM_FILE_NAMER_CLASS_NAME,
					CtakesFileNamer.class.getName()
			);
			aggregate.add(xWriter2);
			//		SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter, xWriter2);
		}

		SimplePipeline.runPipeline(collectionReader, aggregate.createAggregateDescription());
		logger.info("Finished!");
	}

}
