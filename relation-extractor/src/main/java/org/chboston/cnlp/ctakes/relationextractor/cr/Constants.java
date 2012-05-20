package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Various global settings.
 * 
 * @author dmitriy dligach
 *
 */
public class Constants {
	
	// semantic roles of degree_of arguments (Modifier degree_of EntityMention)
	public static final String shareModifierRole = "Related_to";
	public static final String shareEntityMentionRole = "Argument";
	public static final String mipacqModifierRole = "";                 // **** FIXME: SET THESE!!! *****
	public static final String mipacqEntityMentionRole = "";
	public static final String sharpModifierRole = "";
	public static final String sharpEntityMentionRole = "";

	// modifiers participating in degree_of relation are of one of these types 
	public static final HashSet<String> modifierClasses = 
			new HashSet<String>(Arrays.asList("course_class", "severity_class", " lab_interpretation_indicator"));
	
	// high priority sharp relations 
	public static final HashSet<String> relationSet = new HashSet<String>(Arrays.asList("location_of", "degree_of"));
}