package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import static org.apache.ctakes.assertion.util.AssertionTreeUtils.extractAboveLeftConceptTree;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.TreeUtils;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.util.CleartkInitializationException;

public class AboveLeftFragmentExtractor extends TreeFragmentFeatureExtractor {

  public AboveLeftFragmentExtractor(String prefix, String resourceFilename)
      throws CleartkInitializationException {
    super(prefix, resourceFilename);
  }

  @Override
  public List<Feature> extract(JCas jcas, Annotation annotation) {
    List<Feature> features = new ArrayList<Feature>();
    SimpleTree tree = extractAboveLeftConceptTree(jcas, annotation, sems);
    
    for(SimpleTree frag : frags){
      if(TreeUtils.containsIgnoreCase(tree, frag)){
        features.add(new Feature("TreeFrag_" + prefix, frag.toString()));
      }
    }
  
    return features;
  }

}
