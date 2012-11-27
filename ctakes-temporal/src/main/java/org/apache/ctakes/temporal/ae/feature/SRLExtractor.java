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
import java.util.Collections;
import java.util.List;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.Predicate;
import org.apache.ctakes.typesystem.type.textsem.SemanticArgument;
import org.apache.ctakes.typesystem.type.textsem.SemanticRoleRelation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class SRLExtractor implements SimpleFeatureExtractor {

  @Override
  public List<Feature> extract(JCas jCas, Annotation focusAnnotation)
      throws CleartkExtractorException {
    // and cache the results so that we only do this once per CAS
	String jCasText = jCas.getDocumentText();
	String roleFeat = "SemanticRole";
	String roleVerbFeat = "RoleAndVerb";
	String verb = "noVerb";
    Feature role = new Feature(roleFeat, "NoRole");
    Feature roleVerb = new Feature(roleVerbFeat, "NoRole"+verb);
    ArrayList<Feature> features = new ArrayList<Feature>();
    for (Predicate predicate : JCasUtil.select(jCas, Predicate.class)) {

      for (BaseToken token : JCasUtil.selectCovered(jCas, BaseToken.class, predicate)) {
        if (token.equals(focusAnnotation)) {// token.getBegin()==focusAnnotation.getBegin()){
          role = new Feature(roleFeat,"Predicate");
          verb = jCasText.substring(predicate.getBegin(), predicate.getEnd());
          roleVerb = new Feature(roleVerbFeat, "Predicate::"+verb);
          
          features.add(role);
          //features.add(roleVerb);
          return features;
        }
      }

      for (SemanticRoleRelation relation : JCasUtil.select(
          predicate.getRelations(),
          SemanticRoleRelation.class)) {
        SemanticArgument arg = relation.getArgument();
        // System.out.format("\tArg: %s=%s \n", arg.getLabel(), arg.getCoveredText());
        for (BaseToken token : JCasUtil.selectCovered(jCas, BaseToken.class, arg)) {
          if (token.equals(focusAnnotation)) {// token.getBegin()==focusAnnotation.getBegin()){
            String label = arg.getLabel();
            Predicate currentPred = relation.getPredicate();
            verb = jCasText.substring(currentPred.getBegin(), currentPred.getEnd());
            role = new Feature(roleFeat, label);
            roleVerb = new Feature(roleVerbFeat, label+"::"+verb);
            
            features.add(role);
            //features.add(roleVerb);
            return features;
          }
        }
      }
    }

    features.add(role);
    //features.add(roleVerb);
    return features;
  }

}
