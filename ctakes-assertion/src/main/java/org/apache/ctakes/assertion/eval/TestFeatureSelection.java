package org.apache.ctakes.assertion.eval;

import java.io.File;

import org.apache.ctakes.assertion.attributes.features.selection.FeatureSelection;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.feature.transform.InstanceStream;

public class TestFeatureSelection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File directory = new File("/Users/m081914/work/sharpattr/ctakes/ctakes-assertion-res/resources/model/sharptrain-xval/fold_0/polarity");
		
		InstanceDataWriter.INSTANCES_OUTPUT_FILENAME = "training-data.liblinear";
		// Extracting features and writing instances
		Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(directory);
		
		FeatureSelection<String> featureSelection; 
		featureSelection = PolarityCleartkAnalysisEngine.createFeatureSelection(1f);
		featureSelection.train(instances);
//		featureSelection.save(PolarityCleartkAnalysisEngine.createFeatureSelectionURI(directory));

	}

}
