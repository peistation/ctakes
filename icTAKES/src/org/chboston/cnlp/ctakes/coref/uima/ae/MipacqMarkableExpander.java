/*   Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
 @author Jiaping Zheng
 * @author Tim Miller
 */
package org.chboston.cnlp.ctakes.coref.uima.ae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.chboston.cnlp.ctakes.coref.eval.helpers.Span;

import edu.mayo.bmi.coref.util.FSIteratorToList;
import edu.mayo.bmi.coref.util.MarkableTreeUtils;
import edu.mayo.bmi.uima.core.type.syntax.Chunk;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;
import edu.mayo.bmi.uima.coref.type.DemMarkable;
import edu.mayo.bmi.uima.coref.type.Markable;
import edu.mayo.bmi.uima.coref.type.NEMarkable;

public class MipacqMarkableExpander extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
//		removeDoctors(aJCas);
		removeHistoryOf(aJCas);
		FSIterator<Annotation> iter = aJCas.getJFSIndexRepository().getAnnotationIndex(Markable.type).iterator();
		expandToNP(aJCas, FSIteratorToList.convert(iter));
		mergeNP(aJCas);
		elevateAdjectives(aJCas);
		iter = aJCas.getJFSIndexRepository().getAnnotationIndex(Markable.type).iterator();
		rmDup(aJCas, FSIteratorToList.convert(iter));
	}

	private void removeDoctors(JCas jCas) {
		FSIterator<Annotation> iter = jCas.getAnnotationIndex(NEMarkable.type).iterator();
		ArrayList<Annotation> rm = new ArrayList<Annotation>();
		while(iter.hasNext()){
			NEMarkable m = (NEMarkable) iter.next();
			if(m.getCoveredText().equalsIgnoreCase("dr")){
				rm.add(m);
			}
		}
		for(Annotation a: rm){
			a.removeFromIndexes();
		}
	}

	private void removeHistoryOf(JCas jCas) {
		FSIterator<Annotation> iter = jCas.getAnnotationIndex(NEMarkable.type).iterator();
		ArrayList<Annotation> rm = new ArrayList<Annotation>();
		while(iter.hasNext()){
			NEMarkable m = (NEMarkable) iter.next();
			if(m.getCoveredText().equalsIgnoreCase("history of")){
				rm.add(m);
			}
		}
		for(Annotation a: rm){
			a.removeFromIndexes();
		}
	}

	private void expandToNP (JCas aJCas, LinkedList<Annotation> markables) {
//		FSIterator<Annotation> iter = aJCas.getJFSIndexRepository().getAnnotationIndex(LookupWindowAnnotation.type).iterator();
		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(TreebankNode.type).iterator();
		LinkedList<Annotation> l = FSIteratorToList.convert(iter);

		for (Annotation m : markables){
			TreebankNode node = MarkableTreeUtils.markableNode(aJCas, m.getBegin(), m.getEnd());
			if(node == null) continue;
			while(!(node.getNodeType().equals("NP") || node.getNodeType().equals("NML"))){
				node = node.getParent();
				if(node == null) break;
			}
			if(node == null) continue;
			if(node.getChildren().size() == 3 && node.getChildren(1).getNodeType().equals("CC")){
				continue;
			}
			// only expand if markable is at end of node.
			// the following expression is more complicated however due to the way that
			// NP sentences are annotated (and thus parsed).  The NP will technically include the period so we will allow the node an
			// extra character.  This should allow a period but nothing else.
			if(node.getEnd()-1 > m.getEnd()) continue;
			String s = node.getCoveredText().toLowerCase();
			if (s.startsWith("his ") ||
					s.startsWith("her ") ||
					s.startsWith("its "))
				m.setBegin(node.getBegin()+4);
			else if (s.startsWith("their "))
				m.setBegin(node.getBegin()+6);
			else if (!s.matches("^\\s*$"))          // some lookupwindows start w/ multiple linefeeds in the pitt data
				m.setBegin(node.getBegin());
		}
	}

	// are any of the named entities contained within this chunk?
	// if so return the first that is.
	private Annotation containsAny (Chunk c, LinkedList<Annotation> l) {
		int a = c.getBegin();
		int b = c.getEnd();
		for (Annotation ne : l)
			if (a<=ne.getBegin() && b>=ne.getEnd())
				return ne;
			else if (ne.getBegin()>=b)
				return null;
		return null;
	}

	// merge NP# -> NP' PP, where NP' is marked as a Markable, by making NP# a markable  
	private void mergeNP (JCas jcas) {
		Map<Integer,TreebankNode> innerMap = null;
		// mark the boundaries of every NP:
		FSIterator<Annotation> nodeIter = jcas.getAnnotationIndex(TreebankNode.type).iterator();
		HashMap<Integer,Map<Integer,TreebankNode>> npMap = new HashMap<Integer,Map<Integer,TreebankNode>>();
		while(nodeIter.hasNext()){
			TreebankNode node = (TreebankNode) nodeIter.next();
			if(node.getNodeType().equals("NP")){
				innerMap = npMap.get(node.getBegin());
				if(innerMap == null){
					innerMap = new HashMap<Integer,TreebankNode>();
				}
				innerMap.put(node.getEnd(), node);
				npMap.put(node.getBegin(), innerMap);
			}
		}
		
		// now check if any of the NE markables share the exact same boundaries:
		FSIterator<Annotation> neIter = jcas.getAnnotationIndex(Markable.type).iterator();
		while(neIter.hasNext()){
			Markable nem = (Markable) neIter.next();
			innerMap = npMap.get(nem.getBegin());
			if(innerMap != null && innerMap.containsKey(nem.getEnd())){	
				// found one!  Check if it has an NP parent and a PP sibling:
				TreebankNode node = innerMap.get(nem.getEnd());
				TreebankNode parent = node.getParent();
				if(parent.getChildren().size() == 2 && parent.getChildren(0) == node && parent.getNodeType().equals("NP") && parent.getChildren(1).getNodeType().equals("PP")){
					// 	Jackpot!  Expand the original markable to be the whole NP -> NP PP construction.
					nem.setEnd(parent.getEnd());
				}
			}
		}
	}

	/*
	 * JJ modifiers are often tagged as NEs in addition to the NP phrase which they are a part of when
	 * it's difficult to conceive of the modifier being referred to independently of the whole phrase:
	 * 	surgical procedures
	 */

	private void elevateAdjectives(JCas jcas){
		FSIterator<Annotation> markables = jcas.getAnnotationIndex(NEMarkable.type).iterator();
		while(markables.hasNext()){
			NEMarkable mark = (NEMarkable) markables.next();
			TreebankNode node = MarkableTreeUtils.markableNode(jcas, mark.getBegin(), mark.getEnd());
			if(node.getNodeType().equals("JJ")){
				while(node.getNodeType().equals("JJ")){
					node = node.getParent();
					if(node == null) break;
				}
				if(node != null){
					mark.setBegin(node.getBegin());
					mark.setEnd(node.getEnd());
				}
			}
		}
			
	}

	private void rmDup(JCas aJCas, LinkedList<Annotation> markables) {
		HashSet<Annotation> rm = new HashSet<Annotation>();
		HashMap<String,Annotation> keep = new HashMap<String,Annotation>();
		
		for (int i = 0; i < markables.size(); i++) {
			Annotation m1 = markables.get(i);
			String key = m1.getBegin() + "-" + m1.getEnd();
			if(!keep.containsKey(key)){
				keep.put(key, m1);
			}else{
				Annotation m2 = keep.get(key);
				if(m2 instanceof DemMarkable && m1 instanceof NEMarkable){
					rm.add(m2);
					keep.put(key,m1);
				}else if(m1 instanceof DemMarkable && m2 instanceof NEMarkable){
					rm.add(m1);
				}else{
					// doesn't matter, they're probably both NE's
					rm.add(m1);
				}
			}
			
		}
		for (Annotation a : rm)
			a.removeFromIndexes();
	}
}
