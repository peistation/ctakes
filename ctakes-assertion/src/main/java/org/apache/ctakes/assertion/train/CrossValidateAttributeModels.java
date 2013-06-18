package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;

import scala.actors.threadpool.Arrays;

public class CrossValidateAttributeModels {

	private static ArrayList<String> annotationTypes = new ArrayList<String>();
	static { 
		annotationTypes.add("polarity");
		annotationTypes.add("conditional");
		annotationTypes.add("uncertainty");
		annotationTypes.add("subject");
		annotationTypes.add("generic");
		annotationTypes.add("historyOf");
	}

	// Specify training directories for each attribute in a (semi)colon-separated list, e.g., "sharp_data/dev:sharp_data/train"
	private static HashMap<String,String> trainingDirectories = new HashMap<String,String>();
	static { 
		trainingDirectories.put("polarity","sharp_data/dev");
		trainingDirectories.put("conditional","sharp_data/dev");
		trainingDirectories.put("uncertainty","sharp_data/dev");
		trainingDirectories.put("subject","sharp_data/dev");
		trainingDirectories.put("generic","sharp_data/dev");
		trainingDirectories.put("historyOf","sharp_data/dev");
	}
	public static void main(String[] args) throws Exception {
		
		for (String attribute : annotationTypes) {
			
			ArrayList<String> params = new ArrayList<String>();

			params.add("--train-dir"); 			params.add(trainingDirectories.get(attribute));
			params.add("--models-dir"); 		params.add("sharp_data/model/eval.model");
			params.add("--cross-validation"); 	params.add("5");
			
			// Build up an "ignore" string
			for (String ignoreAttribute : annotationTypes) {
				if (!ignoreAttribute.equals(attribute)) { 

					if (ignoreAttribute.equals("historyOf")) {
						ignoreAttribute = ignoreAttribute.substring(0, ignoreAttribute.length()-2);
					}

					params.add("--ignore-" + ignoreAttribute);
				}
			}
			String[] paramList = params.toArray(new String[]{});
			
			System.out.println(Arrays.asList(paramList).toString());
			
			// Run the actual assertion training on just one attribute
			AssertionEvaluation.main( paramList );
		}
		
		
		
	}
}
