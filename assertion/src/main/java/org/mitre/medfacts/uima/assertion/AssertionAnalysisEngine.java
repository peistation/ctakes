package org.mitre.medfacts.uima.assertion;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.Annotation_Type;
import org.apache.uima.resource.ResourceAccessException;
//import org.jfree.util.Log;
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.PartOfSpeechTagger;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.i2b2.api.AssertionDecoderConfiguration;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.i2b2.api.ctakes.CharacterOffsetToLineTokenConverterCtakesImpl;
import org.mitre.medfacts.i2b2.api.ctakes.SingleDocumentProcessorCtakes;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.types.Assertion;
import org.mitre.medfacts.types.Concept;
import org.mitre.medfacts.types.Concept_Type;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class AssertionAnalysisEngine extends JCasAnnotator_ImplBase
{
  Logger logger = Logger.getLogger(AssertionAnalysisEngine.class.getName());

  public AssertionAnalysisEngine()
  {
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException
  {
    logger.info("(logging statement) AssertionAnalysisEngine.process() BEGIN");
    System.out.println("(stdout) AssertionAnalysisEngine.process() BEGIN");
    String contents = jcas.getDocumentText();

    // String tokenizedContents = tokenizeCasDocumentText(jcas);

    int conceptType = Concept.type;
    AnnotationIndex<Annotation> conceptAnnotationIndex = jcas
        .getAnnotationIndex(conceptType);

    ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
    for (Annotation annotation : conceptAnnotationIndex)
    {
      Concept conceptAnnotation = (Concept) annotation;

      ApiConcept apiConcept = new ApiConcept();
      int begin = conceptAnnotation.getBegin();
      int end = conceptAnnotation.getEnd();
      String conceptText = contents.substring(begin, end);

      apiConcept.setBegin(begin);
      apiConcept.setEnd(end);
      apiConcept.setText(conceptText);
      apiConcept.setType(conceptAnnotation.getConceptType());
      apiConcept.setExternalId(conceptAnnotation.getAddress());

      apiConceptList.add(apiConcept);
    }

    // byte assertionModelContents[];
    String scopeModelFilePath;
    String cueModelFilePath;
    String posModelFilePath;
    File enabledFeaturesFile;

    File assertionModelFile = null;
    try
    {
      String assertionModelResourceKey = "assertionModelResource";
      String assertionModelFilePath = getContext().getResourceFilePath(
          assertionModelResourceKey);
      assertionModelFile = new File(assertionModelFilePath);
      // assertionModelContents = StringHandling
      // .readEntireContentsBinary(assertionModelFile);
      String scopeModelResourceKey = "scopeModelResource";
      scopeModelFilePath = getContext().getResourceFilePath(
          scopeModelResourceKey);
      String cueModelResourceKey = "cueModelResource";
      cueModelFilePath = getContext().getResourceFilePath(cueModelResourceKey);

      String posModelResourceKey = "posModelResource";
      posModelFilePath = getContext().getResourceFilePath(posModelResourceKey);

      String enabledFeaturesResourceKey = "enabledFeaturesResource";
      String enabledFeaturesFilePath = getContext().getResourceFilePath(
          enabledFeaturesResourceKey);
      enabledFeaturesFile = new File(enabledFeaturesFilePath);
    } catch (ResourceAccessException e)
    {
      String message = String.format("problem accessing resource");
      throw new RuntimeException(message, e);
    }

    // String conceptFilePath =
    // currentTextFile.getAbsolutePath().replaceFirst("\\.txt$", ".con");
    // File conceptFile = new File(conceptFilePath);
    // logger.info(String.format("    - using concept file \"%s\"...",
    // conceptFile.getName()));
    // String conceptFileContents =
    // StringHandling.readEntireContents(conceptFile);
    // //List<Concept> parseConceptFileContents(conceptFileContents);
    //
    // LineTokenToCharacterOffsetConverter converter =
    // new LineTokenToCharacterOffsetConverter(contents);
    //
    // List<ApiConcept> apiConceptList = parseConceptFile(conceptFile, contents,
    // converter);

    // LineTokenToCharacterOffsetConverter converter =
    // new LineTokenToCharacterOffsetConverter(contents);

    AssertionDecoderConfiguration assertionDecoderConfiguration = new AssertionDecoderConfiguration();

    logger.info(String.format("scope model file: %s", scopeModelFilePath));
    logger.info(String.format("cue model file: %s", cueModelFilePath));
    ScopeParser scopeParser = new ScopeParser(scopeModelFilePath,
        cueModelFilePath);
    assertionDecoderConfiguration.setScopeParser(scopeParser);

    logger.info(String.format("pos model file: %s", posModelFilePath));
    PartOfSpeechTagger posTagger = new PartOfSpeechTagger(posModelFilePath);
    assertionDecoderConfiguration.setPosTagger(posTagger);

    Set<String> enabledFeatureIdSet = null;
    enabledFeatureIdSet = BatchRunner
        .loadEnabledFeaturesFromFile(enabledFeaturesFile);
    assertionDecoderConfiguration.setEnabledFeatureIdSet(enabledFeatureIdSet);

    JarafeMEDecoder assertionDecoder = null;
    assertionDecoder = new JarafeMEDecoder(assertionModelFile);
    assertionDecoderConfiguration.setAssertionDecoder(assertionDecoder);

    // SingleDocumentProcessor p = new SingleDocumentProcessor();
    SingleDocumentProcessorCtakes p = new SingleDocumentProcessorCtakes();
    p.setJcas(jcas);
    p.setAssertionDecoderConfiguration(assertionDecoderConfiguration);
    // p.setContents(tokenizedContents);
    p.setContents(contents);
    CharacterOffsetToLineTokenConverter converter = new CharacterOffsetToLineTokenConverterCtakesImpl(
        jcas);
    p.setConverter2(converter);
    for (ApiConcept apiConcept : apiConceptList)
    {
      logger
          .info(String.format("dir loader concept: %s", apiConcept.toString()));
      p.addConcept(apiConcept);
    }

    logger
        .info("(logging statement) AssertionAnalysisEngine.process() BEFORE CALLING p.processSingleDocument()");

    p.processSingleDocument();

    logger
        .info("(logging statement) AssertionAnalysisEngine.process() AFTER CALLING p.processSingleDocument()");

    Map<Integer, String> assertionTypeMap = p.getAssertionTypeMap();
    logger.info(String.format("    - done processing ..\"."));

    // Map<Integer, Annotation> annotationMap = generateAnnotationMap(jcas,
    // Concept.type);
    CasIndexer<Annotation> indexer = new CasIndexer<Annotation>(jcas, null);

    logger.info("assertionTypeMap loop OUTSIDE BEFORE...");
    for (Entry<Integer, String> current : assertionTypeMap.entrySet())
    {
      logger.info("    assertionTypeMap loop INSIDE BEGIN");
      String currentAssertionType = current.getValue();
      logger.info(String.format("  currentAssertionType: %s",
          currentAssertionType));
      Integer currentIndex = current.getKey();
      ApiConcept originalConcept = apiConceptList.get(currentIndex);

      Concept associatedConcept = (Concept) indexer
          .lookupByAddress(originalConcept.getExternalId());
      int entityAddress = associatedConcept.getOriginalEntityExternalId();
      EntityMention entityMention = (EntityMention) indexer
          .lookupByAddress(entityAddress);

      // possible values for currentAssertionType:
      // present
      // absent
      // associated_with_someone_else
      // conditional
      // hypothetical
      // possible

//      logger.info(String.format("removed entityMention (%s) from indexes",
//          entityMention.toString()));
//      entityMention.removeFromIndexes();
      if (currentAssertionType == null)
      {
        String message = "current assertion type is null; this is a problem!!";
        System.err.println(message);
        logger.severe(message);
        // Exception runtimeException = new RuntimeException(message);
        // throw new AnalysisEngineProcessException(runtimeException);
      
        // ALL OBVIOUS ERROR VALUES!!
        entityMention.setSubject("skipped");
        entityMention.setPolarity(-2);
        entityMention.setConfidence(-2.0f);
        entityMention.setUncertainty(-2);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);

      } else if (currentAssertionType.equals("present"))
      // PRESENT (mastif value)
      {
        debugAnnotationsInCas(jcas, entityMention, "=== BEFORE setting entity mention properties (PRESENT)... ===");
        // ALL DEFAULT VALUES!! (since this is present)
        entityMention.setSubject("patient");
        entityMention.setPolarity(1);
        entityMention.setConfidence(1.0f);
        entityMention.setUncertainty(0);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);

        debugAnnotationsInCas(jcas, entityMention, "=== AFTER setting entity mention properties (PRESENT)... ===");
      } else if (currentAssertionType.equals("absent"))
      // ABSENT (mastif value)
      {
        entityMention.setSubject("patient");
        entityMention.setPolarity(-1); // NOT DEFAULT VALUE
        entityMention.setConfidence(1.0f);
        entityMention.setUncertainty(0);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);

      } else if (currentAssertionType.equals("associated_with_someone_else"))
      // ASSOCIATED WITH SOMEONE ELSE (mastif value)
      {
        entityMention.setSubject("other"); // NOT DEFAULT VALUE
        entityMention.setPolarity(1);
        entityMention.setConfidence(1.0f);
        entityMention.setUncertainty(0);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);

      } else if (currentAssertionType.equals("conditional"))
      // CONDITIONAL (mastif value)
      {
        entityMention.setSubject("patient");
        entityMention.setPolarity(1);
        entityMention.setConfidence(1.0f);
        entityMention.setUncertainty(0);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);

      } else if (currentAssertionType.equals("hypothetical"))
      // HYPOTHETICAL (mastif value)
      {
        entityMention.setSubject("patient");
        entityMention.setPolarity(1);
        entityMention.setConfidence(1.0f);
        entityMention.setUncertainty(0);
        entityMention.setConditional(true); // NOT DEFAULT VALUE
        entityMention.setGeneric(false);

      } else if (currentAssertionType.equals("possible"))
      // POSSIBLE (mastif value)
      {
        entityMention.setSubject("patient");
        entityMention.setPolarity(1);
        entityMention.setConfidence(0.5f); // NOT DEFAULT VALUE
        entityMention.setUncertainty(0);
        entityMention.setConditional(false);
        entityMention.setGeneric(false);
      } else
      {
        String message = String.format(
            "unexpected assertion value returned!! \"%s\"",
            currentAssertionType);
        logger.severe(message);
        System.err.println(message);
        Exception runtimeException = new RuntimeException(message);
        throw new AnalysisEngineProcessException(runtimeException);
      }
//      entityMention.addToIndexes();
//      logger.info(String.format("added back entityMention (%s) to indexes",
//          entityMention.toString()));

      // Assertion assertion = new Assertion(jcas, originalConcept.getBegin(),
      // originalConcept.getEnd());
      // assertion.setAssertionType(currentAssertionType);
      // Concept associatedConcept = (Concept)
      // annotationMap.get(originalConcept.getExternalId());
      // assertion.setAssociatedConcept(associatedConcept);
      // assertion.addToIndexes();

      logger.info("    assertionTypeMap loop INSIDE END");
    }
    logger.info("assertionTypeMap loop OUTSIDE AFTER!!");
    logger.info("(logging statement) AssertionAnalysisEngine.process() END");
  }

  public void debugAnnotationsInCas(JCas jcas, EntityMention entityMention,
      String label)
  {
    CasIndexer<EntityMention> i = new CasIndexer<EntityMention>(jcas, entityMention.getType());
    
    StringBuilder b = new StringBuilder();
    b.append(String.format("<<<<<%n### TARGET ###%nclass: %s%naddress: %d%nvalue: %s%n### END TARGET ###%n>>>>>%n%n", entityMention.getClass().getName(), entityMention.getAddress(), entityMention.toString()));
    
    String debugOutput = i.convertToDebugOutput(label, entityMention);
    
    b.append(debugOutput);
    
    logger.info(b.toString());
    
  }

  public Map<Integer, Annotation> generateAnnotationMap(JCas jcas)
  {
    return generateAnnotationMap(jcas, null);
  }

  public Map<Integer, Annotation> generateAnnotationMap(JCas jcas,
      Integer typeId)
  {
    Map<Integer, Annotation> annotationMap = new HashMap<Integer, Annotation>();

    AnnotationIndex<Annotation> index = null;
    if (typeId == null)
    {
      index = jcas.getAnnotationIndex();
    } else
    {
      index = jcas.getAnnotationIndex(typeId);
    }
    FSIterator<Annotation> iterator = index.iterator();
    while (iterator.hasNext())
    {
      Annotation current = iterator.next();
      int address = current.getAddress();
      annotationMap.put(address, current);
    }

    return annotationMap;
  }

  // public String tokenizeCasDocumentText(JCas jcas)
  // {
  // ArrayList<ArrayList<String>> arrayOfLines = construct2DTokenArray(jcas);
  //
  // String spaceSeparatedTokensInput = convert2DTokenArrayToText(arrayOfLines);
  //
  // return spaceSeparatedTokensInput;
  // }
  //
  // public ArrayList<ArrayList<String>> construct2DTokenArray(JCas jcas)
  // {
  // int sentenceType = Sentence.type;
  // AnnotationIndex<Annotation> sentenceAnnotationIndex =
  // jcas.getAnnotationIndex(sentenceType);
  // ArrayList<ArrayList<String>> arrayOfLines = new
  // ArrayList<ArrayList<String>>();
  //
  // //ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
  // for (Annotation annotation : sentenceAnnotationIndex)
  // {
  // Sentence sentence = (Sentence)annotation;
  // int sentenceBegin = sentence.getBegin();
  // int sentenceEnd = sentence.getEnd();
  //
  // AnnotationIndex<Annotation> tokenAnnotationIndex =
  // jcas.getAnnotationIndex(BaseToken.type);
  // ArrayList<String> arrayOfTokens = new ArrayList<String>();
  // for (Annotation baseTokenAnnotationUntyped : tokenAnnotationIndex)
  // {
  // // ignore tokens that are outside of the sentence.
  // // there has to be a better way to do this with Constraints, but this
  // // should work for now...
  // if (baseTokenAnnotationUntyped.getBegin() < sentenceBegin ||
  // baseTokenAnnotationUntyped.getEnd() > sentenceEnd)
  // {
  // continue;
  // }
  // BaseToken baseToken = (BaseToken)baseTokenAnnotationUntyped;
  // if (baseToken instanceof WordToken ||
  // baseToken instanceof PunctuationToken)
  // {
  // String currentTokenText = baseToken.getCoveredText();
  // arrayOfTokens.add(currentTokenText);
  // }
  // }
  // arrayOfLines.add(arrayOfTokens);
  //
  // }
  // return arrayOfLines;
  // }
  //
  public String convert2DTokenArrayToText(
      ArrayList<ArrayList<String>> arrayOfLines)
  {
    final String DELIM = " ";
    StringWriter writer = new StringWriter();
    PrintWriter printer = new PrintWriter(writer);

    boolean isFirstLine = true;
    for (ArrayList<String> line : arrayOfLines)
    {
      if (!isFirstLine)
      {
        printer.println();
      }

      boolean isFirstTokenOnLine = true;
      for (String currentToken : line)
      {
        if (!isFirstTokenOnLine)
        {
          printer.print(DELIM);
        }
        printer.print(currentToken);
        isFirstTokenOnLine = false;
      }

      isFirstLine = false;
    }

    printer.close();

    String output = writer.toString();
    return output;
  }

}
