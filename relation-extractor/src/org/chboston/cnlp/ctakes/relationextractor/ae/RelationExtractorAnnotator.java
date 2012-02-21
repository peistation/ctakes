/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
@author
 * 
 */
package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.relation.RelationArgument;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class RelationExtractorAnnotator extends CleartkAnnotator<String> {
  
  public static final String PARAM_GOLD_VIEW_NAME = "GoldViewName";
  
  private static final String NO_RELATION_CATEGORY = "-NONE-";
  
  @ConfigurationParameter(
      name = PARAM_GOLD_VIEW_NAME,
      mandatory = false,
      description = "view containing the manual relation annotations; needed for training")
  private String goldViewName;

  /**
   * The list of feature extractors used by the classifier.
   */
  private List<RelationFeaturesExtractor> featureExtractors = Arrays.asList(
      new TokenFeaturesExtractor(),
      new PartOfSpeechFeaturesExtractor(),
      new ChunkerExtractor(),
      new NamedEntityFeaturesExtractor(),
      new DependencyTreeFeaturesExtractor(),
      new DependencyPathFeaturesExtractor());

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (this.isTraining() && this.goldViewName == null) {
      throw new IllegalArgumentException(PARAM_GOLD_VIEW_NAME + " must be defined during training");
    }
  }

  /*
   * Implement the standard UIMA process method.
   */
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // during training, pull entity and relation annotations from the manual annotation view
    JCas entityMentionView, relationView;
    if (this.isTraining()) {
      try {
        entityMentionView = relationView = jCas.getView(this.goldViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    } else {
      entityMentionView = relationView = jCas;
    }
    
    // lookup from pair of annotations to binary text relation
    // note: assumes that there will be at most one relation per pair
    Map<Set<Annotation>, BinaryTextRelation> relationLookup;
    relationLookup = new HashMap<Set<Annotation>, BinaryTextRelation>();
    if (this.isTraining()) {
      for (BinaryTextRelation relation: JCasUtil.select(relationView, BinaryTextRelation.class)) {
        Annotation arg1 = relation.getArg1().getArgument();
        Annotation arg2 = relation.getArg2().getArgument();
        Set<Annotation> key = new HashSet<Annotation>(Arrays.asList(arg1, arg2));
        relationLookup.put(key, relation);
      }
    }

    // walk through each sentence in the text
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      
      // collect all possible relation arguments from the sentence
      List<EntityMention> args = JCasUtil.selectCovered(
          entityMentionView,
          EntityMention.class,
          sentence);
      
      // walk through the pairs
      for (int i = 0; i < args.size(); ++i) {
        EntityMention arg1 = args.get(i);
        for (int j = i + 1; j < args.size(); ++j) {
          EntityMention arg2 = args.get(j);
  
          // apply all the feature extractors to extract the list of features
          List<Feature> features = new ArrayList<Feature>();
          for (RelationFeaturesExtractor extractor : this.featureExtractors) {
            features.addAll(extractor.extract(jCas, arg1, arg2));
          }
                  
          // during training, feed the features to the data writer
          if (this.isTraining()) {
            
            // load the relation label from the CAS (if there is one)
            Set<Annotation> key = new HashSet<Annotation>(Arrays.asList(arg1, arg2));
            String category;
            if (!relationLookup.containsKey(key)) {
              category = NO_RELATION_CATEGORY;
            } else {
              BinaryTextRelation relation = relationLookup.get(key);
              if (relation.getArg1().equals(arg1)) {
                category = relation.getCategory();
              } else {
                category = relation.getCategory() + "-1"; // inverse
              }
            }

            // create a classification instance and write it to the training data
            this.dataWriter.write(new Instance<String>(category, features));
          }
  
          // during classification feed the features to the classifier and create annotations
          else {
            String category = this.classifier.classify(features);
            
//            // play with classification threshold because of skew in data
//            List<ScoredOutcome<String>> scoredOutcomes = this.classifier.score(features, 2);
//            ScoredOutcome<String> top = scoredOutcomes.get(0);
//            String category;
//            if (top.getOutcome().equals(NO_RELATION_CATEGORY) && top.getScore() < 0.99) {
//              category = scoredOutcomes.get(1).getOutcome();
//            } else {
//              category = top.getOutcome();
//            }
            
            // add a relation annotation if a true relation was predicted
            if (!category.equals(NO_RELATION_CATEGORY)) {
              if (category.endsWith("-1")) {
                category = category.substring(0, category.length() - 2);
                EntityMention temp = arg1;
                arg1 = arg2;
                arg2 = temp;
              }
              RelationArgument relArg1 = new RelationArgument(relationView);
              relArg1.setArgument(arg1);
              relArg1.addToIndexes();
              RelationArgument relArg2 = new RelationArgument(relationView);
              relArg2.setArgument(arg2);
              relArg2.addToIndexes();
              BinaryTextRelation relation = new BinaryTextRelation(relationView);
              relation.setArg1(relArg1);
              relation.setArg2(relArg2);
              relation.setCategory(category);
              relation.addToIndexes();
            }
          }
        }
      }
    }
  }
}