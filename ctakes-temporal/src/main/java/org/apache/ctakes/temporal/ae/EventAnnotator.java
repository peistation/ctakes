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
import java.util.List;
import java.util.Random;

import org.apache.ctakes.temporal.ae.feature.ChunkingExtractor;
import org.apache.ctakes.temporal.ae.feature.PredicateArgumentExtractor;
import org.apache.ctakes.temporal.ae.feature.selection.Chi2FeatureSelection;
import org.apache.ctakes.temporal.ae.feature.selection.FeatureSelection;
import org.apache.ctakes.temporal.utils.SMOTEplus;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class EventAnnotator extends TemporalEntityAnnotator_ImplBase {

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
  protected Float featureSelectionThreshold = 1f; //default is not using feature selection, i.e. select 100% of all features.
  
  public static final String PARAM_SMOTE_NUM_NEIGHBORS = "NumOfNeighborForSMOTE";

  @ConfigurationParameter(
		  name = PARAM_SMOTE_NUM_NEIGHBORS,
		  mandatory = false,
		  description = "the number of neighbors used for minority instances for SMOTE algorithm")
  protected Float smoteNumOfNeighbors = 0f;

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
      float featureSelect, float smoteNeighborNumber) throws ResourceInitializationException {
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
        featureSelect,
        EventAnnotator.PARAM_SMOTE_NUM_NEIGHBORS,
        smoteNeighborNumber);
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

  private BIOChunking<BaseToken, IdentifiedAnnotation> entityChunking;

  private BIOChunking<BaseToken, EventMention> eventChunking;

  private BIOChunking<BaseToken, Chunk> phraseChunking;

  protected SimpleFeatureExtractor tokenFeatureExtractor;

  protected CleartkExtractor contextFeatureExtractor;
  
  private FeatureSelection<String> featureSelection;

  private static final String FEATURE_SELECTION_NAME = "SelectNeighborFeatures";

  public static FeatureSelection<String> createFeatureSelection(double threshold) {
    return new Chi2FeatureSelection<String>(EventAnnotator.FEATURE_SELECTION_NAME, threshold, false);
  }
  
  public static URI createFeatureSelectionURI(File outputDirectoryName) {
    return new File(outputDirectoryName, FEATURE_SELECTION_NAME + "_Chi2_extractor.dat").toURI();
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // define chunkings
    this.entityChunking = new BIOChunking<BaseToken, IdentifiedAnnotation>(
        BaseToken.class,
        IdentifiedAnnotation.class,
        "typeID");
    this.phraseChunking = new BIOChunking<BaseToken, Chunk>(
        BaseToken.class,
        Chunk.class,
        "chunkType");
    this.eventChunking = new BIOChunking<BaseToken, EventMention>(
        BaseToken.class,
        EventMention.class);

    this.tokenFeatureExtractor = new CombinedExtractor(
        new CoveredTextExtractor(),
        new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
        new TypePathExtractor(BaseToken.class, "partOfSpeech"));
    this.contextFeatureExtractor = new CleartkExtractor(
        BaseToken.class,
        this.tokenFeatureExtractor,
        new Preceding(3),
        new Following(3));

    if (featureSelectionThreshold == 1) {
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
  public void process(JCas jCas, Segment segment) throws AnalysisEngineProcessException {
    PredicateArgumentExtractor predicateArgumentExtractor = new PredicateArgumentExtractor(jCas);

    // Create features for tokens that end UMLS (or other) entities
    Multimap<BaseToken, Feature> endOfEntityFeatures = HashMultimap.create();
    for (IdentifiedAnnotation entity : JCasUtil.select(jCas, IdentifiedAnnotation.class)) {
      if (!entity.getClass().equals(EventMention.class)) {
        List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, entity);
        if (tokens.size() > 0){
        	BaseToken lastToken = tokens.get(tokens.size() - 1);
            String value = String.format("%s_%s", entity.getClass().getSimpleName(), entity.getTypeID());
            endOfEntityFeatures.put(lastToken, new Feature("EndOf", value));
        }       
      }
    }

    Random rand = new Random();
    
    //TRY SMOTE algorithm here to generate more minority class samples
    SMOTEplus smote = new SMOTEplus((int)Math.ceil(this.smoteNumOfNeighbors));
        
    // classify tokens within each sentence
    for (Sentence sentence : JCasUtil.selectCovered(jCas, Sentence.class, segment)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);

      // during training, the list of all outcomes for the tokens
      List<String> outcomes;
      if (this.isTraining()) {
        List<EventMention> events = Lists.newArrayList();
        for (EventMention event : JCasUtil.selectCovered(jCas, EventMention.class, sentence)) {
          if (event.getClass().equals(EventMention.class)) {
            events.add(event);
          }
        }
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
      List<IdentifiedAnnotation> entities;
      if (this.isTraining()) {
        entities = Lists.newArrayList();
        for (IdentifiedAnnotation entity : JCasUtil.selectCovered(jCas, IdentifiedAnnotation.class, sentence)) {
          if (!entity.getClass().equals(EventMention.class)) {
            entities.add(entity);
          }
        }
      } else {
        entities = JCasUtil.selectCovered(jCas, IdentifiedAnnotation.class, sentence);
      }
      
      List<ChunkingExtractor> chunkingExtractors = Lists.newArrayList(); 
      for (int typeID : entityTypeIDs) {
        Predicate<IdentifiedAnnotation> hasTypeID = hasEntityType(typeID);
        List<IdentifiedAnnotation> subEntities = Lists.newArrayList(Iterables.filter(entities, hasTypeID));
        chunkingExtractors.add(new ChunkingExtractor("EntityTag", this.entityChunking, jCas, tokens, subEntities));
      }
      
      // add extractor for phase chunks
      List<Chunk> chunks = JCasUtil.selectCovered(jCas, Chunk.class, sentence);
      chunkingExtractors.add(new ChunkingExtractor("PhraseTag", this.phraseChunking, jCas, tokens, chunks));

      // extract features for all tokens
      int tokenIndex = -1;
      int nChunkLabelsBefore = 2;
      int nChunkLabelsAfter = 2;
      int nPreviousClassifications = 2;

      for (BaseToken token : tokens) {
        ++tokenIndex;

        List<Feature> features = new ArrayList<Feature>();

        // features from previous classifications
        for (int i = nPreviousClassifications; i > 0; --i) {
          int index = tokenIndex - i;
          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
          features.add(new Feature("PreviousOutcome_" + i, previousOutcome));
        }
        
        // features from token attributes
        features.addAll(this.tokenFeatureExtractor.extract(jCas, token));

        // features from surrounding tokens
        features.addAll(this.contextFeatureExtractor.extractWithin(jCas, token, sentence));
        
        // features from ends of entities
        features.addAll(endOfEntityFeatures.get(token));

        // features from surrounding entity, phrase, etc. chunk-labels
        for (ChunkingExtractor extractor : chunkingExtractors) {
          features.addAll(extractor.extract(tokenIndex, nChunkLabelsBefore, nChunkLabelsAfter));
        }
        
        // features from semantic roles
        features.addAll(predicateArgumentExtractor.extract(token));

        // apply feature selection, if necessary
        if (this.featureSelection != null) {
          features = this.featureSelection.transform(features);
        }

        // if training, write to data file
        if (this.isTraining()) {
          String outcome = outcomes.get(tokenIndex);
          // if it is an "O" down-sample it
          if (outcome.equals("O")) {
        	  if (rand.nextDouble() <= this.probabilityOfKeepingANegativeExample){
        		  this.dataWriter.write(new Instance<String>(outcome, features));
        	  }		  
          }else{//for minority instances:
        	  Instance<String> minorityInst = new Instance<String>(outcome, features);
        	  this.dataWriter.write(minorityInst);
        	  smote.addInstance(minorityInst);//add minority instances to SMOTE algorithm
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
    if(this.isTraining() && this.smoteNumOfNeighbors >= 1){ //add synthetic instances to datawriter, if smote is selected
    	Iterable<Instance<String>> syntheticInsts = smote.populateMinorityClass();
    	for( Instance<String> sytheticInst: syntheticInsts){
    		this.dataWriter.write(sytheticInst);
    	}
    }
    
  }

  private static Predicate<IdentifiedAnnotation> hasEntityType(final int typeID) {
    return new Predicate<IdentifiedAnnotation>() {
      @Override
      public boolean apply(IdentifiedAnnotation mention) {
        return mention.getTypeID() == typeID;
      }
    };
  }
}
