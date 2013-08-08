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
package org.apache.ctakes.constituency.parser.util;

import java.io.IOException;
import java.util.Scanner;

import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.JCasFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

public class CommandLineParserUtil {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws UIMAException, IOException {
		TypeSystemDescription types = TypeSystemDescriptionFactory.createTypeSystemDescription();
		
		AnalysisEngine ae = AnalysisEngineFactory.createAnalysisEngineFromPath("desc/analysis_engine/AggregateParsingProcessor.xml");
		
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNextLine()){
			JCas jcas = JCasFactory.createJCas(types);
			jcas.setDocumentText(scanner.nextLine());
			SimplePipeline.runPipeline(jcas, ae);
			FSIterator iter = jcas.getAnnotationIndex(TopTreebankNode.type).iterator();
			TopTreebankNode parse = (TopTreebankNode) iter.next();
			System.out.println(parse.getTreebankParse());
		}
	}

}
