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
package org.apache.ctakes.relationextractor.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator.IdentifiedAnnotationPair;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Count various stats such as token and relation counts 
 * based on the gold standard data.
 *  
 * @author dmitriy dligach
 *
 */
public class GoldAnnotationStatsCalculator extends JCasAnnotator_ImplBase {

	public static final String goldViewName = "GoldView";
	public int tokenCount;
	public int sentenceCount;
	public int entityMentionCount;
	public int entityMentionPairCount;
	public Multiset<String> relationTypes;
	public Multiset<String> entityMentionPairTypes;
	
	@Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
	  
	  tokenCount = 0;
	  sentenceCount = 0;
	  entityMentionCount = 0;
	  entityMentionPairCount = 0;
	  relationTypes = HashMultiset.create();
	  entityMentionPairTypes = HashMultiset.create();
	}
  
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {

	  System.out.println();
	  System.out.format("%-30s%d\n", "token count", tokenCount);
	  System.out.format("%-30s%d\n", "sentence count", sentenceCount);
	  System.out.format("%-30s%d\n", "entity mention count", entityMentionCount);
	  System.out.format("%-30s%d\n", "entity mention pair count", entityMentionPairCount);
	  System.out.format("%-30s%d\n", "location_of count", relationTypes.count("location_of"));
	  System.out.format("%-30s%d\n", "degree_of count", relationTypes.count("degree_of"));
	  
	  System.out.println();
	  System.out.format("%-30s%d\n", "as-disorder", entityMentionPairTypes.count("as-dd"));
	  System.out.format("%-30s%d\n", "as-ss", entityMentionPairTypes.count("as-ss"));
	  System.out.format("%-30s%d\n", "as-procedure", entityMentionPairTypes.count("as-procedure"));
  }
  
	@Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    JCas goldView;
    try {
      goldView = jCas.getView(goldViewName);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }	  
    
    countTokens(jCas); // tokens exist in system view (not in gold)
    countSentences(jCas);
    countEntities(goldView);
    countEntityMentionPairs(jCas, goldView); 
    countEntityMentionPairTypes(jCas, goldView);
    countRelationTypes(goldView); 
  }
	
	private void countTokens(JCas jCas) {
    
	  Collection<BaseToken> baseTokens = JCasUtil.select(jCas, BaseToken.class);
	  tokenCount += baseTokens.size();
	}
	
	private void countSentences(JCas jCas) {
	  Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
	  sentenceCount += sentences.size();
	}
	
  private void countEntityMentionPairs(JCas jCas, JCas goldView) {
    
    for(Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      EntityMentionPairRelationExtractorAnnotator emPairAnnot = new EntityMentionPairRelationExtractorAnnotator();
      List<IdentifiedAnnotationPair> pairs = emPairAnnot.getCandidateRelationArgumentPairs(goldView, sentence);
      entityMentionPairCount += pairs.size();
    }
  }

  private void countEntityMentionPairTypes(JCas jCas, JCas goldView) {
    
    for(Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      EntityMentionPairRelationExtractorAnnotator emPairAnnot = new EntityMentionPairRelationExtractorAnnotator();
      DegreeOfRelationExtractorAnnotator degreeOfAnnot = new DegreeOfRelationExtractorAnnotator();
      
      List<IdentifiedAnnotationPair> pairs1 = emPairAnnot.getCandidateRelationArgumentPairs(goldView, sentence);
      // List<IdentifiedAnnotationPair> pairs2 = degreeOfAnnot.getCandidateRelationArgumentPairs(goldView, sentence);
      List<IdentifiedAnnotationPair> pairs = new ArrayList<IdentifiedAnnotationPair>();
      pairs.addAll(pairs1);
      // pairs.addAll(pairs2);
      
      for(IdentifiedAnnotationPair pair : pairs) {
        IdentifiedAnnotation arg1 = pair.getArg1();
        IdentifiedAnnotation arg2 = pair.getArg2();
        String type1 = getEntityType(arg1.getTypeID());
        String type2 = getEntityType(arg2.getTypeID());
        entityMentionPairTypes.add(type1 + "-" + type2);
      }
    }
  }

	private void countRelationTypes(JCas jCas) {
	  
    for(BinaryTextRelation binaryTextRelation : JCasUtil.select(jCas, BinaryTextRelation.class)) {
      String category = binaryTextRelation.getCategory();
      relationTypes.add(category);
    }
	}
	
	private void countEntities(JCas jCas) {
	  
	  Collection<EntityMention> entityMentions = JCasUtil.select(jCas, EntityMention.class);
	  entityMentionCount += entityMentions.size();
	}
	
	private String getEntityType(int typeId) {
	  
	  if(typeId == 0) {
      return "modifier";
    }
	  if(typeId == 1) {
	    return "drug";
	  }
	  if(typeId == 2) {
	    return "dd";
	  } 
	  if(typeId == 3) {
      return "ss";
    }
	  if(typeId == 4) {
      return "none";
    }
	  if(typeId == 5) {
      return "procedure";
    }
	  if(typeId == 6) {
      return "as";
    }
	  return "n/a";
	}
}
