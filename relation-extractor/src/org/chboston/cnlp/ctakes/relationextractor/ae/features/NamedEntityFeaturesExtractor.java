/*
 * Copyright: (c) 2012  Children's Hospital Boston, Regents of the University of Colorado 
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Steven Bethard
 */

package org.chboston.cnlp.ctakes.relationextractor.ae.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.annotationpair.DistanceExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class NamedEntityFeaturesExtractor implements RelationFeaturesExtractor {

  private SimpleFeatureExtractor namedEntityType = new SimpleFeatureExtractor() {
    @Override
    public List<Feature> extract(JCas jCas, Annotation ann) throws CleartkExtractorException {
      IdentifiedAnnotation idAnn = (IdentifiedAnnotation)ann;
      return Arrays.asList(new Feature("TypeID", String.valueOf(idAnn.getTypeID())));
    }
  };

  /**
   * All extractors for mention 1, with features named to distinguish them from mention 2
   */
  private SimpleFeatureExtractor mention1FeaturesExtractor = new NamingExtractor(
      "mention1",
      namedEntityType);

  /**
   * All extractors for mention 2, with features named to distinguish them from mention 1
   */
  private SimpleFeatureExtractor mention2FeaturesExtractor = new NamingExtractor(
      "mention2",
      namedEntityType);

  /**
   * Number of named entities between the two mentions
   */
  private DistanceExtractor nEntityMentionsBetween = new DistanceExtractor(null, EntityMention.class);

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException {

    List<Feature> features = new ArrayList<Feature>();
    features.addAll(this.mention1FeaturesExtractor.extract(jCas, arg1));
    features.addAll(this.mention2FeaturesExtractor.extract(jCas, arg2));
    features.addAll(this.nEntityMentionsBetween.extract(jCas, arg1, arg2));

    // entity type of both mentions, concatenated
    int type1 = arg1.getTypeID();
    int type2 = arg2.getTypeID();
    features.add(new Feature("type1type2", String.format("%s_%s", type1, type2)));

    // is mention1 included in mention2?
    boolean begins1After2 = arg1.getBegin() >= arg2.getBegin();
    boolean ends1Before2 = arg1.getEnd() <= arg2.getEnd();
    features.add(new Feature("mention1InMention2", begins1After2 && ends1Before2));

    // is mention2 included in mention1?
    boolean begins2After1 = arg2.getBegin() >= arg1.getBegin();
    boolean ends2Before1 = arg2.getEnd() <= arg1.getEnd();
    features.add(new Feature("mention2InMention1", begins2After1 && ends2Before1));

    return features;
  }

}
