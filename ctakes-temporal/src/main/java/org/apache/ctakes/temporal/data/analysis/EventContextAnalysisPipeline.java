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
package org.apache.ctakes.temporal.data.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ctakes.relationextractor.eval.XMIReader;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * 
 * @author dmitriy dligach
 */
public class EventContextAnalysisPipeline {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File inputDirectory;

    @Option(
        name = "--token-output-file",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File tokenOutputFile;

    @Option(
        name = "--event-output-file",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File eventOutputFile;
    
    @Option(
        name = "--context-size",
        usage = "specify the number of characters to include on both sides",
        required = false)
    public int contextSize = 50;
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		List<File> trainFiles = Arrays.asList(options.inputDirectory.listFiles());
    CollectionReader collectionReader = getCollectionReader(trainFiles);
		
    AnalysisEngine annotationConsumer = AnalysisEngineFactory.createPrimitive(
    		EventContextAnalysisConsumer.class,
    		"TokenOutputFile",
    		options.tokenOutputFile,
    		"EventOutputFile",
    		options.eventOutputFile,
    		"ContextSize",
    		options.contextSize);
    		
		SimplePipeline.runPipeline(collectionReader, annotationConsumer);
	}
	
  private static CollectionReader getCollectionReader(List<File> inputFiles) throws Exception {
    
    List<String> fileNames = new ArrayList<String>();
    for(File file : inputFiles) {
      if(! (file.isHidden())) {
        fileNames.add(file.getPath());
      }
    }
    
    String[] paths = new String[fileNames.size()];
    fileNames.toArray(paths);
    
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        XMIReader.PARAM_FILES,
        paths);
  }
}
