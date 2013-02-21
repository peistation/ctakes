package org.apache.ctakes.core.util;

import java.util.Map;

import org.apache.ctakes.core.knowtator.KnowtatorAnnotation;
import org.apache.ctakes.typesystem.type.constants.CONST;

/**
 * A helper class that centralizes methods to assign all the 
 * default values from the SHARP corpus annotation guidelines.
 * 
 * @author stephen wu
 *
 */
public class SHARPKnowtatorXMLDefaults {

	public static String getSubject(String normalizedSubject) {
		if (normalizedSubject==null) {
			return CONST.ATTR_SUBJECT_PATIENT;
		}
		return normalizedSubject;
	}

	public static String getSubject() {
		return CONST.ATTR_SUBJECT_PATIENT;
	}

	public static int getHistoryOf() {
		return CONST.NE_HISTORY_OF_ABSENT;
	}

//	public static String getSubject(Map<String, KnowtatorAnnotation> annotationSlots) {
//		if (annotationSlots.get("subject_CU")==null) {
//			return CONST.ATTR_SUBJECT_PATIENT;
//		}
//		return annotationSlots.get("subject_CU").stringSlots.get("subject_normalization_CU");
//	}


}