package org.apache.ctakes.temporal.utils;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * A simple implementation of SMOTE algorithm. 
 * Nitesh V. Shawla et. al. SMOTE: Synthetic Minority Over-sampling Technique, 06/02
 * currently only get the nearest neighbor for each minority instance.
 * May be modified to my SMOTE-plus algorithm.
 *  
 * @author Chen Lin
 */
public class SMOTEplus {

	protected List<Instance<String>> minorityInsts;
	protected Table<Instance<String>, String, Integer> instanceFeatureCount;
	protected List<Instance<String>> syntheticInsts;
	
	public SMOTEplus() {
		this.minorityInsts = Lists.newArrayList();
		this.syntheticInsts = Lists.newArrayList();
		this.instanceFeatureCount = HashBasedTable.<Instance<String>, String, Integer> create();
	}
	
	public Iterable<Instance<String>> populateMinorityClass() {
		//1. populate Minority instance-Feature matrix
		for (Instance<String> instance : this.minorityInsts) {
		      for (Feature feature : instance.getFeatures()) {
		    	  this.instanceFeatureCount.put(instance, getFeatureName(feature), 1);
		      }
		}
		
		//2. Iterate through all minority instances:
		for (Instance<String> aMinorityInst : this.instanceFeatureCount.rowKeySet()) {
			//3. find its nearest neighbor minority instance:
			//TODO: Should be modified to take nearest K neighbors
			double minDis = Double.MAX_VALUE;
			Instance<String> nearestNeighbor = null;
			for (Instance<String> bMinorityInst : this.instanceFeatureCount.rowKeySet()){
				if ( aMinorityInst==bMinorityInst || (aMinorityInst!=null && aMinorityInst.equals(bMinorityInst)) ){
					double distance = calculateDistance(aMinorityInst, bMinorityInst);
					if (distance < minDis){
						minDis = distance;
						nearestNeighbor = bMinorityInst;
					}
				}
			}
			Instance<String> sytheticInst = generateInstance(aMinorityInst, nearestNeighbor);
			this.syntheticInsts.add(sytheticInst);
		}
		
		return this.syntheticInsts;
	}

	private Instance<String> generateInstance(Instance<String> aMinorityInst,
			Instance<String> nearestNeighbor) {
		List<Feature> features = new ArrayList<Feature>();
		//iterate through all features:
		for( String featureName: this.instanceFeatureCount.columnKeySet()){
			Integer valA = this.instanceFeatureCount.get(aMinorityInst, featureName);
			Integer valB = this.instanceFeatureCount.get(nearestNeighbor, featureName);
			if (valA != null && valB != null){
				features.add(new Feature(featureName.split(":",2)[0],featureName.split(":",2)[1]));
			}
		}
		Instance<String> syntheticInst = new Instance<String>(aMinorityInst.getOutcome(), features);
		return syntheticInst;
	}

	private double calculateDistance(Instance<String> instA,
			Instance<String> instB) {
		double distance = 0;
		//iterate through all features:
		for( String featureName: this.instanceFeatureCount.columnKeySet()){
			Integer valA = this.instanceFeatureCount.get(instA, featureName);
			Integer valB = this.instanceFeatureCount.get(instB, featureName);
			if ( (valA!=null && valB == null) || (valA==null && valB != null)){
				distance ++;
			}
		}
		return Math.pow(distance, .5);
	}

	public String getFeatureName(Feature feature) {
	    String featureName = feature.getName();
	    Object featureValue = feature.getValue();
	    return featureValue instanceof Number ? featureName : featureName + ":" + featureValue;
	  }

	public void addInstance(Instance<String> minorityInst) {
		this.minorityInsts.add(minorityInst);
	}
}
