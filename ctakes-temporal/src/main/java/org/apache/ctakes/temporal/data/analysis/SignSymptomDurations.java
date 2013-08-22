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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.ctakes.relationextractor.eval.XMIReader;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

/**
 * ...
 * 
 * @author dmitriy dligach
 */
public class SignSymptomDurations {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the xmi files",
        required = true)
    public File inputDirectory;
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		List<File> trainFiles = Arrays.asList(options.inputDirectory.listFiles());
    CollectionReader collectionReader = getCollectionReader(trainFiles);
		
    AnalysisEngine durationPrinter = AnalysisEngineFactory.createPrimitive(
    		DurationPrinter.class);
    		
		SimplePipeline.runPipeline(collectionReader, durationPrinter);
	}
  
  public static class DurationPrinter extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

      JCas systemView;
      try {
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }   

      Collection<DocumentID> ids = JCasUtil.select(systemView, DocumentID.class);
      String fileName = ids.iterator().next().getDocumentID();
      String signSymptomText = fileName.split("\\.")[0]; // e.g. "smoker.txt"
      
      for(SignSymptomMention signSymptomMention : JCasUtil.select(systemView, SignSymptomMention.class)) {
        if(signSymptomMention.getCoveredText().equals(signSymptomText)) {
          for(TimeMention timeMention : JCasUtil.selectFollowing(systemView, TimeMention.class, signSymptomMention, 1)) {
            System.out.println(signSymptomMention.getCoveredText() + ": " + timeMention.getCoveredText());
          }
        }
      }
    }
  }
  
  private static CollectionReader getCollectionReader(List<File> items) throws Exception {

    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }
    
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        XMIReader.PARAM_FILES,
        paths);
  }
}
