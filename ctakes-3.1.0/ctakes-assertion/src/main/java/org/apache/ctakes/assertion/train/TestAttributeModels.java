package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;

import scala.actors.threadpool.Arrays;


/**
 * For each assertion attribute (polarity, conditional, etc), run against the test directories
 * for that attribute, using models that are under the models-dir.
 * Note that this uses constants within {@link AssertionConst} for the directory names.
 */
public class TestAttributeModels {

	public static void main(String[] args) throws Exception {
		
		for (String attribute : AssertionConst.annotationTypes) {
			
			ArrayList<String> params = new ArrayList<String>();

			params.add("--test-dir"); 	params.add(AssertionConst.testDirectories.get(attribute));
			params.add("--models-dir"); params.add(AssertionConst.modelDirectory);
			params.add("--evaluation-output-dir");	params.add(AssertionConst.evalOutputDir);
			params.add("--test-only");	
			
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
			
			System.out.println(Arrays.asList(paramList).toString());
			
			// Run the actual assertion test on just one attribute
			AssertionEvaluation.main( paramList );
		}
		
		
		
	}
}
