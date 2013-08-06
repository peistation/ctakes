package org.apache.ctakes.temporal.ae;

import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.TemporalLink;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * for every cTAKES EventMention annotation, create a ClearTK Event Annotation
 * @author Chen Lin
 *
 */
public class ClearTKDocTimeRelAnnotator extends JCasAnnotator_ImplBase {

	public ClearTKDocTimeRelAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(JCas jCas)
			throws AnalysisEngineProcessException {
		for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
			// create a cleartk event object
			String relation = tlink.getRelationType();
//			System.out.println("__find tlink relation: " + relation + "		srouce: " + tlink.getSource().getCoveredText() + "  target: " + tlink.getTarget().getType().getName());
			
			Anchor linksource = tlink.getSource();
			
			EventMention ement = new EventMention(jCas);
			ement.setBegin(linksource.getBegin());
			ement.setEnd(linksource.getEnd());
			Event event = new Event(jCas);
			
			EventProperties eventProperties = new EventProperties(jCas);
			if ("INCLUDES".equals(relation)){
  		    	eventProperties.setDocTimeRel("OVERLAP");
  		    }else{
  		    	eventProperties.setDocTimeRel(relation);
  		    }
  		    event.setProperties(eventProperties);
  		    event.setMentions(new FSArray(jCas, 1));
  		    event.setMentions(0, ement);
		    ement.setEvent(event);
		    
		    eventProperties.addToIndexes();
  		  	event.addToIndexes();
  		  	ement.addToIndexes();
		    
		}

	}

	public static AnalysisEngineDescription getAnnotatorDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(ClearTKDocTimeRelAnnotator.class);
	}

}
