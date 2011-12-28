/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
@author Tim Miller
 * 
 */
package edu.mayo.bmi.uima.coref.ae;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.chboston.cnlp.ctakes.coref.eval.helpers.Span;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanAlignment;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanOffsetComparator;

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.coref.type.DemMarkable;
import edu.mayo.bmi.uima.coref.type.Markable;
import edu.mayo.bmi.uima.coref.type.NEMarkable;
import edu.mayo.bmi.uima.coref.type.PronounMarkable;

public class GoldMarkableCreator extends JCasAnnotator_ImplBase {

	String goldDir = null;

	@Override
	public void initialize(org.apache.uima.UimaContext aContext) throws org.apache.uima.resource.ResourceInitializationException {
		goldDir = (String) aContext.getConfigParameterValue("goldPairsDirectory");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
		if(docId == null) docId = "1322566403_1";
		Scanner scanner;
		try {
			scanner = new Scanner(new File(goldDir + "/" + docId));
			// gold standard markables
			HashSet<Span> gold = new HashSet<Span>();
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				String[] inds = line.split("\\s+");
				gold.add(new Span(inds[0]));
				gold.add(new Span(inds[1]));
			}
			
			HashSet<Span> sys = new HashSet<Span>();
			HashMap<Span,Markable> span2mark = new HashMap<Span,Markable>();
			HashSet<Markable> rm = new HashSet<Markable>();
			HashSet<Markable> keepers = new HashSet<Markable>();
			FSIterator iter = jcas.getAnnotationIndex(Markable.type).iterator();
			while(iter.hasNext()){
				Markable m = (Markable) iter.next();
				String key = m.getBegin() + "-" + m.getEnd();
				Span span = new Span(key);
				sys.add(span);
				span2mark.put(span, m);
				rm.add(m);
			}
			ArrayList<Span> goldList = new ArrayList<Span>(gold);
			ArrayList<Span> sysList = new ArrayList<Span>(sys);
			Collections.sort(goldList, new SpanOffsetComparator());
			Collections.sort(sysList, new SpanOffsetComparator());
			SpanAlignment align = new SpanAlignment(goldList.toArray(new Span[]{}), sysList.toArray(new Span[]{}));
			int[] span1 = align.get1();
			int[] span2 = align.get2();
			int i = 0; int j = 0;
			while(i < span1.length && j < span2.length){
				if(span1[i] == span2[j]){
					keepers.add(span2mark.get(sysList.get(j)));
					i++; j++;
				}else if(span1[i] < span2[j]){
					i++;
				}else if(span1[i] > span2[j]){
					j++;
				}
			}
			
			for(Markable m : rm){
				if(!keepers.contains(m)){
					m.removeFromIndexes();
				}
			}
//			for(int i = 0; i < span1.length; i++){
//				System.out.print(span1[i] + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < span2.length; i++){
//				System.out.print(span2[i] + " ");
//			}
//			System.out.println();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

//	@Override
//	public void process(JCas jcas) throws AnalysisEngineProcessException {
//		String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
//		if(docId == null) docId = "1111626129_1";
//		Scanner scanner;
//		try {
//			scanner = new Scanner(new File(goldDir + "/" + docId));
//			// gold standard markables
//			HashSet<Span> gold = new HashSet<Span>();
//			// keepers - those that have been matched with 
//			HashSet<Markable> keepers = new HashSet<Markable>();
//			HashSet<Markable> holding = new HashSet<Markable>();
//			
//			while(scanner.hasNextLine()){
//				String line = scanner.nextLine();
//				String[] inds = line.split("\\s+");
//				gold.add(new Span(inds[0]));
//				gold.add(new Span(inds[1]));
//			}
//
//	
//			// now iterate over previously found markables and delete ones that aren't in our collection
//			FSIterator iter = jcas.getAnnotationIndex(Markable.type).iterator();
//			
//			// mark all the markables that are in the gold standard exactly (same span)
//			while(iter.hasNext()){
//				Markable m = (Markable) iter.next();
//				int start = m.getBegin();
//				int end = m.getEnd();
//				Span newSpan = new Span(start+"-"+end);
//				if(gold.contains(newSpan)){
//					gold.remove(newSpan);
//					keepers.add(m);
//				}else{
//					holding.add(m);
//				}
//				
//			}
//		
//			
//			// keep all the remaining ne's that end in the same position as gold standard
////			for(String m : postMarkables){
////				String[] inds = m.split("-");
////				
////			}
//			
//			// remove all markables that are not in our list of "keepers"
//			HashSet<Markable> rm = new HashSet<Markable>();
//			iter = jcas.getAnnotationIndex(Markable.type).iterator();
//			while(iter.hasNext()){
//				Markable m = (Markable) iter.next();
//				if(!keepers.contains(m)){
//					rm.add(m);
//				}
//			}
//			
//			for(Markable m : rm){
//				m.removeFromIndexes();
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
}
