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
//import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
//import java.util.logging.Logger;

//import org.apache.ctakes.temporal.ae.feature.CoveredTextToValuesExtractor;
import org.apache.ctakes.temporal.ae.feature.PhraseExtractor;
import org.apache.ctakes.temporal.ae.feature.SRLExtractor;
import org.apache.ctakes.temporal.ae.feature.SurfaceFormFeatureExtractor;
import org.apache.ctakes.temporal.ae.feature.selection.Chi2NeighborFSExtractor;
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
//import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
//import org.cleartk.classifier.feature.transform.InstanceDataWriter;
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
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

//import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
//import com.google.common.io.LineProcessor;
//import com.google.common.io.Resources;

public class EventAnnotator extends CleartkAnnotator<String> {

  public static final String PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE = "ProbabilityOfKeepingANegativeExample";

  @ConfigurationParameter(
			name = PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
			mandatory = false,
			description = "probability that a negative example should be retained for training")
  protected Float probabilityOfKeepingANegativeExample = 0.8f;
  
  public static final String PARAM_FEATURE_TRIM_ORNOT = "WhetherToDoFeatureSelection";

  @ConfigurationParameter(
			name = PARAM_FEATURE_TRIM_ORNOT,
			mandatory = false,
			description = "set whether feature selection is used or not")
  public static Float featureTrim = 0f;
  
  public static AnalysisEngineDescription createDataWriterDescription(
      String dataWriterName,
      File outputDirectory, float downratio, float featureSelect) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        EventAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        dataWriterName,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory,
        EventAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
        downratio,
        EventAnnotator.PARAM_FEATURE_TRIM_ORNOT,
        featureSelect);
  }

  public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
      throws ResourceInitializationException {
	 AnalysisEngineDescription fsEventAnnotator =AnalysisEngineFactory.createPrimitiveDescription(
        EventAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(modelDirectory, "model.jar"));
	 ConfigurationParameterFactory.addConfigurationParameter(
		fsEventAnnotator,	
		EventAnnotator.PARAM_NB_FS_URI,
		EventAnnotator.createNbFSURI(modelDirectory) );
	 
     return(fsEventAnnotator);
  }

  protected List<SimpleFeatureExtractor> tokenFeatureExtractors;

  protected List<CleartkExtractor> contextFeatureExtractors;

  private BIOChunking<BaseToken, EntityMention> entityChunking;

  private BIOChunking<BaseToken, EventMention> eventChunking;
  
  public static final String PARAM_NB_FS_URI = ConfigurationParameterFactory.createConfigurationParameterName(
		      EventAnnotator.class,
		      "neighborFsUri");

	  @ConfigurationParameter(
		  mandatory = false,
		  description = "provides a URI where the neighbor annotation's feature selection data will be written")
	  protected URI neighborFsUri;
		    
  public static final String FS_NEIGHBOR_EXTRACTOR_KEY = "SelectNeighborFeatures";

  private Chi2NeighborFSExtractor<String> chi2NeighborFsExtractor;
  
  
  public static URI createNbFSURI(File outputDirectoryName) {
	    File f = new File(outputDirectoryName, FS_NEIGHBOR_EXTRACTOR_KEY + "_Chi2_extractor.dat");
	    return f.toURI();
	  }
  //*****feature selection related parameters

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
      
    //configure FS extractor:
    if (featureTrim > 0){//if feature selection
        CombinedExtractor forneighbors    = new CombinedExtractor(
    			new CoveredTextExtractor(),
    			new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
    			new TypePathExtractor(BaseToken.class, "partOfSpeech"),
    			new SurfaceFormFeatureExtractor(),
    	      	new PhraseExtractor(),
    	      	new SRLExtractor());
        
        try {
    		this.chi2NeighborFsExtractor = initNbFSExtractor(forneighbors);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }else{//if no feature selection
        // add features: word, stem, pos and more
        this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
        // try {
        this.tokenFeatureExtractors.addAll(Arrays.asList(
        		new CoveredTextExtractor(),
//            	new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
//            	new TypePathExtractor(BaseToken.class, "partOfSpeech"),//);
//            	new SurfaceFormFeatureExtractor(),
//            	new PhraseExtractor(),
            	new SRLExtractor()));
        		// new CoveredTextToValuesExtractor("ACF", StringToDoublesProcessor.parse("/word_freq.lst")),
        		// new CoveredTextToValuesExtractor("PCA", StringToDoublesProcessor.parse("/word_pca.lst")),
        		// new CoveredTextToValuesExtractor("TimPCA", StringToDoublesProcessor.parse("/tim_word_pca.txt"))));

        //add window of features before and after
        CombinedExtractor subExtractor = new CombinedExtractor(
        	new CoveredTextExtractor(),
        	new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
        	new TypePathExtractor(BaseToken.class, "partOfSpeech"),//);
        	new SurfaceFormFeatureExtractor(),
        	new SRLExtractor());

        this.contextFeatureExtractors = new ArrayList<CleartkExtractor>();
        this.contextFeatureExtractors.add(new CleartkExtractor(
        	BaseToken.class,
        	subExtractor,
        	new Preceding(3),
        	new Following(3)));
    }


  }


private Chi2NeighborFSExtractor<String> initNbFSExtractor(
		CombinedExtractor subextractor) throws IOException{

	Chi2NeighborFSExtractor<String> chi2NbFSExtractor = new  Chi2NeighborFSExtractor<String>(EventAnnotator.FS_NEIGHBOR_EXTRACTOR_KEY, BaseToken.class, subextractor, featureTrim, new Preceding(4),
	    	new Following(4)); //the 3rd last parameter is used to control chi2 threshold, the last two are used to control window size
	
	if (this.neighborFsUri != null) {
		chi2NbFSExtractor.load(this.neighborFsUri);
	    }
	return chi2NbFSExtractor;
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
        
        if (featureTrim >0 ){//if feature selection
        	features.addAll(this.chi2NeighborFsExtractor.extract(jCas, token)); //base features
        	features.addAll(this.chi2NeighborFsExtractor.extractWithin(jCas, token, sentence)); //neighbor features
        	features.addAll(this.chi2NeighborFsExtractor.extract(entityTypeIDs, entityTagsByType,tokenIndex, window)); // features from surrounding entities
        	features.addAll(this.chi2NeighborFsExtractor.extract(nPreviousClassifications, tokenIndex, outcomes)); //features from previous classifications
        }else{ //if no feature selection
        	// features from token attributes
            for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
              features.addAll(extractor.extract(jCas, token));
            }
            // features from surrounding tokens
            for (CleartkExtractor extractor : this.contextFeatureExtractors) {
              features.addAll(extractor.extractWithin(jCas, token, sentence));
            }
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
        }
        
        // if training, write to data file
        if (this.isTraining()) {
            String outcome = outcomes.get(tokenIndex);
            if(outcome.equals("O")){ //if it is an "O". downsample it
          	  if (rand.nextDouble()<=probabilityOfKeepingANegativeExample)
          		  this.dataWriter.write(new Instance<String>(outcome, features));
            }else {
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
      public boolean apply(EntityMention mention) {
        return mention.getTypeID() == typeID;
      }
    };
  }

//  private static class StringToDoublesProcessor implements LineProcessor<Map<String, double[]>> {
//    private Logger logger = Logger.getLogger(this.getClass().getName());
//
//    private Map<String, double[]> result = new HashMap<String, double[]>();
//
//    private int length = -1;
//
//    @Override
//    public Map<String, double[]> getResult() {
//      return this.result;
//    }
//
//    @Override
//    public boolean processLine(String line) throws IOException {
//      String[] parts = line.trim().split(",");
//      String key = parts[0];
//      int partsOffset = 0;
//      if (this.length == -1) {
//        this.length = parts.length;
//      } else if (parts.length != this.length) {
//        String message = "expected %d parts, found %d, skipping line '%s'";
//        this.logger.warning(String.format(message, this.length, parts.length, line));
//        return true;
//      }
//      double[] values = new double[parts.length - 1];
//      for (int i = 0; i < values.length; ++i) {
//        values[i] = Double.parseDouble(parts[i + 1 + partsOffset]);
//      }
//      this.result.put(key, values);
//      return true;
//    }
//  }


public Chi2NeighborFSExtractor<String> getChi2NbSubExtractor() {
	return this.chi2NeighborFsExtractor;
}
}
