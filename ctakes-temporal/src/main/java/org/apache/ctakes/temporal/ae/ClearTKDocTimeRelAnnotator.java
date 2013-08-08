package org.apache.ctakes.temporal.ae;

import java.util.Map;

import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.TemporalLink;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Maps;

/**
 * for every cTAKES EventMention annotation, create a ClearTK Event Annotation
 * 
 * @author Chen Lin
 * 
 */
public class ClearTKDocTimeRelAnnotator extends JCasAnnotator_ImplBase {

	public ClearTKDocTimeRelAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		DocumentCreationTime dct = JCasUtil.selectSingle(jCas, DocumentCreationTime.class);
		Map<EventMention, String> docTimeRels = Maps.newHashMap();
		for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
			Anchor source = tlink.getSource();
			Anchor target = tlink.getTarget();
			if (source instanceof org.cleartk.timeml.type.Event && target.equals(dct)) {
				for (EventMention event : JCasUtil.selectCovered(jCas, EventMention.class, source)) {
					docTimeRels.put(event, tlink.getRelationType());
				}
			}
		}

		for (EventMention eventMention : JCasUtil.select(jCas, EventMention.class)) {
			// needed because SignSymptomMention, etc. do not have EventProperties
			if (eventMention.getClass().equals(EventMention.class)) {
				String docTimeRel = docTimeRels.get(eventMention);
				
				// convert missing or INCLUDES to OVERLAP
				if (docTimeRel == null || "INCLUDES".equals(docTimeRel)) {
					docTimeRel = "OVERLAP";
				}
				eventMention.getEvent().getProperties().setDocTimeRel(docTimeRel);
			}
		}
	}

	public static AnalysisEngineDescription getAnnotatorDescription()
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(ClearTKDocTimeRelAnnotator.class);
	}

}
