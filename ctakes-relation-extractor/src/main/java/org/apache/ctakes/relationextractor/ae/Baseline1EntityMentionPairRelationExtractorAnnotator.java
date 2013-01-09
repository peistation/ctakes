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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

/**
 * Annotate location_of relation between two entities in sentences containing
 * exactly two entities (where the entities are of the correct types).
 */
public class Baseline1EntityMentionPairRelationExtractorAnnotator extends RelationExtractorAnnotator {
	
	public static final String PARAM_CLASSIFY_BOTH_DIRECTIONS = "ClassifyBothDirections";

	@ConfigurationParameter(
			name = PARAM_CLASSIFY_BOTH_DIRECTIONS,
			mandatory = false,
			description = "run the classifier in both directions, that is, classify each pair of events "
					+ "{X,Y} once in the order X-to-Y and once in the order Y-to-X (default: classify each "
					+ "pair of events {X, Y} once, giving the label 'R' if a relation exists with the order "
					+ "X-to-Y, and 'R-1' if a relation exists with the order Y-to-X)")
	protected boolean classifyBothDirections = false;

	@Override
	public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas identifiedAnnotationView, Sentence sentence) {

		// collect all possible relation arguments from the sentence
		List<EntityMention> args = JCasUtil.selectCovered(
				identifiedAnnotationView,
				EntityMention.class,
				sentence);

		// Create pairings (this will change depending on the classification direction)
		List<IdentifiedAnnotationPair> pairs = new ArrayList<IdentifiedAnnotationPair>();
		
		for (int i = 0; i < args.size(); ++i) {
			EntityMention arg1 = args.get(i);
			int jStart = this.classifyBothDirections ? 0 : i + 1;
			for (int j = jStart; j < args.size(); ++j) {
        EntityMention arg2 = args.get(j);
			  // skip identical entity mentions and mentions with identical spans
				if (i == j || (arg1.getBegin() == arg2.getBegin() && arg1.getEnd() == arg2.getEnd())) {
				  continue;
				}
				pairs.add(new IdentifiedAnnotationPair(arg1, arg2));
			}
		}

		// return the pairs if there is only a single pair of entities
		// and the argument types are legitimate for location_of
		if(pairs.size() == 1) {
		  if(validateArgumentTypes(pairs.get(0))) {
		    System.out.println(sentence.getCoveredText());
		    System.out.println("arg1: " + pairs.get(0).getArg1().getCoveredText());
		    System.out.println("arg2: " + pairs.get(0).getArg2().getCoveredText());
		    System.out.println();
		    return pairs;
		  }
		}

		// for all other cases, return no entity pairs
		return new ArrayList<IdentifiedAnnotationPair>();
	}
	
	/*
	 * Are entity types of the arguments valid for location_of? 
	 * The following combinations are allowed:
	 * 
	 * location-of(anatomical site/6, disorder/2)
   * location-of(anatomical site/6, sign/symptom/3)
   * location-of(anatomical site/6, procedure/5)
	 */
	private static boolean validateArgumentTypes(IdentifiedAnnotationPair pair) {
	  
    // allowable arg2 types for location_of
    HashSet<Integer> okArg2Types = new HashSet<Integer>(Arrays.asList(2, 3, 5));
    
	  IdentifiedAnnotation arg1 = pair.getArg1(); // Argument (should be anatomical site)
	  IdentifiedAnnotation arg2 = pair.getArg2(); // Related_to (should be either disorder, sign/symptom, or procedure)
	  int type1 = arg1.getTypeID();
	  int type2 = arg2.getTypeID();
	  
	  if(type1 == 6 && okArg2Types.contains(type2)) {
	    return true;
	  }
	  
	  return false;
	}
	
	@Override
	protected String getRelationCategory(Map<List<Annotation>, BinaryTextRelation> relationLookup,
			IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {

		BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
		String category;
		if (this.classifyBothDirections) {
			// if classifying both directions, we'll see {X, Y} once when X is first and
			// once when Y is first, so just do the single direction lookup here
			if (relation != null) {
				category = relation.getCategory();
			} else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
			  category = NO_RELATION_CATEGORY;
			} else {
			  category = null;
			}
		} else {
			// if classifying in a single direction, we'll see {X, Y} only once,
			// so do lookups in both directions, and change the category name for
			// the relations in the reverse order
			if (relation != null) {
			  category = relation.getCategory();
			} else {
				relation = relationLookup.get(Arrays.asList(arg2, arg1));
				if (relation != null) {
					// Change category name to show reverse order
				  category = relation.getCategory() + "-1";
				} else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
				  category = NO_RELATION_CATEGORY;
				} else {
				  category = null;
				}
			}
		}
		return category;
	}

  @Override
  public String classify(List<Feature> features) {
    return "location_of";
  }
}
