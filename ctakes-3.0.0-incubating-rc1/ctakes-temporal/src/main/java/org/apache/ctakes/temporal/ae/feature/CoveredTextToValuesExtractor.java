/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

public class CoveredTextToValuesExtractor implements SimpleFeatureExtractor {

  private String name;

  private Map<String, double[]> textDoublesMap;

  private double[] meanValues;

  public CoveredTextToValuesExtractor(String name, Map<String, double[]> textDoublesMap) {
    super();
    this.name = name;
    this.textDoublesMap = textDoublesMap;
    int nMapEntries = this.textDoublesMap.size();
    if (nMapEntries == 0) {
      throw new IllegalArgumentException("textDoublesMap cannot be empty");
    }
    int nValues = textDoublesMap.entrySet().iterator().next().getValue().length;
    this.meanValues = new double[nValues];
    for (double[] values : textDoublesMap.values()) {
      for (int i = 0; i < values.length; ++i) {
        this.meanValues[i] += values[i];
      }
    }
    for (int i = 0; i < this.meanValues.length; ++i) {
      this.meanValues[i] /= nMapEntries;
    }
  }

  @Override
  public List<Feature> extract(JCas view, Annotation annotation) throws CleartkExtractorException {
    double[] values = this.textDoublesMap.get(annotation.getCoveredText());
    if (values == null) {
      values = this.meanValues;
    }
    ArrayList<Feature> features = new ArrayList<Feature>();
    for (int i = 0; i < values.length; ++i) {
      String featureName = Feature.createName(this.name, String.valueOf(i));
      features.add(new Feature(featureName, values[i]));
    }
    return features;
  }

}
