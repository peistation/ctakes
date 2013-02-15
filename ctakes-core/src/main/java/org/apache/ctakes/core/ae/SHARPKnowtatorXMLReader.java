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
import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.core.util.SHARPKnowtatorXMLDefaults;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.BodyLaterality;
import org.apache.ctakes.typesystem.type.refsem.BodySide;
import org.apache.ctakes.typesystem.type.refsem.Course;
import org.apache.ctakes.typesystem.type.refsem.Date;
import org.apache.ctakes.typesystem.type.refsem.Event;
import org.apache.ctakes.typesystem.type.refsem.EventProperties;
import org.apache.ctakes.typesystem.type.refsem.LabReferenceRange;
import org.apache.ctakes.typesystem.type.refsem.LabValue;
import org.apache.ctakes.typesystem.type.refsem.MedicationDosage;
import org.apache.ctakes.typesystem.type.refsem.MedicationDuration;
import org.apache.ctakes.typesystem.type.refsem.MedicationForm;
import org.apache.ctakes.typesystem.type.refsem.MedicationFrequency;
import org.apache.ctakes.typesystem.type.refsem.MedicationRoute;
import org.apache.ctakes.typesystem.type.refsem.MedicationStatusChange;
import org.apache.ctakes.typesystem.type.refsem.MedicationStrength;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.ProcedureDevice;
import org.apache.ctakes.typesystem.type.refsem.ProcedureMethod;
import org.apache.ctakes.typesystem.type.refsem.Severity;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;
import org.apache.ctakes.typesystem.type.textsem.BodyLateralityModifier;
import org.apache.ctakes.typesystem.type.textsem.BodySideModifier;
import org.apache.ctakes.typesystem.type.textsem.ConditionalModifier;
import org.apache.ctakes.typesystem.type.textsem.CourseModifier;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.GenericModifier;
import org.apache.ctakes.typesystem.type.textsem.HistoryOfModifier;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabEstimatedModifier;
import org.apache.ctakes.typesystem.type.textsem.LabInterpretationModifier;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textsem.LabReferenceRangeModifier;
import org.apache.ctakes.typesystem.type.textsem.LabValueModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationAllergyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDosageModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationDurationModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationFormModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationFrequencyModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationRouteModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStatusChangeModifier;
import org.apache.ctakes.typesystem.type.textsem.MedicationStrengthModifier;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textsem.PolarityModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureDeviceModifier;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMethodModifier;
import org.apache.ctakes.typesystem.type.textsem.SeverityModifier;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.apache.ctakes.typesystem.type.textsem.SubjectModifier;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textsem.UncertaintyModifier;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.jdom2.JDOMException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class SHARPKnowtatorXMLReader extends JCasAnnotator_ImplBase {
  static Logger LOGGER = Logger.getLogger(SHARPKnowtatorXMLReader.class);
  
  // paramater that should contain the path to text files, with Knowtator XML in a "nephew"
  public static final String PARAM_TEXTURI = "TextURI";
  public static final String SET_DEFAULTS = "SetDefaults";

  private static final Map<String, String> knowtatorSubjectValuesMappedToCasValues;
  static {
	  knowtatorSubjectValuesMappedToCasValues = Maps.newHashMap();
	  String [] knowtatorValues = {  // subject_normalization_CU
			  "patient",
			  "family_member",
			  "donor_family_member",
			  "donor_other",
			  "other",
	  };

	  String [] casValues = {
				CONST.ATTR_SUBJECT_PATIENT,
				CONST. ATTR_SUBJECT_FAMILY_MEMBER, // = "family_member";
				CONST.ATTR_SUBJECT_DONOR_FAMILY_MEMBER, // = "donor_family_member";
				CONST.ATTR_SUBJECT_DONOR_OTHER, // = "donor_other";
				CONST.ATTR_SUBJECT_OTHER, // = "other";
			  
	  };
	  
	  for (int i=0; i<knowtatorValues.length; i++) {
		  knowtatorSubjectValuesMappedToCasValues.put(knowtatorValues[i], casValues[i]);
		  
	  }
	  
  }
  
  // path to knowtator xml files
  public static File textURIDirectory;
  public static Boolean setDefaults;

  /**
   * Get the URI that the text in this class was loaded from
   */
  protected URI getTextURI(JCas jCas) throws AnalysisEngineProcessException {
	  
    try {
	  if (!(textURIDirectory==null) && !"".equals(textURIDirectory.toString())) {
	    return new URI(textURIDirectory.toURI().toString() +File.separator+ JCasUtil.selectSingle(jCas, DocumentID.class).getDocumentID());
	  } else {
		return new URI(JCasUtil.selectSingle(jCas, DocumentID.class).getDocumentID());
	  }
	  
    } catch (URISyntaxException e) {
	  throw new AnalysisEngineProcessException(e);
	}
  }
  
  /**
   * Get the URI for the Knowtator XML file that should be loaded
   */
  protected URI getKnowtatorURI(JCas jCas) throws AnalysisEngineProcessException {
    String textURI = this.getTextURI(jCas).toString();
    String fileSeparator;
    if (!textURI.contains("Knowtator"+File.separator)) {
    	fileSeparator = "/";
    } else {
    	fileSeparator = File.separator;
    }
    String xmlURI = textURI.replaceAll("Knowtator"+fileSeparator+"text", "Knowtator_XML") + ".knowtator.xml";
    // check if directory structure doesn't have underscores
    try {
    	if (!new File(new URI(xmlURI)).exists()) {
    		xmlURI = textURI.replaceAll("Knowtator"+fileSeparator+"text", "Knowtator%20XML") + ".knowtator.xml";
    	}
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
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
	super.initialize(aContext);

  Boolean sd = (Boolean) aContext.getConfigParameterValue(SET_DEFAULTS);
  setDefaults = (sd==null)? true : sd;
  try {
		textURIDirectory = new File( (String) aContext.getConfigParameterValue(PARAM_TEXTURI) );
	} catch (NullPointerException e) {
		textURIDirectory = null;
	}
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String text = jCas.getDocumentText();
    URI textURI = this.getTextURI(jCas);
    LOGGER.info("processing " + textURI);

    // determine Knowtator XML file from the CAS
    URI knowtatorURI = this.getKnowtatorURI(jCas);
    if (!new File(knowtatorURI).exists()) {
      LOGGER.fatal("no such Knowtator XML file " + knowtatorURI);
      return;
    }

    // parse the Knowtator XML file into annotation objects
    KnowtatorXMLParser parser = new KnowtatorXMLParser(this.getAnnotatorNames());
    Collection<KnowtatorAnnotation> annotations;
    try {
      annotations = parser.parse(knowtatorURI);
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
    
    Set<String> nonAnnotationTypes = Sets.newHashSet("Strength", "Frequency", "Value");
    nonAnnotationTypes.addAll(entityRelationTypes);
    nonAnnotationTypes.addAll(eventRelationTypes);

    // create a CAS object for each annotation
    Map<String, TOP> idAnnotationMap = new HashMap<String, TOP>();
    List<DelayedRelation> delayedRelations = new ArrayList<DelayedRelation>();
    List<DelayedFeature> delayedFeatures = new ArrayList<DelayedFeature>();
    for (final KnowtatorAnnotation annotation : annotations) {

      // copy the slots so we can remove them as we use them
      Map<String, String> stringSlots = new HashMap<String, String>(annotation.stringSlots);
      Map<String, Boolean> booleanSlots = new HashMap<String, Boolean>(annotation.booleanSlots);
      Map<String, KnowtatorAnnotation> annotationSlots = new HashMap<String, KnowtatorAnnotation>(
          annotation.annotationSlots);
      KnowtatorAnnotation.Span coveringSpan = annotation.getCoveringSpan();
      
      if (nonAnnotationTypes.contains(annotation.type)) {
        if (coveringSpan.begin != Integer.MAX_VALUE || coveringSpan.end != Integer.MIN_VALUE) {
          LOGGER.error(String.format(
              "expected no span but found %s for '%s' with id '%s' in %s'",
              annotation.spans,
              annotation.type,
              annotation.id,
              knowtatorURI));
        }
      } else {
        if (coveringSpan.begin == Integer.MAX_VALUE || coveringSpan.end == Integer.MIN_VALUE) {
          LOGGER.error(String.format(
              "expected span but found none for '%s' with id '%s' in %s'",
              annotation.type,
              annotation.id,
              knowtatorURI));
        }
      }

      if ("Anatomical_site".equals(annotation.type)) {
        final AnatomicalSiteMention mention = new AnatomicalSiteMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_ANATOMICAL_SITE,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new DelayedFeature(bodyLaterality) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodyLaterality((BodyLateralityModifier) value);
          }
        });
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new DelayedFeature(bodyLocation) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we set a body location on a AnatomicalSiteMention?
          }
        });
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new DelayedFeature(bodySide) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodySide((BodySideModifier) value);
          }
        });

      } else if ("Clinical_attribute".equals(annotation.type)) {
        EntityMention mention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Devices".equals(annotation.type)) {
        EntityMention mention = new EntityMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Disease_Disorder".equals(annotation.type)) {
        final DiseaseDisorderMention mention = new DiseaseDisorderMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_DISORDER,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation alleviatingFactor = annotationSlots.remove("alleviating_factor");
        delayedFeatures.add(new DelayedFeature(alleviatingFactor) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get an ElementRelation here?
          }
        });
        KnowtatorAnnotation signOrSymptom = annotationSlots.remove("associated_sign_or_symptom");
        delayedFeatures.add(new DelayedFeature(signOrSymptom) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get a ManifestationOf here?
          }
        });
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new DelayedFeature(bodyLaterality) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodyLaterality((BodyLateralityModifier) value);
          }
        });
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new DelayedFeature(bodyLocation) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get a LocationOf here?
          }
        });
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new DelayedFeature(bodySide) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodySide((BodySideModifier) value);
          }
        });
        final KnowtatorAnnotation course = annotationSlots.remove("course");
        delayedFeatures.add(new DelayedFeature(course) {
          @Override
          protected void setValue(TOP value) {
            BinaryTextRelation relation = (BinaryTextRelation) value;
            try {
              mention.setCourse((CourseModifier) relation.getArg2().getArgument());
            } catch (ClassCastException e) {
              LOGGER.error(String.format(
                  "Expected CourseModifier for \"course\" of %s with id \"%s\", found %s with id \"%s\"",
                  annotation.type,
                  annotation.id,
                  relation.getArg2().getArgument().getClass().getSimpleName(),
                  course.id));
            }
          }
        });
        KnowtatorAnnotation exacerbatingFactor = annotationSlots.remove("exacerbating_factor");
        delayedFeatures.add(new DelayedFeature(exacerbatingFactor) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get an ElementRelation here?
          }
        });
        KnowtatorAnnotation severity = annotationSlots.remove("severity");
        delayedFeatures.add(new DelayedFeature(severity) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we set a severity on a DiseaseDisorderMention?
          }
        });

      } else if ("Lab".equals(annotation.type)) {
        final LabMention mention = new LabMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation ordinal = annotationSlots.remove("ordinal_interpretation");
        delayedFeatures.add(new DelayedFeature(ordinal) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get a DegreeOf here?
          }
        });
        KnowtatorAnnotation referenceRange = annotationSlots.remove("reference_range_narrative");
        delayedFeatures.add(new DelayedFeature(referenceRange) {
          @Override
          protected void setValue(TOP value) {
            mention.setReferenceRangeNarrative((LabReferenceRangeModifier) value);
          }
        });
        KnowtatorAnnotation labValue = annotationSlots.remove("lab_value");
        delayedFeatures.add(new DelayedFeature(labValue) {
          @Override
          protected void setValue(TOP value) {
            BinaryTextRelation relation = (BinaryTextRelation) value;
            mention.setLabValue((LabValueModifier) relation.getArg2().getArgument());
          }
        });

      } else if ("Medications/Drugs".equals(annotation.type)) {
        final MedicationEventMention mention = new MedicationEventMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_DRUG,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation allergy = annotationSlots.remove("allergy_indicator");
        delayedFeatures.add(new DelayedFeature(allergy) {
          @Override
          protected void setValue(TOP value) {
            // TODO: where is the allergy set?
          }
        });
        KnowtatorAnnotation changeStatus = annotationSlots.remove("change_status_model");
        delayedFeatures.add(new DelayedFeature(changeStatus) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationStatusChange expect an Attribute, not a Modifier?
            // mention.setMedicationStatusChange((MedicationStatusChangeModifier) value);
          }
        });
        KnowtatorAnnotation dosage = annotationSlots.remove("dosage_model");
        delayedFeatures.add(new DelayedFeature(dosage) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationDosage expect an Attribute, not a Modifier?
            // mention.setMedicationDosage((MedicationDosageModifier) value);
          }
        });
        KnowtatorAnnotation duration = annotationSlots.remove("duration_model");
        delayedFeatures.add(new DelayedFeature(duration) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationDuration expect an Attribute, not a Modifier?
            // mention.setMedicationDuration((MedicationDurationModifier) value);
          }
        });
        KnowtatorAnnotation form = annotationSlots.remove("form_model");
        delayedFeatures.add(new DelayedFeature(form) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationForm expect an Attribute, not a Modifier?
            // mention.setMedicationForm((MedicationFormModifier) value);
          }
        });
        KnowtatorAnnotation frequency = annotationSlots.remove("frequency_model");
        delayedFeatures.add(new DelayedFeature(frequency) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationFrequency expect an Attribute, not a Modifier?
            // mention.setMedicationFrequency((MedicationFrequencyModifier) value);
          }
        });
        KnowtatorAnnotation route = annotationSlots.remove("route_model");
        delayedFeatures.add(new DelayedFeature(route) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationRoute expect an Attribute, not a Modifier?
            // mention.setMedicationRoute((MedicationRouteModifier) value);
          }
        });
        KnowtatorAnnotation startDate = annotationSlots.remove("start_date");
        delayedFeatures.add(new DelayedFeature(startDate) {
          @Override
          protected void setValue(TOP value) {
            // TODO: where is the start date set?
          }
        });
        KnowtatorAnnotation strength = annotationSlots.remove("strength_model");
        delayedFeatures.add(new DelayedFeature(strength) {
          @Override
          protected void setValue(TOP value) {
            // TODO: why does setMedicationStrength expect an Attribute, not a Modifier?
            // mention.setMedicationStrength((MedicationStrengthModifier) value);
          }
        });

      } else if ("Phenomena".equals(annotation.type)) {
        EventMention mention = new EventMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_UNKNOWN /* TODO: is this the correct type? */,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);

      } else if ("Procedure".equals(annotation.type)) {
        final ProcedureMention mention = new ProcedureMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_PROCEDURE,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new DelayedFeature(bodyLaterality) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodyLaterality((BodyLateralityModifier) value);
          }
        });
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new DelayedFeature(bodyLocation) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get a LocationOf here?
          }
        });
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new DelayedFeature(bodySide) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodySide((BodySideModifier) value);
          }
        });
        KnowtatorAnnotation device = annotationSlots.remove("device");
        delayedFeatures.add(new DelayedFeature(device) {
          @Override
          protected void setValue(TOP value) {
            // TODO: where do we set the device?
          }
        });
        KnowtatorAnnotation method = annotationSlots.remove("method");
        delayedFeatures.add(new DelayedFeature(method) {
          @Override
          protected void setValue(TOP value) {
            mention.setMethod((ProcedureMethodModifier) value);
          }
        });

      } else if ("Sign_symptom".equals(annotation.type)) {
        final SignSymptomMention mention = new SignSymptomMention(jCas, coveringSpan.begin, coveringSpan.end);
        addIdentifiedAnnotationFeatures(
            annotation,
            mention,
            jCas,
            CONST.NE_TYPE_ID_FINDING,
            stringSlots,
            booleanSlots,
            annotationSlots,
            idAnnotationMap,
            delayedFeatures);
        KnowtatorAnnotation alleviatingFactor = annotationSlots.remove("alleviating_factor");
        delayedFeatures.add(new DelayedFeature(alleviatingFactor) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get an ElementRelation here?
          }
        });
        KnowtatorAnnotation bodyLaterality = annotationSlots.remove("body_laterality");
        delayedFeatures.add(new DelayedFeature(bodyLaterality) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodyLaterality((BodyLateralityModifier) value);
          }
        });
        KnowtatorAnnotation bodyLocation = annotationSlots.remove("body_location");
        delayedFeatures.add(new DelayedFeature(bodyLocation) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get a LocationOf here?
          }
        });
        KnowtatorAnnotation bodySide = annotationSlots.remove("body_side");
        delayedFeatures.add(new DelayedFeature(bodySide) {
          @Override
          protected void setValue(TOP value) {
            mention.setBodySide((BodySideModifier) value);
          }
        });
        final KnowtatorAnnotation course = annotationSlots.remove("course");
        delayedFeatures.add(new DelayedFeature(course) {
          @Override
          protected void setValue(TOP value) {
            BinaryTextRelation relation = (BinaryTextRelation) value;
            try {
              mention.setCourse((CourseModifier) relation.getArg2().getArgument());
            } catch (ClassCastException e) {
              LOGGER.error(String.format(
                  "Expected CourseModifier for \"course\" of %s with id \"%s\", found %s with id \"%s\"",
                  annotation.type,
                  annotation.id,
                  relation.getArg2().getArgument().getClass().getSimpleName(),
                  course.id));
            }
          }
        });
        KnowtatorAnnotation exacerbatingFactor = annotationSlots.remove("exacerbating_factor");
        delayedFeatures.add(new DelayedFeature(exacerbatingFactor) {
          @Override
          protected void setValue(TOP value) {
            // TODO: how do we get an ElementRelation here?
          }
        });
        KnowtatorAnnotation severity = annotationSlots.remove("severity");
        delayedFeatures.add(new DelayedFeature(severity) {
          @Override
          protected void setValue(TOP value) {
            BinaryTextRelation relation = (BinaryTextRelation) value;
            mention.setSeverity(((SeverityModifier) relation.getArg2().getArgument()).getValue());
          }
        });

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
        // TODO: how do we specify that this is the document creation time?
        TimeMention timeMention = new TimeMention(jCas, coveringSpan.begin, coveringSpan.end);
        timeMention.addToIndexes();
        idAnnotationMap.put(annotation.id, timeMention);

      } else if ("SECTIONTIME".equals(annotation.type)) {
        // TODO: how do we specify that this is a section time?
        TimeMention timeMention = new TimeMention(jCas, coveringSpan.begin, coveringSpan.end);
        timeMention.addToIndexes();
        idAnnotationMap.put(annotation.id, timeMention);

      } else if ("TIMEX3".equals(annotation.type)) {
        String timexClass = stringSlots.remove("class");
        TimeMention timeMention = new TimeMention(jCas, coveringSpan.begin, coveringSpan.end);
        timeMention.addToIndexes();
        idAnnotationMap.put(annotation.id, timeMention);
        // TODO
        
      } else if ("conditional_class".equals(annotation.type)) {
        Boolean value = booleanSlots.remove("conditional_normalization");
        ConditionalModifier modifier = new ConditionalModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setConditional(value == null ? false : value);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("generic_class".equals(annotation.type)) {
        Boolean value = booleanSlots.remove("generic_normalization");
        GenericModifier modifier = new GenericModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setGeneric(value == null ? false : value);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("negation_indicator_class".equals(annotation.type)) {
        String value = stringSlots.remove("negation_indicator_normalization");
        PolarityModifier modifier = new PolarityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming NE_POLARITY_NEGATION_PRESENT for \"%s\" with id \"%s\"",
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          modifier.setPolarity(CONST.NE_POLARITY_NEGATION_PRESENT);
        } else if (value.equals("negation_absent")) {
          modifier.setPolarity(CONST.NE_POLARITY_NEGATION_ABSENT);
        } else if (value.equals("negation_present")) {
          modifier.setPolarity(CONST.NE_POLARITY_NEGATION_PRESENT);
        } else {
          throw new UnsupportedOperationException("Invalid negation: " + value);
        }
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("uncertainty_indicator_class".equals(annotation.type)) {
        String value = stringSlots.remove("uncertainty_indicator_normalization");
        UncertaintyModifier modifier = new UncertaintyModifier(jCas, coveringSpan.begin, coveringSpan.end);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming NE_UNCERTAINTY_PRESENT for \"%s\" with id \"%s\"",
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          modifier.setUncertainty(CONST.NE_UNCERTAINTY_PRESENT);
        } else if (value.equals("indicator_absent")) {
          modifier.setUncertainty(CONST.NE_UNCERTAINTY_ABSENT);
        } else if (value.equals("indicator_present")) {
          modifier.setUncertainty(CONST.NE_UNCERTAINTY_PRESENT);
        } else {
          throw new UnsupportedOperationException("Invalid uncertainty: " + value);
        }
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Person".equals(annotation.type)) {
        String value = stringSlots.remove("subject_normalization_CU");
        if (value != null) {
          value = knowtatorSubjectValuesMappedToCasValues.get(value);
        }
        String code = stringSlots.remove("associatedCode");
        if (code != null) {
          if ("C0030705".equals(code)) {
            if (value == null) {
              value = CONST.ATTR_SUBJECT_PATIENT;
            } else if (!CONST.ATTR_SUBJECT_PATIENT.equals(value)) {
              LOGGER.error(String.format(
                  "subject value \"%s\" and code \"%s\" are inconsistent for annotation with id \"%s\"",
                  value,
                  code,
                  annotation.id));
            }
          } else {
            LOGGER.error(String.format(
                "unrecognized code \"%s\" for annotation with id \"%s\"",
                code,
                annotation.id));
          }
        }
        SubjectModifier modifier = new SubjectModifier(jCas, coveringSpan.begin, coveringSpan.end);
        if (setDefaults) value = SHARPKnowtatorXMLDefaults.getSubject(value);
        modifier.setSubject(value);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("historyOf_indicator_class".equals(annotation.type)) {
        String value = stringSlots.remove("historyOf_normalization");
        HistoryOfModifier modifier = new HistoryOfModifier(jCas, coveringSpan.begin, coveringSpan.end);
        if (null == value) {
          modifier.setIndicated(false);
        } else if ("historyOf_present".equals(value)) {
          modifier.setIndicated(true);
        } else if ("historyOf_absent".equals(value)) {
          modifier.setIndicated(false);
        } else {
          LOGGER.error(String.format(
              "unrecognized history-of value \"%s\" on annotation with id \"%s\"",
              value,
              annotation.id));
        }
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("distal_or_proximal".equals(annotation.type)) {
        String value = stringSlots.remove("distal_or_proximal_normalization");
        BodyLateralityModifier modifier = new BodyLateralityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        BodyLaterality attribute = new BodyLaterality(jCas);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming \"%s\" for \"%s\" with id \"%s\"",
              CONST.ATTR_BODYLATERALITY_UNMARKED,
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          value = CONST.ATTR_BODYLATERALITY_UNMARKED;
        } else if (!value.equals(CONST.ATTR_BODYLATERALITY_DISTAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_PROXIMAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_UNMARKED)) {
          throw new UnsupportedOperationException("Invalid BodyLaterality: " + value);
        }
        attribute.setValue(value);
        attribute.addToIndexes();
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("superior_or_inferior".equals(annotation.type)) {
        String value = stringSlots.remove("superior_or_inferior_normalization");
        BodyLateralityModifier modifier = new BodyLateralityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        BodyLaterality attribute = new BodyLaterality(jCas);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming \"%s\" for \"%s\" with id \"%s\"",
              CONST.ATTR_BODYLATERALITY_UNMARKED,
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          value = CONST.ATTR_BODYLATERALITY_UNMARKED;
        } else if (!value.equals(CONST.ATTR_BODYLATERALITY_DISTAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_SUPERIOR) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_INFERIOR)) {
          throw new UnsupportedOperationException("Invalid BodyLaterality: " + value);
        }
        attribute.setValue(value);
        attribute.addToIndexes();
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("medial_or_lateral".equals(annotation.type)) {
        String value = stringSlots.remove("medial_or_lateral_normalization");
        
        BodyLateralityModifier modifier = new BodyLateralityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        BodyLaterality attribute = new BodyLaterality(jCas);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming \"%s\" for \"%s\" with id \"%s\"",
              CONST.ATTR_BODYLATERALITY_UNMARKED,
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          value = CONST.ATTR_BODYLATERALITY_UNMARKED;
        } else if (!value.equals(CONST.ATTR_BODYLATERALITY_DISTAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_MEDIAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_LATERAL)) {
          throw new UnsupportedOperationException("Invalid BodyLaterality: " + value);
        }
        attribute.setValue(value);
        attribute.addToIndexes();
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("dorsal_or_ventral".equals(annotation.type)) {
        String value = stringSlots.remove("dorsal_or_ventral_normalization");
        
        BodyLateralityModifier modifier = new BodyLateralityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        BodyLaterality attribute = new BodyLaterality(jCas);
        if (value == null) {
          LOGGER.warn(String.format(
              "assuming \"%s\" for \"%s\" with id \"%s\"",
              CONST.ATTR_BODYLATERALITY_UNMARKED,
              modifier.getEnd() < 0 ? "<no-span>" : modifier.getCoveredText(),
              annotation.id));
          value = CONST.ATTR_BODYLATERALITY_UNMARKED;
        } else if (!value.equals(CONST.ATTR_BODYLATERALITY_DISTAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_DORSAL) &&
            !value.equals(CONST.ATTR_BODYLATERALITY_VENTRAL)) {
          throw new UnsupportedOperationException("Invalid BodyLaterality: " + value);
        }
        attribute.setValue(value);
        attribute.addToIndexes();
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("body_side_class".equals(annotation.type)) {
        BodySide attribute = new BodySide(jCas);
        attribute.setValue(stringSlots.remove("body_side_normalization"));
        attribute.addToIndexes();
        BodySideModifier modifier = new BodySideModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("course_class".equals(annotation.type)) {
        Course attribute = new Course(jCas);
        attribute.setValue(stringSlots.remove("course_normalization"));
        attribute.addToIndexes();
        CourseModifier modifier = new CourseModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setTypeID(CONST.MODIFIER_TYPE_ID_COURSE_CLASS);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("estimated_flag_indicator".equals(annotation.type)) {
        boolean value = booleanSlots.remove("estimated_normalization");
        LabEstimatedModifier modifier = new LabEstimatedModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setIndicated(value);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("lab_interpretation_indicator".equals(annotation.type)) {
        String value = stringSlots.remove("lab_interpretation_normalization");
        LabInterpretationModifier modifier = new LabInterpretationModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setTypeID(CONST.MODIFIER_TYPE_ID_LAB_INTERPRETATION_INDICATOR);
        modifier.setValue(value);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("reference_range".equals(annotation.type)) {
        LabReferenceRange attribute = new LabReferenceRange(jCas);
        // TODO: set value (from where?)
        attribute.addToIndexes();
        LabReferenceRangeModifier modifier = new LabReferenceRangeModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Value".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("value_unit");
        KnowtatorAnnotation number = annotationSlots.remove("value_number");
        LabValue attribute = new LabValue(jCas);
        if (unit != null) {
          KnowtatorAnnotation.Span unitSpan = unit.getCoveringSpan();
          String unitString = text.substring(unitSpan.begin, unitSpan.end);
          attribute.setUnit(unitString);
        }
        if (number != null) {
          KnowtatorAnnotation.Span numberSpan = number.getCoveringSpan();
          String numberString = text.substring(numberSpan.begin, numberSpan.end);
          attribute.setNumber(numberString);
        }
        attribute.addToIndexes();
        LabValueModifier modifier = new LabValueModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setUnit(attribute.getUnit());
        modifier.setNumber(attribute.getNumber());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Value number".equals(annotation.type)) {
        // already handled in "Value" above

      } else if ("Value unit".equals(annotation.type)) {
        // already handled in "Value" above

      } else if ("allergy_indicator_class".equals(annotation.type)) {
        String value = stringSlots.remove("allergy_indicator_normalization");
        MedicationAllergyModifier modifier = new MedicationAllergyModifier(jCas, coveringSpan.begin, coveringSpan.end);
        if (null == value) {
          modifier.setIndicated(false);
        } else if ("indicator_present".equals(value)) {
          modifier.setIndicated(true);
        } else if ("indicator_absent".equals(value)) {
          modifier.setIndicated(false);
        } else {
          LOGGER.error(String.format(
              "unrecognized allergy-indicator value \"%s\" on annotation with id \"%s\"",
              value,
              annotation.id));
        }
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Dosage".equals(annotation.type)) {
        String value = stringSlots.remove("dosage_values");
        MedicationDosage attribute = new MedicationDosage(jCas);
        attribute.setValue(value);
        attribute.addToIndexes();
        MedicationDosageModifier modifier = new MedicationDosageModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Duration".equals(annotation.type)) {
        String value = stringSlots.remove("duration_values");
        MedicationDuration attribute = new MedicationDuration(jCas);
        attribute.setValue(value);
        attribute.addToIndexes();
        MedicationDurationModifier modifier = new MedicationDurationModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Form".equals(annotation.type)) {
        String value = stringSlots.remove("form_values");
        MedicationForm attribute = new MedicationForm(jCas);
        attribute.setValue(value);
        attribute.addToIndexes();
        MedicationFormModifier modifier = new MedicationFormModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);
        
      } else if ("Frequency".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("frequency_unit");
        KnowtatorAnnotation number = annotationSlots.remove("frequency_number");
        MedicationFrequency attribute = new MedicationFrequency(jCas);
        if (unit != null) {
          String unitString = unit.stringSlots.get("frequency_unit_values");
          attribute.setUnit(unitString);
        }
        if (number != null) {
          String numberString = number.stringSlots.get("frequency_number_normalization");
          attribute.setNumber(numberString);
        }
        attribute.addToIndexes();
        MedicationFrequencyModifier modifier = new MedicationFrequencyModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setUnit(attribute.getUnit());
        modifier.setNumber(attribute.getNumber());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Frequency number".equals(annotation.type)) {
        // already handled in "Frequency" above
        stringSlots.remove("frequency_number_normalization");

      } else if ("Frequency unit".equals(annotation.type)) {
        // already handled in "Frequency" above
        stringSlots.remove("frequency_unit_values");

      } else if ("Route".equals(annotation.type)) {
        String value = stringSlots.remove("route_values");
        MedicationRoute attribute = new MedicationRoute(jCas);
        attribute.setValue(value);
        attribute.addToIndexes();
        MedicationRouteModifier modifier = new MedicationRouteModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);
        
      } else if ("Status change".equals(annotation.type)) {
        String value = stringSlots.remove("change_status_value");
        MedicationStatusChange attribute = new MedicationStatusChange(jCas);
        attribute.setValue(value);
        attribute.addToIndexes();
        MedicationStatusChangeModifier modifier = new MedicationStatusChangeModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Strength".equals(annotation.type)) {
        KnowtatorAnnotation unit = annotationSlots.remove("strength_unit");
        KnowtatorAnnotation number = annotationSlots.remove("strength_number");
        MedicationStrength attribute = new MedicationStrength(jCas);
        if (unit != null) {
          KnowtatorAnnotation.Span unitSpan = unit.getCoveringSpan();
          String unitString = text.substring(unitSpan.begin, unitSpan.end);
          attribute.setUnit(unitString);
        }
        if (number != null) {
          KnowtatorAnnotation.Span numberSpan = number.getCoveringSpan();
          String numberString = text.substring(numberSpan.begin, numberSpan.end);
          attribute.setNumber(numberString);
        }
        attribute.addToIndexes();
        MedicationStrengthModifier modifier = new MedicationStrengthModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setUnit(attribute.getUnit());
        modifier.setNumber(attribute.getNumber());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Strength number".equals(annotation.type)) {
        // already handled in "Strength" above

      } else if ("Strength unit".equals(annotation.type)) {
        // already handled in "Strength" above

      } else if ("device_class".equals(annotation.type)) {
        String code = stringSlots.remove("associatedCode");
        ProcedureDevice attribute = new ProcedureDevice(jCas);
        attribute.setValue(code);
        ProcedureDeviceModifier modifier = new ProcedureDeviceModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("method_class".equals(annotation.type)) {
        String code = stringSlots.remove("associatedCode");
        ProcedureMethod attribute = new ProcedureMethod(jCas);
        attribute.setValue(code);
        ProcedureMethodModifier modifier = new ProcedureMethodModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("severity_class".equals(annotation.type)) {
        Severity attribute = new Severity(jCas);
        attribute.setValue(stringSlots.remove("severity_normalization"));
        attribute.addToIndexes();
        SeverityModifier modifier = new SeverityModifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.setTypeID(CONST.MODIFIER_TYPE_ID_SEVERITY_CLASS);
        modifier.setNormalizedForm(attribute);
        modifier.setValue(attribute.getValue());
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if ("Date".equals(annotation.type)) {
        String month = stringSlots.remove("month");
        String day = stringSlots.remove("day");
        // TODO: not clear where to add this Date to
        Date date = new Date(jCas);
        date.setMonth(month);
        date.setDay(day);
        date.addToIndexes();
        // TODO: set the modifier type (or use an appropriate Modifier sub-type?)
        Modifier modifier = new Modifier(jCas, coveringSpan.begin, coveringSpan.end);
        modifier.addToIndexes();
        idAnnotationMap.put(annotation.id, modifier);

      } else if (eventRelationTypes.contains(annotation.type)) {
        // store the ALINK information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.sourceFile = knowtatorURI;
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Event");
        relation.target = annotationSlots.remove("related_to");
        relation.type = stringSlots.remove("Relationtype");
        delayedRelations.add(relation);

      } else if (entityRelationTypes.contains(annotation.type)) {
        // store the relation information for later, once all annotations are in the CAS
        DelayedRelation relation = new DelayedRelation();
        relation.sourceFile = knowtatorURI;
        relation.annotation = annotation;
        relation.source = annotationSlots.remove("Argument_CU");
        relation.target = annotationSlots.remove("Related_to_CU");
        relation.conditional = annotationSlots.remove("conditional_CU");
        relation.negation = annotationSlots.remove("negation_indicator_CU");
        relation.uncertainty = annotationSlots.remove("uncertainty_indicator_CU");
        delayedRelations.add(relation);

      } else {
        LOGGER.error(String.format(
            "unrecognized type '%s' for annotation with id \"%s\"",
            annotation.type,
            annotation.id));
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
              knowtatorURI));
        }
      }
    }

    // all mentions should be added, so add relations between annotations
    for (DelayedRelation delayedRelation : delayedRelations) {
      delayedRelation.addToIndexes(jCas, idAnnotationMap);
    }

    // all mentions should be added, so add features that required other annotations
    for (DelayedFeature delayedFeature : delayedFeatures) {
      delayedFeature.setValueFrom(idAnnotationMap);
    }
  }

  private static void addIdentifiedAnnotationFeatures(
      KnowtatorAnnotation annotation,
      final IdentifiedAnnotation mention,
      JCas jCas,
      int typeID,
      Map<String, String> stringSlots,
      Map<String, Boolean> booleanSlots,
      Map<String, KnowtatorAnnotation> annotationSlots,
      Map<String, TOP> idAnnotationMap,
      List<DelayedFeature> delayedFeatures) {
    mention.setTypeID(typeID);
    mention.setConfidence(1.0f);
    mention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);

    // convert negation to an integer
    Boolean negation = booleanSlots.remove("Negation");
    mention.setPolarity(negation == null
        ? CONST.NE_POLARITY_NEGATION_ABSENT
        : negation == true ? CONST.NE_POLARITY_NEGATION_PRESENT : CONST.NE_POLARITY_NEGATION_ABSENT);

    // add features for conditional, generic, etc.
    KnowtatorAnnotation conditional = annotationSlots.remove("conditional_CU");
    delayedFeatures.add(new DelayedFeature(conditional) {
      @Override
      protected void setValue(TOP value) {
        mention.setConditional(((ConditionalModifier) value).getConditional());
      }
    });
    KnowtatorAnnotation generic = annotationSlots.remove("generic_CU");
    delayedFeatures.add(new DelayedFeature(generic) {
      @Override
      protected void setValue(TOP value) {
        mention.setGeneric(((GenericModifier) value).getGeneric());
      }
    });
    KnowtatorAnnotation historyOf = annotationSlots.remove("historyOf_CU");
    delayedFeatures.add(new DelayedFeature(historyOf) {
      @Override
      protected void setValue(TOP value) {
        HistoryOfModifier modifier = (HistoryOfModifier) value;
        // TODO: where do we set the history of?
      }
    });
    KnowtatorAnnotation negationIndicator = annotationSlots.remove("negation_indicator_CU");
    delayedFeatures.add(new DelayedFeature(negationIndicator) {
      @Override
      protected void setValue(TOP value) {
        mention.setPolarity(((PolarityModifier) value).getPolarity());
      }
    });
    KnowtatorAnnotation subject = annotationSlots.remove("subject_CU");
    delayedFeatures.add(new DelayedFeature(subject) {
      @Override
      protected void setValue(TOP value) {
        String normalizedSubject = ((SubjectModifier) value).getSubject(); 
        if (setDefaults) normalizedSubject = SHARPKnowtatorXMLDefaults.getSubject(normalizedSubject);
        mention.setSubject(normalizedSubject);
      }
    });
    KnowtatorAnnotation uncertainty = annotationSlots.remove("uncertainty_indicator_CU");
    delayedFeatures.add(new DelayedFeature(uncertainty) {
      @Override
      protected void setValue(TOP value) {
        mention.setUncertainty(((UncertaintyModifier) value).getUncertainty());
      }
    });

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
    if (mention.getTypeID() == CONST.NE_TYPE_ID_DRUG) {
      ontologyConcept = new OntologyConcept(jCas);
      ontologyConcept.setCode(code);
    } else {
      UmlsConcept umlsConcept = new UmlsConcept(jCas);
      umlsConcept.setCui(code);
      ontologyConcept = umlsConcept;
    }
    ontologyConcept.addToIndexes();
    mention.setOntologyConceptArr(new FSArray(jCas, 1));
    mention.setOntologyConceptArr(0, ontologyConcept);

    // add entity mention to CAS
    mention.addToIndexes();
    idAnnotationMap.put(annotation.id, mention);
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

    public void addToIndexes(JCas jCas, Map<String, TOP> idAnnotationMap) {
      if (this.source == null) {
        // throw new UnsupportedOperationException(String.format(
        LOGGER.error(String.format(
            "no source for '%s' with id '%s' and annotationSlots %s in %s",
            this.annotation.type,
            this.annotation.id,
            this.annotation.annotationSlots.keySet(),
            this.sourceFile));
        return;
      }
      if (this.target == null) {
        // throw new UnsupportedOperationException(String.format(
        LOGGER.error(String.format(
            "no target for '%s' with id '%s' and annotationSlots %s in %s",
            this.annotation.type,
            this.annotation.id,
            this.annotation.annotationSlots.keySet(),
            this.sourceFile));
        return;
      }

      // look up the relations in the map and issue an error if they're missing or an invalid type
      Annotation sourceMention, targetMention;
      try {
        sourceMention = (Annotation)idAnnotationMap.get(this.source.id);
      } catch (ClassCastException e) {
        LOGGER.error(String.format("invalid source %s: %s", this.source.id, e.getMessage()));
        return;
      }
      try {
        targetMention = (Annotation)idAnnotationMap.get(this.target.id);
      } catch (ClassCastException e) {
        LOGGER.error(String.format("invalid target %s: %s", this.target.id, e.getMessage()));
        return;
      }
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
        Annotation conditionalAnnotation = (Annotation)idAnnotationMap.get(this.conditional.id);
        if (conditionalAnnotation == null) {
          throw new UnsupportedOperationException(String.format(
              "no annotation with id '%s' in %s",
              this.conditional.id,
              this.sourceFile));
        }
      }

      // get the negation
      if (this.negation != null) {
        Annotation negationAnnotation = (Annotation)idAnnotationMap.get(this.negation.id);
        if (negationAnnotation == null) {
          throw new UnsupportedOperationException(String.format(
              "no annotation with id '%s' in %s",
              this.negation.id,
              this.sourceFile));
        }
      }

      // get the uncertainty
      if (this.uncertainty != null) {
        Annotation uncertaintyAnnotation = (Annotation)idAnnotationMap.get(this.uncertainty.id);
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
      
      // add the relation to the map so it can be used in features of other annotations
      idAnnotationMap.put(this.annotation.id, relation);
    }
  }

  private static abstract class DelayedFeature {
    private String featureValueID;

    public DelayedFeature(KnowtatorAnnotation featureValue) {
      this.featureValueID = featureValue == null ? null : featureValue.id;
    }

    public void setValueFrom(Map<String, ? extends TOP> idAnnotationMap) {
      if (this.featureValueID != null) {
        TOP valueAnnotation = idAnnotationMap.get(this.featureValueID);
        if (valueAnnotation == null) {
          LOGGER.warn(String.format(
              "unable to set feature; found no annotation for %s",
              this.featureValueID));
        } else {
          this.setValue(valueAnnotation);
        }
      }
    }

    protected abstract void setValue(TOP value);
  }
  
  /**
   * This main method is only for testing purposes. It runs the reader on Knowtator directories.
   * Expects directory named "Knowtator" and a sibling directory "Knowtator_XML".
   * "Knowtator" should have a subdirectory called "text" containing plaintext files
   * "Knowtator_XML" should have files that end with .knowtator.xml
   * @see #getKnowtatorURI
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      throw new IllegalArgumentException(String.format(
          "usage: java %s path/to/Knowtator/text [path/to/Knowtator/text ...]",
          SHARPKnowtatorXMLReader.class.getName()));
    }
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(SHARPKnowtatorXMLReader.class);
    
    /////////////////////////
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
            XWriter.class,
            XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
	    	    "/tmp",
	    	    XWriter.PARAM_FILE_NAMER_CLASS_NAME,
	    	    CtakesFileNamer.class.getName()
           );
    /////////////////////////
    
    for (String knowtatorTextDirectoryPath : args) {
      File knowtatorTextDirectory = new File(knowtatorTextDirectoryPath);
      for (File textFile : knowtatorTextDirectory.listFiles()) {
        JCas jCas = engine.newJCas();
        jCas.setDocumentText(Files.toString(textFile, Charsets.US_ASCII));
        DocumentID documentID = new DocumentID(jCas);
        documentID.setDocumentID(textFile.toURI().toString());
        documentID.addToIndexes();
        engine.process(jCas);
        documentID.setDocumentID(textFile.getName());
        //xWriter.process(jCas); ///////////////////
      }
    }

  }
}
