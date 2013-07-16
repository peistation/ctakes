package org.apache.ctakes.temporal.ae.baselines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class RecallBaselineEventEventRelationAnnotator extends
		RelationExtractorAnnotator {
	  public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
		      throws ResourceInitializationException {
		    return AnalysisEngineFactory.createPrimitiveDescription(
		        RecallBaselineEventEventRelationAnnotator.class,
		        CleartkAnnotator.PARAM_IS_TRAINING,
		        false,
		        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
		        new File(modelDirectory, "model.jar"));
		  }

	@Override
	protected Class<? extends Annotation> getCoveringClass() {
		return Sentence.class;
	}

	@Override
	protected List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas jCas, Annotation sentence) {
	    List<IdentifiedAnnotationPair> results = new ArrayList<IdentifiedAnnotationPair>();
	    // get all event mentions in the sentence
	    List<EventMention> events = JCasUtil.selectCovered(jCas, EventMention.class, sentence);
	    
	    // filter out all the ctakes-generated events with more specific types
	    List<EventMention> realEvents = new ArrayList<EventMention>();
	    for(EventMention event : events){
	    	if(event.getClass().equals(EventMention.class)){
	    		realEvents.add(event);
	    	}
	    }
	    events = realEvents;
	    
	    // if we still have more than 1 we can continue
	    if(events.size() < 2) {
	      return results;
	    }
	    
	    // left-most event is the anchor and we will say all others are contained by it.
	    EventMention anchor = events.get(0);
	    for(int i = 1; i < events.size(); i++){
	          IdentifiedAnnotationPair pair = new IdentifiedAnnotationPair(anchor, events.get(i));
	          results.add(pair);
	    }
	    
	    return results;
	}
	
	/*
	 * For this method, we simply return the positive label "CONTAINS". All of the work in the baseline is
	 * done by getCandidateRelationArgumentPairs().
	 * 
	 * (non-Javadoc)
	 * @see org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator#classify(java.util.List)
	 */
	@Override
	public String classify(List<Feature> features) {
		return "CONTAINS";
	}
}
