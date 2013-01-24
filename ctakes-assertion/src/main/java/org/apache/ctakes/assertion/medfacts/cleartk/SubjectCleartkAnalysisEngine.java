/*
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
package org.apache.ctakes.assertion.medfacts.cleartk;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;

import org.apache.ctakes.assertion.attributes.features.SubjectFeaturesExtractor;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

public class SubjectCleartkAnalysisEngine extends
		AssertionCleartkAnalysisEngine {

	boolean USE_DEFAULT_EXTRACTORS = false;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		if (this.isTraining() && this.goldViewName == null) {
			throw new IllegalArgumentException(PARAM_GOLD_VIEW_NAME + " must be defined during training");
		}
		
//		if (USE_DEFAULT_EXTRACTORS) {
//		} else {
			initialize_subject_extractor();
//		}

	}


	private void initialize_subject_extractor() {
		
		if (this.contextFeatureExtractors==null) {
			this.contextFeatureExtractors = new ArrayList<ContextExtractor<IdentifiedAnnotation>>();
		}
		this.contextFeatureExtractors.add( 
				new ContextExtractor<IdentifiedAnnotation>(
						IdentifiedAnnotation.class, new SubjectFeaturesExtractor()) );
				
	}
	
	@Override
	public void setClassLabel(IdentifiedAnnotation entityMention,
			Instance<String> instance) throws AnalysisEngineProcessException {
		if (this.isTraining())
	      {
	        String subj = entityMention.getSubject();
	        instance.setOutcome(subj);
	        logger.log(Level.INFO,  String.format("[%s] expected: ''; actual: ''; features: %s",
		      		  this.getClass().getSimpleName(),
		      		  instance.toString()
		      		  //StringUtils.join(instance.getFeatures(), ", ")
		      		  ));
	        this.dataWriter.write(instance);
	      } else
	      {
	        String label = this.classifier.classify(instance.getFeatures());
	        entityMention.setSubject(label);
	        logger.log(Level.INFO, "SUBJECT is being set on an IdentifiedAnnotation: "+label+" "+entityMention.getSubject());
	      }
	}

}
