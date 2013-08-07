package org.apache.ctakes.temporal.ae;

import java.io.File;

import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.Predicate;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class NEPredicateEventAnnotator extends TemporalEntityAnnotator_ImplBase {

	public NEPredicateEventAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(JCas jCas, Segment segment)
			throws AnalysisEngineProcessException {
		for (EntityMention neMention : JCasUtil.select(jCas, EntityMention.class)) {
			// create the event object
			Event event = new Event(jCas);
			event.setConfidence(1.0f);

			// create the event mention
			EventMention eventMention = new EventMention(jCas, neMention.getBegin(), neMention.getEnd());
			eventMention.setConfidence(1.0f);

			// add the links between event, mention and properties
			//  		  event.setProperties(eventProperties);
			//  		  event.setMentions(neMention.getOntologyConceptArr());
			eventMention.setEvent(event);
			eventMention.setOntologyConceptArr(neMention.getOntologyConceptArr());

			// add the annotations to the indexes
			//  		  eventProperties.addToIndexes();
			event.addToIndexes();
			eventMention.addToIndexes();
		}

		//add predicates
		for (Predicate predicate : JCasUtil.select(jCas, Predicate.class)) {
			// create the event object
			Event event = new Event(jCas);
			event.setConfidence(1.0f);

			// create the event mention
			EventMention eventMention = new EventMention(jCas, predicate.getBegin(), predicate.getEnd());
			eventMention.setConfidence(1.0f);

			// add the links between event, mention and properties
			//	  		  event.setProperties(eventProperties);
			//	  		  event.setMentions(neMention.getOntologyConceptArr());
			eventMention.setEvent(event);

			// add the annotations to the indexes
			//	  		  eventProperties.addToIndexes();
			event.addToIndexes();
			eventMention.addToIndexes();
		}

	}

	public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
		      throws ResourceInitializationException {
		    return AnalysisEngineFactory.createPrimitiveDescription(
		    	NEPredicateEventAnnotator.class,
		    	CleartkAnnotator.PARAM_IS_TRAINING,
		        false,
		        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
		        new File(modelDirectory, "model.jar"));
	}

}
