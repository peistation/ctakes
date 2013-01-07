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

import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
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
	public Multiset<String> relationTypes;
	
	@Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
	  
	  tokenCount = 0;
	  relationTypes = HashMultiset.create();
	}
  
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {

	  System.out.println();
	  System.out.println("token count: " + tokenCount);
	  System.out.println("location_of count: " + relationTypes.count("location_of"));
	  System.out.println("degree_of count: " + relationTypes.count("degree_of"));
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
    countRelationTypes(goldView); // gold relations are in gold view
  }
	
	private void countTokens(JCas jCas) {
    
	  for(BaseToken baseToken : JCasUtil.select(jCas, BaseToken.class)) {
	    tokenCount++;
    }
	}
	
	private void countRelationTypes(JCas jCas) {
	  
    for(BinaryTextRelation binaryTextRelation : JCasUtil.select(jCas, BinaryTextRelation.class)) {
      String category = binaryTextRelation.getCategory();
      relationTypes.add(category);
    }
	}
}
