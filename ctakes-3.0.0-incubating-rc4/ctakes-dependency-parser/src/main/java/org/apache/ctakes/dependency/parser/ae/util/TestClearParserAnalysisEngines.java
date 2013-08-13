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
package org.apache.ctakes.dependency.parser.ae.util;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;

import org.apache.ctakes.dependency.parser.ae.ClearParserDependencyParserAE;
import org.apache.ctakes.dependency.parser.ae.ClearParserSemanticRoleLabelerAE;
import org.apache.ctakes.dependency.parser.util.DependencyUtility;
import org.apache.ctakes.dependency.parser.util.SRLUtility;
import org.apache.ctakes.typesystem.type.textspan.Sentence;

/**
 * This class illustrates the pipeline needed to run the ClearParser dependency parser and SRL systems
 * Note: This uses small, highly inaccurate model files, to keep the expense of running down.
 *       For real applications, use the model files recommended in the README.txt file, or leave the model file
 *       configuration parameter unspecified
 * @author lbecker
 *
 */
public class TestClearParserAnalysisEngines {
	public static String DEP_DUMMY_MODEL_FILE = "src/resources/dependency/dummy.dep.mod.jar";
	public static String SRL_DUMMY_MODEL_FILE = "src/resources/srl/dummy.srl.mod.jar";
	public static String INPUT_FILE = "../ctakes-clinical-pipeline/test/data/plaintext/testpatient_plaintext_1.txt";


	public static class Options extends Options_ImplBase {
		
		@Option(name = "-d",
				aliases = "--depModelFile",
				usage = "specify the path to the dependency parser model file",
				required = false)
		public File depModelFile = new File(DEP_DUMMY_MODEL_FILE);
		
		@Option(name = "-s",
				aliases = "--srlModelFile",
				usage = "specify the path to the clearparser srl model file",
				required = false)
		public File srlModelFile = new File(SRL_DUMMY_MODEL_FILE);
		
		
		@Option(name = "-i",
				aliases = "--inputFile",
				usage = "specify the path to the plaintext input",
				required = false)
		public File inputFile = new File(INPUT_FILE);
	}

	
	/**
	 * Simple inner class for dumping out ClearParser output
	 * @author lbecker
	 *
	 */
	public static class DumpClearParserOutputAE extends JCasAnnotator_ImplBase {

		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
				System.out.println("SOURCE SENTENCE:" + sentence.getCoveredText());
				System.out.println("Dependency Parse:");
				System.out.println(DependencyUtility.dumpDependencyGraph(sentence));
				System.out.println("Semantic Roles:");
				System.out.println(SRLUtility.dumpSRLOutput(sentence));
						
			}
		}
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, UIMAException, SAXException {
		Options options = new Options();
		options.parseOptions(args);

		File depModelFile = options.depModelFile;
		File srlModelFile = options.srlModelFile;
		File inputFile = options.inputFile;
		
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../ctakes-type-system/desc/common_type_system.xml");
				
		CollectionReader reader1 = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class,
				typeSystem,
				FilesCollectionReader.PARAM_ROOT_FILE,
				inputFile.toString()
				);
		
		// Load preprocessing pipeline (consists of 
		AnalysisEngine preprocessingAE = WriteClearParserDescriptors.getPlaintextAggregateBuilder().createAggregate();
		
		
		// Create dependency parsers analysis engine
		AnalysisEngine clearparserDepParser = AnalysisEngineFactory.createPrimitive(
				ClearParserDependencyParserAE.class,
				typeSystem,
				ClearParserDependencyParserAE.PARAM_PARSER_MODEL_FILE_NAME,
				depModelFile.toString()
				);
	
				
		// Create analysis engine for SRL
		AnalysisEngine clearparserSRL = AnalysisEngineFactory.createPrimitive(
				ClearParserSemanticRoleLabelerAE.class,
				typeSystem,
				ClearParserSemanticRoleLabelerAE.PARAM_PARSER_MODEL_FILE_NAME,
				srlModelFile.toString()
				);
		
	
		
		AnalysisEngine dumpClearParserOutput = AnalysisEngineFactory.createPrimitive(
				DumpClearParserOutputAE.class,
				typeSystem);
		
		SimplePipeline.runPipeline(reader1, preprocessingAE, clearparserDepParser, clearparserSRL, dumpClearParserOutput);
	}

}
