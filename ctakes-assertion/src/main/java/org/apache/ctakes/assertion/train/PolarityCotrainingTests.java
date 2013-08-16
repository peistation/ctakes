package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;
import org.apache.ctakes.assertion.train.PolarityCotrainingTrain.*;

import scala.actors.threadpool.Arrays;


/**
 * For each assertion attribute (polarity, conditional, etc), run against the test directories
 * for that attribute, using models that are under the models-dir.
 * Note that this uses constants within {@link AssertionConst} for the directory names.
 */
public class PolarityCotrainingTests {

	protected final static String SHARP_TEST = AssertionConst.DATA_DIR + "preprocessed_data/sharp/test";
	protected final static String I2B2_TEST  = AssertionConst.DATA_DIR + "preprocessed_data/i2b2/test";
	protected final static String MIPACQ_TEST = AssertionConst.DATA_DIR + "preprocessed_data/mipacq/cTAKES-xmi";
	protected final static String NEGEX_TEST = AssertionConst.DATA_DIR + "preprocessed_data/negex";
	
	public static void main(String[] args) throws Exception {

		AssertionEvaluation.useEvaluationLogFile = true;

		HashMap<String,String> testGrid = new HashMap<String,String>();
		testGrid.put(PolarityCotrainingTrain.SHARP_MODEL, 	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MODEL, 	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MODEL, 	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MODEL, 	NEGEX_TEST);
		testGrid.put(PolarityCotrainingTrain.I2B2_MODEL,  	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.I2B2_MODEL,  	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.I2B2_MODEL,  	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.I2B2_MODEL,  	NEGEX_TEST);
		testGrid.put(PolarityCotrainingTrain.MIPACQ_MODEL,  SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.MIPACQ_MODEL,  I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.MIPACQ_MODEL,  MIPACQ_TEST); // not valid
		testGrid.put(PolarityCotrainingTrain.MIPACQ_MODEL,  NEGEX_TEST);
		testGrid.put(PolarityCotrainingTrain.NEGEX_MODEL,  	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.NEGEX_MODEL,  	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.NEGEX_MODEL,  	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.NEGEX_MODEL,  	NEGEX_TEST);  // not valid
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MODEL,  	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MODEL,  	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MODEL,  	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MODEL,  	NEGEX_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MIPACQ_MODEL,  SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MIPACQ_MODEL,  I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_MIPACQ_MODEL,  MIPACQ_TEST); // not valid
		testGrid.put(PolarityCotrainingTrain.SHARP_MIPACQ_MODEL,  NEGEX_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_NEGEX_MODEL,  	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_NEGEX_MODEL,  	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_NEGEX_MODEL,  	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_NEGEX_MODEL,  	NEGEX_TEST);  // not valid
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MIPACQ_NEGEX_MODEL,  	SHARP_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MIPACQ_NEGEX_MODEL,  	I2B2_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MIPACQ_NEGEX_MODEL,  	MIPACQ_TEST);
		testGrid.put(PolarityCotrainingTrain.SHARP_I2B2_MIPACQ_NEGEX_MODEL,  	NEGEX_TEST);

		
		String attribute = "polarity";

		for (Entry<String, String> oneTest : testGrid.entrySet()) {
			ArrayList<String> params = new ArrayList<String>();

			params.add("--test-dir"); 	params.add(oneTest.getValue());
			params.add("--models-dir"); params.add(oneTest.getKey());
			//			params.add("--ytex-negation");
			//		params.add("--evaluation-output-dir");	params.add(AssertionConst.evalOutputDir);
			params.add("--test-only");	
			params.add("--print-errors");

			// Build up an "ignore" string
			for (String ignoreAttribute : AssertionConst.allAnnotationTypes) {
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
