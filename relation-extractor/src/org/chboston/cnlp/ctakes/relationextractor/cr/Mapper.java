package org.chboston.cnlp.ctakes.relationextractor.cr;

import edu.mayo.bmi.uima.core.type.constants.CONST;

/**
 * Map various category names to their ctakes integer id.
 *  
 * @author dmitriy dligach
 *
 */
public class Mapper {

	/**
	 * Map entity type to its integer id
	 */
	public static int getEntityTypeId(String entityType) {

		if(entityType.equals("Disease_Disorder")) return CONST.NE_TYPE_ID_DISEASE_DISORDER;
		else if(entityType.equals("Activities_and_Behaviors")) return CONST.NE_TYPE_ID_ACTIVITIES_AND_BEHAVIORS;
		else if(entityType.equals("Organizations")) return CONST.NE_TYPE_ID_ORGANIZATIONS;
		else if(entityType.equals("Objects")) return CONST.NE_TYPE_ID_OBJECTS;
	  else if(entityType.equals("Devices")) return CONST.NE_TYPE_ID_DEVICES;
	  else if(entityType.equals("Physiology")) return CONST.NE_TYPE_ID_PHYSIOLOGY;
	  else if(entityType.equals("Geographic_areas")) return CONST.NE_TYPE_ID_GEOGRAPHIC_AREAS;
	  else if(entityType.equals("Phenomena")) return CONST.NE_TYPE_ID_PHENOMENA;
	  else if(entityType.equals("Procedure")) return CONST.NE_TYPE_ID_PROCEDURE;
	  else if(entityType.equals("Medications/Drugs")) return CONST.NE_TYPE_ID_MEDICATIONS_DRUGS;
	  else if(entityType.equals("Event_umls")) return CONST.NE_TYPE_ID_EVENT_UMLS;
	  else if(entityType.equals("Concepts_and_Ideas")) return CONST.NE_TYPE_ID_CONCEPTS_AND_IDEAS;
	  else if(entityType.equals("Living_Beings")) return CONST.NE_TYPE_ID_LIVING_BEINGS;
	  else if(entityType.equals("Classification")) return CONST.NE_TYPE_ID_CLASSIFICATION;
	  else if(entityType.equals("Sign_symptom")) return CONST.NE_TYPE_ID_SIGN_SYMPTOM;
	  else if(entityType.equals("TIMEX3")) return CONST.NE_TYPE_ID_TIMEX3;
	  else if(entityType.equals("Language")) return CONST.NE_TYPE_ID_LANGUAGE;
	  else if(entityType.equals("Occupations")) return CONST.NE_TYPE_ID_OCCUPATIONS;
	  else if(entityType.equals("Laboratory_or_test_result")) return CONST.NE_TYPE_ID_LABORATORY_OR_TEST_PROCEDURE;
	  else if(entityType.equals("Anatomical_site")) return CONST.NE_TYPE_ID_ANATOMICAL_SITE;
	  else if(entityType.equals("Genes_and_Molecular_Sequences")) return CONST.NE_TYPE_ID_GENES_AND_MOLECULAR_SEQUENCES;
	  else if(entityType.equals("Person")) return CONST.NE_TYPE_ID_PERSON;
	  else return CONST.NE_TYPE_ID_UNKNOWN;
	}	  
}
