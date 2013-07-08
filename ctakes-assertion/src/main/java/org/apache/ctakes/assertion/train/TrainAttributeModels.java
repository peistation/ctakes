package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;

import scala.actors.threadpool.Arrays;
/**
 * For each assertion attribute (polarity, conditional, etc), train a model using the data
 * in the training directories for that attribute, and store the model under the models-dir
 * Note that this uses constants within {@link AssertionConst} for the directory names.
 */
public class TrainAttributeModels {

	public static void main(String[] args) throws Exception {
		
		for (String attribute : AssertionConst.annotationTypes) {
			
			ArrayList<String> params = new ArrayList<String>();

			params.add("--train-dir"); 	params.add(AssertionConst.trainingDirectories.get(attribute));
//			params.add("--test-dir"); 	params.add("sharp_data/dev");
			params.add("--models-dir"); params.add(AssertionConst.modelDirectory);
//			params.add("--evaluation-output-dir");	params.add(AssertionConst.evalOutputDir); 
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
