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

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.cleartk.classifier.Instance;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

public class PolarityCleartkAnalysisEngine extends AssertionCleartkAnalysisEngine {

	public void setClassLabel(IdentifiedAnnotation entityMention, Instance<String> instance) throws AnalysisEngineProcessException {
	      if (this.isTraining())
	      {
	        String polarity = (entityMention.getPolarity() == -1) ? "negated" : "present";
	        instance.setOutcome(polarity);
	        if ("negated".equals(polarity))
	        {
	          logger.info("TRAINING: " + polarity);
	        }
	        this.dataWriter.write(instance);
	      } else
	      {
	        String label = this.classifier.classify(instance.getFeatures());
	        int polarity = 1;
	        if (label!= null && label.equals("present"))
	        {
	          polarity = 0;
	        } else if (label != null && label.equals("negated"))
	        {
	          polarity = -1;
	        }
	        entityMention.setPolarity(polarity);
	        if ("negated".equals(label))
	        {
	          logger.info(String.format("DECODING/EVAL: %s//%s [%d-%d] (%s)", label, polarity, entityMention.getBegin(), entityMention.getEnd(), entityMention.getClass().getName()));
	        }
	      }
	}
}
