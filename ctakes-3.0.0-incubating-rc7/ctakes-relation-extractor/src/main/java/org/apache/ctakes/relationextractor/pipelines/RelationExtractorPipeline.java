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
package org.apache.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * A simple pipeline that runs relation extraction on all files in a directory and saves
 * the resulting annotations as XMI files. The core part of this pipeline is the aggregate
 * relation extractor AE which runs all the preprocessing that is necessary for relation
 * extraction as well as the AEs that extract relations.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorPipeline {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public String inputDirectory;
    
    @Option(
        name = "--output-dir",
        usage = "specify the path to the directory where the output xmi files are to be saved",
        required = true)
    public String outputDirectory;
  }
  
	public static void main(String[] args) throws UIMAException, IOException {
		
		Options options = new Options();
		options.parseOptions(args);

		CollectionReader collectionReader = CollectionReaderFactory.createCollectionReaderFromPath(
				"../ctakes-core/desc/collection_reader/FilesInDirectoryCollectionReader.xml",
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR,
				options.inputDirectory);

		// make sure the model parameters match those used for training
		AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
				"desc/analysis_engine/RelationExtractorAggregate.xml");
    
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
    		XWriter.class,
    		XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
    		options.outputDirectory);
		
		SimplePipeline.runPipeline(collectionReader, relationExtractor, xWriter);
	}
}
