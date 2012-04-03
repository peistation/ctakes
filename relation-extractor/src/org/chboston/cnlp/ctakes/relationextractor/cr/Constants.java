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
	// degree_of relation is currently excluded since we don't have an annotator that handles it yet
	public static final HashSet<String> sharpRelations = 
			new HashSet<String>(Arrays.asList("manages/treats", "causes/brings_about", "location_of", "indicates"));
}
