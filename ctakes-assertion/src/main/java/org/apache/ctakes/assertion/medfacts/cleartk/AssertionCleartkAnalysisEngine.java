/*
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
package org.apache.ctakes.assertion.medfacts.cleartk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.ctakes.assertion.zoner.types.Zone;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.temporary.assertion.AssertionCuePhraseAnnotation;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;
//import org.chboston.cnlp.ctakes.relationextractor.ae.ModifierExtractorAnnotator;

public abstract class AssertionCleartkAnalysisEngine extends
    CleartkAnnotator<String>
{
  Logger logger = Logger.getLogger(AssertionCleartkAnalysisEngine.class);

  public static final String PARAM_GOLD_VIEW_NAME = "GoldViewName";
	
  public static int relationId; // counter for error logging

  @ConfigurationParameter(
      name = PARAM_GOLD_VIEW_NAME,
      mandatory = false,
      description = "view containing the manual identified annotations (especially EntityMention and EventMention annotations); needed for training")
  protected String goldViewName;

  public static final String PARAM_PRINT_ERRORS = "PrintErrors";
  
  @ConfigurationParameter(
     name = PARAM_PRINT_ERRORS,
     mandatory = false,
     description = "Print errors true/false",
     defaultValue = "false")
  boolean printErrors;
  
  public static final String PARAM_PROBABILITY_OF_KEEPING_DEFAULT_EXAMPLE = "ProbabilityOfKeepingADefaultExample";

  @ConfigurationParameter(
      name = PARAM_PROBABILITY_OF_KEEPING_DEFAULT_EXAMPLE,
      mandatory = false,
      description = "probability that a default example should be retained for training")
  protected double probabilityOfKeepingADefaultExample = 1.0;
  
  protected Random coin = new Random(0);

  
/* DEPRECATED: STW 2013/03/28.  Use DependencyUtility:getNominalHeadNode(jCas,annotation) instead */
//  public ConllDependencyNode findAnnotationHead(JCas jcas, Annotation annotation) {
//		
//	    for (ConllDependencyNode depNode : JCasUtil.selectCovered(jcas, ConllDependencyNode.class, annotation)) {
//	    	
//	    	ConllDependencyNode head = depNode.getHead();
//	    	if (head == null || head.getEnd() <= annotation.getBegin() || head.getBegin() > annotation.getEnd()) {
//	    		// The head is outside the bounds of the annotation, so this node must be the annotation's head
//	    		return depNode;
//	    	}
//	    }
//	    // Can this happen?
//	    return null;
//	}

  
	
	
//private SimpleFeatureExtractor tokenFeatureExtractor;
//  protected List<ContextExtractor<IdentifiedAnnotation>> contextFeatureExtractors;
//  protected List<ContextExtractor<BaseToken>> tokenContextFeatureExtractors;
  protected List<CleartkExtractor> contextFeatureExtractors;
  protected List<CleartkExtractor> tokenContextFeatureExtractors;
  protected List<CleartkExtractor> tokenCleartkExtractors;
  protected List<SimpleFeatureExtractor> entityFeatureExtractors;
  protected CleartkExtractor cuePhraseInWindowExtractor;
  
  @Override
@SuppressWarnings("deprecation")
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    
    if (this.isTraining() && this.goldViewName == null) {
      throw new IllegalArgumentException(PARAM_GOLD_VIEW_NAME + " must be defined during training");
    }
    
    // alias for NGram feature parameters
//    int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;

    // a list of feature extractors that require only the token:
    // the stem of the word, the text of the word itself, plus
    // features created from the word text like character ngrams
    this.entityFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    
    // a list of feature extractors that require the token and the sentence
//    this.contextFeatureExtractors = new ArrayList<CleartkExtractor>();
    
    this.tokenCleartkExtractors = new ArrayList<CleartkExtractor>();

    CleartkExtractor tokenExtraction1 = 
    		new CleartkExtractor(
    				BaseToken.class, 
//    				new FeatureFunctionExtractor(new CoveredTextExtractor(), new LowerCaseFeatureFunction()),
    				new CoveredTextExtractor(),
    				//new CleartkExtractor.Covered(),
    				new CleartkExtractor.LastCovered(2),
    				new CleartkExtractor.Preceding(5),
    				new CleartkExtractor.Following(4),
    				new CleartkExtractor.Bag(new CleartkExtractor.Preceding(3)),
    				new CleartkExtractor.Bag(new CleartkExtractor.Following(3)),
            new CleartkExtractor.Bag(new CleartkExtractor.Preceding(5)),
            new CleartkExtractor.Bag(new CleartkExtractor.Following(5)),
            new CleartkExtractor.Bag(new CleartkExtractor.Preceding(10)),
            new CleartkExtractor.Bag(new CleartkExtractor.Following(10))
    				);
    
//    CleartkExtractor posExtraction1 = 
//    		new CleartkExtractor(
//    				BaseToken.class,
//    				new TypePathExtractor(BaseToken.class, "partOfSpeech"),
//    				new CleartkExtractor.LastCovered(2),
//    				new CleartkExtractor.Preceding(3),
//    				new CleartkExtractor.Following(2)
//    				);

    this.tokenCleartkExtractors.add(tokenExtraction1);
    //this.tokenCleartkExtractors.add(posExtraction1);
    
//    this.contextFeatureExtractors.add(new CleartkExtractor(IdentifiedAnnotation.class,
//        new CoveredTextExtractor(),
//        //new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
//        new Preceding(2),
//        new Following(2)));
    
    // stab at dependency-based features
    //List<Feature> features = new ArrayList<Feature>();
    //ConllDependencyNode node1 = findAnnotationHead(jCas, arg1);

    CombinedExtractor baseExtractorCuePhraseCategory =
        new CombinedExtractor
          (
           new CoveredTextExtractor(),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhrase"),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhraseCategory"),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhraseAssertionFamily")
          );
    
    cuePhraseInWindowExtractor = new CleartkExtractor(
        BaseToken.class,
        new CoveredTextExtractor(),
        new CleartkExtractor.Bag(new CleartkExtractor.Covered())
//          AssertionCuePhraseAnnotation.class,
//          baseExtractorCuePhraseCategory,
//          new CleartkExtractor.Bag(new CleartkExtractor.Preceding(3)),
//          new CleartkExtractor.Bag(new CleartkExtractor.Following(3)),
//          new CleartkExtractor.Bag(new CleartkExtractor.Preceding(5)),
//          new CleartkExtractor.Bag(new CleartkExtractor.Following(5)),
//          new CleartkExtractor.Bag(new CleartkExtractor.Preceding(10)),
//          new CleartkExtractor.Bag(new CleartkExtractor.Following(10))
          );
    
  }

  public abstract void setClassLabel(IdentifiedAnnotation entityMention, Instance<String> instance) throws AnalysisEngineProcessException;


  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException
  {
    DocumentID documentId = JCasUtil.selectSingle(jCas, DocumentID.class);
    if (documentId != null)
    {
      logger.debug("processing next doc: " + documentId.getDocumentID());
    } else
    {
      logger.warn("processing next doc (doc id is null)");
    }
//    // get gold standard relation instances during testing for error analysis
//    if (! this.isTraining() && printErrors) {
//      JCas goldView;
//      try {
//        goldView = jCas.getView("GoldView");
//      } catch(CASException e) {
//        throw new AnalysisEngineProcessException(e);
//      }
//      
//      //categoryLookup = createCategoryLookup(goldView); 
//    }
    
    JCas identifiedAnnotationView;
    if (this.isTraining()) {
      try {
        identifiedAnnotationView = jCas.getView(this.goldViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    } else {
      identifiedAnnotationView = jCas;
    }


//    Map<IdentifiedAnnotation, Collection<Sentence>> coveringSentenceMap = JCasUtil.indexCovering(identifiedAnnotationView, IdentifiedAnnotation.class, Sentence.class);
//    Map<Sentence, Collection<BaseToken>> tokensCoveredInSentenceMap = JCasUtil.indexCovered(identifiedAnnotationView, Sentence.class, BaseToken.class);

    Map<IdentifiedAnnotation, Collection<Zone>> coveringZoneMap =
        JCasUtil.indexCovering(jCas, IdentifiedAnnotation.class, Zone.class);
//    Map<IdentifiedAnnotation, Collection<Sentence>> coveringSents =
//        JCasUtil.indexCovering(jCas, IdentifiedAnnotation.class, Sentence.class);
    
//    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    // generate a list of training instances for each sentence in the document
    Collection<IdentifiedAnnotation> entities = JCasUtil.select(identifiedAnnotationView, IdentifiedAnnotation.class);
    for (IdentifiedAnnotation identifiedAnnotation : entities)
    {
      if (!(identifiedAnnotation instanceof EntityMention || identifiedAnnotation instanceof EventMention))
      {
        continue;
      }
      IdentifiedAnnotation entityOrEventMention = identifiedAnnotation;
      if (entityOrEventMention.getPolarity() == -1)
      {
        logger.debug(String.format(" - identified annotation: [%d-%d] polarity %d (%s)",
            entityOrEventMention.getBegin(),
            entityOrEventMention.getEnd(),
            entityOrEventMention.getPolarity(),
            entityOrEventMention.getClass().getName()));
      }
      Instance<String> instance = new Instance<String>();
      
//      // extract all features that require only the entity mention annotation
//      instance.addAll(tokenFeatureExtractor.extract(jCas, entityMention));

      // extract all features that require the token and sentence annotations

      //Sentence sentence = sentenceList.iterator().next();
      
      /*
      if (sentence != null)
      {
        for (ContextExtractor<IdentifiedAnnotation> extractor : this.contextFeatureExtractors) {
          instance.addAll(extractor.extractWithin(identifiedAnnotationView, entityMention, sentence));
        }
      } else
      {
        // TODO extract context features for annotations that don't fall within a sentence
        logger.log(Level.WARN, "FIXME/TODO: generate context features for entities that don't fall within a sentence");
      }
      */
      
      /*
      for (ContextExtractor<BaseToken> extractor : this.tokenContextFeatureExtractors) {
          instance.addAll(extractor.extract(identifiedAnnotationView, entityMention));
        }
        */
      for (CleartkExtractor extractor : this.tokenCleartkExtractors) {
          //instance.addAll(extractor.extractWithin(identifiedAnnotationView, entityMention, sentence));
    	  instance.addAll(extractor.extract(identifiedAnnotationView, entityOrEventMention));
        }
      
//      List<Feature> cuePhraseFeatures = null;
//          cuePhraseInWindowExtractor.extract(jCas, entityOrEventMention);
          //cuePhraseInWindowExtractor.extractWithin(jCas, entityMention, firstCoveringSentence);
//      List<Sentence> sents = new ArrayList<Sentence>(coveringSents.get(entityOrEventMention));
      List<Sentence> sents = new ArrayList<Sentence>(JCasUtil.selectCovering(jCas, Sentence.class, entityOrEventMention.getBegin(), entityOrEventMention.getEnd()));
      if(sents.size() > 0){
        Sentence sentence = sents.get(0);
        List<AssertionCuePhraseAnnotation> cues = JCasUtil.selectCovered(AssertionCuePhraseAnnotation.class, sentence);
        int closest = Integer.MAX_VALUE;
        AssertionCuePhraseAnnotation closestCue = null;
        for(AssertionCuePhraseAnnotation cue : cues){
          List<BaseToken> tokens = JCasUtil.selectBetween(BaseToken.class, cue, entityOrEventMention);
          if(tokens.size() < closest){
            closestCue = cue;
            closest = tokens.size();
          }
//          instance.addAll(cuePhraseInWindowExtractor.extractBetween(jCas, cue, entityOrEventMention));
        }
        if(closestCue != null){
          instance.add(new Feature("ClosestCue_Word", closestCue.getCoveredText()));
//          instance.add(new Feature("ClosestCue_Phrase", closestCue.getCuePhrase()));
          instance.add(new Feature("ClosestCue_PhraseFamily", closestCue.getCuePhraseAssertionFamily()));
          instance.add(new Feature("ClosestCue_PhraseCategory", closestCue.getCuePhraseCategory()));
        }
      }
//      if (cuePhraseFeatures != null && !cuePhraseFeatures.isEmpty())
//      {
//        instance.addAll(cuePhraseFeatures);
//      }


      instance.add(new Feature("ENTITY_TYPE", entityOrEventMention.getTypeID()));
      
      for (SimpleFeatureExtractor extractor : this.entityFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, entityOrEventMention));
      }
      
      
      List<Feature> zoneFeatures = extractZoneFeatures(coveringZoneMap, entityOrEventMention);
      if (zoneFeatures != null && !zoneFeatures.isEmpty())
      {
//        instance.addAll(zoneFeatures);
      }
      
      List<Feature> feats = instance.getFeatures();
//      List<Feature> lcFeats = new ArrayList<Feature>();
      
      for(Feature feat : feats){
    	  if(feat.getName() != null && (feat.getName().startsWith("TreeFrag") || feat.getName().startsWith("WORD") || feat.getName().startsWith("NEG"))) continue;
    	  if(feat.getValue() instanceof String){
    		  feat.setValue(((String)feat.getValue()).toLowerCase());
//    		  lcFeats.add(new Feature("LC_" + feat.getName(), ((String)feat.getValue()).toLowerCase()));
    	  }
      }
//      instance.addAll(lcFeats);

      setClassLabel(entityOrEventMention, instance);
      
    }
    
  }
  
  public List<Feature> extractZoneFeatures(Map<IdentifiedAnnotation, Collection<Zone>> coveringZoneMap, IdentifiedAnnotation entityOrEventMention)
  {
    final Collection<Zone> zoneList = coveringZoneMap.get(entityOrEventMention);
    
    if (zoneList == null || zoneList.isEmpty())
    {
      //logger.info("AssertionCleartkAnalysisEngine.extractZoneFeatures() early END (no zones)");
      return new ArrayList<Feature>();
    } else
    {
      logger.debug("AssertionCleartkAnalysisEngine.extractZoneFeatures() found zones and adding zone features");
    }
    
    ArrayList<Feature> featureList = new ArrayList<Feature>();
    for (Zone zone : zoneList)
    {
      Feature currentFeature = new Feature("zone", zone.getLabel());
      logger.debug(String.format("zone: %s", zone.getLabel()));
      logger.debug(String.format("zone feature: %s", currentFeature.toString()));
      featureList.add(currentFeature);
    }
    
    return featureList;
  }

  public static AnalysisEngineDescription getDescription(Object... additionalConfiguration)
	      throws ResourceInitializationException {
	    AnalysisEngineDescription desc = AnalysisEngineFactory.createPrimitiveDescription(AssertionCleartkAnalysisEngine.class);
	    if (additionalConfiguration.length > 0) {
	      ConfigurationParameterFactory.addConfigurationParameters(desc, additionalConfiguration);
	    }
	    return desc;
	  }


  
  /*
  public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
      throws ResourceInitializationException {
    return CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
        AssertionCleartkAnalysisEngine.class,
        AssertionComponents.TYPE_SYSTEM_DESCRIPTION,
        modelFileName);
  }

  public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
      throws ResourceInitializationException {
    AnalysisEngineDescription aed = CleartkAnnotatorDescriptionFactory.createViterbiAnnotator(
        AssertionCleartkAnalysisEngine.class,
        AssertionComponents.TYPE_SYSTEM_DESCRIPTION,
        DefaultMaxentDataWriterFactory.class,
        outputDirectory);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        MaxentDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);
    return aed;
  }
  */
}
