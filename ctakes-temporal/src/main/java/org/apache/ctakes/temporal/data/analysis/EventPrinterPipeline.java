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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.relationextractor.eval.XMIReader;
import org.apache.ctakes.typesystem.type.syntax.TerminalTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

/**
 * Print events of given UMLS semantic type.
 * 
 * @author dmitriy dligach
 */
public class EventPrinterPipeline {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File inputDirectory;

    @Option(
        name = "--umls-type",
        usage = "specify the UMLS semantic type (e.g. 5, i.e. procedure)",
        required = true)
    public int umlsSemanticType;

    @Option(
        name = "--event-output-file",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File eventOutputFile;
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		List<File> trainFiles = Arrays.asList(options.inputDirectory.listFiles());
    CollectionReader collectionReader = getCollectionReader(trainFiles);
		
    AnalysisEngine annotationConsumer = AnalysisEngineFactory.createPrimitive(
    		EventAndTokenContextWriter.class,
    		"UmlsSemanticType",
    		options.umlsSemanticType,
    		"EventOutputFile",
    		options.eventOutputFile);
    		
		SimplePipeline.runPipeline(collectionReader, annotationConsumer);
	}

	/**
	 * Print events with a given UMLS semantic type.
	 */
	public static class EventAndTokenContextWriter extends JCasAnnotator_ImplBase {

	  @ConfigurationParameter(
	      name = "EventOutputFile",
	      mandatory = true,
	      description = "path to the file that stores the events")
	  private String eventOutputFile;

	  @ConfigurationParameter(
	      name = "UmlsSemanticType",
	      mandatory = true,
	      description = "umls semantic type")
	  private int umlsSemanticType;
	  
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

	    BufferedWriter eventWriter = getWriter(eventOutputFile, true);
	    try {
	      for(EventMention eventMention : JCasUtil.select(goldView, EventMention.class)) {
	        
	        List<EntityMention> coveringEntityMentions = JCasUtil.selectCovered(
	            systemView, 
	            EntityMention.class, 
	            eventMention.getBegin(), 
	            eventMention.getEnd());
	        
	        for(EntityMention entityMention : coveringEntityMentions) {
	          if(entityMention.getTypeID() == umlsSemanticType) { 
	            String output = String.format(
	                "%s|%s\n", 
	                entityMention.getCoveredText().toLowerCase(),
	                expandToNP(systemView, eventMention).toLowerCase());
	            try {
	              eventWriter.write(output);
	            } catch (IOException e) {
	              throw new AnalysisEngineProcessException(e);
	            }
	          }
	        }
	      }
	    } finally {
	      try {
	        eventWriter.close();
	      } catch (IOException e) {
	        throw new AnalysisEngineProcessException(e);
	      }
	    }
	  }
	}

  public static BufferedWriter getWriter(String filePath, boolean append) {

    BufferedWriter bufferedWriter = null;
    try {
      FileWriter fileWriter = new FileWriter(filePath, append);
      bufferedWriter = new BufferedWriter(fileWriter);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bufferedWriter;
  }
  
	public static CollectionReader getCollectionReader(List<File> inputFiles) throws Exception {

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

	public static String expandToNP(JCas jCas, IdentifiedAnnotation identifiedAnnotation) {

	  // map each covering treebank node to its character length
	  Map<TreebankNode, Integer> treebankNodeSizes = new HashMap<TreebankNode, Integer>();
	  for(TreebankNode treebankNode : JCasUtil.selectCovering(
	      jCas, 
	      TreebankNode.class, 
	      identifiedAnnotation.getBegin(), 
	      identifiedAnnotation.getEnd())) {

	    // only expand nouns (and not verbs or adjectives)
	    if(treebankNode instanceof TerminalTreebankNode) {
	      if(! treebankNode.getNodeType().startsWith("N")) {
	        return identifiedAnnotation.getCoveredText();
	      }
	    }

	    // because only nouns are expanded, look for covering NPs
	    if(treebankNode.getNodeType().equals("NP")) {
	      treebankNodeSizes.put(treebankNode, treebankNode.getCoveredText().length());
	    }
	  }

	  // find the shortest covering treebank node
	  List<TreebankNode> sortedTreebankNodes = new ArrayList<TreebankNode>(treebankNodeSizes.keySet());
	  Function<TreebankNode, Integer> getValue = Functions.forMap(treebankNodeSizes);
	  Collections.sort(sortedTreebankNodes, Ordering.natural().onResultOf(getValue));

	  if(sortedTreebankNodes.size() > 0) {
	    return sortedTreebankNodes.get(0).getCoveredText();
	  } 

	  return identifiedAnnotation.getCoveredText();
	}
}
