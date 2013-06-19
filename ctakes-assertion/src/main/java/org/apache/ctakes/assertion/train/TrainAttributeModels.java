package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;

import scala.actors.threadpool.Arrays;

public class TrainAttributeModels {

	public static void main(String[] args) throws Exception {
		
		for (String attribute : AssertionConst.annotationTypes) {
			
			ArrayList<String> params = new ArrayList<String>();

			params.add("--train-dir"); 	params.add(AssertionConst.trainingDirectories.get(attribute));
//			params.add("--test-dir"); 	params.add("sharp_data/dev");
			params.add("--models-dir"); params.add("sharp_data/model/eval.model");
//			params.add("--evaluation-output-dir");	params.add("sharp_data/output"); 
			params.add("--train-only"); 
			
			// Build up an "ignore" string
			for (String ignoreAttribute : AssertionConst.annotationTypes) {
				if (!ignoreAttribute.equals(attribute)) { 

					if (ignoreAttribute.equals("historyOf")) {
						ignoreAttribute = ignoreAttribute.substring(0, ignoreAttribute.length()-2);
					}

					params.add("--ignore-" + ignoreAttribute);
				}
			}
			String[] paramList = params.toArray(new String[]{});
			
//			System.out.println(Arrays.asList(paramList).toString());
			
			// Run the actual assertion training on just one attribute
			AssertionEvaluation.main( paramList );
		}
		
		
		
	}
}
