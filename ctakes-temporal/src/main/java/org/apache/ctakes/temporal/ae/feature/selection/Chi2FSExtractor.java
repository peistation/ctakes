package org.apache.ctakes.temporal.ae.feature.selection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.TransformableFeature;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class Chi2FSExtractor<OUTCOME_T> extends FeatureSelectionExtractor<OUTCOME_T>
		implements SimpleFeatureExtractor {
			
	protected boolean isTrained;
	private CombinedExtractor subExtractor;
	private ArrayList<String> selectedFeatures;
	private int numFeatures;

	public Chi2FSExtractor(String name, CombinedExtractor featureExtractor) {
		super(name);
		this.subExtractor = featureExtractor;
	}

	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws CleartkExtractorException {
		List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
	    List<Feature> result = new ArrayList<Feature>();
	    if (this.isTrained) {
	      // Filter out selected features
	      result.addAll(Collections2.filter(extracted, this));
	    } else {
	      // We haven't trained this extractor yet, so just mark the existing features
	      // for future modification, by creating one uber-container feature
	      result.add(new TransformableFeature(this.name, extracted));
	    }

	    return result;
	}

	@Override
	public boolean apply(Feature feature) {
		return this.selectedFeatures.contains(this.nameFeature(feature));
	}
	
	public String nameFeature(Feature feature) {
	    return (feature.getValue() instanceof Number) ? feature.getName() : feature.getName() + ":"
	        + feature.getValue();
	  }

	@Override
	public void train(Iterable<Instance<OUTCOME_T>> instances) {
		//step1: change cleartk instance to weka instances
		
		//step2: step up weka Attribute Selection
		
		//step3: get selected features
		this.selectedFeatures = null;
		this.isTrained = true;
		
	}

	@Override
	public void save(URI uri) throws IOException {
		if (!this.isTrained) {
		      throw new IOException("Chi2FSExtractor: Cannot save before training.");
		}
		File out = new File(uri);
	    BufferedWriter writer = new BufferedWriter(new FileWriter(out));
//	    writer.append("CombineScoreType\t");
//	    writer.append(this.combineScoreMethod.toString());
//	    writer.append("\n");

//	    ComputeFeatureScore<OUTCOME_T> computeScore = this.mutualInfoStats.getScoreFunction(this.combineScoreMethod);
	    for (String feature : this.selectedFeatures) {
	      writer.append(String.format("%s\t\n", feature)); //), computeScore.apply(feature)));
	    }

	    writer.close();
	}

	@Override
	public void load(URI uri) throws IOException {
		this.selectedFeatures = Lists.newArrayList();
	    File in = new File(uri);
	    BufferedReader reader = new BufferedReader(new FileReader(in));

	    // The rest of the lines are feature + selection scores
	    String line = null;
	    int n = 0;
	    while ((line = reader.readLine()) != null && n < this.numFeatures) {
	      String[] featureValuePair = line.split("\\t");
	      this.selectedFeatures.add(featureValuePair[0]);
	      n++;
	    }

	    reader.close();
	    this.isTrained = true;
		
	}

}
