

/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.relation.TemporalRelation;
import edu.mayo.bmi.uima.core.type.relation.LocationOf;


/** 
 * Updated by JCasGen Fri Jan 20 10:31:33 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class Procedure extends Event {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Procedure.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Procedure() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Procedure(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Procedure(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: bodyLaterality

  /** getter for bodyLaterality - gets 
   * @generated */
  public BodyLaterality getBodyLaterality() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (BodyLaterality)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLaterality)));}
    
  /** setter for bodyLaterality - sets  
   * @generated */
  public void setBodyLaterality(BodyLaterality v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLaterality, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodySide

  /** getter for bodySide - gets 
   * @generated */
  public BodySide getBodySide() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (BodySide)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodySide)));}
    
  /** setter for bodySide - sets  
   * @generated */
  public void setBodySide(BodySide v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodySide, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLocation

  /** getter for bodyLocation - gets 
   * @generated */
  public LocationOf getBodyLocation() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (LocationOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLocation)));}
    
  /** setter for bodyLocation - sets  
   * @generated */
  public void setBodyLocation(LocationOf v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLocation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: procedureDevice

  /** getter for procedureDevice - gets 
   * @generated */
  public ProcedureDevice getProcedureDevice() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_procedureDevice == null)
      jcasType.jcas.throwFeatMissing("procedureDevice", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (ProcedureDevice)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_procedureDevice)));}
    
  /** setter for procedureDevice - sets  
   * @generated */
  public void setProcedureDevice(ProcedureDevice v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_procedureDevice == null)
      jcasType.jcas.throwFeatMissing("procedureDevice", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_procedureDevice, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: duration

  /** getter for duration - gets 
   * @generated */
  public TemporalRelation getDuration() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_duration)));}
    
  /** setter for duration - sets  
   * @generated */
  public void setDuration(TemporalRelation v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_duration, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: endTime

  /** getter for endTime - gets 
   * @generated */
  public Time getEndTime() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_endTime)));}
    
  /** setter for endTime - sets  
   * @generated */
  public void setEndTime(Time v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_endTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: method

  /** getter for method - gets 
   * @generated */
  public ProcedureMethod getMethod() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_method == null)
      jcasType.jcas.throwFeatMissing("method", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (ProcedureMethod)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_method)));}
    
  /** setter for method - sets  
   * @generated */
  public void setMethod(ProcedureMethod v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_method == null)
      jcasType.jcas.throwFeatMissing("method", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_method, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: startTime

  /** getter for startTime - gets 
   * @generated */
  public Time getStartTime() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_startTime)));}
    
  /** setter for startTime - sets  
   * @generated */
  public void setStartTime(Time v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_startTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relativeTemporalContext

  /** getter for relativeTemporalContext - gets 
   * @generated */
  public TemporalRelation getRelativeTemporalContext() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_relativeTemporalContext)));}
    
  /** setter for relativeTemporalContext - sets  
   * @generated */
  public void setRelativeTemporalContext(TemporalRelation v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_relativeTemporalContext, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    