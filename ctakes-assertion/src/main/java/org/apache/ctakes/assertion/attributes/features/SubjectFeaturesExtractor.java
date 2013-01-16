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
package org.apache.ctakes.assertion.attributes.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.ctakes.assertion.attributes.subject.SubjectAttributeClassifier;
import org.apache.ctakes.dependency.parser.util.DependencyUtility;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;


/** SubjectFeaturesExtractor
 * 		Ports the features and classification decisions of the first version (logic) of the subject tool
 * 
 * @author m081914
 *
 */
public class SubjectFeaturesExtractor implements SimpleFeatureExtractor {
	
	
	@Override
	public List<Feature> extract(JCas jCas, Annotation arg) {
		
		List<Feature> features = new ArrayList<Feature>();
		
		// Pull in general dependency-based features -- externalize to another extractor?
	    ConllDependencyNode node = DependencyUtility.getNominalHeadNode(jCas, arg);
	    if (node!= null) {
	    	features.add(new Feature("DEPENDENCY_HEAD", node));
		}
	    
	    HashMap<String, Boolean> featsMap = SubjectAttributeClassifier.extract(jCas, arg);

	    // Pull in all the features that were used for the rule-based module
	    features.addAll( hashToFeatureList(featsMap) );
	    // Pull in the result of the rule-based module as well
	    features.add(new Feature("SUBJECT_CLASSIFIER_LOGIC", SubjectAttributeClassifier.classifyWithLogic(featsMap)));

	    
	    return features;
	}

	private Collection<? extends Feature> hashToFeatureList(
			HashMap<String, Boolean> featsIn) {
		
		Collection<Feature> featsOut = new HashSet<Feature>();
		for (String featName : featsIn.keySet()) {
			featsOut.add(new Feature(featName,featsIn.get(featName)));
		}
		
		return featsOut;
	}

}
