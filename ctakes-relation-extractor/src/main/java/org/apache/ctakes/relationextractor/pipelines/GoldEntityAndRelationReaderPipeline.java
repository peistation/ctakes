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

import java.io.File;
import java.io.IOException;

import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;
import org.apache.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.apache.ctakes.relationextractor.eval.PreprocessAndWriteXmi.Options;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.kohsuke.args4j.Option;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * 
 * A class for testing the reader for the gold standard relation data. 
 * Currently this class runs the reader and saves the resulting annotations as xmi files.
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityAndRelationReaderPipeline {

  @Option(name = "-t", 
      aliases = "--textRoot", 
      usage = "specify the directory contraining the textFiles (for example /NLP/Corpus/Relations/mipacq/text/train",
      required = true)
  public File textRoot;

  @Option(name = "-x",
      aliases = "--xmlRoot",
      usage = "specify the directory containing the knowtator xml files (for example: /NLP/Corpus/Relations/mipacq/xml/train",
      required = true)
  public File xmlRoot;

  @Option(name = "-o",
      aliases = "--outputRoot",
      usage = "specify the directory to write out CAS XMI files",
      required = true)
  public File outputRoot;

  public static void main(String[] args) throws UIMAException, IOException {
	
    Options options = new Options();
    options.parseOptions(args);
  
    File textRoot = options.textRoot;
    File xmlRoot = options.xmlRoot;
    File outputRoot = options.outputRoot;

	  CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
	      FilesInDirectoryCollectionReader.class,
	      "InputDirectory",
	      textRoot.getPath());

	  AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
	      GoldEntityAndRelationReader.class,
	      "InputDirectory",
	      xmlRoot.getPath());

	  AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
	      XWriter.class,
	      XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
	      outputRoot.getPath());

	  SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter);
	}
}
