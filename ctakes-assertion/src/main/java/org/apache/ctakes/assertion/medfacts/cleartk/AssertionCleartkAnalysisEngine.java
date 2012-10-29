package org.apache.ctakes.assertion.medfacts.cleartk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
//import org.chboston.cnlp.ctakes.relationextractor.ae.ModifierExtractorAnnotator;
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
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.structured.DocumentID;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.EventMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public class AssertionCleartkAnalysisEngine extends
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
  
	
	
//private SimpleFeatureExtractor tokenFeatureExtractor;
  private List<ContextExtractor<IdentifiedAnnotation>> contextFeatureExtractors;
  private List<ContextExtractor<BaseToken>> tokenContextFeatureExtractors;
  private List<SimpleFeatureExtractor> entityFeatureExtractors;
  
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    
    if (this.isTraining() && this.goldViewName == null) {
      throw new IllegalArgumentException(PARAM_GOLD_VIEW_NAME + " must be defined during training");
    }
    
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
    DocumentID documentId = JCasUtil.selectSingle(jCas, DocumentID.class);
    if (documentId != null)
    {
      logger.info("processing next doc: " + documentId.getDocumentID());
    } else
    {
      logger.info("processing next doc (doc id is null)");
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
    
    JCas identifiedAnnotationView, relationView;
    if (this.isTraining()) {
      try {
        identifiedAnnotationView = relationView = jCas.getView(this.goldViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    } else {
      identifiedAnnotationView = relationView = jCas;
    }


    Map<IdentifiedAnnotation, Collection<Sentence>> coveringSentenceMap = JCasUtil.indexCovering(identifiedAnnotationView, IdentifiedAnnotation.class, Sentence.class);
    Map<Sentence, Collection<BaseToken>> tokensCoveredInSentenceMap = JCasUtil.indexCovered(identifiedAnnotationView, Sentence.class, BaseToken.class);

    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    // generate a list of training instances for each sentence in the document
    Collection<IdentifiedAnnotation> entities = JCasUtil.select(identifiedAnnotationView, IdentifiedAnnotation.class);
    for (IdentifiedAnnotation entityMention : entities)
    {
      if (!(entityMention instanceof EntityMention || entityMention instanceof EventMention))
      {
        continue;
      }
      if (entityMention.getPolarity() == -1)
      {
        logger.info(String.format(" - identified annotation: [%d-%d] polarity %d (%s)",
            entityMention.getBegin(),
            entityMention.getEnd(),
            entityMention.getPolarity(),
            entityMention.getClass().getName()));
      }
      Instance<String> instance = new Instance<String>();
      
//      // extract all features that require only the entity mention annotation
//      instance.addAll(tokenFeatureExtractor.extract(jCas, entityMention));

      // extract all features that require the token and sentence annotations
      Collection<Sentence> sentenceList = coveringSentenceMap.get(entityMention);
      Sentence sentence = null;
      if (sentenceList == null || sentenceList.isEmpty())
      {
        String message = "no surrounding sentence found";
        Exception runtimeException = new RuntimeException(message);
        AnalysisEngineProcessException aeException = new AnalysisEngineProcessException(runtimeException);
        logger.log(Level.ERROR, message);
      } else if (sentenceList.size() > 1)
      {
        String message = "more than one surrounding sentence found";
        Exception runtimeException = new RuntimeException(message);
        AnalysisEngineProcessException aeException = new AnalysisEngineProcessException(runtimeException);
        logger.log(Level.ERROR, message);
      } else
      {
        sentence = sentenceList.iterator().next();
      }
      //Sentence sentence = sentenceList.iterator().next();
      
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
      
      for (ContextExtractor<BaseToken> extractor : this.tokenContextFeatureExtractors) {
        instance.addAll(extractor.extract(identifiedAnnotationView, entityMention));
      }
      for (SimpleFeatureExtractor extractor : this.entityFeatureExtractors) {
        instance.addAll(extractor.extract(identifiedAnnotationView, entityMention));
      }
      
      if (this.isTraining())
      {
        String polarity = (entityMention.getPolarity() == -1) ? "negated" : "present";
        instance.setOutcome(polarity);
        if ("negated".equals(polarity))
        {
          logger.info("TRAINING: " + polarity);
        }
        this.dataWriter.write(instance);
      } else
      {
        String label = this.classifier.classify(instance.getFeatures());
        int polarity = 1;
        if (label!= null && label.equals("present"))
        {
          polarity = 0;
        } else if (label != null && label.equals("negated"))
        {
          polarity = -1;
        }
        entityMention.setPolarity(polarity);
        if ("negated".equals(label))
        {
          logger.info(String.format("DECODING/EVAL: %s//%s [%d-%d] (%s)", label, polarity, entityMention.getBegin(), entityMention.getEnd(), entityMention.getClass().getName()));
        }
      }
      
    }
    
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
