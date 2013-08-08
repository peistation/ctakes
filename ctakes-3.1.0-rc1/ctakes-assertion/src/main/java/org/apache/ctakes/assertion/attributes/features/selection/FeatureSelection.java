package org.apache.ctakes.assertion.attributes.features.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cleartk.classifier.feature.transform.TrainableExtractor_ImplBase;
import org.cleartk.classifier.feature.transform.TransformableFeature;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public abstract class FeatureSelection<OUTCOME_T> extends
    TrainableExtractor_ImplBase<OUTCOME_T> implements Predicate<Feature> {

  protected boolean isTrained;
  
  protected Set<String> selectedFeatureNames;

  public FeatureSelection(String name) {
    super(name);
    this.isTrained = false;
  }

  @Override
  public boolean apply(Feature feature) {
    return this.selectedFeatureNames.contains(this.getFeatureName(feature));
  }

  @Override
  public Instance<OUTCOME_T> transform(Instance<OUTCOME_T> instance) {
    List<Feature> features = new ArrayList<Feature>();
    for (Feature feature : instance.getFeatures()) {
      if (this.isTransformable(feature)) {
        // Filter down to selected features
        features.addAll(Collections2.filter(((TransformableFeature) feature).getFeatures(), this));
      } else {
        // Pass non-relevant features through w/o filtering
        features.add(feature);
      }
    }
    return new Instance<OUTCOME_T>(instance.getOutcome(), features);
  }

  public List<Feature> transform(List<Feature> features) {
    List<Feature> results = Lists.newArrayList();
    if (this.isTrained) {
      results.addAll(Collections2.filter(features, this));
    } else {
      results.add(new TransformableFeature(this.name, features));
    }
    return results;
  }

  protected String getFeatureName(Feature feature) {
    String featureName = feature.getName();
    Object featureValue = feature.getValue();
    return featureValue instanceof Number ? featureName : featureName + ":" + featureValue;
  }

}
