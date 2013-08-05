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
package org.apache.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.LocationOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

/**
 * Identifies Location_Of relations between {@link EventMention}s and
 * {@link AnatomicalSiteMention}s.
 */
public class LocationOfRelationExtractorAnnotator extends RelationExtractorAnnotator {

  @Override
  protected Class<? extends BinaryTextRelation> getRelationClass() {
    return LocationOfTextRelation.class;
  }

  @Override
  public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
      JCas identifiedAnnotationView,
      Annotation sentence) {

    List<EventMention> events =
        JCasUtil.selectCovered(identifiedAnnotationView, EventMention.class, sentence);
    List<AnatomicalSiteMention> sites =
        JCasUtil.selectCovered(identifiedAnnotationView, AnatomicalSiteMention.class, sentence);

    List<IdentifiedAnnotationPair> pairs = new ArrayList<IdentifiedAnnotationPair>();
    for (EventMention event : events) {
      for (AnatomicalSiteMention site : sites) {
        pairs.add(new IdentifiedAnnotationPair(event, site));
      }
    }
    return pairs;
  }

  @Override
  protected void createRelation(
      JCas jCas,
      IdentifiedAnnotation arg1,
      IdentifiedAnnotation arg2,
      String predictedCategory) {
    RelationArgument relArg1 = new RelationArgument(jCas);
    relArg1.setArgument(arg1);
    relArg1.setRole("Argument");
    relArg1.addToIndexes();
    RelationArgument relArg2 = new RelationArgument(jCas);
    relArg2.setArgument(arg2);
    relArg2.setRole("Related_to");
    relArg2.addToIndexes();
    LocationOfTextRelation relation = new LocationOfTextRelation(jCas);
    relation.setArg1(relArg1);
    relation.setArg2(relArg2);
    relation.setCategory(predictedCategory);
    relation.addToIndexes();
  }

  @Override
  protected Class<? extends Annotation> getCoveringClass() {
    return Sentence.class;
  }
}
