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
package org.apache.ctakes.core.ae;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.refsem.MedicationDosage;
import org.apache.ctakes.typesystem.type.refsem.MedicationDuration;
import org.apache.ctakes.typesystem.type.refsem.MedicationForm;
import org.apache.ctakes.typesystem.type.refsem.MedicationFrequency;
import org.apache.ctakes.typesystem.type.refsem.MedicationRoute;
import org.apache.ctakes.typesystem.type.refsem.MedicationStatusChange;
import org.apache.ctakes.typesystem.type.refsem.MedicationStrength;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.ProcedureMethod;
import org.apache.ctakes.typesystem.type.refsem.Severity;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.jdom2.JDOMException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class SHARPKnowtatorXMLReader extends JCasAnnotator_ImplBase {
  static Logger LOGGER = Logger.getLogger(SHARPKnowtatorXMLReader.class);
  
  private static String getJCasURIString(JCas jCas) {
    return JCasUtil.selectSingle(jCas, DocumentID.class).getDocumentID();
  }
  
  /**
   * Given the URI of the plain text file, determines the URI of the Knowtator XML file
   */
  protected URI getKnowtatorXML(JCas jCas) throws AnalysisEngineProcessException {
    String textURI = getJCasURIString(jCas);
    String xmlURI = textURI.replaceAll("Knowtator/text", "Knowtator_XML") + ".knowtator.xml";
    try {
      return new URI(xmlURI);
    } catch (URISyntaxException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /**
   * Returns the names of the annotators in the Knowtator files that represent the gold standard
   */
  protected String[] getAnnotatorNames() {
    return new String[] { "consensus set annotator team" };
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // determine Knowtator XML file from the CAS
    String uriString = getJCasURIString(jCas);
    LOGGER.info("processing " + uriString);
    URI knowtatorXML = this.getKnowtatorXML(jCas);
    if (!new File(knowtatorXML).exists()) {
      LOGGER.fatal("no such Knowtator XML file " + knowtatorXML);
      return;
    }

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
    entityRelationTypes.add("affects");
    entityRelationTypes.add("causes/brings_about");
    entityRelationTypes.add("complicates/disrupts");
    entityRelationTypes.add("contraindicates");
    entityRelationTypes.add("degree_of");
    entityRelationTypes.add("diagnoses");
    entityRelationTypes.add("indicates");
    entityRelationTypes.add("is_indicated_for");
    entityRelationTypes.add("location_of");
    entityRelationTypes.add("manages/treats");
    entityRelationTypes.add("manifestation_of"); // TODO: is this an error/misspelling in the data?
    entityRelationTypes.add("result_of");
    Set<String> eventRelationTypes = new HashSet<String>();
    eventRelationTypes.add("TLINK");
    eventRelationTypes.add("ALINK");

    // create a CAS object for each annotation
    Map<String, Annotation> idAnnotationMap = new HashMap<String, Annotation>();
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
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new BodyLateralityFeature(entityMention, bodyLaterality));
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new BodyLocationFeature(entityMention, bodyLocation));
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new BodySideFeature(entityMention, bodySide));

      } else if ("Clinical_attribute".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Devices".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

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
        KnowtatorAnnotation alleviatingFactor = annotationSlots.remove("alleviating_factor");
        delayedFeatures.add(new AlleviatingFactorFeature(entityMention, alleviatingFactor));
        KnowtatorAnnotation signOrSymptom = annotationSlots.remove("associated_sign_or_symptom");
        delayedFeatures.add(new AssociatedSignOrSymptomFeature(entityMention, signOrSymptom));
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new BodyLateralityFeature(entityMention, bodyLaterality));
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new BodyLocationFeature(entityMention, bodyLocation));
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new BodySideFeature(entityMention, bodySide));
        KnowtatorAnnotation course = annotationSlots.remove("course");
        delayedFeatures.add(new CourseFeature(entityMention, course));
        KnowtatorAnnotation exacerbatingFactor = annotationSlots.remove("exacerbating_factor");
        delayedFeatures.add(new ExacerbatingFactorFeature(entityMention, exacerbatingFactor));
        KnowtatorAnnotation severity = annotationSlots.remove("severity");
        delayedFeatures.add(new SeverityFeature(entityMention, severity));

      } else if ("Lab".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation ordinal = annotationSlots.remove("ordinal_interpretation");
        delayedFeatures.add(new LabOrdinalFeature(entityMention, ordinal));
        KnowtatorAnnotation referenceRange = annotationSlots.remove("reference_range_narrative");
        delayedFeatures.add(new LabReferenceRangeFeature(entityMention, referenceRange));
        KnowtatorAnnotation value = annotationSlots.remove("lab_value");
        delayedFeatures.add(new LabValueFeature(entityMention, value));

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
        KnowtatorAnnotation allergy = annotationSlots.remove("allergy_indicator");
        delayedFeatures.add(new MedicationAllergyFeature(entityMention, allergy));
        KnowtatorAnnotation changeStatus = annotationSlots.remove("change_status_model");
        delayedFeatures.add(new MedicationDurationFeature(entityMention, changeStatus));
        KnowtatorAnnotation dosage = annotationSlots.remove("dosage_model");
        delayedFeatures.add(new MedicationDosageFeature(entityMention, dosage));
        KnowtatorAnnotation duration = annotationSlots.remove("duration_model");
        delayedFeatures.add(new MedicationStatusChangeFeature(entityMention, duration));
        KnowtatorAnnotation form = annotationSlots.remove("form_model");
        delayedFeatures.add(new MedicationFormFeature(entityMention, form));
        KnowtatorAnnotation frequency = annotationSlots.remove("frequency_model");
        delayedFeatures.add(new MedicationFrequencyFeature(entityMention, frequency));
        KnowtatorAnnotation route = annotationSlots.remove("route_model");
        delayedFeatures.add(new MedicationRouteFeature(entityMention, route));
        KnowtatorAnnotation startDate = annotationSlots.remove("start_date");
        delayedFeatures.add(new MedicationStartDateFeature(entityMention, startDate));
        KnowtatorAnnotation strength = annotationSlots.remove("strength_model");
        delayedFeatures.add(new MedicationStrengthFeature(entityMention, strength));

      } else if ("Phenomena".equals(annotation.type)) {
        EntityMention entityMention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addEntityMentionFeatures(
            annotation,
            entityMention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
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
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new BodyLateralityFeature(entityMention, bodyLaterality));
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new BodyLocationFeature(entityMention, bodyLocation));
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new BodySideFeature(entityMention, bodySide));
        KnowtatorAnnotation device = annotationSlots.remove("device");
        delayedFeatures.add(new ProcedureDeviceFeature(entityMention, device));
        KnowtatorAnnotation method = annotationSlots.remove("method");
        delayedFeatures.add(new ProcedureMethodFeature(entityMention, method));

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
        KnowtatorAnnotation alleviatingFactor = annotationSlots.remove("alleviating_factor");
        delayedFeatures.add(new AlleviatingFactorFeature(entityMention, alleviatingFactor));
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new BodyLateralityFeature(entityMention, bodyLaterality));
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new BodyLocationFeature(entityMention, bodyLocation));
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new BodySideFeature(entityMention, bodySide));
        KnowtatorAnnotation course = annotationSlots.remove("course");
        delayedFeatures.add(new CourseFeature(entityMention, course));
        KnowtatorAnnotation exacerbatingFactor = annotationSlots.remove("exacerbating_factor");
        delayedFeatures.add(new ExacerbatingFactorFeature(entityMention, exacerbatingFactor));
        KnowtatorAnnotation severity = annotationSlots.remove("severity");
        delayedFeatures.add(new SeverityFeature(entityMention, severity));

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
        // TODO: unclear where this slot goes
        Boolean value = booleanSlots.remove("generic_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("severity_class".equals(annotation.type)) {
        Severity severity = new Severity(jCas);
        severity.setValue(stringSlots.remove("severity_normalization"));
        severity.addToIndexes();
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(severity);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("conditional_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        Boolean value = booleanSlots.remove("conditional_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("course_class".equals(annotation.type)) {
        Course course = new Course(jCas);
        course.setValue(stringSlots.remove("course_normalization"));
        course.addToIndexes();
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(course);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("uncertainty_indicator_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("uncertainty_indicator_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("distal_or_proximal".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("distal_or_proximal_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Person".equals(annotation.type)) {
        // TODO: unclear where these slots go
        String value = stringSlots.remove("subject_normalization_CU");
        String code = stringSlots.remove("associatedCode");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("body_side_class".equals(annotation.type)) {
        BodySide bodySide = new BodySide(jCas);
        bodySide.setValue(stringSlots.remove("body_side_normalization"));
        bodySide.addToIndexes();
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(bodySide);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("negation_indicator_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("negation_indicator_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("historyOf_indicator_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("historyOf_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("superior_or_inferior".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("superior_or_inferior_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("medial_or_lateral".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("medial_or_lateral_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("dorsal_or_ventral".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String value = stringSlots.remove("dorsal_or_ventral_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("method_class".equals(annotation.type)) {
        String code = stringSlots.remove("associatedCode");
        ProcedureMethod method = new ProcedureMethod(jCas);
        method.setValue(code);
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(method);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("device_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String code = stringSlots.remove("associatedCode");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("allergy_indicator_class".equals(annotation.type)) {
        // TODO: unclear where this slot goes
        String code = stringSlots.remove("allergy_indicator_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Route".equals(annotation.type)) {
        // TODO: there's currently no Route in the type system
        String value = stringSlots.remove("route_values");
        MedicationRoute route = new MedicationRoute(jCas);
        route.setValue(value);
        route.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(route);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);
        
      } else if ("Form".equals(annotation.type)) {
        String value = stringSlots.remove("form_values");
        MedicationForm form = new MedicationForm(jCas);
        form.setValue(value);
        form.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(form);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);
        
      } else if ("Strength".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("strength_unit");
        KnowtatorAnnotation number = annotationSlots.remove("strength_number");
        MedicationStrength strength = new MedicationStrength(jCas);
        strength.addToIndexes();
        delayedFeatures.add(new DelayedFeature<MedicationStrength>(strength, unit) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setUnit(...)
          }
        });
        delayedFeatures.add(new DelayedFeature<MedicationStrength>(strength, number) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setNumber(...)
          }
        });
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Strength number".equals(annotation.type)) {
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Strength unit".equals(annotation.type)) {
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Frequency".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("frequency_unit");
        KnowtatorAnnotation number = annotationSlots.remove("frequency_number");
        MedicationFrequency frequency = new MedicationFrequency(jCas);
        frequency.addToIndexes();
        delayedFeatures.add(new DelayedFeature<MedicationFrequency>(frequency, unit) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setUnit(...)
          }
        });
        delayedFeatures.add(new DelayedFeature<MedicationFrequency>(frequency, number) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setNumber(...)
          }
        });
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(frequency);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Frequency number".equals(annotation.type)) {
        String number = stringSlots.remove("frequency_number_normalization");
        MedicationFrequency frequency = new MedicationFrequency(jCas);
        frequency.setNumber(number);
        frequency.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(frequency);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Frequency unit".equals(annotation.type)) {
        String unit = stringSlots.remove("frequency_unit_values");
        MedicationFrequency frequency = new MedicationFrequency(jCas);
        frequency.setUnit(unit);
        frequency.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(frequency);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Value".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("value_unit");
        KnowtatorAnnotation number = annotationSlots.remove("value_number");
        LabValue labValue = new LabValue(jCas);
        labValue.addToIndexes();
        delayedFeatures.add(new DelayedFeature<LabValue>(labValue, unit) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setUnit(...)
          }
        });
        delayedFeatures.add(new DelayedFeature<LabValue>(labValue, number) {
          @Override
          protected void setValue(Annotation valueAnnotation) {
            // TODO: this.annotation.setNumber(...)
          }
        });
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(labValue);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Value number".equals(annotation.type)) {
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Value unit".equals(annotation.type)) {
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("lab_interpretation_indicator".equals(annotation.type)) {
        // TODO: unclear where the slot value goes
        String value = stringSlots.remove("lab_interpretation_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("estimated_flag_indicator".equals(annotation.type)) {
        // TODO: unclear where the slot value goes
        boolean value = booleanSlots.remove("estimated_normalization");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("reference_range".equals(annotation.type)) {
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Date".equals(annotation.type)) {
        // TODO: unclear where these slot values go
        String month = stringSlots.remove("month");
        String day = stringSlots.remove("day");
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Status change".equals(annotation.type)) {
        String value = stringSlots.remove("change_status_value");
        MedicationStatusChange statusChange = new MedicationStatusChange(jCas);
        statusChange.setValue(value);
        statusChange.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(statusChange);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Duration".equals(annotation.type)) {
        String value = stringSlots.remove("duration_values");
        MedicationDuration duration = new MedicationDuration(jCas);
        duration.setValue(value);
        duration.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(duration);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Dosage".equals(annotation.type)) {
        String value = stringSlots.remove("dosage_values");
        MedicationDosage dosage = new MedicationDosage(jCas);
        dosage.setValue(value);
        dosage.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(dosage);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Attributes_lab".equals(annotation.type)) {
        // TODO: what does this even mean?
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Temporal".equals(annotation.type)) {
        // TODO: what does this even mean?
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if (":THING".equals(annotation.type)) {
        // TODO: what does this even mean?
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Entities".equals(annotation.type)) {
        // TODO: what does this even mean?
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if (eventRelationTypes.contains(annotation.type)) {
        // store the ALINK information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.sourceFile = knowtatorXML;
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Event");
        relation.target = annotationSlots.remove("related_to");
        relation.type = stringSlots.remove("Relationtype");
        delayedRelations.add(relation);

      } else if (entityRelationTypes.contains(annotation.type)) {
        // store the relation information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.sourceFile = knowtatorXML;
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Argument_CU");
        relation.target = annotationSlots.remove("Related_to_CU");
        relation.conditional = annotationSlots.remove("conditional_CU");
        relation.negation = annotationSlots.remove("negation_indicator_CU");
        relation.uncertainty = annotationSlots.remove("uncertainty_indicator_CU");
        delayedRelations.add(relation);

      } else {
        throw new UnsupportedOperationException(String.format(
            "unrecognized type '%s' in %s",
            annotation.type,
            knowtatorXML));
      }

      // make sure all slots have been consumed
      Map<String, Set<String>> slotGroups = new HashMap<String, Set<String>>();
      slotGroups.put("stringSlots", stringSlots.keySet());
      slotGroups.put("booleanSlots", booleanSlots.keySet());
      slotGroups.put("annotationSlots", annotationSlots.keySet());
      for (Map.Entry<String, Set<String>> entry : slotGroups.entrySet()) {
        Set<String> remainingSlots = entry.getValue();
        if (!remainingSlots.isEmpty()) {
          throw new UnsupportedOperationException(String.format(
              "%s has unprocessed %s %s in %s",
              annotation.type,
              entry.getKey(),
              remainingSlots,
              knowtatorXML));
        }
      }
    }

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

    // add features for conditional, generic, etc.
    KnowtatorAnnotation conditional = annotationSlots.remove("conditional_CU");
    delayedFeatures.add(new ConditionalFeature(entityMention, conditional));
    KnowtatorAnnotation generic = annotationSlots.remove("generic_CU");
    delayedFeatures.add(new GenericFeature(entityMention, generic));
    KnowtatorAnnotation historyOf = annotationSlots.remove("historyOf_CU");
    delayedFeatures.add(new HistoryOfFeature(entityMention, historyOf));
    KnowtatorAnnotation negationIndicator = annotationSlots.remove("negation_indicator_CU");
    delayedFeatures.add(new NegationFeature(entityMention, negationIndicator));
    KnowtatorAnnotation subject = annotationSlots.remove("subject_CU");
    delayedFeatures.add(new SubjectFeature(entityMention, subject));
    KnowtatorAnnotation uncertainty = annotationSlots.remove("uncertainty_indicator_CU");
    delayedFeatures.add(new UncertaintyFeature(entityMention, uncertainty));

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
    public URI sourceFile;

    public KnowtatorAnnotation annotation;

    public KnowtatorAnnotation source;

    public KnowtatorAnnotation target;

    public String type;

    public KnowtatorAnnotation conditional;
    
    public KnowtatorAnnotation negation;
    
    public KnowtatorAnnotation uncertainty;
    
    public DelayedRelation() {
    }

    public void addToIndexes(JCas jCas, Map<String, Annotation> idAnnotationMap) {
      if (this.source == null) {
        // throw new UnsupportedOperationException(String.format(
        LOGGER.error(String.format(
            "no source for '%s' with annotationSlots %s in %s",
            this.annotation.id,
            this.annotation.annotationSlots.keySet(),
            this.sourceFile));
        return;
      }
      if (this.target == null) {
        // throw new UnsupportedOperationException(String.format(
        LOGGER.error(String.format(
            "no target for '%s' with annotationSlots %s in %s",
            this.annotation.id,
            this.annotation.annotationSlots.keySet(),
            this.sourceFile));
        return;
      }
      
      // look up the relations in the map and issue an error if they're missing
      Annotation sourceMention = idAnnotationMap.get(this.source.id);
      Annotation targetMention = idAnnotationMap.get(this.target.id);
      if (sourceMention == null) {
        LOGGER.error(String.format(
            "no Annotation for source id '%s' in %s",
            this.source.id,
            this.sourceFile));
        return;
      } else if (targetMention == null) {
        LOGGER.error(String.format(
            "no Annotation for target id '%s' in %s",
            this.target.id,
            this.sourceFile));
        return;
      }

      // get the conditional
      if (this.conditional != null) {
        Annotation conditionalAnnotation = idAnnotationMap.get(this.conditional.id);
        if (conditionalAnnotation == null) {
          throw new UnsupportedOperationException(String.format(
              "no annotation with id '%s' in %s",
              this.conditional.id,
              this.sourceFile));
        }
      }

      // get the negation
      if (this.negation != null) {
        Annotation negationAnnotation = idAnnotationMap.get(this.negation.id);
        if (negationAnnotation == null) {
          throw new UnsupportedOperationException(String.format(
              "no annotation with id '%s' in %s",
              this.negation.id,
              this.sourceFile));
        }
      }

      // get the uncertainty
      if (this.uncertainty != null) {
        Annotation uncertaintyAnnotation = idAnnotationMap.get(this.uncertainty.id);
        if (uncertaintyAnnotation == null) {
          throw new UnsupportedOperationException(String.format(
              "no annotation with id '%s' in %s",
              this.uncertainty.id,
              this.sourceFile));
        }
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
      this.featureValueID = featureValue == null ? null : featureValue.id;
    }

    public void setValueFrom(Map<String, ? extends Annotation> idAnnotationMap) {
      if (this.featureValueID != null) {
        this.setValue(idAnnotationMap.get(this.featureValueID));
      }
    }

    protected abstract void setValue(Annotation valueAnnotation);
  }
  
  private static class AlleviatingFactorFeature extends DelayedFeature<EntityMention> {
    public AlleviatingFactorFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setAlleviatingFactor(...)
    }
  }

  private static class AssociatedSignOrSymptomFeature extends DelayedFeature<EntityMention> {
    public AssociatedSignOrSymptomFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setSignOrSymptom(...)
    }
  }
  
  private static class BodySideFeature extends DelayedFeature<EntityMention> {
    public BodySideFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setBodySide(...)
    }
  }

  private static class BodyLateralityFeature extends DelayedFeature<EntityMention> {
    public BodyLateralityFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setBodyLaterality(...)
    }
  }
  
  private static class BodyLocationFeature extends DelayedFeature<EntityMention> {
    public BodyLocationFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setBodyLocation(...)
    }
  }
  
  private static class CourseFeature extends DelayedFeature<EntityMention> {
    public CourseFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setCourse(...)
    }
  }

  private static class ConditionalFeature extends DelayedFeature<EntityMention> {
    public ConditionalFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setConditional(...)
    }
  }

  private static class ExacerbatingFactorFeature extends DelayedFeature<EntityMention> {
    public ExacerbatingFactorFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setExacerbatingFactor(...)
    }
  }

  private static class GenericFeature extends DelayedFeature<EntityMention> {
    public GenericFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setGeneric(...)
    }
  }
  
  private static class HistoryOfFeature extends DelayedFeature<EntityMention> {
    public HistoryOfFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setHistoryOf(...)
    }
  }
  
  private static class LabOrdinalFeature extends DelayedFeature<EntityMention> {
    public LabOrdinalFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setLabOrdinal(...)
    }
  }
  
  private static class LabReferenceRangeFeature extends DelayedFeature<EntityMention> {
    public LabReferenceRangeFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setLabReferenceRange(...)
    }
  }
  
  private static class LabValueFeature extends DelayedFeature<EntityMention> {
    public LabValueFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setLabValue(...)
    }
  }
  
  private static class MedicationAllergyFeature extends DelayedFeature<EntityMention> {
    public MedicationAllergyFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setAllergy(...)
    }
  }

  private static class MedicationDosageFeature extends DelayedFeature<EntityMention> {
    public MedicationDosageFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setDosage(...)
    }
  }

  private static class MedicationDurationFeature extends DelayedFeature<EntityMention> {
    public MedicationDurationFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setDuration(...)
    }
  }

  private static class MedicationFormFeature extends DelayedFeature<EntityMention> {
    public MedicationFormFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setMedicationForm(...)
    }
  }
  
  private static class MedicationFrequencyFeature extends DelayedFeature<EntityMention> {
    public MedicationFrequencyFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setFrequency(...)
    }
  }
  
  private static class MedicationRouteFeature extends DelayedFeature<EntityMention> {
    public MedicationRouteFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setRoute(...)
    }
  }
  
  private static class MedicationStartDateFeature extends DelayedFeature<EntityMention> {
    public MedicationStartDateFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setStartDate(...)
    }
  }
  
  private static class MedicationStatusChangeFeature extends DelayedFeature<EntityMention> {
    public MedicationStatusChangeFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setMedicationStatusChange(...)
    }
  }
  
  private static class MedicationStrengthFeature extends DelayedFeature<EntityMention> {
    public MedicationStrengthFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setMedicationStrength(...)
    }
  }

  private static class NegationFeature extends DelayedFeature<EntityMention> {
    public NegationFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setNegation(...)
    }
  }
  
  private static class ProcedureDeviceFeature extends DelayedFeature<EntityMention> {
    public ProcedureDeviceFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setProcedureDevice(...)
    }
  }
  
  private static class ProcedureMethodFeature extends DelayedFeature<EntityMention> {
    public ProcedureMethodFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setProcedureMethod(...)
    }
  }
  
  private static class SeverityFeature extends DelayedFeature<EntityMention> {
    public SeverityFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setSeverity(...)
    }
  }
  
  private static class SubjectFeature extends DelayedFeature<EntityMention> {
    public SubjectFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setSubject(...)
    }
  }
  
  private static class UncertaintyFeature extends DelayedFeature<EntityMention> {
    public UncertaintyFeature(EntityMention entityMention, KnowtatorAnnotation value) {
      super(entityMention, value);
    }
    @Override
    protected void setValue(Annotation valueAnnotation) {
      // TODO: this.annotation.setUncertainty(...)
    }
  }
  
  /**
   * This main method is only for testing purposes. It runs the reader on Knowtator directories.
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      throw new IllegalArgumentException(String.format(
          "usage: java %s path/to/Knowtator/text [path/to/Knowtator/text ...]",
          SHARPKnowtatorXMLReader.class.getName()));
    }
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(SHARPKnowtatorXMLReader.class);
    for (String knowtatorTextDirectoryPath : args) {
      File knowtatorTextDirectory = new File(knowtatorTextDirectoryPath);
      for (File textFile : knowtatorTextDirectory.listFiles()) {
        JCas jCas = engine.newJCas();
        DocumentID documentID = new DocumentID(jCas);
        documentID.setDocumentID(textFile.toURI().toString());
        documentID.addToIndexes();
        engine.process(jCas);
      }
    }

  }
}
