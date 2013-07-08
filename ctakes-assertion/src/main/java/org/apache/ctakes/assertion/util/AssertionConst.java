package org.apache.ctakes.assertion.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AssertionConst {
	
	/*** CHANGE THESE ***/

	// Locally-stored data models
	
	public static final String BASE_DIRECTORY =  "/Users/m081914/"; //"/usr/data/work/data/assertion/";  // "/usr/data";
	static {
		if (!BASE_DIRECTORY.endsWith("/") && !BASE_DIRECTORY.endsWith("\\")) {
			throw new RuntimeException("BASE_DIRECTORY should end with a slash");
		}
	}

	// expects subdirectories: "Mayo/UMLS_CEM/*batch*/Knowtator*" "Seattle Group Health/UMLS_CEM/*batch*/Knowtator*"
	public static final String SHARP_SEED_CORPUS = BASE_DIRECTORY + "work/data/sharp/Seed Corpus/";
	// expects subdirectories: ast, txt 
	public static final String I2B2_2010_CORPUS = BASE_DIRECTORY + "work/data/i2b2Challenge2010/Data/i2b2Challenge2010AllTrain/";
	// expects subdirectories: ast, txt
	public static final String I2B2_2010_TEST_CORPUS = BASE_DIRECTORY + "work/data/i2b2Challenge2010/Data/Test/reports/";

	// raw and processed text, expects subdirectories for different sources, then subsubdirectories for train/test/dev
	public static final String DATA_DIR = BASE_DIRECTORY + "work/data/assertion/";
	
	// specify the model to write (train/crossvalidate) or read (test/crossvalidate).
	//  please rename for different configurations of training data 
	public static String modelDirectory = "../ctakes-assertion-res/resources/model/sharptrain-xval";
//	public static String modelDirectory = "../ctakes-assertion-res/resources/model/sharptrain-xval";
//	public static String modelDirectory = "../ctakes-assertion-res/resources/model/sharptrain";
//	public static String modelDirectory = "../ctakes-assertion-res/resources/model/sharptrain+i2b2train";
//	public static String modelDirectory = "../ctakes-assertion-res/resources/model/i2b2train";

	
	// Specify training directories for each attribute in a (semi)colon-separated list, e.g., "sharp_data/dev:sharp_data/train"
	public static HashMap<String,String> trainingDirectories = new HashMap<String,String>();
	static { 
		trainingDirectories.put("polarity", DATA_DIR + "fromtim_sharp_data/train");
//		trainingDirectories.put("polarity","sharp_data/train");
//		trainingDirectories.put("polarity","i2b2_data/train");
		trainingDirectories.put("conditional", DATA_DIR + "fromtim_sharp_data/train");
		trainingDirectories.put("uncertainty", DATA_DIR + "fromtim_sharp_data/train");
		trainingDirectories.put("subject", DATA_DIR + "fromtim_sharp_data/train");
		trainingDirectories.put("generic", DATA_DIR + "fromtim_sharp_data/train");
		trainingDirectories.put("historyOf", DATA_DIR + "fromtim_sharp_data/train");
	}
		
	public static HashMap<String,String> testDirectories = new HashMap<String,String>();
	static { 
		testDirectories.put("polarity", DATA_DIR + "fromtim_sharp_data/test");
//		testDirectories.put("polarity","i2b2_data/test");
		testDirectories.put("conditional", DATA_DIR + "fromtim_sharp_data/test");
		testDirectories.put("uncertainty", DATA_DIR + "fromtim_sharp_data/test");
		testDirectories.put("subject", DATA_DIR + "fromtim_sharp_data/test");
		testDirectories.put("generic", DATA_DIR + "fromtim_sharp_data/test");
		testDirectories.put("historyOf", DATA_DIR + "fromtim_sharp_data/test");
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
	
	
	/*** DON'T CHANGE THESE ***/ /* TODO - please comment why these should not be changed */

	// Specify input and output data locations for preprocessing.  Results will be used for model training
	public static HashMap<String,String> preprocessRootDirectory = new HashMap<String,String>();
	static { 
		preprocessRootDirectory.put(SHARP_SEED_CORPUS+"Mayo/UMLS_CEM", DATA_DIR + "sharp_data/train");
		preprocessRootDirectory.put(SHARP_SEED_CORPUS+"Seattle Group Health/UMLS_CEM", DATA_DIR + "sharp_data/train");
		preprocessRootDirectory.put(I2B2_2010_CORPUS, DATA_DIR + "i2b2_data/train");
		preprocessRootDirectory.put(I2B2_2010_TEST_CORPUS, DATA_DIR + "i2b2_data/test");
		
		// If one of the preprocessRootDirectory entries above is commented out, warn user with a popup
		if (preprocessRootDirectory.keySet().size()<4) {
			JFrame frame = new JFrame("DialogDemo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			//Create and set up the content pane.
			JOptionPane.showMessageDialog(frame, "Commented out one or more data dir(s) for now.. add back before using for real.");
			frame.dispose();
			//frame.setContentPane(newContentPane);

			//Display the window.
			//frame.pack();
			//frame.setVisible(true);
		}
	}
	
	
	// Specify input and output data locations for preprocessing.  Results will be used for model test
	public static HashMap<String,String> preprocessForTest = new HashMap<String,String>();
	static { 
		preprocessForTest.put(SHARP_SEED_CORPUS+"/Mayo/UMLS_CEM", DATA_DIR + "sharp_data/test");
		preprocessForTest.put(SHARP_SEED_CORPUS+"/Seattle Group Health/UMLS_CEM", DATA_DIR + "sharp_data/test");
	}

	// Specify input and output data locations for preprocessing.  Results will be used for model dev
	public static HashMap<String,String> preprocessForDev = new HashMap<String,String>();
	static { 
		preprocessForDev.put(SHARP_SEED_CORPUS+"/Mayo/UMLS_CEM", DATA_DIR + "sharp_data/dev");
		preprocessForDev.put(SHARP_SEED_CORPUS+"/Seattle Group Health/UMLS_CEM", DATA_DIR + "sharp_data/dev");
	}
		
	public static String evalOutputDir = "sharp_data/output";
	
	public static String instanceGatheringOutputDir = "sharp_data/output_instancegathering";
}
