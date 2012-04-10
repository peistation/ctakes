package org.mitre.medfacts.uima.assertion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceAccessException;
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.i2b2.api.AssertionDecoderConfiguration;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.types.Assertion;
import org.mitre.medfacts.types.Concept;
import org.mitre.medfacts.types.Concept_Type;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
import edu.mayo.bmi.uima.core.type.refsem.UmlsConcept;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class ConceptConverterAnalysisEngine extends JCasAnnotator_ImplBase
{
  public static final Logger logger = Logger
      .getLogger(ConceptConverterAnalysisEngine.class.getName());

  public ConceptConverterAnalysisEngine()
  {
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException
  {
    logger.info("beginning of ConceptConverterAnalysisEngine.process()");
    String contents = jcas.getDocumentText();

    int umlsConceptType = UmlsConcept.type;
    // AnnotationIndex<Annotation> conceptAnnotationIndex =
    // jcas.getAnnotationIndex(umlsConceptType);

    Map<Integer, UmlsConcept> idToConceptMap = new HashMap<Integer, UmlsConcept>();
    Map<Integer, Set<Integer>> idToConceptMapForEntity = new HashMap<Integer, Set<Integer>>();
    Map<Integer, Set<EntityMention>> idToNamedEntityMap = new HashMap<Integer, Set<EntityMention>>();

    int entityMentionType = EntityMention.type;
    AnnotationIndex<Annotation> entityMentionAnnotationIndex = jcas
        .getAnnotationIndex(entityMentionType);

    int totalAnnotationCount = jcas.getAnnotationIndex().size();
    int entityMentionAnnotationCount = entityMentionAnnotationIndex.size();

    logger.info(String.format("    total annotation count %d",
        totalAnnotationCount));
    logger.info(String.format("    named entity annotation count %d",
        entityMentionAnnotationCount));

    //logger.info("    before iterating over named entities...");
    for (FeatureStructure featureStructure : entityMentionAnnotationIndex)
    {
      //logger.info("    begin single named entity");
      EntityMention entityMentionAnnotation = (EntityMention) featureStructure;

      int begin = entityMentionAnnotation.getBegin();
      int end = entityMentionAnnotation.getEnd();
      String conceptText = entityMentionAnnotation.getCoveredText();

      //logger.info(String.format("NAMED ENTITY: \"%s\" [%d-%d]", conceptText,
      //    begin, end));

      Concept concept = new Concept(jcas, begin, end);
      concept.setConceptText(conceptText);
      concept.setConceptType(null);

      concept.setOriginalEntityExternalId(entityMentionAnnotation.getAddress());

      FSArray ontologyConceptArray = entityMentionAnnotation
          .getOntologyConceptArr();

      ConceptType conceptType = ConceptLookup
          .lookupConceptType(ontologyConceptArray);

      //logger.info(String.format("got concept type: %s", conceptType));

      // if (conceptType != null)
      // {
      // concept.setConceptType(conceptType.toString());
      // concept.addToIndexes();
      // }

      // now always generating a concept annotation whether or not the
      // conceptType is null (previously, we only generated a concept
      // annotation if the conceptType was not null)
      if (conceptType != null)
      {
        concept.setConceptType(conceptType.toString());
      }
      concept.addToIndexes();

      //logger.info("finished adding new Concept annotation. " + concept);

      // FSArray conceptArray = namedEntityAnnotation.getOntologyConceptArr();
      //
      // logger.info("        before iterating over concepts...");
      // for (FeatureStructure currentConceptArrayItem : conceptArray.toArray())
      // {
      // logger.info("        begin single concept");
      // UmlsConcept umlsConcept = (UmlsConcept)currentConceptArrayItem;
      //
      // String codingScheme = umlsConcept.getCodingScheme();
      // String cui = umlsConcept.getCui();
      // String tui = umlsConcept.getTui();
      //
      // Concept concept = new Concept(jcas, begin, end);
      // concept.setConceptText(conceptText);
      // concept.setConceptType(null);
      //
      // concept.addToIndexes();
      // logger.info("        end single concept");
      // }
      // logger.info("        after iterating over concepts.");
      //logger.info("    end single named entity");
    }
    //logger.info("    after iterating over named entities.");

    // ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
    // for (FeatureStructure featureStructure : conceptAnnotationIndex)
    // {
    // UmlsConcept umlsConceptAnnotation = (UmlsConcept)featureStructure;
    //
    // ApiConcept apiConcept = new ApiConcept();
    // int umlsConceptId = umlsConceptAnnotation.getAddress();
    //
    // int begin = 0; //umlsConceptAnnotation.getBegin();
    // int end = 0; //umlsConceptAnnotation.getEnd();
    // String conceptText = contents.substring(begin, begin + end + 1);
    //
    // apiConcept.setBegin(begin);
    // apiConcept.setEnd(end);
    // apiConcept.setText(conceptText);
    // apiConcept.setType(null);
    // //apiConcept.setType(umlsConceptAnnotation.getConceptType());
    //
    // apiConceptList.add(apiConcept);
    // }

    /*
     * String assertionModelContents; String scopeModelFilePath; String
     * cueModelFilePath; File enabledFeaturesFile;
     * 
     * try { String assertionModelResourceKey = "assertionModelResource"; String
     * assertionModelFilePath = getContext().getResourceFilePath(
     * assertionModelResourceKey); File assertionModelFile = new
     * File(assertionModelFilePath); assertionModelContents = StringHandling
     * .readEntireContents(assertionModelFile); String scopeModelResourceKey =
     * "scopeModelResource"; scopeModelFilePath =
     * getContext().getResourceFilePath( scopeModelResourceKey); String
     * cueModelResourceKey = "cueModelResource"; cueModelFilePath =
     * getContext().getResourceFilePath( cueModelResourceKey); String
     * enabledFeaturesResourceKey = "enabledFeaturesResource"; String
     * enabledFeaturesFilePath = getContext().getResourceFilePath(
     * enabledFeaturesResourceKey); enabledFeaturesFile = new
     * File(enabledFeaturesFilePath); } catch (ResourceAccessException e) {
     * String message = String.format("problem accessing resource"); throw new
     * RuntimeException(message, e); }
     * 
     * // String conceptFilePath =
     * currentTextFile.getAbsolutePath().replaceFirst("\\.txt$", ".con"); //
     * File conceptFile = new File(conceptFilePath); //
     * logger.info(String.format("    - using concept file \"%s\"...",
     * conceptFile.getName())); // String conceptFileContents =
     * StringHandling.readEntireContents(conceptFile); // //List<Concept>
     * parseConceptFileContents(conceptFileContents); // //
     * LineTokenToCharacterOffsetConverter converter = // new
     * LineTokenToCharacterOffsetConverter(contents); // // List<ApiConcept>
     * apiConceptList = parseConceptFile(conceptFile, contents, converter);
     * 
     * LineTokenToCharacterOffsetConverter converter = new
     * LineTokenToCharacterOffsetConverter(contents);
     * 
     * AssertionDecoderConfiguration assertionDecoderConfiguration = new
     * AssertionDecoderConfiguration();
     * 
     * ScopeParser scopeParser = new ScopeParser(scopeModelFilePath,
     * cueModelFilePath);
     * assertionDecoderConfiguration.setScopeParser(scopeParser);
     * 
     * Set<String> enabledFeatureIdSet = null; enabledFeatureIdSet =
     * BatchRunner.loadEnabledFeaturesFromFile(enabledFeaturesFile);
     * assertionDecoderConfiguration
     * .setEnabledFeatureIdSet(enabledFeatureIdSet);
     * 
     * JarafeMEDecoder assertionDecoder = null; assertionDecoder = new
     * JarafeMEDecoder(assertionModelContents);
     * assertionDecoderConfiguration.setAssertionDecoder(assertionDecoder);
     * 
     * SingleDocumentProcessor p = new SingleDocumentProcessor(converter);
     * p.setAssertionDecoderConfiguration(assertionDecoderConfiguration);
     * p.setContents(contents); for (ApiConcept apiConcept : apiConceptList) {
     * logger.info(String.format("dir loader concept: %s",
     * apiConcept.toString())); p.addConcept(apiConcept); }
     * p.processSingleDocument(); Map<Integer, String> assertionTypeMap =
     * p.getAssertionTypeMap();
     * logger.info(String.format("    - done processing \"%s\"."));
     * 
     * for (Entry<Integer, String> current : assertionTypeMap.entrySet()) {
     * String currentAssertionType = current.getValue(); Integer currentIndex =
     * current.getKey(); ApiConcept originalConcept =
     * apiConceptList.get(currentIndex);
     * 
     * Assertion assertion = new Assertion(jcas, originalConcept.getBegin(),
     * originalConcept.getEnd());
     * assertion.setAssertionType(currentAssertionType);
     * assertion.addToIndexes(); }
     */

    logger.info("end of ConceptConverterAnalysisEngine.process()");
  }

}
