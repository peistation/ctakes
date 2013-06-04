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
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
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
 * Print gold standard relations and their context.
 * 
 * @author dmitriy dligach
 */
public class RelationContextViewerPipeline {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the xmi files",
        required = true)
    public File inputDirectory;

    @Option(
        name = "--output-file",
        usage = "specify the path to the file to which the output will be written",
        required = true)
    public File outputFile;
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		List<File> trainFiles = Arrays.asList(options.inputDirectory.listFiles());
    CollectionReader collectionReader = getCollectionReader(trainFiles);
		
    AnalysisEngine annotationConsumer = AnalysisEngineFactory.createPrimitive(
    		RelationContextPrinter.class,
    		"OutputFile",
    		options.outputFile);
    		
		SimplePipeline.runPipeline(collectionReader, annotationConsumer);
	}
	  
  /**
   * Print gold standard relations and their context.
   * 
   * @author dmitriy dligach
   */
  public static class RelationContextPrinter extends JCasAnnotator_ImplBase {

    @ConfigurationParameter(
        name = "OutputFile",
        mandatory = true,
        description = "path to the file that stores relation data")
    private String outputFile;
    
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

      BufferedWriter writer = getWriter(outputFile, true);
      try {
        for(BinaryTextRelation binaryTextRelation : JCasUtil.select(goldView, BinaryTextRelation.class)) {
          
          Annotation arg1 = binaryTextRelation.getArg1().getArgument();
          Annotation arg2 = binaryTextRelation.getArg2().getArgument();
      
          String category = binaryTextRelation.getCategory();
          String text = getTextBetweenAnnotations(systemView, arg1, arg2);
          String output = String.format("%s|%s|%s|%s\n", category, arg1.getCoveredText(), arg2.getCoveredText(), text);
      
          try {
            writer.write(output);
          } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
          }
        }
      } finally {      
        try {
          writer.close();
        } catch (IOException e) {
          throw new AnalysisEngineProcessException(e);
        }
      }
    }
    
    private static String getTextBetweenAnnotations(JCas jCas, Annotation arg1, Annotation arg2) {
      
      int begin = Math.min(arg1.getBegin(), arg2.getBegin());
      int end = Math.max(arg1.getEnd(), arg2.getEnd());
      
      return jCas.getDocumentText().substring(begin, end).replaceAll("[\r\n]", " ");
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
