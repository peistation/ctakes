package org.chboston.cnlp.ctakes.relationextractor.cr;

import edu.mayo.bmi.uima.core.type.constants.CONST;

/**
 * Map various category names to their ctakes integer id. 
 * If an entity type that cannot be extracted by CTAKEs
 * automatically is passed, "unknown relation" id is returned.
 *  
 * @author dmitriy dligach
 *
 */
public class Mapper {

	/**
	 * Map entity type to its integer id.
	 */
	public static int getEntityTypeId(String entityType) {

		if(entityType.equals("Disease_Disorder")) return CONST.NE_TYPE_ID_DISORDER;
	  else if(entityType.equals("Procedure")) return CONST.NE_TYPE_ID_PROCEDURE;
	  else if(entityType.equals("Medications/Drugs")) return CONST.NE_TYPE_ID_DRUG;
	  else if(entityType.equals("Sign_symptom")) return CONST.NE_TYPE_ID_FINDING;
	  else if(entityType.equals("Anatomical_site")) return CONST.NE_TYPE_ID_ANATOMICAL_SITE;
	  else return CONST.NE_TYPE_ID_UNKNOWN;
	}
	
	/**
	 * Map modifier type to its integer id.
	 */
	public static int getModifierTypeId(String modifierType) {
		
		if(modifierType.equals("course_class")) return CONST.MODIFIER_TYPE_ID_COURSE_CLASS;
		else if(modifierType.equals("severity_class")) return CONST.MODIFIER_TYPE_ID_SEVERITY_CLASS;
		else if(modifierType.equals("lab_interpretation_indicator")) return CONST.MODIFIER_TYPE_ID_LAB_INTERPRETATION_INDICATOR;
		else return CONST.MODIFIER_TYPE_ID_UNKNOWN;
	}
}
