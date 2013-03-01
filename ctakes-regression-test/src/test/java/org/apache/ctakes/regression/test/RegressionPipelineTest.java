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
package org.apache.ctakes.regression.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.util.XMLInputSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Runs a full pipeline and compares the xml output 
 * to ensure all annotators work together in harmony.
 * 
 * The Apache cTAKES Release Manager should ensure that at a min
 * this test passes before releasing.
 * 
 * If there are new modules, be sure to add the desc here.
 * If there are changes, after it's been manually reviewed, 
 * re-record the expected output and put them in output folder.
 * 
 * This also tests the UMLS annotator(s) so be sure to download
 * the UMLS Resources per README and add the jvm params
 * -Dctakes.umlsuser= -Dctakes.umlspw= parameters before running
 *
 */
public class RegressionPipelineTest {

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());
	private CollectionProcessingEngine mCPE;
	private static final File dir = new File("testdata/generatedoutput");
	private static final String CPEDESC = "desc/collection_processing_engine/RegressionPipelineCPETest.xml";
	
	@Test
	public void test() throws Exception {
		// create the pipeline
		setUp();
		
		// Start Processing
		logger.info("Running CPE");
		mCPE.process();
	}

	@Before
	public void setUp() throws Exception {

		//Setup output dir
	    Assert.assertTrue("Unable to create " + dir.getAbsolutePath(), dir.exists() || dir.mkdirs());
	    
		// parse CPE descriptor
		logger.info("Parsing CPE Descriptor");
		CpeDescription cpeDesc = UIMAFramework.getXMLParser()
				.parseCpeDescription(new XMLInputSource(CPEDESC));
		// instantiate CPE
		logger.info("Instantiating CPE");
		mCPE = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);

	}
	
	@Test
	public void testCompareXMLOutput() throws IOException {
		//TODO: need to implement here.
	    //final File expected = new File("xyz.txt");
	    //final File output = new File(dir, "xyz.txt");
	    //Assert.assertEquals(FileUtils.readLines(expected), FileUtils.readLines(output));
	}
}
