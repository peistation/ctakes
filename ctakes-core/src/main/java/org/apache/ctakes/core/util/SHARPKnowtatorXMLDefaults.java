package org.apache.ctakes.core.util;

import org.apache.ctakes.typesystem.type.constants.CONST;

/**
 * A helper class that centralizes methods to assign all the 
 * default values from the SHARP corpus annotation guidelines.
 * 
 * @author stephen wu
 *
 */
public class SHARPKnowtatorXMLDefaults {

	public static String getSubject() {
		return CONST.ATTR_SUBJECT_PATIENT;
	}

	public static int getHistoryOf() {
		return CONST.NE_HISTORY_OF_ABSENT;
	}
}