/*
 * Copyright: (c) 2012  Children's Hospital Boston, Regents of the University of Colorado 
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author 
 */
package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

/**
 * Identifies and classifies relations between pairs of named entities
 *
 */
public class EntityMentionPairRelationExtractorAnnotator extends RelationExtractorAnnotator {
	
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
		return pairs;
	}


	@Override
	protected String getRelationCategory(Map<List<Annotation>, BinaryTextRelation> relationLookup,
			IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {

		BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
		if (this.classifyBothDirections) {
			// if classifying both directions, we'll see {X, Y} once when X is first and
			// once when Y is first, so just do the single direction lookup here
			if (relation != null) {
				return relation.getCategory();
			} else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
				return NO_RELATION_CATEGORY;
			} else {
				return null;
			}
		} else {
			// if classifying in a single direction, we'll see {X, Y} only once,
			// so do lookups in both directions, and change the category name for
			// the relations in the reverse order
			if (relation != null) {
				return relation.getCategory();
			} else {
				relation = relationLookup.get(Arrays.asList(arg2, arg1));
				if (relation != null) {
					// Change category name to show reverse order
					return relation.getCategory() + "-1";
				} else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
					return NO_RELATION_CATEGORY;
				} else {
					return null;
				}
			}
		}
	}

}
