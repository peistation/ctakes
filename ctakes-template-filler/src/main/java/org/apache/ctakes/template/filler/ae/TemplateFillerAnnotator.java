package org.apache.ctakes.template.filler.ae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.ctakes.typesystem.type.constants.CONST;

import org.apache.ctakes.typesystem.type.refsem.Date;
import org.apache.ctakes.typesystem.type.refsem.MedicationDosage;
import org.apache.ctakes.typesystem.type.refsem.MedicationDuration;
import org.apache.ctakes.typesystem.type.refsem.MedicationForm;
import org.apache.ctakes.typesystem.type.refsem.MedicationFrequency;
import org.apache.ctakes.typesystem.type.refsem.MedicationRoute;
import org.apache.ctakes.typesystem.type.refsem.MedicationStatusChange;
import org.apache.ctakes.typesystem.type.refsem.MedicationStrength;
import org.apache.ctakes.typesystem.type.refsem.Severity;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.DegreeOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.LocationOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationDosageModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDurationModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationFormModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFrequencyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationRouteModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStatusChangeModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStrengthModifier;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;

public class TemplateFillerAnnotator extends JCasAnnotator_ImplBase{

    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());
    private UimaContext uimaContext;


    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {

    	super.initialize(aContext);
    	uimaContext = aContext;

    	logger.info("Initializing " +  TemplateFillerAnnotator.class.getName());
	
    }

    /**
     * Copy values from <tt>original</tt> to <tt>mention</tt> and add <tt>mention</tt> to the indexes.
     * If <tt>original</tt> is a MedicationEventMention (which used to be created by ctakes-drug-ner),
     * create a MedicationMention from the MedicationEventMention, setting the MedicationMention specific
     * attributes like dosage from the MedicationEventMention attributes.
     * @param mention The annotation to copy values to
     * @param original The annotation to copy values from
     * @throws CASException
     */
    private void setAttributesFromOriginal(IdentifiedAnnotation mention, IdentifiedAnnotation original) throws CASException {
    	mention.setBegin(original.getBegin());
    	mention.setEnd(original.getEnd());
    	mention.setConditional(original.getConditional());
    	mention.setConfidence(original.getConfidence());
    	mention.setDiscoveryTechnique(original.getDiscoveryTechnique());
    	mention.setGeneric(original.getGeneric());
    	mention.setOntologyConceptArr(original.getOntologyConceptArr());
    	mention.setPolarity(original.getPolarity());
    	mention.setSegmentID(original.getSegmentID());
    	mention.setSentenceID(original.getSentenceID());
    	mention.setSubject(original.getSubject());
    	mention.setTypeID(original.getTypeID());
    	mention.setUncertainty(original.getUncertainty());
    	mention.setHistoryOf(original.getHistoryOf());
    	

    	if (original instanceof MedicationEventMention) {
    		MedicationEventMention medEventMention = (MedicationEventMention) original;
    		MedicationMention medMention = (MedicationMention) mention;
    		JCas jcas = medMention.getCAS().getJCas();
    		if (medEventMention.getStartDate()!=null) {
    			Date date = medEventMention.getStartDate();
    			TimeMention timeMention = new TimeMention(jcas);
    			timeMention.setDate(date);
    			medMention.setStartDate(timeMention);
    		}
    		if (medEventMention.getEndDate()!=null) {
    			Date date = medEventMention.getEndDate();
    			TimeMention timeMention = new TimeMention(jcas);
    			timeMention.setDate(date);
    			medMention.setEndDate(timeMention);
    		}
    		if (medEventMention.getMedicationDosage()!=null) {
    			MedicationDosage dosage = medEventMention.getMedicationDosage();
    			MedicationDosageModifier medDosageModifier = new MedicationDosageModifier(jcas);
    			medDosageModifier.setNormalizedForm(dosage);
    			medDosageModifier.setCategory(dosage.getValue());
    			medMention.setMedicationDosage(medDosageModifier);
    			
    		}
    		if (medEventMention.getMedicationDuration()!=null) {
    			MedicationDuration duration = medEventMention.getMedicationDuration();
    			MedicationDurationModifier medDurationModifier = new MedicationDurationModifier(jcas);
    			medDurationModifier.setNormalizedForm(duration);
    			medDurationModifier.setCategory(duration.getValue());
    			medMention.setMedicationDuration(medDurationModifier);
    		}
    		if (medEventMention.getMedicationForm()!=null) {
    			MedicationForm form = medEventMention.getMedicationForm();
    			MedicationFormModifier medFormModifier = new MedicationFormModifier(jcas);
    			medFormModifier.setNormalizedForm(form);
    			medFormModifier.setCategory(form.getValue());
    			medMention.setMedicationForm(medFormModifier);
    		}
    		if (medEventMention.getMedicationFrequency()!=null) {
    			MedicationFrequency frequency = medEventMention.getMedicationFrequency();
    			MedicationFrequencyModifier medFrequencyModifier = new MedicationFrequencyModifier(jcas);
    			medFrequencyModifier.setNormalizedForm(frequency);
    			medFrequencyModifier.setCategory(frequency.getNumber() + frequency.getUnit());
    			medMention.setMedicationFrequency(medFrequencyModifier);
    		}
    		if (medEventMention.getMedicationRoute()!=null) {
    			MedicationRoute route = medEventMention.getMedicationRoute();
    			MedicationRouteModifier medRouteModifier = new MedicationRouteModifier(jcas);
    			medRouteModifier.setNormalizedForm(route);
    			medRouteModifier.setCategory(route.getValue());
    			medMention.setMedicationRoute(medRouteModifier);
    		}
    		if (medEventMention.getMedicationStatusChange()!=null) {
    			MedicationStatusChange statusChange = medEventMention.getMedicationStatusChange();
    			MedicationStatusChangeModifier medStatusChangeModifier = new MedicationStatusChangeModifier(jcas);
    			medStatusChangeModifier.setNormalizedForm(statusChange);
    			medStatusChangeModifier.setCategory(statusChange.getValue());
    			medMention.setMedicationStatusChange(medStatusChangeModifier);
    		}
    		if (medEventMention.getMedicationStrength()!=null) {
    			MedicationStrength strength = medEventMention.getMedicationStrength();
    			MedicationStrengthModifier medStrengthModifier = new MedicationStrengthModifier(jcas);
    			medStrengthModifier.setNormalizedForm(strength);
    			medStrengthModifier.setCategory(strength.getNumber() + strength.getUnit());
    			medMention.setMedicationStrength(medStrengthModifier);
    		}
    		
    		// TODO handle MedicationAllergyModifier here when / if the value is set/disovered some day
    		
    	}

    	mention.addToIndexes();
    	
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
	
	logger.debug("process(JCas) in " + TemplateFillerAnnotator.class.getName());
	
	// Get all IdentifiedAnnotations
	FSIterator<Annotation> identifiedAnnotationsIter = getAllAnnotations(jcas, IdentifiedAnnotation.type);

	Map<IdentifiedAnnotation, IdentifiedAnnotation> mapToMentions = new HashMap<IdentifiedAnnotation, IdentifiedAnnotation>();

	// For each IdentifiedAnnotations, if it is of one of the right types, copy to the appropriate new subtype
	List<Annotation> annotationsToRemoveFromCas = new ArrayList<Annotation>(); 
	try {
	while (identifiedAnnotationsIter.hasNext()) {
	    IdentifiedAnnotation original = (IdentifiedAnnotation)identifiedAnnotationsIter.next();
	    IdentifiedAnnotation mention = null;
	    if (true)  { 
	    	// for 3.0 and earlier, needed to map mentions to the more specific types. in post-3.0, already creating proper type
	    	// for things other than medications. Drug NER was creating MedicationEventMentions still for a while (in trunk)
	    	int t = original.getTypeID(); 
	    	if (t==CONST.NE_TYPE_ID_ANATOMICAL_SITE || t==CONST.NE_TYPE_ID_DISORDER || t==CONST.NE_TYPE_ID_DISORDER 
	    			|| t==CONST.NE_TYPE_ID_FINDING || t==CONST.NE_TYPE_ID_PROCEDURE) {
	    		mapToMentions.put(original, original); // with 3.1 don't need to map to proper mention type, already creating as proper type
	    	} else if (t==CONST.NE_TYPE_ID_DRUG) {
	    		// Drug NER (ctakes-drug-ner) was creating MedicationEventMention, 
	    		// if found, create MedicationMention with its attributes based on the MedicationEventMention attributes
	    		if (original instanceof MedicationEventMention) {
	    			mention = new MedicationMention(jcas);
	    			mapToMentions.put(original, mention);
	    			setAttributesFromOriginal(mention, original);
	    			annotationsToRemoveFromCas.add(original);
	    		}

	    	}  else {
	    		// Some other type of IdentifiedAnnotation such as TimeMention, Modifier, DateMention, RomanNumeralAnnotation, etc
	    		// For each those we do nothing in this annotator.
	    	}
	    } else {
	    	// This switch should no longer be needed now that with 3.1 we are using correct specific types like 
	    	// AnatomicalSiteMention rather than EntityMention and DiseaseDisorderMention etc
	    	switch (original.getTypeID()) {

	    	case CONST.NE_TYPE_ID_ANATOMICAL_SITE: ;
	    	//org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention
	    	//org.apache.ctakes.typesystem.type.textsem.EntityMention
	    	mention = new AnatomicalSiteMention(jcas);
	    	mapToMentions.put(original, mention);
	    	setAttributesFromOriginal(mention, original);
	    	annotationsToRemoveFromCas.add(original);
	    	break;

	    	case CONST.NE_TYPE_ID_DISORDER: ;
	    	//org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention
	    	//org.apache.ctakes.typesystem.type.textsem.EventMention
	    	mention = new DiseaseDisorderMention(jcas);
	    	mapToMentions.put(original, mention);
	    	setAttributesFromOriginal(mention, original);
	    	annotationsToRemoveFromCas.add(original);
	    	break;

	    	case CONST.NE_TYPE_ID_DRUG: ;

    		if (original instanceof MedicationEventMention) {
    			mention = new MedicationMention(jcas);
    			mapToMentions.put(original, mention);
    			setAttributesFromOriginal(mention, original);
    			annotationsToRemoveFromCas.add(original);
    		}
	    	break;

	    	case CONST.NE_TYPE_ID_FINDING: ; // aka sign/symptom
	    	//org.apache.ctakes.typesystem.type.textsem.SignSymptomMention
	    	//org.apache.ctakes.typesystem.type.textsem.EventMention
	    	mention = new SignSymptomMention(jcas);
	    	mapToMentions.put(original, mention);
	    	setAttributesFromOriginal(mention, original);
	    	annotationsToRemoveFromCas.add(original);
	    	break;

	    	case CONST.NE_TYPE_ID_PROCEDURE: ;
	    	//org.apache.ctakes.typesystem.type.textsem.ProcedureMention
	    	//org.apache.ctakes.typesystem.type.textsem.EventMention
	    	mention = new ProcedureMention(jcas);
	    	mapToMentions.put(original, mention);
	    	setAttributesFromOriginal(mention, original);
	    	annotationsToRemoveFromCas.add(original);
	    	break;

	    	default: ;
	    	// Some other type of IdentifiedAnnotation such as TimeMention, Modifier, DateMention, RomanNumeralAnnotation, etc
	    	// For each those we do nothing in this annotator.
	    	}
	    }
	}

	} catch (CASException e) {
		throw new AnalysisEngineProcessException(e);
	}
	
	// Fill in template slots from relations. 
	
	//FSIndex<FeatureStructure> relationArgs = jcas.getFSIndexRepository().getIndex("_org.apache.ctakes.typesystem.type.relation.RelationArgument_GeneratedIndex");
	FSIndex<FeatureStructure> binaryTextRelations = jcas.getFSIndexRepository().getIndex("_org.apache.ctakes.typesystem.type.relation.BinaryTextRelation_GeneratedIndex");
	int i = 0;
	
	if (binaryTextRelations != null) {
		
	    for (FeatureStructure binaryTextRelationFS: binaryTextRelations) {
	    	
	    	i++;
	    	//logger.info("binaryTextRelationFS = " + binaryTextRelationFS);
	    	BinaryTextRelation binaryTextRelation = (BinaryTextRelation) binaryTextRelationFS;
	    	LocationOfTextRelation locationOfTextRelation = null;
	    	DegreeOfTextRelation degreeOfTextRelation = null;
	    	if (binaryTextRelation instanceof LocationOfTextRelation) {
	    		locationOfTextRelation = (LocationOfTextRelation) binaryTextRelationFS;
	    	} else if (binaryTextRelation instanceof DegreeOfTextRelation) {
	    		degreeOfTextRelation = (DegreeOfTextRelation) binaryTextRelationFS;
	    	}
	    	
	    	RelationArgument arg1 = binaryTextRelation.getArg1(); // an EntityMention  OR  location
	    	RelationArgument arg2 = binaryTextRelation.getArg2(); // a Modifier  OR   what is located at location
	    	String relation = binaryTextRelation.getCategory(); // "degree_of", "location_of"

	    	if (relation.equals("degree_of")) {
	    		
	    		Modifier severity = (Modifier) arg2.getArgument();
	    		// degree_of is aka severity, which applies to SignSymptomMention/SignSymptom and DiseaseDisorder
	    		// find Mention associated with arg1
	    		IdentifiedAnnotation arg1Arg = (IdentifiedAnnotation) arg1.getArgument();
	    		// set severity within the Mention to be arg2 (the Modifier)
	    		// Note at this point mapToMentions.get(entityMention) might be an entityMention instead of an EventMention
	    		// for example rec041 in the seed set resulted in 
	    		//  ClassCastException: org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention 
	    		//  cannot be cast to org.apache.ctakes.typesystem.type.textsem.EventMention
	    		IdentifiedAnnotation ia = mapToMentions.get(arg1Arg);
	    		if (ia instanceof EntityMention) {
	    			EntityMention entityMention = (EntityMention) ia;
	    			logger.error("Need to implement cases for handling EntityMention " + entityMention + " within relation: " + relation);
	    			logger.error("   severity " + severity + " in relation " + relation + " with/to " + entityMention);
	    			logger.error("   Using covered text: severity " + severity.getCoveredText() + " in relation " + relation + " with/to " + entityMention.getCoveredText());
	    		} else { 
	    			EventMention eventMention = (EventMention) ia;
	    			if (eventMention instanceof DiseaseDisorderMention) {
	    				DiseaseDisorderMention ddm = (DiseaseDisorderMention) eventMention;
	    				logger.error("Need to implement attr for " + relation + " for DiseaseDisorderMention"); 
	    			} else if (eventMention instanceof SignSymptomMention) {
	    				SignSymptomMention ssm = (SignSymptomMention) eventMention;
	    				ssm.setSeverity(degreeOfTextRelation);
	    			} else {
	    				logger.error("Need to implement more cases for handling EventMention " + eventMention + " within relation: " + relation);
	    			}
	    		}
	    		
	    	} else if (relation.equals("location_of")) {

	    		EntityMention arg2Arg = (EntityMention) arg2.getArgument(); // wrongly created as EntityMention prior to 3.0
	    		IdentifiedAnnotation ia = mapToMentions.get(arg2Arg);

	    		if (ia instanceof EntityMention) {
	    			
	    			// Note you apparently can have an AnatomicalSiteMention be the location_of an AnatomicalSiteMention
	    			// from running rec041, end up with things like "Left lower extremity"  location_of "common femoral vein"
	    			// and "left renal vein" in relation location_of to anatomical site mention "renal vein"
	    			// and "vein" in relation location_of to anatomical site mention "renal vein"
	    			EntityMention entityMention = (EntityMention) ia;
	    			IdentifiedAnnotation location = (IdentifiedAnnotation) arg1.getArgument();
	    			IdentifiedAnnotation loc = (IdentifiedAnnotation)mapToMentions.get(location);
	    			if (loc instanceof AnatomicalSiteMention) { 
	    				AnatomicalSiteMention asm = (AnatomicalSiteMention) loc;
	    				//asm.setBodyLocation(binaryTextRelation); // uncomment iff AnatomicalSiteMention ends up with a bodyLocation attribute
	    			} else {
	    				logger.error("Need to implement cases for handling EntityMention " + entityMention + " within relation: " + relation);
	    				logger.error("   loc " + loc + " in relation " + relation + " with/to " + entityMention);
	    				logger.error("   Using covered text: loc " + loc.getCoveredText() + " in relation " + relation + " with/to " + entityMention.getCoveredText());
	    			}

	    		} else { 
	    			
	    			EventMention eventMention = (EventMention) ia;

	    			if (eventMention instanceof DiseaseDisorderMention) { //(eventMention.getTypeID()==CONST.NE_TYPE_ID_DISORDER) { 
	    				DiseaseDisorderMention ddm = (DiseaseDisorderMention) eventMention;
	    				ddm.setBodyLocation(locationOfTextRelation);
	    			} else if (eventMention instanceof ProcedureMention) { //(eventMention.getTypeID()==CONST.NE_TYPE_ID_PROCEDURE) { 
	    				ProcedureMention pm = (ProcedureMention) eventMention;
	    				pm.setBodyLocation(locationOfTextRelation);
	    			} else if (eventMention instanceof SignSymptomMention) { //(eventMention.getTypeID()==CONST.NE_TYPE_ID_FINDING) {
	    				SignSymptomMention ssm = (SignSymptomMention) eventMention;
	    				ssm.setBodyLocation(locationOfTextRelation);
	    			} else {
	    				logger.error("Need to implement more cases for handling EventMention " + eventMention + " within relation: " + relation);
	    			}
	    			
	    		}
	    	} else {
	    		logger.error("Need to implement more cases for relation: " + relation);
	    	}
	    }
	}
	
	logger.debug("Number of BinaryTextRelations = " + i);
	 
	
    }

    private FSIterator<Annotation> getAllAnnotations(JCas jcas, int type) {
	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
	FSIterator<Annotation> annotationsIter = indexes.getAnnotationIndex(type).iterator();
        //	while (segmentItr.hasNext()) {
        //	}
	return annotationsIter;
    }


}
