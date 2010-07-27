

/* First created by JCasGen Tue Jul 20 20:16:46 CDT 2010 */
package edu.mayo.bmi.uima.pad.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.NamedEntity;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * XML source: C:/general_workspace/PAD term spotter/desc/type_system/PADSiteAndTerm.xml
 * @generated */
public class PADLocation extends NamedEntity {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PADLocation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PADLocation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PADLocation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PADLocation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PADLocation(JCas jcas, int begin, int end) {
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
  //* Feature: hitDictionaryValue

  /** getter for hitDictionaryValue - gets Value from the Dictionary that was successfully looked up
   * @generated */
  public String getHitDictionaryValue() {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_hitDictionaryValue == null)
      jcasType.jcas.throwFeatMissing("hitDictionaryValue", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PADLocation_Type)jcasType).casFeatCode_hitDictionaryValue);}
    
  /** setter for hitDictionaryValue - sets Value from the Dictionary that was successfully looked up 
   * @generated */
  public void setHitDictionaryValue(String v) {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_hitDictionaryValue == null)
      jcasType.jcas.throwFeatMissing("hitDictionaryValue", "edu.mayo.bmi.uima.pad.type.PADLocation");
    jcasType.ll_cas.ll_setStringValue(addr, ((PADLocation_Type)jcasType).casFeatCode_hitDictionaryValue, v);}    
   
    
  //*--------------*
  //* Feature: isStandAlone

  /** getter for isStandAlone - gets Identifies weather this term needs another dictionary hit to be considered as a valid hit. Currently this is being used for Unstable Angina which requires "Term" and "Location" to be present in the text to be considered as a valid hit.

Valid values are 0 and 1. Where, 0 is false (not standalone) and 1 is true (is standalone)
   * @generated */
  public int getIsStandAlone() {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_isStandAlone == null)
      jcasType.jcas.throwFeatMissing("isStandAlone", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((PADLocation_Type)jcasType).casFeatCode_isStandAlone);}
    
  /** setter for isStandAlone - sets Identifies weather this term needs another dictionary hit to be considered as a valid hit. Currently this is being used for Unstable Angina which requires "Term" and "Location" to be present in the text to be considered as a valid hit.

Valid values are 0 and 1. Where, 0 is false (not standalone) and 1 is true (is standalone) 
   * @generated */
  public void setIsStandAlone(int v) {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_isStandAlone == null)
      jcasType.jcas.throwFeatMissing("isStandAlone", "edu.mayo.bmi.uima.pad.type.PADLocation");
    jcasType.ll_cas.ll_setIntValue(addr, ((PADLocation_Type)jcasType).casFeatCode_isStandAlone, v);}    
   
    
  //*--------------*
  //* Feature: relatedTokens

  /** getter for relatedTokens - gets identifies the token that this PADTerm spans
   * @generated */
  public FSArray getRelatedTokens() {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_relatedTokens == null)
      jcasType.jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens)));}
    
  /** setter for relatedTokens - sets identifies the token that this PADTerm spans 
   * @generated */
  public void setRelatedTokens(FSArray v) {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_relatedTokens == null)
      jcasType.jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    jcasType.ll_cas.ll_setRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for relatedTokens - gets an indexed value - identifies the token that this PADTerm spans
   * @generated */
  public TOP getRelatedTokens(int i) {
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_relatedTokens == null)
      jcasType.jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens), i);
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens), i)));}

  /** indexed setter for relatedTokens - sets an indexed value - identifies the token that this PADTerm spans
   * @generated */
  public void setRelatedTokens(int i, TOP v) { 
    if (PADLocation_Type.featOkTst && ((PADLocation_Type)jcasType).casFeat_relatedTokens == null)
      jcasType.jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((PADLocation_Type)jcasType).casFeatCode_relatedTokens), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    