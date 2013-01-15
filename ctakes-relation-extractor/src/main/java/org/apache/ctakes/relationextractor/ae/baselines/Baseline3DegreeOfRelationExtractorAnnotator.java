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
package org.apache.ctakes.relationextractor.ae.baselines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

/**
 * Annotate location_of relation between two entities whenever 
 * they are enclosed within the same noun phrse.
 */
public class Baseline3DegreeOfRelationExtractorAnnotator extends RelationExtractorAnnotator {

	@Override
	public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas identifiedAnnotationView, Sentence sentence) {
		
		List<EntityMention> entities = JCasUtil.selectCovered(identifiedAnnotationView, EntityMention.class, sentence);
		List<Modifier> modifiers = JCasUtil.selectCovered(identifiedAnnotationView, Modifier.class, sentence);
		
		List<IdentifiedAnnotationPair> pairs = new ArrayList<IdentifiedAnnotationPair>();
		for (EntityMention entity : entities) {
			for (Modifier modifier : modifiers) {
				pairs.add(new IdentifiedAnnotationPair(entity, modifier));
			}
		}
		
    // find pairs enclosed inside a noun phrase
    List<IdentifiedAnnotationPair> result = new ArrayList<IdentifiedAnnotationPair>();
    for(IdentifiedAnnotationPair pair : pairs) {
      if(Utils.validateDegreeOfArgumentTypes(pair)) {
        for(TreebankNode nounPhrase : Utils.getNounPhrases(identifiedAnnotationView, sentence)) {
          if(Utils.isEnclosed(pair, nounPhrase)) {
            IdentifiedAnnotation arg1 = pair.getArg1();
            IdentifiedAnnotation arg2 = pair.getArg2();
            result.add(new IdentifiedAnnotationPair(arg1, arg2));
            System.out.println("NP: " + nounPhrase.getCoveredText() + ", " + nounPhrase.getBegin() + ", " + nounPhrase.getEnd());
            System.out.println("arg1: " + arg1.getCoveredText() + ", " + arg1.getBegin() + ", " + arg1.getEnd());
            System.out.println("arg2: " + arg2.getCoveredText() + ", " + arg2.getBegin() + ", " + arg2.getEnd());
            System.out.println();
            break; // don't check other NPs
          }
        }
      }
    }
    
    return result;
	}

	@Override
	protected String getRelationCategory(
			Map<List<Annotation>, BinaryTextRelation> relationLookup,
			IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {
		BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
		return (relation != null) ? relation.getCategory() : NO_RELATION_CATEGORY;
	}

  @Override
  public String classify(List<Feature> features) throws CleartkProcessingException {
    return this.classifier.classify(features);
  }
}
