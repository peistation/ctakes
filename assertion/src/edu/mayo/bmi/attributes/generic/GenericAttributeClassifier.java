/*
 * Copyright: (c) 2012   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
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
 */
package edu.mayo.bmi.attributes.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;

import edu.mayo.bmi.nlp.parser.util.DependencyUtility;
import edu.mayo.bmi.uima.core.type.constants.CONST;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.Chunk;
import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textsem.SemanticArgument;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;


/**
 * @author stephenwu
 *
 */
public class GenericAttributeClassifier {

	private static final String POSTCOORD_NMOD = "donor_srlarg";
	private static final String DISCUSSION_DEPPATH = "discussion_deppath";
	private static final String SUBSUMED_CHUNK = "other_token"; 
	private static final String SUBSUMED_ANNOT = "other_deppath"; 
    public static ArrayList<String> FeatureIndex = new ArrayList<String>();
    static{
            FeatureIndex.add(POSTCOORD_NMOD);
            FeatureIndex.add(DISCUSSION_DEPPATH);
            FeatureIndex.add(SUBSUMED_CHUNK);
            FeatureIndex.add(SUBSUMED_ANNOT);
    }

	// currently goes from entityMention to Sentence to SemanticArgument
	public static Boolean getGeneric(JCas jCas, IdentifiedAnnotation mention) {
		
		HashMap<String,Boolean> vfeat = new HashMap<String,Boolean>();
		for (String feat : FeatureIndex) {
			vfeat.put(feat, false);
		}
		
		// find the sentence that entityMention is in
		Sentence sEntity = null;
		Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
		for (Sentence s : sentences) {
			if ( s.getBegin()<=mention.getBegin() && s.getEnd()>=mention.getEnd()) {
				sEntity = s;
				break;
			}
		}
//		if (sEntity==null)
//			return null;
		
		if (sEntity!=null) {
			

			// 2) some other identified annotation subsumes this one?
			List<IdentifiedAnnotation> lsmentions = JCasUtil.selectPreceding(jCas, IdentifiedAnnotation.class, mention, 5);
			lsmentions.addAll(JCasUtil.selectFollowing(jCas, IdentifiedAnnotation.class, mention, 5));
			for (IdentifiedAnnotation annot : lsmentions) {
				if ( annot.getBegin()>mention.getBegin()) {
					break;
				} else {
					if ( annot.getEnd()<mention.getEnd()) {
						continue;
					} else if ( !DependencyUtility.equalCoverage(
							DependencyUtility.getNominalHeadNode(jCas, annot),
							DependencyUtility.getNominalHeadNode(jCas, mention)) ) {
						// the case that annot is a superset
						vfeat.put(SUBSUMED_ANNOT, true);
					}
				}
			}
			
			// 3) some chunk subsumes this?
			List<Chunk> lschunks = JCasUtil.selectPreceding(jCas, Chunk.class, mention, 5);
			lschunks.addAll(JCasUtil.selectFollowing(jCas, Chunk.class, mention, 5));
			for (Chunk chunk : lschunks) {
				if ( chunk.getBegin()>mention.getBegin()) {
					break;
				} else {
					if ( chunk.getEnd()<mention.getEnd()) {
						continue;
					} else if ( !DependencyUtility.equalCoverage(
							DependencyUtility.getNominalHeadNode(jCas, chunk), 
							DependencyUtility.getNominalHeadNode(jCas, mention)) ) {
						// the case that annot is a superset
						vfeat.put(SUBSUMED_CHUNK, true);
					}
				}
			}
		}
		
		
		List<ConllDependencyNode> depnodes = JCasUtil.selectCovered(jCas, ConllDependencyNode.class, mention);
		if (!depnodes.isEmpty()) { 
			ConllDependencyNode depnode = DependencyUtility.getNominalHeadNode(depnodes);

			// 1) check if the head node of the entity mention is really just part of a larger noun phrase
			if (depnode.getDeprel().matches("(NMOD|amod|nmod|det|predet|nn|poss|possessive|infmod|partmod|rcmod)")) {
				vfeat.put(POSTCOORD_NMOD, true);
			}

			// 4) search dependency paths for discussion context
			for (ConllDependencyNode dn : DependencyUtility.getPathToTop(jCas, depnode)) {
				if ( isDiscussionContext(dn) ) {
					vfeat.put(DISCUSSION_DEPPATH, true);
				}
			}
		}
		
		// Logic to identify cases, may be replaced by learned classification
		int subsumectr = 0;
		if (vfeat.get(SUBSUMED_CHUNK)) { } //subsumectr++; }
		if (vfeat.get(SUBSUMED_ANNOT)) { subsumectr++; }
		if (vfeat.get(POSTCOORD_NMOD)) { subsumectr++; }
		Boolean subsume_summary = (subsumectr>0);
		if (vfeat.get(DISCUSSION_DEPPATH) || subsume_summary) {
			return true;
		} else {
			return false;
		}
			
	}
	
	
	private static boolean isDonorTerm(Annotation arg) {
		return arg.getCoveredText().toLowerCase()
		.matches("(donor).*");
	}

	
	private static boolean isDiscussionContext(Annotation arg) {
		return arg.getCoveredText().toLowerCase()
		.matches("(discuss|ask|understand|understood|tell|told|mention|talk|speak|spoke|address).*");
	}


	// a main method for regex testing
	public static void main(String[] args) {
		String s = "steps";
		if (s.toLowerCase().matches(".*(in-law|stepc|stepd|stepso|stepf|stepm|step-).*")) {
			System.out.println("match");
		} else {
			System.out.println("no match");
		}
	}
}
