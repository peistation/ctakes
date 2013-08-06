package org.apache.ctakes.temporal.ae;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Event;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * for every cTAKES EventMention annotation, create a ClearTK Event Annotation
 * @author Chen Lin
 *
 */
public class EventToClearTKEventAnnotator extends JCasAnnotator_ImplBase {

	public EventToClearTKEventAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(JCas jCas)
			throws AnalysisEngineProcessException {
		for (EventMention eMention : JCasUtil.select(jCas, EventMention.class)) {
			// create a cleartk event object
			Event event = new Event(jCas);
			event.setBegin(eMention.getBegin());
			event.setEnd(eMention.getEnd());
			
			event.addToIndexes();
		}

	}

	public static AnalysisEngineDescription getAnnotatorDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(EventToClearTKEventAnnotator.class);
	}

}
