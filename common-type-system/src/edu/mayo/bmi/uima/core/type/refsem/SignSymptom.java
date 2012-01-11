

/* First created by JCasGen Wed Jan 11 14:37:37 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.relation.ElementRelation;
import edu.mayo.bmi.uima.core.type.relation.TemporalRelation;
import edu.mayo.bmi.uima.core.type.relation.LocationOf;


/** 
 * Updated by JCasGen Wed Jan 11 14:37:37 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class SignSymptom extends Event {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SignSymptom.class);
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
  protected SignSymptom() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SignSymptom(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SignSymptom(JCas jcas) {
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
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_alleviatingFactor == null)
      jcasType.jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (ElementRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_alleviatingFactor)));}
    
  /** setter for alleviatingFactor - sets  
   * @generated */
  public void setAlleviatingFactor(ElementRelation v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_alleviatingFactor == null)
      jcasType.jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_alleviatingFactor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLaterality

  /** getter for bodyLaterality - gets 
   * @generated */
  public BodyLaterality getBodyLaterality() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (BodyLaterality)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodyLaterality)));}
    
  /** setter for bodyLaterality - sets  
   * @generated */
  public void setBodyLaterality(BodyLaterality v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodyLaterality, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodySide

  /** getter for bodySide - gets 
   * @generated */
  public BodySide getBodySide() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (BodySide)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodySide)));}
    
  /** setter for bodySide - sets  
   * @generated */
  public void setBodySide(BodySide v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodySide, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLocation

  /** getter for bodyLocation - gets 
   * @generated */
  public LocationOf getBodyLocation() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (LocationOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodyLocation)));}
    
  /** setter for bodyLocation - sets  
   * @generated */
  public void setBodyLocation(LocationOf v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_bodyLocation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: course

  /** getter for course - gets 
   * @generated */
  public Course getCourse() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_course == null)
      jcasType.jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (Course)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_course)));}
    
  /** setter for course - sets  
   * @generated */
  public void setCourse(Course v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_course == null)
      jcasType.jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_course, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: duration

  /** getter for duration - gets 
   * @generated */
  public TemporalRelation getDuration() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_duration)));}
    
  /** setter for duration - sets  
   * @generated */
  public void setDuration(TemporalRelation v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_duration, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: endTime

  /** getter for endTime - gets 
   * @generated */
  public Time getEndTime() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_endTime)));}
    
  /** setter for endTime - sets  
   * @generated */
  public void setEndTime(Time v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_endTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: exacerbatingFactor

  /** getter for exacerbatingFactor - gets 
   * @generated */
  public ElementRelation getExacerbatingFactor() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_exacerbatingFactor == null)
      jcasType.jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (ElementRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_exacerbatingFactor)));}
    
  /** setter for exacerbatingFactor - sets  
   * @generated */
  public void setExacerbatingFactor(ElementRelation v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_exacerbatingFactor == null)
      jcasType.jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_exacerbatingFactor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: severity

  /** getter for severity - gets 
   * @generated */
  public String getSeverity() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_severity == null)
      jcasType.jcas.throwFeatMissing("severity", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_severity);}
    
  /** setter for severity - sets  
   * @generated */
  public void setSeverity(String v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_severity == null)
      jcasType.jcas.throwFeatMissing("severity", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setStringValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_severity, v);}    
   
    
  //*--------------*
  //* Feature: startTime

  /** getter for startTime - gets 
   * @generated */
  public Time getStartTime() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_startTime)));}
    
  /** setter for startTime - sets  
   * @generated */
  public void setStartTime(Time v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_startTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relativeTemporalContext

  /** getter for relativeTemporalContext - gets 
   * @generated */
  public TemporalRelation getRelativeTemporalContext() {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_relativeTemporalContext)));}
    
  /** setter for relativeTemporalContext - sets  
   * @generated */
  public void setRelativeTemporalContext(TemporalRelation v) {
    if (SignSymptom_Type.featOkTst && ((SignSymptom_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.SignSymptom");
    jcasType.ll_cas.ll_setRefValue(addr, ((SignSymptom_Type)jcasType).casFeatCode_relativeTemporalContext, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    