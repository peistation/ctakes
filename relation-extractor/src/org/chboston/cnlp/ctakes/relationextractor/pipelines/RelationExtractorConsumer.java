package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

/**
 * This is a sample relation annotation consumer. 
 * Currently it displays the relation annotations.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorConsumer extends JCasAnnotator_ImplBase {

	@Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    JCas systemView;
    try {
      systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }	  
    
    for(BinaryTextRelation binaryTextRelation : JCasUtil.select(systemView, BinaryTextRelation.class)) {
    	    	
    	String category = binaryTextRelation.getCategory();
    	
    	IdentifiedAnnotation entity1; // entity whose role is "Argument"
    	IdentifiedAnnotation entity2; // entity whose role is "Related_to"
    	
    	if(binaryTextRelation.getArg1().getRole().equals("Argument")) {
    		entity1 = (IdentifiedAnnotation) binaryTextRelation.getArg1().getArgument();
    		entity2 = (IdentifiedAnnotation) binaryTextRelation.getArg2().getArgument();
    	} else {
    		entity1 = (IdentifiedAnnotation) binaryTextRelation.getArg2().getArgument();
    		entity2 = (IdentifiedAnnotation) binaryTextRelation.getArg1().getArgument();
    	}
    	
    	String arg1 = entity1.getCoveredText();
    	String arg2 = entity2.getCoveredText();
    	
    	int type1 = entity1.getTypeID();
    	int type2 = entity2.getTypeID();
    	
    	// print relation and its arguments: location_of(colon/6, colon cancer/2)
    	System.out.format("%s(%s/%d, %s/%d)\n", category, arg1, type1, arg2, type2);
    	
    	// print the sentence containing this instance
    	List<Sentence> list = JCasUtil.selectCovering(jCas, Sentence.class, entity1.getBegin(), entity1.getEnd());
    	for(Sentence s : list) {
    		System.out.println(s.getCoveredText());
    	}
    	System.out.println();
    }
  }

}
