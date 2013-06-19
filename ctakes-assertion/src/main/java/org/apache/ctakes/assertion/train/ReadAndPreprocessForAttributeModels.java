package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;

import scala.actors.threadpool.Arrays;

public class ReadAndPreprocessForAttributeModels {

	public static void main(String[] args) throws Exception {
		
		for (String source : AssertionConst.preprocessRootDirectory.keySet()) {
			
			ArrayList<String> params = new ArrayList<String>();

			// Always preprocess something to a main directory, usually for training
			params.add("--train-dir"); 		params.add(AssertionConst.preprocessRootDirectory.get(source));

			// Some corpora (SHARP) may have predetermined dev/test splits. Check AssertionConst.
			if (AssertionConst.preprocessForDev.containsKey(source) ) {
				params.add("--dev-dir"); 	params.add(AssertionConst.preprocessForDev.get(source));
			}
			if (AssertionConst.preprocessForTest.containsKey(source) ) {
				params.add("--test-dir"); 	params.add(AssertionConst.preprocessForTest.get(source));
			}
			
			// Specify preprocessing directory (See AssertionConst)
			params.add("--preprocess-only"); 	params.add(source);
			
			String[] paramList = params.toArray(new String[]{});
			
//			System.out.println(Arrays.asList(paramList).toString());
			
			// Run the actual assertion preprocessing on just one data source
			AssertionEvaluation.main( paramList );
		}
		
		
		
	}
}
