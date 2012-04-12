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
package edu.mayo.bmi.attributes.subject;

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
import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textsem.SemanticArgument;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;


/**
 * @author stephenwu
 *
 */
public class SubjectAttributeClassifier {

	private static final String DONOR_TOKEN = "donor_token"; 
	private static final String DONOR_SRLARG = "donor_srlarg";
	private static final String DONOR_DEPPATH = "donor_deppath";
	private static final String DONOR_DEPSRL = "donor_depsrl";
	private static final String DONOR_OR = "donor_or";
	private static final String FAMILY_TOKEN = "family_token"; 
	private static final String FAMILY_SRLARG = "family_srlarg";
	private static final String FAMILY_DEPPATH = "family_deppath";
	private static final String FAMILY_DEPSRL = "family_depsrl";
	private static final String FAMILY_OR = "family_or";
	private static final String OTHER_TOKEN = "other_token"; 
	private static final String OTHER_SRLARG = "other_srlarg"; 
	private static final String OTHER_DEPPATH = "other_deppath"; 
	private static final String OTHER_DEPSRL = "other_depsrl";
	private static final String OTHER_OR = "other_or";
    public static ArrayList<String> FeatureIndex = new ArrayList<String>();
    static{
            FeatureIndex.add(DONOR_TOKEN);
            FeatureIndex.add(DONOR_SRLARG);
            FeatureIndex.add(DONOR_DEPPATH);
            FeatureIndex.add(DONOR_DEPSRL);
            FeatureIndex.add(DONOR_OR);
            FeatureIndex.add(FAMILY_TOKEN);
            FeatureIndex.add(FAMILY_SRLARG);
            FeatureIndex.add(FAMILY_DEPPATH);
            FeatureIndex.add(FAMILY_DEPSRL);
            FeatureIndex.add(FAMILY_OR);
            FeatureIndex.add(OTHER_TOKEN);
            FeatureIndex.add(OTHER_SRLARG);
            FeatureIndex.add(OTHER_DEPPATH);
            FeatureIndex.add(OTHER_DEPSRL);
            FeatureIndex.add(OTHER_OR);
    }

	// currently goes from entityMention to Sentence to SemanticArgument
	public static String getSubject(JCas jCas, IdentifiedAnnotation mention) {
		
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
		
		// look for mentions of "donor" in the tokens
		List<BaseToken> toks = JCasUtil.selectCovered(jCas, BaseToken.class, sEntity);
		for (BaseToken tok : toks) {
			if ( isDonorTerm(tok) ) {
				vfeat.put(DONOR_TOKEN, true);
			}
			if ( isFamilyTerm(tok) ) {
				vfeat.put(FAMILY_TOKEN, true);
			}
			
			if ( isOtherTerm(tok) ) {
				vfeat.put(OTHER_TOKEN, true);
			}
		}
		
		// get any SRL arguments
		List<SemanticArgument> args = JCasUtil.selectCovered(jCas, SemanticArgument.class, sEntity);
		for (SemanticArgument arg : args) {
			
			// look in SRL arguments for a family or other subject 
			if (arg.getLabel().matches("A[01]")) {
				if ( isDonorTerm(arg) ) {
					vfeat.put(DONOR_SRLARG, true);
				}
				if ( isFamilyTerm(arg) ) {
					vfeat.put(FAMILY_SRLARG, true);
				}
				if ( isOtherTerm(arg) ) {
					vfeat.put(OTHER_SRLARG, true);
				}
			}

		}
		
		// search dependency paths for stuff
		List<ConllDependencyNode> depnodes = JCasUtil.selectCovered(jCas, ConllDependencyNode.class, mention);
		if (!depnodes.isEmpty()) {
			ConllDependencyNode depnode = DependencyUtility.getNominalHeadNode(depnodes);
			for (ConllDependencyNode dn : DependencyUtility.getPathToTop(jCas, depnode)) {
				if ( isDonorTerm(dn) ) {
					vfeat.put(DONOR_DEPPATH, true);
				}
				if ( isFamilyTerm(dn) ) {
					vfeat.put(FAMILY_DEPPATH, true);
				}
				if ( isOtherTerm(dn) ) {
					vfeat.put(OTHER_DEPPATH, true);
				}
				
				// check if there is SRL stuff on the dependency path
				
			}
		}
		
		// Logic to identify cases, may be replaced by learned classification
		Boolean donor_summary = new Boolean(vfeat.get(DONOR_TOKEN) || vfeat.get(DONOR_DEPPATH) || 
				vfeat.get(DONOR_DEPSRL) || vfeat.get(DONOR_SRLARG));
		Boolean family_summary = new Boolean(                         vfeat.get(FAMILY_DEPPATH) || 
				vfeat.get(FAMILY_DEPSRL) || vfeat.get(FAMILY_SRLARG));
		Boolean other_summary = new Boolean(                          vfeat.get(OTHER_DEPPATH) || 
				vfeat.get(OTHER_DEPSRL) || vfeat.get(OTHER_SRLARG));
		vfeat.put(DONOR_OR, donor_summary);
		vfeat.put(FAMILY_OR, family_summary);
		vfeat.put(OTHER_OR, other_summary);
		
		if (vfeat.get(DONOR_OR) && vfeat.get(FAMILY_OR)) {
			return CONST.NE_SUBJECT_DONOR_FAMILY_MEMBER;
		} else if (vfeat.get(DONOR_OR) && !vfeat.get(FAMILY_OR)) {
			return CONST.NE_SUBJECT_DONOR_OTHER;
		} else if (!vfeat.get(DONOR_OR) && !vfeat.get(FAMILY_OR) && vfeat.get(OTHER_OR)) {
			return CONST.NE_SUBJECT_OTHER;
		} else if (!vfeat.get(DONOR_OR) && vfeat.get(FAMILY_OR)) {
			return (CONST.NE_SUBJECT_FAMILY_MEMBER);
		} else {
			return CONST.NE_SUBJECT_PATIENT;
		}
			
	}
	
	
	private static boolean isDonorTerm(Annotation arg) {
		return arg.getCoveredText().toLowerCase()
		.matches("(donor).*");
	}

	
	private static boolean isFamilyTerm(Annotation arg) {
		return arg.getCoveredText().toLowerCase()
		.matches("(father|dad|mother|mom|bro|sis|sib|cousin|aunt|uncle|grandm|grandp|grandf|" +
				"wife|spouse|husband|child|offspring|progeny|son|daughter|nephew|niece|kin|family).*");
	}


	private static boolean isOtherTerm(Annotation arg) {
		return arg.getCoveredText().toLowerCase()
		.matches(".*(in-law|stepc|stepd|stepso|stepf|stepm|step-).*");
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
