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
package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;
//import java.util.logging.Logger;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

public class EventPropertyExtractor implements SimpleFeatureExtractor {

  private String name;

//  private Logger logger = Logger.getLogger(this.getClass().getName());

  public EventPropertyExtractor() {
    super();
    this.name = "EventContextualModality";
    
  }

  @Override
  public List<Feature> extract(JCas view, Annotation annotation) throws CleartkExtractorException {
	  List<Feature> features = new ArrayList<Feature>();
	  
	  //1 get event:
	  EventMention event = (EventMention)annotation;
	  String contextModal = event.getEvent().getProperties().getContextualModality();
	  if (contextModal != null && contextModal.equals("GENERIC")){
		  Feature contexmod = new Feature(this.name, contextModal);
		  features.add(contexmod);
//		  logger.info("found a event: "+ contextModal);
	  }
	  
	  return features;
  }

}
