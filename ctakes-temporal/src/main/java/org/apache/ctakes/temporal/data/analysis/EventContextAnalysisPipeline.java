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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ctakes.relationextractor.eval.XMIReader;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

/**
 * Print events and tokens with contexts to two separeate files.
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
    		EventAndTokenContextWriter.class,
    		"TokenOutputFile",
    		options.tokenOutputFile,
    		"EventOutputFile",
    		options.eventOutputFile,
    		"ContextSize",
    		options.contextSize);
    		
		SimplePipeline.runPipeline(collectionReader, annotationConsumer);
	}

	/**
	 * Print all tokens with contexts and all events with contexts for further analysis.
	 */
	public static class EventAndTokenContextWriter extends JCasAnnotator_ImplBase {

	  @ConfigurationParameter(
	      name = "TokenOutputFile",
	      mandatory = true,
	      description = "path to the file that stores token contexts")
	  private String tokenOutputFile;

	  @ConfigurationParameter(
	      name = "EventOutputFile",
	      mandatory = true,
	      description = "path to the file that stores event contexts")
	  private String eventOutputFile;
	  
	  @ConfigurationParameter(
	      name = "ContextSize",
	      mandatory = true,
	      description = "context size in characters")
	  private int contextSize;
	  
	  @Override
	  public void process(JCas jCas) throws AnalysisEngineProcessException {
	    
	    JCas goldView;
	    try {
	      goldView = jCas.getView("GoldView");
	    } catch (CASException e) {
	      throw new AnalysisEngineProcessException(e);
	    }
	    
	    JCas systemView;
	    try {
	      systemView = jCas.getView("_InitialView");
	    } catch (CASException e) {
	      throw new AnalysisEngineProcessException(e);
	    }

	    BufferedWriter tokenWriter = getWriter(tokenOutputFile, true);
	    BufferedWriter eventWriter = getWriter(eventOutputFile, true);
	    try {
	      for(BaseToken baseToken : JCasUtil.select(systemView, BaseToken.class)) {
	        String tokenText = baseToken.getCoveredText().toLowerCase();
	        String output = String.format("%s|%s\n", tokenText, getAnnotationContext(baseToken, contextSize));
	        
	        try {
	          tokenWriter.write(output);
	        } catch (IOException e) {
	          throw new AnalysisEngineProcessException(e);
	        }
	      } 
	  
	      for(EventMention eventMention : JCasUtil.select(goldView, EventMention.class)) {
	        String eventText = eventMention.getCoveredText().toLowerCase();
	        String output = String.format("%s|%s\n", eventText, getAnnotationContext(eventMention, contextSize));
	        
	        try {
	          eventWriter.write(output);
	        } catch (IOException e) {
	          throw new AnalysisEngineProcessException(e);
	        }
	      }
	    } finally {
	      try {
	        tokenWriter.close();
	        eventWriter.close();
	      } catch (IOException e) {
	        throw new AnalysisEngineProcessException(e);
	      }
	    }
	  }
	  
	  private static String getAnnotationContext(Annotation annotation, int maxContextWindowSize) {
	    
	    String text = annotation.getCAS().getDocumentText();
	    int begin = Math.max(0, annotation.getBegin() - maxContextWindowSize);
	    int end = Math.min(text.length(), annotation.getEnd() + maxContextWindowSize);
	    
	    return text.substring(begin, end).replaceAll("[\r\n]", " ");
	  }
	  
	  private static BufferedWriter getWriter(String filePath, boolean append) {

	    BufferedWriter bufferedWriter = null;
	    try {
	      FileWriter fileWriter = new FileWriter(filePath, append);
	      bufferedWriter = new BufferedWriter(fileWriter);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }

	    return bufferedWriter;
	  }
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
