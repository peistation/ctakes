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
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;

/**
 * Annotated degree_of relation in sentences containing a single entity mention
 * of a valid degree_of type and a single modifier.
 */
public class Baseline1DegreeOfRelationExtractorAnnotator extends DegreeOfRelationExtractorAnnotator {

	@Override
	public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas identifiedAnnotationView, Sentence sentence) {
	  List<IdentifiedAnnotationPair> pairs = super.getCandidateRelationArgumentPairs(identifiedAnnotationView, sentence);
		
		// look for sentences with one entity and one modifier
    List<IdentifiedAnnotationPair> result = new ArrayList<IdentifiedAnnotationPair>();
		if (pairs.size() == 1) {
		  IdentifiedAnnotationPair pair = pairs.get(0);
		  if(Utils.validateDegreeOfArgumentTypes(pair)) {
		    System.out.println(sentence.getCoveredText());
		    System.out.println("arg1: " + pair.getArg1().getCoveredText());
		    System.out.println("arg2: " + pair.getArg2().getCoveredText());
		    System.out.println();
		    result.add(pair);
		  }
		}
				
		return result;
	}

  @Override
  public String classify(List<Feature> features) throws CleartkProcessingException {
    return "degree_of";
  }
}
