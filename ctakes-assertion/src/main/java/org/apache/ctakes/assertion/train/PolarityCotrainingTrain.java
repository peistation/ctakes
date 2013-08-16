package org.apache.ctakes.assertion.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.ctakes.assertion.eval.AssertionEvaluation;
import org.apache.ctakes.assertion.util.AssertionConst;

import scala.actors.threadpool.Arrays;
/**
 * For each assertion attribute (polarity, conditional, etc), train a model using the data
 * in the training directories for that attribute, and store the model under the models-dir
 * Note that this uses constants within {@link AssertionConst} for the directory names.
 */
public class PolarityCotrainingTrain {
	protected final static String SHARP_TRAIN = AssertionConst.DATA_DIR + "preprocessed_data/sharp/train";
	protected final static String I2B2_TRAIN  = AssertionConst.DATA_DIR + "preprocessed_data/i2b2/train";
	protected final static String MIPACQ_TRAIN = AssertionConst.DATA_DIR + "preprocessed_data/mipacq/cTAKES-xmi"; // actually test
	protected final static String NEGEX_TRAIN = AssertionConst.DATA_DIR + "preprocessed_data/negex"; // actually test

	protected final static String SHARP_MODEL = "../ctakes-assertion-res/resources/model/sharptrain";
	protected final static String I2B2_MODEL  = "../ctakes-assertion-res/resources/model/i2b2train";
	protected final static String MIPACQ_MODEL  = "../ctakes-assertion-res/resources/model/mipacqtest";
	protected final static String NEGEX_MODEL  = "../ctakes-assertion-res/resources/model/negextest";
	protected final static String SHARP_I2B2_MODEL = "../ctakes-assertion-res/resources/model/sharptrain+i2b2train";
	protected final static String SHARP_MIPACQ_MODEL = "../ctakes-assertion-res/resources/model/sharptrain+mipacqtest";
	protected final static String SHARP_NEGEX_MODEL = "../ctakes-assertion-res/resources/model/sharptrain+negextest";
	protected final static String SHARP_I2B2_MIPACQ_NEGEX_MODEL = "../ctakes-assertion-res/resources/model/sharpi2b2mipacqnegex";

	public static void main(String[] args) throws Exception {

		String attribute = "polarity";
		
		HashMap<String,String> trainGrid = new HashMap<String,String>();
		trainGrid.put(SHARP_TRAIN, 	SHARP_MODEL);
		trainGrid.put(I2B2_TRAIN, 	I2B2_MODEL);
		trainGrid.put(MIPACQ_TRAIN,	MIPACQ_MODEL);
		trainGrid.put(NEGEX_TRAIN,	NEGEX_MODEL);
		trainGrid.put(SHARP_TRAIN+":"+I2B2_TRAIN,	SHARP_I2B2_MODEL);
		trainGrid.put(SHARP_TRAIN+":"+MIPACQ_TRAIN,	SHARP_MIPACQ_MODEL);
		trainGrid.put(SHARP_TRAIN+":"+NEGEX_TRAIN,	SHARP_NEGEX_MODEL);
		trainGrid.put(SHARP_TRAIN+":"+I2B2_TRAIN+":"+MIPACQ_TRAIN+":"+NEGEX_TRAIN,	
				SHARP_I2B2_MIPACQ_NEGEX_MODEL);

		for (Entry<String, String> oneTrain : trainGrid.entrySet()) {
			
			ArrayList<String> params = new ArrayList<String>();

			params.add("--train-dir"); 	params.add(oneTrain.getKey());
//			params.add("--test-dir"); 	params.add("sharp_data/dev");
			params.add("--models-dir"); params.add(oneTrain.getValue());
//			params.add("--evaluation-output-dir");	params.add(AssertionConst.evalOutputDir); 
			params.add("--train-only"); 
//			params.add("--feature-selection");	params.add("1.0");
			
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
			
//			System.out.println(Arrays.asList(paramList).toString());
			
			// Run the actual assertion training on just one attribute
			AssertionEvaluation.main( paramList );
		}
		
		
		
	}
}
