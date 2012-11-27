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

import java.util.Collections;
import java.util.List;

import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class PhraseExtractor implements SimpleFeatureExtractor {

  @Override
  public List<Feature> extract(JCas jCas, Annotation token) throws CleartkExtractorException {
    String featureValue = "NotNPVP";
    for (Chunk chunk : JCasUtil.selectCovered(jCas, Chunk.class, token)) {
      String chunkType = chunk.getChunkType();
      if (chunkType.equals("NP")) {
        featureValue = "NP";
        break;
      } else if (chunkType.equals("VP")) {
        featureValue = "VP";
        break;
      }
    }
    return Collections.singletonList(new Feature("PhraseType", featureValue));
  }

}
