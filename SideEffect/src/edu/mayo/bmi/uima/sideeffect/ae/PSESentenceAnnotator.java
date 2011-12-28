package edu.mayo.bmi.uima.sideeffect.ae;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;

import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.util.FSUtil;
import edu.mayo.bmi.uima.sideeffect.type.PSESentence;
import edu.mayo.bmi.uima.sideeffect.util.SEUtil;

/**
 * Identify the sentence(s) that contains PSE (signs/symptoms or disease/disorder) and drugs
 * and add them to PSESentence.
 * - If the sentence containing PSE doesn't have drug, include the previous sentence if it contains drug
 *   and it is in the same line (paragraph).
 * - Disregard the particular section(s) (eg, allergy section (20105))
 * This annotation will be used to extract features for side-effect sentence classification.
 * 
 * @author Mayo Clinic
 *
 */
public class PSESentenceAnnotator extends JTextAnnotator_ImplBase {
	public static final String PARAM_IGNORE_SECTIONS = "SectionsToIgnore";
	private Set setionsToIgnore;
	
	public void initialize(AnnotatorContext aContext) 
		throws AnnotatorInitializationException, AnnotatorConfigurationException
	{
		super.initialize(aContext);

		// get sections to ignore
		String[] sections;
		try {
			sections = (String[]) getContext().getConfigParameterValue(PARAM_IGNORE_SECTIONS);
			setionsToIgnore = new HashSet();
			for (int i = 0; i < sections.length; i++)
				setionsToIgnore.add(sections[i]);      
		} catch (AnnotatorContextException e) {
			e.printStackTrace();
		}

	}

	public void process(JCas jcas, ResultSpecification arg1)
			throws AnnotatorProcessException {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator senIter = indexes.getAnnotationIndex(Sentence.type).iterator();

		while(senIter.hasNext()) {
			Sentence sen = (Sentence) senIter.next();
			boolean foundDrug = false;
			boolean foundPSE = false;
			
			if(setionsToIgnore.contains(sen.getSegmentId())) continue;
			
			// if drug is not found in the same sentence and the previous sentence contains
			// drug and they are in the same line, then sentence will be previous + current sentence
			Iterator neIter = FSUtil.getAnnotationsInSpanIterator(
					jcas, NamedEntity.type, sen.getBegin(), sen.getEnd()+1);
			while(neIter.hasNext()) {
				NamedEntity n = (NamedEntity) neIter.next();
				if(n.getTypeID()==2 || n.getTypeID()==3) 
					foundPSE = true;
				
				if(n.getTypeID()==1) 
					foundDrug = true;				
			}

			if(!foundPSE) continue;
			
			if(foundPSE && foundDrug) {				
				PSESentence ps = new PSESentence(jcas);
				ps.setBegin(sen.getBegin());
				ps.setEnd(sen.getEnd()); 
				ps.addToIndexes();
			}
			else if(foundPSE && !foundDrug) {
				int num = sen.getSentenceNumber();
				num = (num>0) ? num-1 : num;    			
				int [] previousSenSpan = SEUtil.getSentenceSpanOfGivenSentenceNum(jcas, num);

				//only if they are in the same line
				if(SEUtil.isSpanInSameLine(jcas, previousSenSpan[0], sen.getEnd())) {
					neIter = FSUtil.getAnnotationsInSpanIterator(jcas, NamedEntity.type, 
							previousSenSpan[0], previousSenSpan[1]+1);
					while(neIter.hasNext()) {
						NamedEntity n = (NamedEntity) neIter.next();
						if(n.getTypeID()==1) { 							
							PSESentence ps = new PSESentence(jcas);
							ps.setBegin(previousSenSpan[0]);
							ps.setEnd(sen.getEnd()); 
							ps.addToIndexes();
							break;
						}
					}
				}				
			}			
		}
	}
}
