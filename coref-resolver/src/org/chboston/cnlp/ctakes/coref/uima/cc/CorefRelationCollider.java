/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.chboston.cnlp.ctakes.coref.uima.cc;

import java.util.HashSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceProcessException;

import edu.mayo.bmi.uima.core.type.relation.CollectionTextRelation;
import edu.mayo.bmi.uima.core.type.relation.RelationArgument;

public class CorefRelationCollider extends CasConsumer_ImplBase {

	HashSet<RelationArgument> args = new HashSet<RelationArgument>();
	
	@Override
	public void processCas(CAS arg0) throws ResourceProcessException {
		JCas jcas = null;
		try{
			jcas = arg0.getCurrentView().getJCas();
		}catch (CASException e) {
			e.printStackTrace();
			System.err.println("No processing done in ODIEVectoFileWriter!");
			return;
		}
		// process all relation arguments to find out where they are
		
		// process all coreference chains to find out which relations they partake in
		FSIterator iter = jcas.getAnnotationIndex(CollectionTextRelation.type).iterator();
		int i = 0;
		while(iter.hasNext()){
			System.out.println("Chain: " + i);
			CollectionTextRelation col = (CollectionTextRelation) iter.next();
			FSList head = col.getMembers();
			while(head instanceof NonEmptyFSList){
				Annotation a = (Annotation) ((NonEmptyFSList)head).getHead();
				head = ((NonEmptyFSList)head).getTail();
				System.out.println(a.getCoveredText() + " (" + a.getBegin() + "," + a.getEnd() + ")");
			}
			i++;
		}
	}

}
