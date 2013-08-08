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
package org.apache.ctakes.constituency.parser.ae;

import java.io.FileNotFoundException;

import org.apache.ctakes.constituency.parser.MaxentParserWrapper;
import org.apache.ctakes.constituency.parser.ParserWrapper;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;


public class ConstituencyParser extends JCasAnnotator_ImplBase {
	public static final String PARAM_MODELFILE = "modelFilename";
	ParserWrapper parser = null;
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		String modelFileOrDirname = (String) aContext.getConfigParameterValue(PARAM_MODELFILE);
		try {
					logger.info("Initializing parser...");
					parser = new MaxentParserWrapper(FileLocator.locateFile(modelFileOrDirname).getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Error reading parser model file/directory: " + e.getMessage());
		}
	}


	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
			parser.createAnnotations(jcas);
	}
}
