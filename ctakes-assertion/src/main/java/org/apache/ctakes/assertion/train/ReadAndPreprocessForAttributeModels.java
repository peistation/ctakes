package org.apache.ctakes.assertion.train;

import java.io.File;
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
			String froot = AssertionConst.preprocessRootDirectory.get(source);
			if (!(new File(froot).exists())) {
				(new File(froot)).createNewFile();
			}
			params.add("--train-dir"); 		params.add(froot);

			// Some corpora (SHARP) may have predetermined dev/test splits. Check AssertionConst.
			if (AssertionConst.preprocessForDev.containsKey(source) ) {
				String fdev = AssertionConst.preprocessRootDirectory.get(source);
				if (!(new File(fdev).exists())) {
					(new File(fdev)).createNewFile();
				}
				params.add("--dev-dir"); 	params.add(fdev);
			}
			if (AssertionConst.preprocessForTest.containsKey(source) ) {
				String ftest = AssertionConst.preprocessRootDirectory.get(source);
				if (!(new File(ftest).exists())) {
					(new File(ftest)).createNewFile();
				}
				params.add("--test-dir"); 	params.add(ftest);
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
