package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

public class TemporalAttributeFeatureExtractor implements
		RelationFeaturesExtractor {

	@Override
	public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
			IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
		ArrayList<Feature> feats = new ArrayList<Feature>();
		EventMention event = null;
		TimeMention time = null;

		// swap the order if necessary:
		if(arg2.getBegin() <= arg1.getBegin() && arg2.getEnd() <= arg1.getEnd()){
			IdentifiedAnnotation temp = arg1;
			arg1 = arg2;
			arg2 = temp;
		}

		if(arg1 instanceof EventMention){
			event = (EventMention) arg1;
			time = (TimeMention) arg2;
			feats.add(new Feature("Arg1-Event-Modality", event.getEvent().getProperties().getContextualModality()));
			feats.add(new Feature("Arg2-Time-Class-" + time.getTimeClass()));
		}else{
			time = (TimeMention) arg1;
			event = (EventMention) arg2;
			feats.add(new Feature("Arg1-Timex-", time.getTimeClass()));
			feats.add(new Feature("Arg2-Event-", event.getEvent().getProperties().getContextualModality()));
		}
		
		
		return feats;
	}

}
