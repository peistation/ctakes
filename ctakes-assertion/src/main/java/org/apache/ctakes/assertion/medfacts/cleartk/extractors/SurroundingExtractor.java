package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.assertion.zoner.types.Zone;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class SurroundingExtractor implements SimpleFeatureExtractor
{
	protected static final Logger logger = Logger.getLogger(SurroundingExtractor.class);
	
//	Class<? extends Annotation> ancestorAnnotationClass;
//	
//	public SurroundingExtractor(Class<? extends Annotation> ancestorAnnotationClass)
//	{
//		this.ancestorAnnotationClass = ancestorAnnotationClass;
//	}

	@Override
	public List<Feature> extract(JCas jcas, Annotation targetAnnotation)
			throws CleartkExtractorException
	{
		logger.info("SurroundingExtractor.extract() BEGIN");
		//JCasUtil.indexCovering(jcas, IdentifiedAnnotation.class, this.ancestorAnnotationClass)
		
		Map<EntityMention, Collection<Zone>> coveringMap =
			JCasUtil.indexCovering(jcas, EntityMention.class, Zone.class);
		
		IdentifiedAnnotation targetEntityAnnotation = (IdentifiedAnnotation)targetAnnotation;
		
		Collection<Zone> zoneList = coveringMap.get(targetEntityAnnotation);
		
		if (zoneList == null || zoneList.isEmpty())
		{
			//return null;
			logger.info("SurroundingExtractor.extract() early END (no zones)");
			new ArrayList<Feature>();
		}
		
		ArrayList<Feature> featureList = new ArrayList<Feature>();
		for (Zone zone : zoneList)
		{
			Feature currentFeature = new Feature("zone", zone.getLabel());
			logger.info(String.format("zone: %s", zone.getLabel()));
			logger.info(String.format("zone feature: %s", currentFeature.toString()));
			featureList.add(currentFeature);
		}
		
		logger.debug("SurroundingExtractor.extract() END");
		return featureList;
	}

}
