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
	
	// high priority sharp relations
	public static final HashSet<String> sharpRelations = 
			new HashSet<String>(Arrays.asList("manages/treats", "degree_of", "causes/brings_about", "location_of", "indicates"));
}
