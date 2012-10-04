package org.apache.ctakes.core.ae;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.core.knowtator.KnowtatorAnnotation;
import org.apache.ctakes.core.knowtator.KnowtatorXMLParser;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.BodySide;
import org.apache.ctakes.typesystem.type.refsem.Course;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.Severity;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.jdom2.JDOMException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

public abstract class SHARPKnowtatorXMLReader extends JCasAnnotator_ImplBase {

  public static final String PARAM_KNOWTATOR_XML_DIRECTORY = "knowtatorXMLDirectory";

  @ConfigurationParameter(name = PARAM_KNOWTATOR_XML_DIRECTORY, mandatory = true)
  protected File knowtatorXMLDirectory;

  /**
   * Given the URI of the plain text file, determines the URI of the Knowtator XML file
   */
  protected abstract URI getKnowtatorXML(JCas jCas) throws AnalysisEngineProcessException;

  /**
   * Returns the names of the annotators in the Knowtator files that represent the gold standard
   */
  protected abstract String[] getAnnotatorNames();

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // determine Knowtator XML file from the CAS
    URI knowtatorXML = this.getKnowtatorXML(jCas);

    // parse the Knowtator XML file into annotation objects
    KnowtatorXMLParser parser = new KnowtatorXMLParser(this.getAnnotatorNames());
    Collection<KnowtatorAnnotation> annotations;
    try {
      annotations = parser.parse(knowtatorXML);
    } catch (JDOMException e) {
      throw new AnalysisEngineProcessException(e);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }

    // the relation types
    Set<String> entityRelationTypes = new HashSet<String>();
    entityRelationTypes.add("location_of");
    entityRelationTypes.add("degree_of");
    Set<String> eventRelationTypes = new HashSet<String>();
    eventRelationTypes.add("TLINK");
    eventRelationTypes.add("ALINK");

    // create a CAS object for each annotation
    Map<String, Annotation> idAnnotationMap = new HashMap<String, Annotation>();
    Map<String, TOP> idTopMap = new HashMap<String, TOP>();
    List<DelayedRelation> delayedRelations = new ArrayList<DelayedRelation>();
    List<DelayedFeature<?>> delayedFeatures = new ArrayList<DelayedFeature<?>>();
    for (KnowtatorAnnotation annotation : annotations) {

      // copy the slots so we can remove them as we use them
      Map<String, String> stringSlots = new HashMap<String, String>(annotation.stringSlots);
      Map<String, Boolean> booleanSlots = new HashMap<String, Boolean>(annotation.booleanSlots);
      Map<String, KnowtatorAnnotation> annotationSlots = new HashMap<String, KnowtatorAnnotation>(
          annotation.annotationSlots);
      KnowtatorAnnotation.Span coveringSpan = annotation.getCoveringSpan();

      if ("Anatomical_site".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_ANATOMICAL_SITE,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        if (bodySide != null) {
          delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, bodySide) {
            @Override
            protected void setValue(TOP valueAnnotation) {
              // TODO: this.annotation.setBodySide(...)
            }
          });
        }
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        if (bodyLaterality != null) {
          delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, bodyLaterality) {
            @Override
            protected void setValue(TOP valueAnnotation) {
              // TODO: this.annotation.setBodyLaterality(...)
            }
          });
        }

      } else if ("Disease_Disorder".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_DISORDER,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        if (bodyLocation != null) {
          delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, bodyLocation) {
            @Override
            protected void setValue(TOP valueAnnotation) {
              // TODO: this.annotation.setBodyLocation(...)
            }
          });
        }
        KnowtatorAnnotation severity = annotationSlots.remove("severity");
        if (severity != null) {
          delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, severity) {
            @Override
            protected void setValue(TOP valueAnnotation) {
              // TODO: this.annotation.setSeverity(...)
            }
          });
        }

      } else if ("Medications/Drugs".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_DRUG,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Procedure".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_PROCEDURE,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Sign_symptom".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_FINDING,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        if (bodyLocation != null) {
          delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, bodyLocation) {
            @Override
            protected void setValue(TOP valueAnnotation) {
              // TODO: this.annotation.setBodyLocation(...)
            }
          });
        }

      } else if ("EVENT".equals(annotation.type)) {

        // collect the event properties (setting defaults as necessary)
        EventProperties eventProperties = new EventProperties(jCas);
        eventProperties.setCategory(stringSlots.remove("type"));
        if (eventProperties.getCategory() == null) {
          eventProperties.setCategory("N/A");
        }
        eventProperties.setContextualModality(stringSlots.remove("contextualmoduality"));
        if (eventProperties.getContextualModality() == null) {
          eventProperties.setContextualModality("ACTUAL");
        }
        eventProperties.setContextualAspect(stringSlots.remove("contextualaspect"));
        if (eventProperties.getContextualAspect() == null) {
          eventProperties.setContextualAspect("N/A");
        }
        eventProperties.setDegree(stringSlots.remove("degree"));
        if (eventProperties.getDegree() == null) {
          eventProperties.setDegree("N/A");
        }
        eventProperties.setDocTimeRel(stringSlots.remove("DocTimeRel"));
        if (eventProperties.getDocTimeRel() == null) {
          // TODO: this should not be necessary - DocTimeRel should always be specified
          eventProperties.setDocTimeRel("OVERLAP");
        }
        eventProperties.setPermanence(stringSlots.remove("permanence"));
        if (eventProperties.getPermanence() == null) {
          eventProperties.setPermanence("UNDETERMINED");
        }
        String polarityStr = stringSlots.remove("polarity");
        int polarity;
        if (polarityStr == null || polarityStr.equals("POS")) {
          polarity = CONST.NE_POLARITY_NEGATION_ABSENT;
        } else if (polarityStr.equals("NEG")) {
          polarity = CONST.NE_POLARITY_NEGATION_PRESENT;
        } else {
          throw new IllegalArgumentException("Invalid polarity: " + polarityStr);
        }
        eventProperties.setPolarity(polarity);

        // create the event object
        Event event = new Event(jCas);
        event.setConfidence(1.0f);
        event.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);

        // create the event mention
        EventMention eventMention = new EventMention(jCas, coveringSpan.begin, coveringSpan.end);
        eventMention.setConfidence(1.0f);
        eventMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);

        // add the links between event, mention and properties
        event.setProperties(eventProperties);
        event.setMentions(new FSArray(jCas, 1));
        event.setMentions(0, eventMention);
        eventMention.setEvent(event);

        // add the annotations to the indexes
        eventProperties.addToIndexes();
        event.addToIndexes();
        eventMention.addToIndexes();
        idAnnotationMap.put(annotation.id, eventMention);

      } else if ("DOCTIME".equals(annotation.type)) {
        // TODO

      } else if ("SECTIONTIME".equals(annotation.type)) {
        // TODO

      } else if ("TIMEX3".equals(annotation.type)) {
        String timexClass = stringSlots.remove("class");
        TimeMention timeMention = new TimeMention(jCas, coveringSpan.begin, coveringSpan.end);
        timeMention.addToIndexes();
        idAnnotationMap.put(annotation.id, timeMention);
        // TODO

      } else if ("generic_class".equals(annotation.type)) {
        // TODO: there's currently no Generic in the type system
        boolean value = booleanSlots.remove("generic_normalization");

      } else if ("severity_class".equals(annotation.type)) {
        // TODO: severity has a span, but it extends TOP
        Severity severity = new Severity(jCas);
        severity.setValue(stringSlots.remove("severity_normalization"));
        severity.addToIndexes();
        idTopMap.put(annotation.id, severity);

      } else if ("conditional_class".equals(annotation.type)) {
        // TODO: there's currently no Generic in the type system
        boolean value = booleanSlots.remove("conditional_normalization");

      } else if ("course_class".equals(annotation.type)) {
        // TODO: course has a span, but it extends TOP
        Course course = new Course(jCas);
        course.setValue(stringSlots.remove("course_normalization"));
        course.addToIndexes();
        idTopMap.put(annotation.id, course);

      } else if ("uncertainty_indicator_class".equals(annotation.type)) {
        // TODO: there's currently no Uncertainty in the type system
        String value = stringSlots.remove("uncertainty_indicator_normalization");

      } else if ("distal_or_proximal".equals(annotation.type)) {
        // TODO: there's currently no Distal or Proximal in the type system
        String value = stringSlots.remove("distal_or_proximal_normalization");

      } else if ("Person".equals(annotation.type)) {
        // TODO: there's currently no Subject in the type system
        String value = stringSlots.remove("subject_normalization_CU");

      } else if ("body_side_class".equals(annotation.type)) {
        // TODO: BodySide has a span, but it extends TOP
        BodySide bodySide = new BodySide(jCas);
        bodySide.setValue(stringSlots.remove("body_side_normalization"));
        bodySide.addToIndexes();
        idTopMap.put(annotation.id, bodySide);

      } else if ("negation_indicator_class".equals(annotation.type)) {
        // TODO: there's currently no Negation in the type system
        String value = stringSlots.remove("negation_indicator_normalization");

      } else if (eventRelationTypes.contains(annotation.type)) {
        // store the ALINK information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Event");
        relation.target = annotationSlots.remove("related_to");
        relation.type = stringSlots.remove("Relationtype");
        delayedRelations.add(relation);

      } else if (entityRelationTypes.contains(annotation.type)) {
        // store the relation information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Argument_CU");
        relation.target = annotationSlots.remove("Related_to_CU");
        relation.uncertainty = annotationSlots.remove("uncertainty_indicator_CU");
        delayedRelations.add(relation);

      } else {
        throw new IllegalArgumentException("Unrecognized type: " + annotation.type);
      }

      // make sure all slots have been consumed
      Map<String, Set<String>> slotGroups = new HashMap<String, Set<String>>();
      slotGroups.put("stringSlots", stringSlots.keySet());
      slotGroups.put("booleanSlots", booleanSlots.keySet());
      slotGroups.put("annotationSlots", annotationSlots.keySet());
      for (Map.Entry<String, Set<String>> entry : slotGroups.entrySet()) {
        Set<String> remainingSlots = entry.getValue();
        if (!remainingSlots.isEmpty()) {
          String format = "%s has unprocessed %s: %s";
          String message = String.format(format, annotation.type, entry.getKey(), remainingSlots);
          throw new UnsupportedOperationException(message);
        }
      }
    }

    // add all annotations to the TOP map
    idTopMap.putAll(idAnnotationMap);

    // all mentions should be added, so add features that required other annotations
    for (DelayedFeature<?> delayedFeature : delayedFeatures) {
      delayedFeature.setValueFrom(idAnnotationMap);
    }

    // all mentions should be added, so add relations between annotations
    for (DelayedRelation delayedRelation : delayedRelations) {
      delayedRelation.addToIndexes(jCas, idAnnotationMap);
    }
  }

  private static void addEntityMentionFeatures(
      KnowtatorAnnotation annotation,
      EntityMention entityMention,
      JCas jCas,
      int typeID,
      Map<String, String> stringSlots,
      Map<String, Boolean> booleanSlots,
      Map<String, KnowtatorAnnotation> annotationSlots,
      Map<String, Annotation> idAnnotationMap,
      List<DelayedFeature<?>> delayedFeatures) {
    entityMention.setTypeID(typeID);
    entityMention.setConfidence(1.0f);
    entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);

    // convert negation to an integer
    Boolean negation = booleanSlots.remove("Negation");
    entityMention.setPolarity(negation == null
        ? CONST.NE_POLARITY_NEGATION_ABSENT
        : negation == true ? CONST.NE_POLARITY_NEGATION_PRESENT : CONST.NE_POLARITY_NEGATION_ABSENT);

    // negation must be delayed until the Negation annotations are present
    KnowtatorAnnotation negationIndicator = annotationSlots.remove("negation_indicator_CU");
    if (negationIndicator != null) {
      delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, negationIndicator) {
        @Override
        protected void setValue(TOP valueAnnotation) {
          // TODO: this.annotation.setPolarity(...)
        }
      });
    }

    // conditional must be delayed until the Conditional annotations are present
    KnowtatorAnnotation conditional = annotationSlots.remove("conditional_CU");
    if (conditional != null) {
      delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, conditional) {
        @Override
        protected void setValue(TOP valueAnnotation) {
          // TODO: this.annotation.setConditional(...)
        }
      });
    }

    // subject must be delayed until the Subject annotations are present
    KnowtatorAnnotation subject = annotationSlots.remove("subject_CU");
    if (subject != null) {
      delayedFeatures.add(new DelayedFeature<EntityMention>(entityMention, subject) {
        @Override
        protected void setValue(TOP valueAnnotation) {
          // TODO: this.annotation.setSubject(...)
        }
      });
    }

    // convert status as necessary
    String status = stringSlots.remove("Status");
    if (status != null) {
      if ("HistoryOf".equals(status)) {
        // TODO
      } else if ("FamilyHistoryOf".equals(status)) {
        // TODO
      } else if ("Possible".equals(status)) {
        // TODO
      } else {
        throw new UnsupportedOperationException("Unknown status: " + status);
      }
    }

    // convert code to ontology concept or CUI
    String code = stringSlots.remove("AssociateCode");
    if (code == null) {
      code = stringSlots.remove("associatedCode");
    }
    OntologyConcept ontologyConcept;
    if (entityMention.getTypeID() == CONST.NE_TYPE_ID_DRUG) {
      ontologyConcept = new OntologyConcept(jCas);
      ontologyConcept.setCode(code);
    } else {
      UmlsConcept umlsConcept = new UmlsConcept(jCas);
      umlsConcept.setCui(code);
      ontologyConcept = umlsConcept;
    }
    ontologyConcept.addToIndexes();
    entityMention.setOntologyConceptArr(new FSArray(jCas, 1));
    entityMention.setOntologyConceptArr(0, ontologyConcept);

    // add entity mention to CAS
    entityMention.addToIndexes();
    idAnnotationMap.put(annotation.id, entityMention);
  }

  private static class DelayedRelation {
    public KnowtatorAnnotation annotation;

    public KnowtatorAnnotation source;

    public KnowtatorAnnotation target;

    public String type;

    public KnowtatorAnnotation uncertainty;

    public void addToIndexes(JCas jCas, Map<String, Annotation> idAnnotationMap) {
      // look up the relations in the map and issue an error if they're missing
      Annotation sourceMention = idAnnotationMap.get(this.source.id);
      Annotation targetMention = idAnnotationMap.get(this.target.id);
      String badId = null;
      if (sourceMention == null) {
        badId = this.source.id;
      } else if (targetMention == null) {
        badId = this.target.id;
      }
      if (badId != null) {
        String message = String.format("no annotation with id '%s'", badId);
        throw new UnsupportedOperationException(message);
      }

      // get the uncertainty
      if (this.uncertainty != null) {
        Annotation uncertainty = idAnnotationMap.get(this.uncertainty);
        System.err.println(uncertainty.getCoveredText());
      }

      // add the relation to the CAS
      RelationArgument sourceRA = new RelationArgument(jCas);
      sourceRA.setArgument(sourceMention);
      sourceRA.addToIndexes();
      RelationArgument targetRA = new RelationArgument(jCas);
      targetRA.setArgument(targetMention);
      targetRA.addToIndexes();
      BinaryTextRelation relation = new BinaryTextRelation(jCas);
      if (this.type != null) {
        // TODO: do something better with knowtatorRelation.annotation.type
        relation.setCategory(this.annotation.type + '_' + this.type);
      } else {
        relation.setCategory(this.annotation.type);
      }
      relation.setArg1(sourceRA);
      relation.setArg2(targetRA);
      relation.addToIndexes();
    }
  }

  private static abstract class DelayedFeature<ANNOTATION_TYPE extends TOP> {
    protected ANNOTATION_TYPE annotation;

    private String featureValueID;

    public DelayedFeature(ANNOTATION_TYPE annotation, KnowtatorAnnotation featureValue) {
      this.annotation = annotation;
      this.featureValueID = featureValue.id;
    }

    public void setValueFrom(Map<String, ? extends TOP> idAnnotationMap) {
      this.setValue(idAnnotationMap.get(this.featureValueID));
    }

    protected abstract void setValue(TOP valueAnnotation);
  }
}
