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
import java.util.List;
import java.util.Map;
import java.util.Random;

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

  private static final String NO_RELATION_CATEGORY = "-NONE-";

  public static final String PARAM_GOLD_VIEW_NAME = "GoldViewName";

  @ConfigurationParameter(
      name = PARAM_GOLD_VIEW_NAME,
      mandatory = false,
      description = "view containing the manual relation annotations; needed for training")
  private String goldViewName;

  public static final String PARAM_CLASSIFY_BOTH_DIRECTIONS = "ClassifyBothDirections";

  @ConfigurationParameter(
      name = PARAM_CLASSIFY_BOTH_DIRECTIONS,
      mandatory = false,
      description = "run the classifier in both directions, that is, classify each pair of events "
          + "{X,Y} once in the order X-to-Y and once in the order Y-to-X (default: classify each "
          + "pair of events {X, Y} once, giving the label 'R' if a relation exists with the order "
          + "X-to-Y, and 'R-1' if a relation exists with the order Y-to-X)")
  private boolean classifyBothDirections = false;

  public static final String PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE = "ProbabilityOfKeepingANegativeExample";

  @ConfigurationParameter(
      name = PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
      mandatory = false,
      description = "probability that a negative example should be retained for training")
  private double probabilityOfKeepingANegativeExample = 1.0;

  private Random coin = new Random(0);

  /**
   * The list of feature extractors used by the classifier.
   */
  private List<RelationFeaturesExtractor> featureExtractors = Arrays.<RelationFeaturesExtractor> asList(
      new TokenFeaturesExtractor(),
      new PartOfSpeechFeaturesExtractor(),
      new PhraseChunkingExtractor(),
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
    Map<List<Annotation>, BinaryTextRelation> relationLookup;
    relationLookup = new HashMap<List<Annotation>, BinaryTextRelation>();
    if (this.isTraining()) {
      for (BinaryTextRelation relation : JCasUtil.select(relationView, BinaryTextRelation.class)) {
        Annotation arg1, arg2;
        if (relation.getArg1().getRole().equals("Argument")) {
          arg1 = relation.getArg1().getArgument();
          arg2 = relation.getArg2().getArgument();
        } else {
          arg2 = relation.getArg1().getArgument();
          arg1 = relation.getArg2().getArgument();
        }
        relationLookup.put(Arrays.asList(arg1, arg2), relation);
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
        int jStart = this.classifyBothDirections ? 0 : i + 1;
        for (int j = jStart; j < args.size(); ++j) {
          if (i != j) {
            EntityMention arg2 = args.get(j);

            // apply all the feature extractors to extract the list of features
            List<Feature> features = new ArrayList<Feature>();
            for (RelationFeaturesExtractor extractor : this.featureExtractors) {
              features.addAll(extractor.extract(jCas, arg1, arg2));
            }
            
            // sanity check on feature values
            for (Feature feature : features) {
              if (feature.getValue() == null) {
                String message = "Null value found in %s from %s";
                throw new IllegalArgumentException(String.format(message, feature, features));
              }
            }

            // during training, feed the features to the data writer
            if (this.isTraining()) {
              String category;

              // if classifying both directions, we'll see {X, Y} once when X is first and
              // once when Y is first, so just do the single direction lookup here
              if (this.classifyBothDirections) {
                BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
                if (relation != null) {
                  category = relation.getCategory();
                } else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
                  category = NO_RELATION_CATEGORY;
                } else {
                  continue;
                }
              }

              // if classifying in a single direction, we'll see {X, Y} only once,
              // so do lookups in both directions, and change the category name for
              // the relations in the reverse order
              else {
                BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
                if (relation != null) {
                  category = relation.getCategory();
                } else {
                  relation = relationLookup.get(Arrays.asList(arg2, arg1));
                  if (relation != null) {
                    category = relation.getCategory() + "-1";
                  } else if (coin.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
                    category = NO_RELATION_CATEGORY;
                  } else {
                    continue;
                  }
                }
              }

              // create a classification instance and write it to the training data
              this.dataWriter.write(new Instance<String>(category, features));
            }

            // during classification feed the features to the classifier and create annotations
            else {
              String category = this.classifier.classify(features);

              // add a relation annotation if a true relation was predicted
              if (!category.equals(NO_RELATION_CATEGORY)) {

                // if we predict an inverted relation, reverse the order of the arguments
                if (category.endsWith("-1")) {
                  category = category.substring(0, category.length() - 2);
                  EntityMention temp = arg1;
                  arg1 = arg2;
                  arg2 = temp;
                }

                // add the relation to the CAS
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
}