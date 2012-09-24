package org.mitre.medfacts.uima.assertion.cleartk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.type.test.Token;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public class AssertionCleartkAnalysisEngine extends
    CleartkSequenceAnnotator<String>
{

  private SimpleFeatureExtractor tokenFeatureExtractor;
  private List<ContextExtractor<IdentifiedAnnotation>> contextFeatureExtractors;
  private List<SimpleFeatureExtractor> entityFeatureExtractors;
  
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // alias for NGram feature parameters
    int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;

    // a list of feature extractors that require only the token:
    // the stem of the word, the text of the word itself, plus
    // features created from the word text like character ngrams
    this.entityFeatureExtractors = Arrays.asList(
        new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
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
        new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
        new Preceding(2),
        new Following(2)));

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
      
      // extract all features that require only the entity mention annotation
      instance.addAll(tokenFeatureExtractor.extract(jCas, entityMention));

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
        if (label != null && label.equals("negated"))
        {
          polarity = -1;
        }
        entityIter.next().setPolarity(polarity);
      } // end for loop
    } // end else

  }

}
