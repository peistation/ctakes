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
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.utils.struct.CounterMap;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

public class UmlsFeatureExtractor implements RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException {

    List<Feature> features = new ArrayList<Feature>();
    
    JCas systemView;
    try {
      systemView = jCas.getView("_InitialView");
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
    
    if(arg1 instanceof EventMention) {
//      List<EntityMention> entityMentions = JCasUtil.selectCovering(systemView, EntityMention.class, arg1.getBegin(), arg1.getEnd());

      CounterMap<String> typeCounts = 
          getMentionTypes(JCasUtil.selectCovering(systemView, EntityMention.class, arg1.getBegin(), arg1.getEnd()));
      
      // print out totals:
      for(String typeId : typeCounts.keySet()){
        features.add(new Feature("arg1EntityTypeID_"+typeId, typeCounts.get(typeId)));        
      }
      
      // TO print out just the types without counts:
//      for(String typeId : typeCounts.keySet()){
//        features.add(new Feature("arg1EntityTypeID_", typeId));
//      }
      
      // TODO: this is the correct implementatino, but it does not perform as well 
//      for(int typeID : uniqueTypeIDs) {
//        features.add(new Feature("arg1EntityTypeID", String.valueOf(typeID)));
//      }

    }

    if(arg2 instanceof EventMention){
      CounterMap<String> typeCounts = 
          getMentionTypes(JCasUtil.selectCovering(systemView, EntityMention.class, arg2.getBegin(), arg2.getEnd()));
      
      // print out totals:
      for(String typeId : typeCounts.keySet()){
        features.add(new Feature("arg2EntityTypeID_"+typeId, typeCounts.get(typeId)));        
      }      
    }
    return features;
  }
  
  private static CounterMap<String> getMentionTypes(List<EntityMention> entities){
    CounterMap<String> typeCounts = new CounterMap<String>();
    for(EntityMention entityMention : entities) {
      typeCounts.add(String.valueOf(entityMention.getTypeID()));
    }
    return typeCounts;
    
  }
}
