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
import java.util.List;

import org.apache.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Annotate degree_of relation between two entities whenever 
 * they are enclosed within the same noun phrse.
 */
public class Baseline4DegreeOfRelationExtractorAnnotator extends DegreeOfRelationExtractorAnnotator {

	@Override
	public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas identifiedAnnotationView, Annotation sentence) {
		List<IdentifiedAnnotationPair> pairs = super.getCandidateRelationArgumentPairs(identifiedAnnotationView, sentence);
		
    // find pairs enclosed inside a noun phrase
    List<IdentifiedAnnotationPair> result = new ArrayList<IdentifiedAnnotationPair>();
    for(IdentifiedAnnotationPair pair : pairs) {
      if(Utils.validateDegreeOfArgumentTypes(pair)) {
        for(TreebankNode nounPhrase : Utils.getNounPhrases(identifiedAnnotationView, (Sentence) sentence)) {
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
}
