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
    // TODO: don't iterate over the entire CAS for each focusAnnotation; use JCasUtil.indexCovering
    // and cache the results so that we only do this once per CAS

    Feature feature = new Feature("NoRole");
    for (Predicate predicate : JCasUtil.select(jCas, Predicate.class)) {

      for (BaseToken token : JCasUtil.selectCovered(jCas, BaseToken.class, predicate)) {
        if (token.equals(focusAnnotation)) {// token.getBegin()==focusAnnotation.getBegin()){
          feature = new Feature("Predicate");
          // System.out.println("*******************\tPredicate is :"+ predicate.getCoveredText());
          return Collections.singletonList(feature);
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
            feature = new Feature(label);
            // System.out.println("*******************\tfeature is :");
            return Collections.singletonList(feature);
          }
        }
      }
    }

    return Collections.singletonList(feature);
  }

}
