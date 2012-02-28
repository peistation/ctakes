package org.chboston.cnlp.ctakes.relationextractor.data;

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
	public static final HashSet<String> sharpRelationsHighPriority = 
			new HashSet<String>(Arrays.asList("location_of", "degree_of", "prevents", "compicates", "disrupts"));
	
	// low priority sharp relations
	public static final HashSet<String> sharpRelationsLowPriority = 
			new HashSet<String>(Arrays.asList("manifestation_of", "affects", "manages/treats", "causes/brings_about",
					"contraindicates", "diagnoses", "indicates", "is_indicated_for"));
	
	// all sharp relations
	public static final HashSet<String> sharpRelationsAll = 
			new HashSet<String>(Arrays.asList("location_of", "degree_of", "prevents", "compicates", "disrupts",
					"manifestation_of", "affects", "manages/treats", "causes/brings_about",
					"contraindicates", "diagnoses", "indicates", "is_indicated_for"));
	
	// sharp relations selected for march 2012 deliverable
	public static final HashSet<String> sharpRelationsSelected = 
			new HashSet<String>(Arrays.asList("manages/treats", "degree_of", "causes/brings_about", "location_of", "indicates"));
}