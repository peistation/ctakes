package edu.mayo.bmi.uima.sideeffect.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.NewlineToken;
import edu.mayo.bmi.uima.core.type.Segment;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.WordToken;
import edu.mayo.bmi.uima.core.util.FSUtil;

/**
 * Utility methods used in the project
 * @author Mayo Clinic
 */

public class SEUtil {
	/** Return sentence span containing the span (begin & end)
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return int[] - int[0] is begin offset and int[1] is end offset 
	 */
	public static int[] getSentenceSpanContainingGivenSpan(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator<Annotation> iter= indexes.getAnnotationIndex(Sentence.type).iterator();
		int[] span = {-1, -1};
		
		while(iter.hasNext()) {
			Sentence sa = (Sentence) iter.next();
			if(begin>=sa.getBegin() && end<=sa.getEnd()) {
				span[0] = sa.getBegin();
				span[1] = sa.getEnd();
				break;
			}
		}

		return span;
	}
	
	/** Return sentence text containing the span (begin & end)
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return int[] - int[0] is begin offset and int[1] is end offset 
	 */
	public static String getSentenceTextContainingGivenSpan(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator iter= indexes.getAnnotationIndex(Sentence.type).iterator();
		String str="";
		
		while(iter.hasNext()) {
			Sentence sa = (Sentence) iter.next();
			if(begin>=sa.getBegin() && end<=sa.getEnd()) {
				str = sa.getCoveredText().trim();
				break;
			}
		}
		
		return str;
	}
	
	/** Return sentence span of the given sentence number
	 * @param jcas
	 * @param senNum
	 * @return int[] - int[0] is begin offset and int[1] is end offset 
	 */
	public static int[] getSentenceSpanOfGivenSentenceNum(JCas jcas, int senNum) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator iter= indexes.getAnnotationIndex(Sentence.type).iterator();
		
		int[] span = {-1, -1};
		int num = 0;		
		while(iter.hasNext()) {
			Sentence sa = (Sentence) iter.next();
			if(senNum == num) {
				span[0] = sa.getBegin();
				span[1] = sa.getEnd();
				break;
			}
			num++;
		}

		return span;
	}
	
	/**
	 * Return segmentID of the sentence containing the given span
	 * @param jcas
	 * @param begin - begin offset
	 * @param end - end offset
	 * @return
	 */
	public static String getSegmentIDOfSpan(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator iter= indexes.getAnnotationIndex(Sentence.type).iterator();
		String segID=null;
		
		while(iter.hasNext()) {
			Sentence sa = (Sentence) iter.next();
			if(begin>=sa.getBegin() && end<=sa.getEnd()) {
				segID = sa.getSegmentId();
				break;
			}
		}
				
		return segID;
	}
	
	/**
	 * Return segmentID contain the given span
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static String getSegmentID(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator iter= indexes.getAnnotationIndex(Segment.type).iterator();
		String segID=null;
		
		while(iter.hasNext()) {
			Segment seg = (Segment) iter.next();
			if(seg.getBegin()<=begin && seg.getEnd()>=end) {
				segID = seg.getId();
				break;
			}
		}
		
		return segID;
	}
	
	/**
	 * Returns 1 if 1 contains 2
	 * Returns 2 if 2 contains 1
	 * Returns 0 otherwise
	 */
	public static int contains(int b1, int e1, int b2, int e2) {
		if(b1<=b2 &&  e1>=e2) return 1;
		else if(b2<=b1 &&  e2>=e1) return 2;
		else return 0;
	}
	
	/**
	 * Returns true if one span intersects with the other
	 */
	public static boolean intersects(int b1, int e1, int b2, int e2) {
		if(contains(b1, e1, b2, e2)!=0) return true;
		//either 1's begin is within 2 or 2's begin is within 1
		return (b1<=b2 && b2<e1 ||
				b2<=b1 && b1<e2);
	}
	
	/**
	 * Return the List of drug NamedEntity within the span
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static List getDrugsInSpan(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
        Iterator neItr= indexes.getAnnotationIndex(NamedEntity.type).iterator();
        List l = new ArrayList();
        
        //add drug to List
        while (neItr.hasNext()) {
        	NamedEntity nea = (NamedEntity) neItr.next();        	
        	if(nea.getTypeID()==1)
        		if(nea.getBegin()>=begin && nea.getEnd()<=end)
        			l.add(nea);
        } 
        
        return l;
	}
	
	/**
	 * Return sentenceNumber of the sentence containing the given span
	 * 
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return sentenceNumber of the sentence containing the given span
	 */
	public static int getSentenceNumContainingGivenSpan(JCas jcas, int begin, int end) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator iter= indexes.getAnnotationIndex(Sentence.type).iterator();
		int senNum=-1;
		
		while(iter.hasNext()) {
			Sentence sa = (Sentence) iter.next();
			if(begin>=sa.getBegin() && end<=sa.getEnd()) {
				senNum = sa.getSentenceNumber();
				break;
			}
		}
		
		return senNum;
	}
	
	/**
	 * return the number of words except for "and" "or" in span 
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int getNumOfWordTokensInSpan(JCas jcas, int begin, int end) {
		Set ignoreWords = new HashSet();
		ignoreWords.add("and");
		ignoreWords.add("or");

		Iterator wtIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, WordToken.type, begin, end);
		int cnt=0;
		
		while(wtIter.hasNext()) {
			WordToken wt = (WordToken) wtIter.next();
			if(ignoreWords.contains(wt.getCoveredText().toLowerCase())) continue;				
			cnt++;
		}
		
		return cnt;
	}
	
	/**
	 * return the number of words in span except for "and" "or" and given NE  
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int getNumOfWordTokensInSpanExceptGivenNE(JCas jcas, int begin, int end, int neType) {
		Set<String> ignoreWords = new HashSet<String>();
		ignoreWords.add("and");
		ignoreWords.add("or");

		List<NamedEntity> neLst = new ArrayList<NamedEntity>();
		Iterator neIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, NamedEntity.type, begin, end+1);
		while(neIter.hasNext()) {
			NamedEntity ne = (NamedEntity) neIter.next();
			if(ne.getTypeID()==neType) neLst.add(ne);
		}
		
		Iterator wtIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, WordToken.type, begin, end);
		int cnt=0;
		while(wtIter.hasNext()) {
			WordToken wt = (WordToken) wtIter.next();
			if(ignoreWords.contains(wt.getCoveredText().toLowerCase())) continue;	
			
			boolean isNE = false;
			for(NamedEntity n : neLst) {
				if(n.getBegin()<=wt.getBegin() && n.getEnd()>=wt.getEnd()) {
					isNE = true;
					break;
				}					
			}
			if(isNE) continue;
			
			cnt++;
		}
		
		return cnt;
	}
	
	public static boolean isUpperCaseString(String str) {
		for(int i=str.length()-1; i>0; i--) 
			if(Character.isLowerCase(str.charAt(i)))
				return false;
		return true;
	}
	
	/**
	 * Return true if the given NE belongs to the line consisting of a upper-cased string without " - " 
	 * 
	 * @param jcas
	 * @param nea
	 * @return
	 */
	public static boolean isInUpperCaseStringLine(JCas jcas, NamedEntity nea) {
		boolean flag = false;
		int[] senSpan = getSentenceSpanContainingGivenSpan(
				jcas, nea.getBegin(), nea.getEnd());
		String senText = getSentenceTextContainingGivenSpan(
				jcas, nea.getBegin(), nea.getEnd());
		
		if(senText.indexOf(" - ")!=-1) return false;
		
		//sentence end is newline begin
		Iterator ntIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, NewlineToken.type, senSpan[0], senSpan[1]+2);
		while(ntIter.hasNext()) {
			NewlineToken nt = (NewlineToken) ntIter.next();
			//if sentence per line 
			if(senSpan[1]==nt.getBegin()) {
				if(isUpperCaseString(senText)) {
					flag = true;
					break;
				}
			}
		}
		
		return flag;
	}
	
	/**
	 * Return true if the given offsets are in the same line 
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isSpanInSameLine(JCas jcas, int begin, int end) {
		Iterator ntIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, NewlineToken.type, begin, end-1);
		if(ntIter.hasNext()) return false;
		return true;
	}
	
	/**
	 * Return true if a drug is between begin and end
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isDrugBetween(JCas jcas, int begin, int end) {
		Iterator neIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, NamedEntity.type, begin, end+1);
		while(neIter.hasNext()) {
			NamedEntity ne = (NamedEntity) neIter.next();
			if(ne.getTypeID()==1) return true;
		}
		
		return false;
	}
	
	/**
	 * Return true if a sign/symptom or disease/disorder is between begin and end
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isPSEBetween(JCas jcas, int begin, int end) {
		Iterator neIter = FSUtil.getAnnotationsInSpanIterator(
				jcas, NamedEntity.type, begin, end+1);
		while(neIter.hasNext()) {
			NamedEntity ne = (NamedEntity) neIter.next();
			if(ne.getTypeID()==2 || ne.getTypeID()==3) return true;
		}
		
		return false;
	}
	
	

	/**
	 * helper to look for plain text view for CDA processing or else use the default view.
	 * @param cas
	 * @param name
	 * @return
	 * @throws CASException
	 */
	public static JCas getJCasViewWithDefault(CAS cas, String name) throws CASException{
		JCas returnCas = null;
		Iterator<JCas> viewItr = cas.getJCas().getViewIterator();
		while(viewItr.hasNext()){
			JCas newJcas = viewItr.next();
			if(newJcas.getViewName().equals(name)){
				returnCas = newJcas;
			}
		}
		
		if (returnCas == null)
			returnCas = cas.getJCas();
		
		return returnCas;
	}
}
