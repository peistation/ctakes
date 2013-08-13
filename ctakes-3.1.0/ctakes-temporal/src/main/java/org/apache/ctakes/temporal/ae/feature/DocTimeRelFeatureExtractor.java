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

import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

public class DocTimeRelFeatureExtractor implements RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException {

    List<Feature> features = new ArrayList<Feature>();
    
    if(arg1 instanceof EventMention) {
      EventMention event = (EventMention) arg1;
      String docTimeRel = event.getEvent().getProperties().getDocTimeRel();
      features.add(new Feature("arg1DocTimeRel", docTimeRel));
    }
    
    if(arg2 instanceof EventMention) {
      EventMention event = (EventMention) arg2;
      String docTimeRel = event.getEvent().getProperties().getDocTimeRel();
      features.add(new Feature("arg2DocTimeRel", docTimeRel));
    }
    
    return features;
  }
}
