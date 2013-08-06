package org.apache.ctakes.temporal.ae.baselines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
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

public class TreeHeightBaseline extends RelationExtractorAnnotator {

	  public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
		      throws ResourceInitializationException {
		    return AnalysisEngineFactory.createPrimitiveDescription(
		        TreeHeightBaseline.class,
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
	public String classify(List<Feature> features) {
		return "CONTAINS";
	}
	
	@Override
	protected List<RelationFeaturesExtractor> getFeatureExtractors() {
		return new ArrayList<RelationFeaturesExtractor>();
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
	    
	    // for each event, cmopute the tree distance to the root, and call the closest to the root the anchor.
	    int minDepth = Integer.MAX_VALUE;
	    EventMention anchorMention = null;
	    for(EventMention event : events){
	    	TreebankNode node = AnnotationTreeUtils.annotationNode(jCas, event);
	    	int depth = 0;
	    	while(node.getParent() != null){
	    		depth++;
	    		node = node.getParent();
	    	}
	    	if(depth < minDepth){
	    		minDepth = depth;
	    		anchorMention = event;
	    	}
	    }
	    
	    // now that we have the anchor, connect every other mention to it:
	    for(EventMention event : events){
	    	if(event != anchorMention){
	    		results.add(new IdentifiedAnnotationPair(anchorMention, event));
	    	}
	    }
	    
	    return results;
	}

}
