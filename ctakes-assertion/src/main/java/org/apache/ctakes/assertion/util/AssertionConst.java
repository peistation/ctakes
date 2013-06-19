package org.apache.ctakes.assertion.util;

import java.util.ArrayList;
import java.util.HashMap;

public class AssertionConst {
	
	/*** CHANGE THESE ***/

	// Locally-stored data models
	
	// expects subdirectories: "Mayo/UMLS_CEM/*batch*/Knowtator*" "Seattle Group Health/UMLS_CEM/*batch*/Knowtator*"
	public static final String SHARP_SEED_CORPUS = "/Users/m081914/work/data/sharp/Seed Corpus";
	// expects subdirectories: ast, txt 
	public static final String I2B2_2010_CORPUS = "/Users/m081914/work/data/i2b2Challenge2010/Data/i2b2Challenge2010AllTrain";
	// expects subdirectories: ast, txt
	public static final String I2B2_2010_TEST_CORPUS = "/Users/m081914/work/data/i2b2Challenge2010/Data/Test/reports";

	// Specify training directories for each attribute in a (semi)colon-separated list, e.g., "sharp_data/dev:sharp_data/train"
	public static HashMap<String,String> trainingDirectories = new HashMap<String,String>();
	static { 
		trainingDirectories.put("polarity","sharp_data/train:i2b2_data/train");
//		trainingDirectories.put("polarity","sharp_data/train");
//		trainingDirectories.put("polarity","i2b2_data/train");
		trainingDirectories.put("conditional","sharp_data/train");
		trainingDirectories.put("uncertainty","sharp_data/train");
		trainingDirectories.put("subject","sharp_data/train");
		trainingDirectories.put("generic","sharp_data/train");
		trainingDirectories.put("historyOf","sharp_data/train");
	}
		
	// If you don't want to train/cross-validate everything, comment these out
	public static ArrayList<String> annotationTypes = new ArrayList<String>();
	static { 
		annotationTypes.add("polarity");
		annotationTypes.add("conditional");
		annotationTypes.add("uncertainty");
		annotationTypes.add("subject");
		annotationTypes.add("generic");
		annotationTypes.add("historyOf");
	}
	
	
	/*** DON'T CHANGE THESE ***/

	// Specify input and output data locations for preprocessing.  Results will be used for model training
	public static HashMap<String,String> preprocessRootDirectory = new HashMap<String,String>();
	static { 
		preprocessRootDirectory.put(SHARP_SEED_CORPUS+"/Mayo/UMLS_CEM","sharp_data/train");
		preprocessRootDirectory.put(SHARP_SEED_CORPUS+"/Seattle Group Health/UMLS_CEM","sharp_data/train");
		preprocessRootDirectory.put(I2B2_2010_CORPUS,"i2b2_data/train");
		preprocessRootDirectory.put(I2B2_2010_TEST_CORPUS,"i2b2_data/test");
	}
	
	// Specify input and output data locations for preprocessing.  Results will be used for model test
	public static HashMap<String,String> preprocessForTest = new HashMap<String,String>();
	static { 
		preprocessForTest.put(SHARP_SEED_CORPUS+"/Mayo/UMLS_CEM","sharp_data/test");
		preprocessForTest.put(SHARP_SEED_CORPUS+"/Seattle Group Health/UMLS_CEM","sharp_data/test");
	}

	// Specify input and output data locations for preprocessing.  Results will be used for model dev
	public static HashMap<String,String> preprocessForDev = new HashMap<String,String>();
	static { 
		preprocessForDev.put(SHARP_SEED_CORPUS+"/Mayo/UMLS_CEM","sharp_data/dev");
		preprocessForDev.put(SHARP_SEED_CORPUS+"/Seattle Group Health/UMLS_CEM","sharp_data/dev");
	}
	
	
}
