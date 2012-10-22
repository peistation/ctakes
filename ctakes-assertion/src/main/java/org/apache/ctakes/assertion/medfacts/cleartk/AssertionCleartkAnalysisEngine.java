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
package org.apache.ctakes.assertion.medfacts.cleartk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.opennlp.MaxentDataWriterFactory_ImplBase;
import org.cleartk.type.test.Token;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;

public class AssertionCleartkAnalysisEngine extends
    CleartkSequenceAnnotator<String>
{

	public static final String PARAM_GOLD_VIEW_NAME = "GoldViewName";
//private SimpleFeatureExtractor tokenFeatureExtractor;
  private List<ContextExtractor<IdentifiedAnnotation>> contextFeatureExtractors;
  private List<ContextExtractor<BaseToken>> tokenContextFeatureExtractors;
  private List<SimpleFeatureExtractor> entityFeatureExtractors;
  
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // alias for NGram feature parameters
    int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;

    // a list of feature extractors that require only the token:
    // the stem of the word, the text of the word itself, plus
    // features created from the word text like character ngrams
    this.entityFeatureExtractors = Arrays.asList(
        new CoveredTextExtractor(),
        //new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
        new ProliferatingExtractor(
            new SpannedTextExtractor(),
            new LowerCaseProliferator(),    
            new CapitalTypeProliferator(),
            new NumericTypeProliferator(),
            new CharacterNGramProliferator(fromRight, 0, 2),
            new CharacterNGramProliferator(fromRight, 0, 3)));

    // a list of feature extractors that require the token and the sentence
    this.contextFeatureExtractors = new ArrayList<ContextExtractor<IdentifiedAnnotation>>();
    this.contextFeatureExtractors.add(new ContextExtractor<IdentifiedAnnotation>(
        IdentifiedAnnotation.class,
        new CoveredTextExtractor(),
        //new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
        new Preceding(2),
        new Following(2)));
    ContextExtractor<BaseToken> tokenContextExtractor1 = new ContextExtractor<BaseToken>( 
        BaseToken.class, 
        new SpannedTextExtractor(), 
        new ContextExtractor.Ngram(new Covered()),
        
        new ContextExtractor.Ngram(new Preceding(1)), 
        new ContextExtractor.Ngram(new Preceding(2)), 
        //new ContextExtractor.Ngram(new Preceding(1, 2)), 
        new ContextExtractor.Ngram(new Preceding(3)), 
        //new ContextExtractor.Ngram(new Preceding(2, 3)), 
        new ContextExtractor.Ngram(new Following(1)), 
        new ContextExtractor.Ngram(new Following(2)),
        //new ContextExtractor.Ngram(new Following(1, 2)),
        new ContextExtractor.Ngram(new Following(3))
        //new ContextExtractor.Ngram(new Following(2,3))
        ); 
    tokenContextFeatureExtractors = new ArrayList<ContextExtractor<BaseToken>>();
    tokenContextFeatureExtractors.add(tokenContextExtractor1);
    
    TypePathExtractor posExtractor = new TypePathExtractor(BaseToken.class, "partOfSpeech");
    ContextExtractor<BaseToken> extractor2 = new ContextExtractor<BaseToken>( 
        BaseToken.class, 
        posExtractor, 
        new ContextExtractor.Ngram(new Covered()), 
        new ContextExtractor.Ngram(new Preceding(1)), 
        new ContextExtractor.Ngram(new Preceding(2)), 
        new ContextExtractor.Ngram(new Following(1)), 
        new ContextExtractor.Ngram(new Following(2)) 
        /*
        new ContextExtractor.Covered(), 
        new ContextExtractor.Ngram(new Covered()) 
        
        new ContextExtractor.Ngram(new Preceding(1)), 
        new ContextExtractor.Ngram(new Preceding(2)), 
        */
        );
    tokenContextFeatureExtractors.add(extractor2);

  }



  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException
  {
    Map<IdentifiedAnnotation, Collection<Sentence>> coveringSentenceMap = JCasUtil.indexCovering(jCas, IdentifiedAnnotation.class, Sentence.class);
    Map<Sentence, Collection<BaseToken>> tokensCoveredInSentenceMap = JCasUtil.indexCovered(jCas, Sentence.class, BaseToken.class);

    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    // generate a list of training instances for each sentence in the document
    Collection<IdentifiedAnnotation> entities = JCasUtil.select(jCas, IdentifiedAnnotation.class);
    for (IdentifiedAnnotation entityMention : entities)
    {
      Instance<String> instance = new Instance<String>();
      
//      // extract all features that require only the entity mention annotation
//      instance.addAll(tokenFeatureExtractor.extract(jCas, entityMention));

      // extract all features that require the token and sentence annotations
      Collection<Sentence> sentenceList = coveringSentenceMap.get(entityMention);
      if (sentenceList == null || sentenceList.isEmpty())
      {
        String message = "no surrounding sentence found";
        Exception runtimeException = new RuntimeException(message);
        throw new AnalysisEngineProcessException(runtimeException);
      } else if (sentenceList.size() > 1)
      {
        String message = "more than one surrounding sentence found";
        Exception runtimeException = new RuntimeException(message);
        throw new AnalysisEngineProcessException(runtimeException);
      }
      Sentence sentence = sentenceList.iterator().next();
      for (ContextExtractor<IdentifiedAnnotation> extractor : this.contextFeatureExtractors) {
        instance.addAll(extractor.extractWithin(jCas, entityMention, sentence));
      }
      for (ContextExtractor<BaseToken> extractor : this.tokenContextFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, entityMention));
      }
      for (SimpleFeatureExtractor extractor : this.entityFeatureExtractors) {
        instance.addAll(extractor.extract(jCas, entityMention));
      }
      
      if (this.isTraining())
      {
        String polarity = (entityMention.getPolarity() == 1) ? "present" : "negated";
        instance.setOutcome(polarity);
      }
      
      // add the instance to the list
      instances.add(instance);
    }
    
    // TODO figure out exactly what should be happening on training/evaluation/decoding

    // for training, write instances to the data write
    if (this.isTraining()) {
        this.dataWriter.write(instances);
    }
    // for classification, set the labels as the token POS labels
    else
    {
      Iterator<IdentifiedAnnotation> entityIter = entities.iterator();
      for (String label : this.classify(instances))
      {
        int polarity = 1;
        if (label!= null && label.equals("present"))
        {
          polarity = 1;
        } else if (label != null && label.equals("negated"))
        {
          polarity = -1;
        }
        entityIter.next().setPolarity(polarity);
      } // end for loop
    } // end else

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
