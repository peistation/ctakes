package org.apache.ctakes.assertion.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.uima.resource.ResourceInitializationException;

public class SemanticClasses extends HashMap<String,HashSet<String>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// loads files in the input directory into a hashmap that maps the filename minus the extension ("allergy.txt" becomes "allergy")
	// to the set of words in that file ("allergy" => ("allergic", "allergies", "allergy", ...)
	public SemanticClasses(String semClassDir) throws ResourceInitializationException{
		File classDir = new File(semClassDir);
		if(classDir.exists() && classDir.isDirectory()){
			File[] classFiles = classDir.listFiles();
			for(File semClass : classFiles){
				if(semClass.isDirectory() || semClass.isHidden()) continue;
				HashSet<String> classWords = new HashSet<String>();
				Scanner scanner = null;
				try {
					scanner = new Scanner(semClass);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new ResourceInitializationException("Error: Could not open file:", new Object[]{ semClass}, e);
				}
				while(scanner.hasNextLine()){
					String term = scanner.nextLine().trim();
					// if the term on this line is a multi-word expression, ignore, because we can't
					// place these in the tree anyways
					if(!term.contains(" ")){
						classWords.add(term);
					}
				}
				put(semClass.getName().replace(".txt", ""), classWords);
			}
		}
	}
}
