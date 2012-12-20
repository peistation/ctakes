package org.apache.ctakes.temporal.ae;

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.ctakes.temporal.ae.feature.PhraseExtractor;
import org.apache.ctakes.temporal.ae.feature.SRLExtractor;
import org.apache.ctakes.temporal.ae.feature.SurfaceFormFeatureExtractor;
import org.apache.ctakes.temporal.ae.feature.selection.Chi2FeatureSelection;
import org.apache.ctakes.temporal.ae.feature.selection.FeatureSelection;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor.PatternType;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class EventAnnotator extends CleartkAnnotator<String> {

  public static final String PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE = "ProbabilityOfKeepingANegativeExample";

  @ConfigurationParameter(
      name = PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
      mandatory = false,
      description = "probability that a negative example should be retained for training")
  protected Float probabilityOfKeepingANegativeExample = 1f;

  public static final String PARAM_FEATURE_SELECTION_THRESHOLD = "WhetherToDoFeatureSelection";

  @ConfigurationParameter(
      name = PARAM_FEATURE_SELECTION_THRESHOLD,
      mandatory = false,
      description = "the Chi-squared threshold at which features should be removed")
  protected Float featureSelectionThreshold = 0f;

  public static final String PARAM_FEATURE_SELECTION_URI = "FeatureSelectionURI";

  @ConfigurationParameter(
      mandatory = false,
      name = PARAM_FEATURE_SELECTION_URI,
      description = "provides a URI where the feature selection data will be written")
  protected URI featureSelectionURI;

  public static AnalysisEngineDescription createDataWriterDescription(
      Class<?> dataWriter,
      File outputDirectory,
      float downratio,
      float featureSelect) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        EventAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        dataWriter,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory,
        EventAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
        downratio,
        EventAnnotator.PARAM_FEATURE_SELECTION_THRESHOLD,
        featureSelect);
  }

  public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        EventAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(modelDirectory, "model.jar"),
        EventAnnotator.PARAM_FEATURE_SELECTION_URI,
        EventAnnotator.createFeatureSelectionURI(modelDirectory));
  }

  protected SimpleFeatureExtractor tokenFeatureExtractor;

  protected CleartkExtractor contextFeatureExtractor;

  private BIOChunking<BaseToken, EntityMention> entityChunking;

  private BIOChunking<BaseToken, EventMention> eventChunking;

  private FeatureSelection<String> featureSelection;

  private static final String FEATURE_SELECTION_NAME = "SelectNeighborFeatures";

  public static FeatureSelection<String> createFeatureSelection(double threshold) {
    return new Chi2FeatureSelection<String>(EventAnnotator.FEATURE_SELECTION_NAME, threshold);
  }
  
  public static URI createFeatureSelectionURI(File outputDirectoryName) {
    return new File(outputDirectoryName, FEATURE_SELECTION_NAME + "_Chi2_extractor.dat").toURI();
  }


  // *****feature selection related parameters

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // define chunkings
    this.entityChunking = new BIOChunking<BaseToken, EntityMention>(
        BaseToken.class,
        EntityMention.class,
        "typeID");
    this.eventChunking = new BIOChunking<BaseToken, EventMention>(
        BaseToken.class,
        EventMention.class);

    this.tokenFeatureExtractor = new CombinedExtractor(
        new CoveredTextExtractor(),
        new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
        new TypePathExtractor(BaseToken.class, "partOfSpeech"),
        new SurfaceFormFeatureExtractor(),
        new PhraseExtractor(),
        new SRLExtractor());
    this.contextFeatureExtractor = new CleartkExtractor(
        BaseToken.class,
        this.tokenFeatureExtractor,
        new Preceding(3),
        new Following(3));

    if (featureSelectionThreshold == 0) {
      this.featureSelection = null;
    } else {
      this.featureSelection = EventAnnotator.createFeatureSelection(this.featureSelectionThreshold);

      if (this.featureSelectionURI != null) {
        try {
          this.featureSelection.load(this.featureSelectionURI);
        } catch (IOException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    Random rand = new Random();
    // classify tokens within each sentence
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);

      // during training, the list of all outcomes for the tokens
      List<String> outcomes;
      if (this.isTraining()) {
        List<EventMention> events = JCasUtil.selectCovered(jCas, EventMention.class, sentence);
        outcomes = this.eventChunking.createOutcomes(jCas, tokens, events);
      }
      // during prediction, the list of outcomes predicted so far
      else {
        outcomes = new ArrayList<String>();
      }

      // get BIO entity tags for each entity type
      int[] entityTypeIDs = new int[] {
          CONST.NE_TYPE_ID_ANATOMICAL_SITE,
          CONST.NE_TYPE_ID_DISORDER,
          CONST.NE_TYPE_ID_DRUG,
          CONST.NE_TYPE_ID_FINDING,
          CONST.NE_TYPE_ID_PROCEDURE,
          CONST.NE_TYPE_ID_UNKNOWN };
      List<EntityMention> entities = JCasUtil.selectCovered(jCas, EntityMention.class, sentence);
      Map<Integer, List<String>> entityTagsByType = new HashMap<Integer, List<String>>();
      for (int typeID : entityTypeIDs) {
        Predicate<EntityMention> hasTypeID = hasEntityType(typeID);
        List<EntityMention> subEntities = Lists.newArrayList(Iterables.filter(entities, hasTypeID));
        entityTagsByType.put(typeID, this.entityChunking.createOutcomes(jCas, tokens, subEntities));
      }

      // extract features for all tokens
      int tokenIndex = -1;
      int window = 2;
      int nPreviousClassifications = 2;

      for (BaseToken token : tokens) {
        ++tokenIndex;

        List<Feature> features = new ArrayList<Feature>();

        // features from token attributes
        features.addAll(this.tokenFeatureExtractor.extract(jCas, token));

        // features from surrounding tokens
        features.addAll(this.contextFeatureExtractor.extractWithin(jCas, token, sentence));

        // features from surrounding entities
        for (int typeID : entityTypeIDs) {
          List<String> tokenEntityTags = entityTagsByType.get(typeID);
          int begin = Math.max(tokenIndex - window, 0);
          int end = Math.min(tokenIndex + window, tokenEntityTags.size());
          for (int i = begin; i < end; ++i) {
            String name = String.format("EntityTag_%d_%d", typeID, i - begin);
            features.add(new Feature(name, tokenEntityTags.get(i)));
          }
        }
        // features from previous classifications
        for (int i = nPreviousClassifications; i > 0; --i) {
          int index = tokenIndex - i;
          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
          features.add(new Feature("PreviousOutcome_" + i, previousOutcome));
        }

        // apply feature selection, if necessary
        if (this.featureSelection != null) {
          features = this.featureSelection.transform(features);
        }

        // if training, write to data file
        if (this.isTraining()) {
          String outcome = outcomes.get(tokenIndex);
          // if it is an "O" down-sample it
          if (!outcome.equals("O") || rand.nextDouble() <= this.probabilityOfKeepingANegativeExample) {
            this.dataWriter.write(new Instance<String>(outcome, features));
          }
        }

        // if predicting, add prediction to outcomes
        else {
          outcomes.add(this.classifier.classify(features));
        }
      }

      // during prediction, convert chunk labels to events and add them to the CAS
      if (!this.isTraining()) {
        this.eventChunking.createChunks(jCas, tokens, outcomes);
      }
    }
  }

  private static Predicate<EntityMention> hasEntityType(final int typeID) {
    return new Predicate<EntityMention>() {
      @Override
      public boolean apply(EntityMention mention) {
        return mention.getTypeID() == typeID;
      }
    };
  }
}
