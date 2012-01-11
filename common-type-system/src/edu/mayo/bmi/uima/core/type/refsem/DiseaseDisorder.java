

/* First created by JCasGen Wed Jan 11 14:37:37 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.relation.ManifestationOf;
import edu.mayo.bmi.uima.core.type.relation.ElementRelation;
import edu.mayo.bmi.uima.core.type.relation.TemporalRelation;
import edu.mayo.bmi.uima.core.type.relation.LocationOf;


/** 
 * Updated by JCasGen Wed Jan 11 14:37:37 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class DiseaseDisorder extends Event {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiseaseDisorder.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DiseaseDisorder() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiseaseDisorder(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiseaseDisorder(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: alleviatingFactor

  /** getter for alleviatingFactor - gets 
   * @generated */
  public ElementRelation getAlleviatingFactor() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_alleviatingFactor == null)
      jcasType.jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (ElementRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_alleviatingFactor)));}
    
  /** setter for alleviatingFactor - sets  
   * @generated */
  public void setAlleviatingFactor(ElementRelation v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_alleviatingFactor == null)
      jcasType.jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_alleviatingFactor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: associatedSignSymptom

  /** getter for associatedSignSymptom - gets 
   * @generated */
  public ManifestationOf getAssociatedSignSymptom() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_associatedSignSymptom == null)
      jcasType.jcas.throwFeatMissing("associatedSignSymptom", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (ManifestationOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_associatedSignSymptom)));}
    
  /** setter for associatedSignSymptom - sets  
   * @generated */
  public void setAssociatedSignSymptom(ManifestationOf v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_associatedSignSymptom == null)
      jcasType.jcas.throwFeatMissing("associatedSignSymptom", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_associatedSignSymptom, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLaterality

  /** getter for bodyLaterality - gets 
   * @generated */
  public BodyLaterality getBodyLaterality() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (BodyLaterality)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodyLaterality)));}
    
  /** setter for bodyLaterality - sets  
   * @generated */
  public void setBodyLaterality(BodyLaterality v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodyLaterality, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodySide

  /** getter for bodySide - gets 
   * @generated */
  public BodySide getBodySide() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (BodySide)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodySide)));}
    
  /** setter for bodySide - sets  
   * @generated */
  public void setBodySide(BodySide v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodySide, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLocation

  /** getter for bodyLocation - gets 
   * @generated */
  public LocationOf getBodyLocation() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (LocationOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodyLocation)));}
    
  /** setter for bodyLocation - sets  
   * @generated */
  public void setBodyLocation(LocationOf v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_bodyLocation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: course

  /** getter for course - gets 
   * @generated */
  public Course getCourse() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_course == null)
      jcasType.jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (Course)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_course)));}
    
  /** setter for course - sets  
   * @generated */
  public void setCourse(Course v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_course == null)
      jcasType.jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_course, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: duration

  /** getter for duration - gets 
   * @generated */
  public TemporalRelation getDuration() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_duration)));}
    
  /** setter for duration - sets  
   * @generated */
  public void setDuration(TemporalRelation v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_duration, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: endTime

  /** getter for endTime - gets 
   * @generated */
  public Time getEndTime() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_endTime)));}
    
  /** setter for endTime - sets  
   * @generated */
  public void setEndTime(Time v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_endTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: exacerbatingFactor

  /** getter for exacerbatingFactor - gets 
   * @generated */
  public ElementRelation getExacerbatingFactor() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_exacerbatingFactor == null)
      jcasType.jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (ElementRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_exacerbatingFactor)));}
    
  /** setter for exacerbatingFactor - sets  
   * @generated */
  public void setExacerbatingFactor(ElementRelation v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_exacerbatingFactor == null)
      jcasType.jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_exacerbatingFactor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: startTime

  /** getter for startTime - gets 
   * @generated */
  public Time getStartTime() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_startTime)));}
    
  /** setter for startTime - sets  
   * @generated */
  public void setStartTime(Time v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_startTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relativeTemporalContext

  /** getter for relativeTemporalContext - gets 
   * @generated */
  public TemporalRelation getRelativeTemporalContext() {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_relativeTemporalContext)));}
    
  /** setter for relativeTemporalContext - sets  
   * @generated */
  public void setRelativeTemporalContext(TemporalRelation v) {
    if (DiseaseDisorder_Type.featOkTst && ((DiseaseDisorder_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    jcasType.ll_cas.ll_setRefValue(addr, ((DiseaseDisorder_Type)jcasType).casFeatCode_relativeTemporalContext, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    