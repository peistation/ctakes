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
package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import static org.apache.ctakes.assertion.util.AssertionTreeUtils.extractAboveLeftConceptTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.ctakes.assertion.util.SemanticClasses;
import org.apache.ctakes.constituency.parser.util.TreeUtils;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.utils.tree.FragmentUtils;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.util.CleartkInitializationException;

/* 
 * This class implements a ClearTK feature extractor for tree kernel fragment features
 * as derived using the flink toolkit (http://danielepighin.net/cms/software/flink).
 * Model location is hardcoded as of right now.
 * TODO: Parameterize & unstaticize this so that, e.g., multiple projects could use this feature if necessary.
 */
public class TreeFragmentFeatureExtractor implements SimpleFeatureExtractor {
	public static final String PARAM_OUTPUTDIR = "outputDir";
	public static final String PARAM_SEMDIR = "semDir";
	static HashSet<SimpleTree> frags = null;
	private SemanticClasses sems = null;

	public TreeFragmentFeatureExtractor(String resourceFilename) throws CleartkInitializationException{
		if(frags == null) initializeFrags(resourceFilename);
		try{
			sems = new SemanticClasses(FileLocator.locateFile("org/apache/ctakes/assertion/semantic_classes").getAbsolutePath());
		}catch(Exception e){
			throw new CleartkInitializationException(e, "org/apache/ctakes/assertion/semantic_classes", "Could not find semantic classes resource.", new Object[]{});
		}
	}

	private static void initializeFrags(String resourceFilename){
		frags = new HashSet<SimpleTree>();
		try{
			File fragsFile = FileLocator.locateFile(resourceFilename);
			Scanner scanner = new Scanner(fragsFile);
			while(scanner.hasNextLine()){
				frags.add(FragmentUtils.frag2tree(scanner.nextLine().trim()));
			}
		}catch(FileNotFoundException e){
			System.err.println("Missing fragment file!");
		}
	}

	@Override
	public List<Feature> extract(JCas jcas, Annotation annotation)
			throws CleartkExtractorException {
		List<Feature> features = new ArrayList<Feature>();
		SimpleTree tree = extractAboveLeftConceptTree(jcas, annotation, sems);
		
		for(SimpleTree frag : frags){
			if(TreeUtils.containsIgnoreCase(tree, frag)){
				features.add(new Feature("TreeFrag", frag.toString()));
			}
		}
	
		return features;
	}

}
