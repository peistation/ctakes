package org.apache.ctakes.assertion.pipelines;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SharpCorpusSplit {
	public enum Subcorpus { TRAIN, TEST, DEV, CROSSVAL }
	
	private static Map<String,Subcorpus> map = new HashMap<String,Subcorpus>();
	static {
		map.put("ss1_batch02", Subcorpus.TRAIN); 
		map.put("ss1_batch03", Subcorpus.TRAIN); 
		map.put("ss1_batch04", Subcorpus.TRAIN); 
		map.put("ss1_batch05", Subcorpus.TRAIN); 
		map.put("ss1_batch06", Subcorpus.TRAIN); 
		map.put("ss1_batch07", Subcorpus.TRAIN); 
		map.put("ss1_batch08", Subcorpus.TRAIN); 
		map.put("ss1_batch09", Subcorpus.TRAIN); 
		map.put("ss1_batch10", Subcorpus.DEV); 
		map.put("ss1_batch11", Subcorpus.TEST); 
		map.put("ss1_batch12", Subcorpus.TEST); 
		map.put("ss1_batch13", Subcorpus.TRAIN); 
		map.put("ss1_batch14", Subcorpus.TRAIN); 
		map.put("ss1_batch15", Subcorpus.TRAIN); 
		map.put("ss1_batch16", Subcorpus.TRAIN); 
		map.put("ss1_batch17", Subcorpus.DEV); 
		map.put("ss1_batch18", Subcorpus.TRAIN); 
		map.put("ss1_batch19", Subcorpus.TRAIN); 
	}
	
	public static Subcorpus split( File directory ) {
		if (map.containsKey(directory.getName())) {
//			System.out.println(directory.toString());
			return map.get(directory.getName());
		} else {
			return Subcorpus.TRAIN;
		}
	}
}
