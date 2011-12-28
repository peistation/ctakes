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
    
@author Jiaping Zheng
 * @author Tim Miller
 */
package edu.mayo.bmi.coref.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode;
import org.chboston.cnlp.ctakes.parser.uima.type.TreebankNode;
import org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode;

import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.resource.FileLocator;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.UmlsConcept;
import edu.mayo.bmi.coref.util.AnnotationSelector;
import edu.mayo.bmi.uima.coref.type.DemMarkable;
import edu.mayo.bmi.uima.coref.type.Markable;
import edu.mayo.bmi.uima.coref.type.NEMarkable;
import edu.mayo.bmi.uima.coref.type.PronounMarkable;
import edu.mayo.bmi.uima.lookup.type.LookupWindowAnnotation;

public class PairAttributeCalculator extends AttributeCalculator {

	protected Markable m1, m2;
	protected String s1, s2;

	public PairAttributeCalculator (JCas jcas, Markable m1, Markable m2) {
		super(jcas);
		this.m1 = m1;
		this.m2 = m2;
		s1 = m1.getCoveredText();
		s2 = m2.getCoveredText();
	}

	public double calcTokenDistance () {
		return AnnotationCounter.countInterval(AnnotationSelector.selectBaseToken(jcas), m1.getEnd(), m2.getBegin()) / (double) CorefConsts.TOKDIST;
	}

	public double calcSentenceDistance () {
		return getSentenceDistance() / (double) CorefConsts.SENTDIST;
	}

	public int getSentenceDistance() {
		return AnnotationCounter.countPoint(AnnotationSelector.selectSentence(jcas), m1.getEnd(), m2.getBegin());
	}

	public String calcExactMatch () {
		return s1.equalsIgnoreCase(s2) ? "yes" : "no";
	}

	public String calcStartMatch () {
		return TextMatch.startMatch(s1, s2) ? "yes" : "no";
	}

	public String calcMidMatch () {
		return "no";
	}

	public String calcEndMatch () {
		return TextMatch.endMatch(s1, s2) ? "yes" : "no";
	}

	public String calcStringMatch() {
		return ( calcExactMatch().equals("yes") || calcStartMatch().equals("yes") || calcEndMatch().equals("yes") ? "yes" : "no");
	}
	
	public String calcSoonStr () {
		String sl1 = s1.toLowerCase();
		String sl2 = s2.toLowerCase();
//		if (sl1.startsWith("the ")) sl1 = sl1.substring(4);
//		if (sl1.startsWith("a ")) sl1 = sl1.substring(2);
//		if (sl2.startsWith("the ")) sl2 = sl2.substring(4);
//		if (sl2.startsWith("a ")) sl2 = sl2.substring(2);
		sl1 = nonDetSubstr(sl1);
		sl2 = nonDetSubstr(sl2);
		return sl1.equals(sl2) ? "C" : "I";
	}
	
	private String nonDetSubstr (String s) {
		if(s.startsWith("the ")) return s.substring(4);
		if(s.startsWith("a ")) return s.substring(2);
		if(s.startsWith("this ")) return s.substring(5);
		if(s.startsWith("that ")) return s.substring(5);
		return s;
	}

	public String calcPronoun1 () {
		return isPronoun(m1) ? "Y" : "N";
	}

	public String calcPronoun2 () {
		return isPronoun(m2) ? "Y" : "N";
	}

	public String calcDefinite2 () {
		return isDefinite(s2) ? "Y" : "N";
	}

	public String calcDemonstrative2 () {
		return isDemonstrative(s2) ? "Y" : "N";
	}

	public String calcNumberMatchC () {
		String n1 = number(m1);
		String n2 = number(m2);
		if (!n1.equals("U") && !n2.equals("U") && n1.equals(n2))
			return "Y";
		else
			return "N";
	}

	public String calcNumberMatchI () {
		String n1 = number(m1);
		String n2 = number(m2);
		if (!n1.equals("U") && !n2.equals("U") && !n1.equals(n2))
			return "Y";
		else
			return "N";
	}

	public String calcNumberMatchNA () {
		String n1 = number(m1);
		String n2 = number(m2);
		if (n1.equals("U") || n2.equals("U"))
			return "Y";
		else return "N";
	}

	public String calcNumberMatch () {
		String n1 = number(m1);
		String n2 = number(m2);
		if (n1.equals("U") || n2.equals("U"))
			return "NA";
		else if (n1.equals(n2))
			return "C";
		else
			return "I";
	}

	// heuristics
	//	public String calcAppositive () {
	//		if (jcas.getDocumentText().substring(m1.getBegin(), m2.getEnd())
	//				.equals(s1 + ", " + s2))
	//			return "yes";
	//		else return "no";
	//	}

	public String calcWnClassC () {
		if (m1.getContent() instanceof NamedEntity &&
				m2.getContent() instanceof NamedEntity) {
				NamedEntity ne1 = (NamedEntity) m1.getContent();
				NamedEntity ne2 = (NamedEntity) m2.getContent();
				if (ne1.getTypeID() == ne2.getTypeID())
					return "C";
				else return "N";
			} else
				return "N";
	}

	public String calcWnClassI () {
		if (m1.getContent() instanceof NamedEntity &&
				m2.getContent() instanceof NamedEntity) {
				NamedEntity ne1 = (NamedEntity) m1.getContent();
				NamedEntity ne2 = (NamedEntity) m2.getContent();
				if (ne1.getTypeID() != ne2.getTypeID())
					return "Y";
				else return "N";
			} else
				return "N";
	}

	public String calcWnClassNA () {
		if (!(m1.getContent() instanceof NamedEntity) ||
				!(m2.getContent() instanceof NamedEntity))
			return "Y";
		else
			return "N";
	}

	public String calcWnClass () {
		if (m1.getContent() instanceof NamedEntity &&
			m2.getContent() instanceof NamedEntity) {
			NamedEntity ne1 = (NamedEntity) m1.getContent();
			NamedEntity ne2 = (NamedEntity) m2.getContent();
			if (ne1.getTypeID() == ne2.getTypeID())
				return "C";
			else return "I";
		} else
			return "NA";
	}

	public String calcAlias () {
		if (m1.getContent() instanceof NamedEntity &&
			m2.getContent() instanceof NamedEntity) {
			NamedEntity ne1 = (NamedEntity) m1.getContent();
			NamedEntity ne2 = (NamedEntity) m2.getContent();

			ArrayList<String> l = new ArrayList<String>();
			FSArray fsa = ne1.getOntologyConceptArr();
			for (int i = 0; i < fsa.size(); ++i)
				if (fsa.get(i) instanceof UmlsConcept)
					l.add(((UmlsConcept)fsa.get(i)).getCui());

			fsa = ne2.getOntologyConceptArr();
			for (int i = 0; i < fsa.size(); ++i)
				if (fsa.get(i) instanceof UmlsConcept &&
					l.contains(((UmlsConcept)fsa.get(i)).getCui()))
					return "C";
		}
		return "I";
	}
	
	// PRO_STR in Ng and Cardie
	public String calcProStr () {
		if (isPronominal(m1) &&
			isPronominal(m2) &&
			s1.equalsIgnoreCase(s2))
			return "C";
		else
			return "I";
	}

//	public String calcPnStr () {
//		if (s1.equalsIgnoreCase(s2)) {
//			ArrayList<BaseToken> toks = containedTokens(m1.getBegin(), m2.getEnd());
//			for (BaseToken t : toks)
//				if (t.getPartOfSpeech().startsWith("NNP"))
//					return "yes";
//		}
//		return "no";
//	}

	// WORDS_STR in Ng and Cardie - currently not used
	public String calcWordsStr () {
		if (!isPronominal(m1) && !isPronominal(m2) &&
			s1.equalsIgnoreCase(s2))
			return "C";
		else
			return "I";
	}

	private String removeArticleAndDemon (String s) {
		if (s.toLowerCase().startsWith("a "))
			return s.substring(2);
		else if (s.toLowerCase().startsWith("an "))
			return s.substring(3);
		else if (s.toLowerCase().startsWith("the "))
			return s.substring(4);
		else if (s.toLowerCase().startsWith("this "))
			return s.substring(5);
		else if (s.toLowerCase().startsWith("that "))
			return s.substring(5);
		else if (s.toLowerCase().startsWith("these "))
			return s.substring(6);
		else if (s.toLowerCase().startsWith("those "))
			return s.substring(6);
		else
			return s;
	}

	// SOON_STR_NONPRO from Ng and Cardie
	public String calcSoonStrNonpro () {
		if (!isPronominal(m1) && !isPronominal(m2)) {
			String str1 = removeArticleAndDemon(s1);
			String str2 = removeArticleAndDemon(s2);
			if (str1.toLowerCase().indexOf(str2.toLowerCase()) >= 0 ||
				str2.toLowerCase().indexOf(str1.toLowerCase()) >= 0)
				return "C";
		}
		return "I";
	}


	// WORD_OVERLAP from Ng and Cardie 02
	public String calcWordOverlap () {
		ArrayList<String> t1 = contentWords(m1);
		ArrayList<String> t2 = contentWords(m2);
		for (String s : t2)
			if (t1.contains(s))
				return "C";
		return "I";
	}

	// TODO with syntax
	// MODIFIER from Ng and Cardie 02
	public String calcModifier () {
		return "yes"; 
	}

//	public String calcPnSubstr () {
//		
//	}

	// is l1 a proper substring of l2?
	// TODO optimize with Stringbuffer instead of concatenation
	private boolean isProperSubstring (ArrayList<String> l1, ArrayList<String> l2) {
		String str1 = "";
		String str2 = "";
		for (String s : l1)
			str1 += " " + s;
		for (String s: l2)
			str2 += " " + s;
		// FIXME This should be an AND ?
		if (str1.length()!=str2.length() || str2.indexOf(str1)>=0)
			return true;
		else
			return false;
	}

	public String calcWordsSubstr () {
		if (!isPronominal(m1) && !isPronominal(m2)) {
			ArrayList<String> t1 = contentWords(m1);
			ArrayList<String> t2 = contentWords(m2);
			if (isProperSubstring(t1, t2) || isProperSubstring(t2, t1))
				return "C";
		}
		return "I";
	}

	public String calcBothDefinitesC () {
		return (isDefinite(s1) && isDefinite(s2)) ? "Y" : "N";
	}

	public String calcBothDefinitesI () {
		return (!isDefinite(s1) && !isDefinite(s2)) ? "Y" : "N";
	}

	public String calcBothDefinitesNA () {
		boolean b1 = isDefinite(s1);
		boolean b2 = isDefinite(s2);
		return (!(b1&&b2) && (b1||b2)) ? "Y" : "N";
	}

	public String calcBothDefinites () {
		boolean b1 = isDefinite(s1);
		boolean b2 = isDefinite(s2);
		if (b1 && b2) return "C";
		if (b1 || b2) return "NA";
		return "I";
	}

	public String calcBothEmbeddedC () {
		return "N"; //TODO: sketch
	}

	public String calcBothEmbeddedI () {
		return "N"; //TODO: sketch
	}

	public String calcBothEmbeddedNA () {
		return "N"; //TODO: sketch
	}

	public String calcBothEmbedded () {
		return "NA"; //TODO: sketch
	}

	public String calcBothPronounsC () {
		boolean b1 = isPronoun(m1);
		boolean b2 = isPronoun(m2);
		return (b1 && b2) ? "Y" : "N";
	}

	public String calcBothPronounsI () {
		boolean b1 = isPronoun(m1);
		boolean b2 = isPronoun(m2);
		return (!b1 && !b2) ? "Y" : "N";
	}

	public String calcBothPronounsNA () {
		boolean b1 = isPronoun(m1);
		boolean b2 = isPronoun(m2);
		return (!(b1&&b2) && (b1||b2)) ? "Y" : "N";
	}

	public String calcBothPronouns () {
		boolean b1 = isPronoun(m1);
		boolean b2 = isPronoun(m2);
		if (b1 && b2) return "C";
		if (b1 || b2) return "NA";
		return "I";
	}

//	public String calcSpan () {
//		if (m1.getBegin()<=m2.getBegin()) {
//			if (m1.getEnd()>=m2.getEnd()) return "C";
//			else return "I";
//		} else {
//			if (m1.getEnd()<=m2.getEnd()) return "C";
//			else return "I";
//		}
//	}

	public String calcIndefinite () {
		if (s2.toLowerCase().startsWith("a ") ||
			s2.toLowerCase().startsWith("an "))
			return "I";
		else
			return "C";
	}

	public String calcPronoun () {
		 return (isPronoun(m1) && !isPronoun(m2)) ? "I" : "C";
	}

//	public String calcContainsPn () {
//		
//	}

	public String calcDefinite1 () {
		return isDefinite(s1)?"Y":"N";
	}

//	public String calcProperNoun () {
//		
//	}

	public String calcClosestComp () {
		if (calcWnClass().equals("C")) {
			ArrayList<Annotation> l = AnnotationSelector.selectNE(jcas);
			int m2type = ((NamedEntity)m2.getContent()).getTypeID();
			for (Annotation a : l) {
				if (((NamedEntity)a).getTypeID()==m2type &&
					a.getBegin()>=m1.getEnd() &&
					a.getEnd()<=m2.getBegin())
					return "I";
			}
			return "C";
		}
		return "I";
	}

	public String calcNPHead () {
		Annotation a = m1.getContent();
//		return (a.getEnd()==m1.getEnd() && a.getBegin()>m1.getBegin()) ? "yes" : "no";
		FSIterator iter = jcas.getJFSIndexRepository().getAnnotationIndex(LookupWindowAnnotation.type).iterator();
		while (iter.hasNext()) {
			LookupWindowAnnotation lwa = (LookupWindowAnnotation) iter.next();
			if (lwa.getBegin()<=a.getBegin() && lwa.getEnd()==a.getEnd())
				return "yes";
		}
		return "no";
	}

	
	public static void main(String[] args){
		// FOR DEBUGGING ONLY!
		//		PairAttributeCalculator.initTrigrams("NP<PP<UCP<VP<SQ<NP<S<TOP>S>VP>VP");

		// OK MAYBE FOR A LITTLE ANALYSIS TOO
		try{
//			trigrams = new HashMap<String,Integer>();
//			Scanner scanner = new Scanner(new File("/home/tmill/Projects/cNLP/cTakes-Ext/coref-resolver/results/v2/fullpaths.out"));
//			while(scanner.hasNextLine()){
//				String line = scanner.nextLine().trim();
//				PairAttributeCalculator.initTrigrams(line);
//			}
//			PrintWriter out = new PrintWriter("/home/tmill/Projects/cNLP/cTakes-Ext/coref-resolver/results/v2/trigramcounts.out");
//			for(Map.Entry<String,Integer> entry : trigrams.entrySet()){
//				out.println(entry.getValue()+" " + entry.getKey());
//			}
//			out.close();
		}catch(Exception e){
			System.err.println(e);
		}
	}

	

	public double calcPermStrDist () {
//		StringSim ss = new StringSim(s1, s2);
//		ss.setStopWords(stopwords);
//		return ss.calc();
		return 0.0;
	}

}
