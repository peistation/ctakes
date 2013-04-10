package org.apache.ctakes.templatefiller.ae;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;

public class TemplateFillerAnnotator extends JCasAnnotator_ImplBase{

    // LOG4J logger based on class name
    private Logger logger = Logger.getLogger(getClass().getName());
    private UimaContext uimaContext;
    private boolean REMOVE_ORIGINAL_MENTIONS = false;

    /**
     * @param args
     */
    public static void main(String[] args) {
	// For simple tests. 
	// TODO Move to junit or other test classes later.

    }

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {

	super.initialize(aContext);
	uimaContext = aContext;

	logger.info("Initializing " +  TemplateFillerAnnotator.class.getName());
	
    }

    private void setAttributesFromOriginal(IdentifiedAnnotation mention, IdentifiedAnnotation original) {
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
    	mention.addToIndexes();
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
	
	logger.info("process(JCas) in " + TemplateFillerAnnotator.class.getName());
	
	// Get all IdentifiedAnnotations
	FSIterator<Annotation> identifiedAnnotationsIter = getAllAnnotations(jcas, IdentifiedAnnotation.type);

	Map<IdentifiedAnnotation, IdentifiedAnnotation> mapToMentions = new HashMap<IdentifiedAnnotation, IdentifiedAnnotation>();

	// For each IdentifiedAnnotations, if it is of one of the right types, copy to the appropriate new subtype
	List<Annotation> annotationsToRemoveFromCas = new ArrayList<Annotation>(); 
	while (identifiedAnnotationsIter.hasNext()) {
	    IdentifiedAnnotation original = (IdentifiedAnnotation)identifiedAnnotationsIter.next();
	    IdentifiedAnnotation mention = null;
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
	    	// TODO use org.apache.ctakes.typesystem.type.textsem.MedicationMention
	    	//org.apache.ctakes.typesystem.type.textsem.MedicationEventMention
	    	//org.apache.ctakes.typesystem.type.textsem.EventMention

	    	// TODO is this needed or are we already creating MedicationEventMentions
	    	//mention = new MedicationEventMention(jcas);
	    	//mapToMentions.put(original, mention);
	    	//setAttributesFromOriginal(mention, original);
	    	//annotationsToRemoveFromCas.add(original);
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
	
	
	// Fill in template slots from relations etc
	logger.info("TODO finish writing code to fill in template slots");
	
	FSIndex<FeatureStructure> relationArgs = jcas.getFSIndexRepository().getIndex("_org.apache.ctakes.typesystem.type.relation.RelationArgument_GeneratedIndex"); // TODO do we need this?
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
                        //	if (ia instanceof AnatomicalSiteMention) {
                        //	    AnatomicalSiteMention asm = (AnatomicalSiteMention) ia;
                        //	    asm.setSeverity(severity);
                        //	} 
			EntityMention entityMention = (EntityMention) ia;
			logger.error("Need to implement cases for handling EntityMention " + entityMention + " within relation: " + relation);
			logger.error("   severity " + severity + " in relation " + relation + " with/to " + entityMention);
			logger.error("   Using covered text: severity " + severity.getCoveredText() + " in relation " + relation + " with/to " + entityMention.getCoveredText());
		    } else { 
			EventMention eventMention = (EventMention) ia;
			if (eventMention instanceof DiseaseDisorderMention) {
			    DiseaseDisorderMention ddm = (DiseaseDisorderMention) eventMention;
			    logger.error("Need to implement attr for " + relation + " for DiseaseDisorderMention"); // TODO 
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

			if (eventMention.getTypeID()==CONST.NE_TYPE_ID_DISORDER) { // TODO in 3.0 consider using arg2 instanceof DiseaseDisorderMention?
			    DiseaseDisorderMention ddm = (DiseaseDisorderMention) eventMention;
			    
			    ddm.setBodyLocation(locationOfTextRelation);
			} else if (eventMention.getTypeID()==CONST.NE_TYPE_ID_PROCEDURE) { // TODO in 3.0 consider using arg2 instanceof ProcedureMention?
			    ProcedureMention pm = (ProcedureMention) eventMention;
			    pm.setBodyLocation(locationOfTextRelation);
			} else if (eventMention.getTypeID()==CONST.NE_TYPE_ID_FINDING) { // TODO in 3.0 consider using arg2 instanceof SignSymptomMention?
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
	logger.info("Number of BinaryTextRelations = " + i);
	 
	
	if (REMOVE_ORIGINAL_MENTIONS) {
	    // Remove the annotations we no longer need
	    for (Annotation anno: annotationsToRemoveFromCas) {
		jcas.removeFsFromIndexes(anno);
	    }
	}
	
	
    }

    private FSIterator<Annotation> getAllAnnotations(JCas jcas, int type) {
	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
	FSIterator<Annotation> annotationsIter = indexes.getAnnotationIndex(type).iterator();
        //	while (segmentItr.hasNext()) {
        //	}
	return annotationsIter;
    }


}
