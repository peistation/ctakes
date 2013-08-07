package org.apache.ctakes.temporal.ae.feature;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.typesystem.type.textsem.Predicate;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

public class FramesetCategoryExtractor implements SimpleFeatureExtractor {
  
  private Map<String, String> frameSetCategories;
  
  public FramesetCategoryExtractor() throws ResourceInitializationException {
    String path = "/org/apache/ctakes/temporal/propbank_noneventive_framesets.txt";
    URL uri = FramesetCategoryExtractor.class.getResource(path);
    this.frameSetCategories = Maps.newHashMap();
    try {
      for (String line : Resources.readLines(uri, Charsets.US_ASCII)) {
        String[] tagAndFrameset = line.split("\\s+");
        this.frameSetCategories.put(tagAndFrameset[1], tagAndFrameset[0]);
      }
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    List<Feature> features = Lists.newArrayList();
    for (Predicate predicate : JCasUtil.selectCovered(view, Predicate.class, focusAnnotation)) {
      String category = this.frameSetCategories.get(predicate.getFrameSet());
      if (category != null) {
        features.add(new Feature("FramesetCategory", category));
      }
    }
    return features;
  }

}
