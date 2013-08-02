package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import org.apache.ctakes.core.ae.DocumentIdPrinterAnalysisEngine;
import org.apache.ctakes.core.cr.XMIReader;
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.temporary.assertion.AssertionCuePhraseAnnotation;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.JCasFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

public class CueWordTest2
{
  Logger logger = Logger.getLogger(CueWordTest2.class.getName());

  /**
   * @param args
   * @throws IOException 
   * @throws UIMAException 
   */
  public static void main(String[] args) throws UIMAException, IOException
  {
    CueWordTest2 t = new CueWordTest2();
    t.execute();
  }
  
  public void execute() throws UIMAException, IOException
  {
    logger.info("starting");

    AggregateBuilder builder = new AggregateBuilder();
    
    TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription();
    String filename = "/work/medfacts/sharp/data/2013-01-11_cue_phrase_feature_test/ON03FP00037D00207__merged.txt.xmi";
    
    JCas jcas = JCasFactory.createJCas(filename, typeSystemDescription);
    
    logger.info("=====");

    Collection<BaseToken> tokens = JCasUtil.select(jcas,  BaseToken.class);
    for (BaseToken currentToken : tokens)
    {
      logger.info(String.format("token \"%s\" [%s]", currentToken.getCoveredText(), currentToken.getClass().getName()));
    }
    
    logger.info("=====");

    Map<IdentifiedAnnotation, Collection<Sentence>> entityToSentenceMap =
        JCasUtil.indexCovering(jcas, IdentifiedAnnotation.class, Sentence.class);

    Map<Sentence, Collection<AssertionCuePhraseAnnotation>>
      sentenceToCoveredCuePhraseMap =
        JCasUtil.indexCovered(jcas, Sentence.class, AssertionCuePhraseAnnotation.class);
    
    CombinedExtractor baseExtractorCuePhraseCategory =
        new CombinedExtractor
          (
           new CoveredTextExtractor(),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhrase"),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhraseCategory"),
           new TypePathExtractor(AssertionCuePhraseAnnotation.class, "cuePhraseAssertionFamily")
          );
    
    CleartkExtractor cuePhraseInWindowExtractor =
        new CleartkExtractor(
              AssertionCuePhraseAnnotation.class,
              baseExtractorCuePhraseCategory,
              new CleartkExtractor.Bag(new CleartkExtractor.Preceding(5)),
              new CleartkExtractor.Bag(new CleartkExtractor.Following(5))
              );
//              new CleartkExtractor.Ngram(new CleartkExtractor.Preceding(5), new CleartkExtractor.Following(5)));
              //new CoveredTextExtractor(),
//              new CleartkExtractor.Covered());
//              new CleartkExtractor.Preceding(5),
//              new CleartkExtractor.Following(5));
    
    CleartkExtractor tokenExtraction1 = 
        new CleartkExtractor(
            BaseToken.class, 
            new CoveredTextExtractor(),
            //new CleartkExtractor.Covered(),
            new CleartkExtractor.LastCovered(2),
            new CleartkExtractor.Preceding(5),
            new CleartkExtractor.Following(4),
            new CleartkExtractor.Bag(new CleartkExtractor.Preceding(10)),
            new CleartkExtractor.Bag(new CleartkExtractor.Following(10))
            );
    
    CleartkExtractor posExtraction1 = 
        new CleartkExtractor(
            BaseToken.class,
            new TypePathExtractor(BaseToken.class, "partOfSpeech"),
            new CleartkExtractor.LastCovered(2),
            new CleartkExtractor.Preceding(3),
            new CleartkExtractor.Following(2)
            );
    
    
    
    List<CleartkExtractor> extractorList = new ArrayList<CleartkExtractor>();
    extractorList.add(cuePhraseInWindowExtractor);
    
    //NamingExtractor cuePhraseInWindowNamingExtractor = new NamingExtractor("cuePhraseCategory__", cuePhraseInWindowExtractor); 

    Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jcas,  IdentifiedAnnotation.class);
    for (IdentifiedAnnotation current : identifiedAnnotations)
    {
      if (!(current instanceof EntityMention) && !(current instanceof EventMention)) continue;
      
      // otherwise current is an entity or event mention...
      logger.info(String.format("identified annotation (event or entity) [%d-%d] \"%s\" [%s]", current.getBegin(), current.getEnd(), current.getCoveredText(), current.getClass().getName()));
      
      Collection<Sentence> coveringSentences = entityToSentenceMap.get(current);
      if (coveringSentences == null || coveringSentences.isEmpty())
      {
        logger.info("no covering sentences found!!! continuing with next entity/event...");
        continue;
      }
      logger.info(String.format("covering sentence count: %d", coveringSentences.size()));
      Sentence firstCoveringSentence = coveringSentences.iterator().next();
      
      logger.info(String.format(
          "first covering sentence: [%d-%d] \"%s\" (%s)", 
          firstCoveringSentence.getBegin(), firstCoveringSentence.getEnd(),
          firstCoveringSentence.getCoveredText(),
          firstCoveringSentence.getClass().getName()));
      
      List<Feature> cuePhraseFeatures =
          //cuePhraseInSentenceExtractor.extract(jcas, firstCoveringSentence);
          cuePhraseInWindowExtractor.extractWithin(jcas, current, firstCoveringSentence);
          //cuePhraseInWindowNamingExtractor.extract(jcas, current);
      if (cuePhraseFeatures != null && !cuePhraseFeatures.isEmpty())
      {
        String featureDebugString = (cuePhraseFeatures == null) ? "(no cue phrase features)" : cuePhraseFeatures.toString();
        logger.info("### cue phrase features: " + featureDebugString);
      }

    
      List<Feature> tokenFeatures =
          tokenExtraction1.extractWithin(jcas, current, firstCoveringSentence);
      if (tokenFeatures != null && !tokenFeatures.isEmpty())
      {
        String featureDebugString = (tokenFeatures == null) ? "(no token phrase features)" : tokenFeatures.toString();
        logger.info("### token phrase features: " + featureDebugString);
      }

    }
    
    logger.info("=====");
     
    logger.info("finished");
  }

}
