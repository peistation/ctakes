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

      // now always generating a concept annotation whether or not the
      // conceptType is null (previously, we only generated a concept
      // annotation if the conceptType was not null)
      if (conceptType != null)
      {
        concept.setConceptType(conceptType.toString());
      }
      concept.addToIndexes();

      //logger.info("finished adding new Concept annotation. " + concept);

    }
    //logger.info("    after iterating over named entities.");

    logger.info("end of ConceptConverterAnalysisEngine.process()");
  }

}
