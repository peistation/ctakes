package org.apache.ctakes.temporal.ae.feature;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.timeml.util.TimeWordsExtractor;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

public class TimeWordTypeExractor implements SimpleFeatureExtractor {
  
  private static final String FEATURE_NAME = "TimeWordType";
  
  private static final String LOOKUP_PATH = "/org/apache/ctakes/temporal/time_word_types.txt";
  
  private Map<String, String> wordTypes;
  
  public TimeWordTypeExractor() throws ResourceInitializationException {
    this.wordTypes = Maps.newHashMap();
    URL url = TimeWordsExtractor.class.getResource(LOOKUP_PATH);
    try {
      for (String line : Resources.readLines(url, Charsets.US_ASCII)) {
        String[] typeAndWord = line.split("\\s+");
        if (typeAndWord.length != 2) {
          throw new IllegalArgumentException("Expected '<type> <word>', found: " + line);
        }
        this.wordTypes.put(typeAndWord[1], typeAndWord[0]);
      }
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    String type = this.wordTypes.get(focusAnnotation.getCoveredText().toLowerCase());
    List<Feature> features;
    if (type == null) {
      features = Collections.emptyList();
    } else {
      features = Collections.singletonList(new Feature(FEATURE_NAME, type));
    }
    return features;
  }
}
