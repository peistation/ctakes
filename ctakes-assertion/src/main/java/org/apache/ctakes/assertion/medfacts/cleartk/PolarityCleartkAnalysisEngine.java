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

import org.apache.ctakes.assertion.medfacts.cleartk.extractors.AboveLeftFragmentExtractor;
import org.apache.ctakes.assertion.medfacts.cleartk.extractors.AboveRightFragmentExtractor;
import org.apache.ctakes.assertion.medfacts.cleartk.extractors.ContextWordWindowExtractor;
import org.apache.ctakes.assertion.medfacts.cleartk.extractors.NegationDependencyFeatureExtractor;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;


public class PolarityCleartkAnalysisEngine extends AssertionCleartkAnalysisEngine {

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		probabilityOfKeepingADefaultExample = 1.0; //0.1;
		
		if(this.entityFeatureExtractors == null){
			this.entityFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		}
		this.entityFeatureExtractors.add(new NegationDependencyFeatureExtractor());
		this.entityFeatureExtractors.add(new ContextWordWindowExtractor("org/apache/ctakes/assertion/models/polarity.txt"));
		this.entityFeatureExtractors.add(new AboveLeftFragmentExtractor("AL_Polarity","org/apache/ctakes/assertion/models/sharpPolarityFrags.txt"));
		this.entityFeatureExtractors.add(new AboveRightFragmentExtractor("AR_Polarity","org/apache/ctakes/assertion/models/sharpArPolarityFrags.txt"));
	}

	@Override
	public void setClassLabel(IdentifiedAnnotation entityOrEventMention, Instance<String> instance) throws AnalysisEngineProcessException {
	      if (this.isTraining())
	      {
	        String polarity = (entityOrEventMention.getPolarity() == -1) ? "negated" : "present";
	        // downsampling. initialize probabilityOfKeepingADefaultExample to 1.0 for no downsampling
	        if ("negated".equals(polarity))
	        {
	          logger.debug("TRAINING: " + polarity);
	        }
	        if ("present".equals(polarity) 
	        		&& coin.nextDouble() >= this.probabilityOfKeepingADefaultExample) {
	        	return;
	        }
	        instance.setOutcome(polarity);
	        this.dataWriter.write(instance);
	      } else
	      {
	        String label = this.classifier.classify(instance.getFeatures());
	        int polarity = 1;
	        if (label!= null && label.equals("present"))
	        {
	          polarity = 1;
	        } else if (label != null && label.equals("negated"))
	        {
	          polarity = -1;
	        }
	        entityOrEventMention.setPolarity(polarity);
	        if ("negated".equals(label))
	        {
	          logger.debug(String.format("DECODING/EVAL: %s//%s [%d-%d] (%s)", label, polarity, entityOrEventMention.getBegin(), entityOrEventMention.getEnd(), entityOrEventMention.getClass().getName()));
	        }
	      }
	}
}
