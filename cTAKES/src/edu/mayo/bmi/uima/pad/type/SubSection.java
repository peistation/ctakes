

/* First created by JCasGen Tue Jul 20 20:16:46 CDT 2010 */
package edu.mayo.bmi.uima.pad.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * XML source: C:/general_workspace/PAD term spotter/desc/type_system/PADSiteAndTerm.xml
 * @generated */
public class SubSection extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SubSection.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SubSection() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SubSection(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SubSection(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SubSection(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: parentSectionId

  /** getter for parentSectionId - gets 
   * @generated */
  public String getParentSectionId() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_parentSectionId == null)
      jcasType.jcas.throwFeatMissing("parentSectionId", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SubSection_Type)jcasType).casFeatCode_parentSectionId);}
    
  /** setter for parentSectionId - sets  
   * @generated */
  public void setParentSectionId(String v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_parentSectionId == null)
      jcasType.jcas.throwFeatMissing("parentSectionId", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setStringValue(addr, ((SubSection_Type)jcasType).casFeatCode_parentSectionId, v);}    
   
    
  //*--------------*
  //* Feature: subSectionBodyBegin

  /** getter for subSectionBodyBegin - gets 
   * @generated */
  public int getSubSectionBodyBegin() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionBodyBegin == null)
      jcasType.jcas.throwFeatMissing("subSectionBodyBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionBodyBegin);}
    
  /** setter for subSectionBodyBegin - sets  
   * @generated */
  public void setSubSectionBodyBegin(int v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionBodyBegin == null)
      jcasType.jcas.throwFeatMissing("subSectionBodyBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionBodyBegin, v);}    
   
    
  //*--------------*
  //* Feature: subSectionBodyEnd

  /** getter for subSectionBodyEnd - gets 
   * @generated */
  public int getSubSectionBodyEnd() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionBodyEnd == null)
      jcasType.jcas.throwFeatMissing("subSectionBodyEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionBodyEnd);}
    
  /** setter for subSectionBodyEnd - sets  
   * @generated */
  public void setSubSectionBodyEnd(int v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionBodyEnd == null)
      jcasType.jcas.throwFeatMissing("subSectionBodyEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionBodyEnd, v);}    
   
    
  //*--------------*
  //* Feature: status

  /** getter for status - gets 
   * @generated */
  public int getStatus() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_status);}
    
  /** setter for status - sets  
   * @generated */
  public void setStatus(int v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_status, v);}    
   
    
  //*--------------*
  //* Feature: subSectionHeaderBegin

  /** getter for subSectionHeaderBegin - gets 
   * @generated */
  public int getSubSectionHeaderBegin() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionHeaderBegin == null)
      jcasType.jcas.throwFeatMissing("subSectionHeaderBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionHeaderBegin);}
    
  /** setter for subSectionHeaderBegin - sets  
   * @generated */
  public void setSubSectionHeaderBegin(int v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionHeaderBegin == null)
      jcasType.jcas.throwFeatMissing("subSectionHeaderBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionHeaderBegin, v);}    
   
    
  //*--------------*
  //* Feature: subSectionHeaderEnd

  /** getter for subSectionHeaderEnd - gets 
   * @generated */
  public int getSubSectionHeaderEnd() {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionHeaderEnd == null)
      jcasType.jcas.throwFeatMissing("subSectionHeaderEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionHeaderEnd);}
    
  /** setter for subSectionHeaderEnd - sets  
   * @generated */
  public void setSubSectionHeaderEnd(int v) {
    if (SubSection_Type.featOkTst && ((SubSection_Type)jcasType).casFeat_subSectionHeaderEnd == null)
      jcasType.jcas.throwFeatMissing("subSectionHeaderEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    jcasType.ll_cas.ll_setIntValue(addr, ((SubSection_Type)jcasType).casFeatCode_subSectionHeaderEnd, v);}    
  }

    