package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.uimafit.util.JCasUtil;

public class EventArgumentPropertyExtractor implements
    RelationFeaturesExtractor {

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1,
      IdentifiedAnnotation arg2) throws AnalysisEngineProcessException {
    List<Feature> feats = new ArrayList<Feature>();
    
    Sentence coveringSent = JCasUtil.selectCovering(jCas, Sentence.class, arg1.getBegin(), arg1.getEnd()).get(0);
    List<EventMention> events = JCasUtil.selectCovered(EventMention.class, coveringSent);
    List<EventMention> realEvents = new ArrayList<EventMention>();
    for(EventMention event : events){
    	// filter out ctakes events
    	if(event.getClass().equals(EventMention.class)){
    		realEvents.add(event);
    	}
    }
    events = realEvents;
    EventMention anchor = events.get(0);
    
    if(arg1.getBegin() == anchor.getBegin() && arg1.getEnd() == anchor.getEnd()){
    	feats.add(new Feature("Arg1LeftmostEvent"));
    }else if(arg2.getBegin() == anchor.getBegin() && arg2.getEnd() == anchor.getEnd()){
    	feats.add(new Feature("Arg2LeftmostEvent"));
    }
    
    if(arg1 instanceof EventMention){
      feats.addAll(getEventFeats("mention1property", (EventMention)arg1));
    }
    if(arg2 instanceof EventMention){
      feats.addAll(getEventFeats("mention2property", (EventMention)arg2));
    }
    return feats;
   }

  private static Collection<? extends Feature> getEventFeats(String name, EventMention mention) {
    List<Feature> feats = new ArrayList<Feature>();
    
    feats.add(new Feature(name + "_modality", mention.getEvent().getProperties().getContextualModality()));
    feats.add(new Feature(name + "_aspect", mention.getEvent().getProperties().getContextualAspect()));
    feats.add(new Feature(name + "_permanence", mention.getEvent().getProperties().getPermanence()));
    feats.add(new Feature(name + "_polarity", mention.getEvent().getProperties().getPolarity()));
    feats.add(new Feature(name + "_category", mention.getEvent().getProperties().getCategory()));
    feats.add(new Feature(name + "_degree", mention.getEvent().getProperties().getDegree()));
    feats.add(new Feature(name + "_doctimerel", mention.getEvent().getProperties().getDocTimeRel()));
    
    return feats;
  }

}
