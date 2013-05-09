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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

/**
 * Print all tokens with contexts and all events with contexts for further analysis.
 * 
 * @author dmitriy dligach
 *
 */
public class EventContextAnalysisConsumer extends JCasAnnotator_ImplBase {

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
