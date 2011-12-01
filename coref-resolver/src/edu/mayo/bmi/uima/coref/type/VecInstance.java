

/* First created by JCasGen Thu Dec 01 13:17:33 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Thu Dec 01 13:17:33 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/VecInst.xml
 * @generated */
public class VecInstance extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(VecInstance.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected VecInstance() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public VecInstance(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public VecInstance(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: vector

  /** getter for vector - gets 
   * @generated */
  public String getVector() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_vector);}
    
  /** setter for vector - sets  
   * @generated */
  public void setVector(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_vector, v);}    
   
    
  //*--------------*
  //* Feature: a1

  /** getter for a1 - gets 
   * @generated */
  public String getA1() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_a1 == null)
      jcasType.jcas.throwFeatMissing("a1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_a1);}
    
  /** setter for a1 - sets  
   * @generated */
  public void setA1(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_a1 == null)
      jcasType.jcas.throwFeatMissing("a1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_a1, v);}    
   
    
  //*--------------*
  //* Feature: a2

  /** getter for a2 - gets 
   * @generated */
  public String getA2() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_a2 == null)
      jcasType.jcas.throwFeatMissing("a2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_a2);}
    
  /** setter for a2 - sets  
   * @generated */
  public void setA2(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_a2 == null)
      jcasType.jcas.throwFeatMissing("a2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_a2, v);}    
   
    
  //*--------------*
  //* Feature: offset1

  /** getter for offset1 - gets 
   * @generated */
  public String getOffset1() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_offset1 == null)
      jcasType.jcas.throwFeatMissing("offset1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_offset1);}
    
  /** setter for offset1 - sets  
   * @generated */
  public void setOffset1(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_offset1 == null)
      jcasType.jcas.throwFeatMissing("offset1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_offset1, v);}    
   
    
  //*--------------*
  //* Feature: offset2

  /** getter for offset2 - gets 
   * @generated */
  public String getOffset2() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_offset2 == null)
      jcasType.jcas.throwFeatMissing("offset2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_offset2);}
    
  /** setter for offset2 - sets  
   * @generated */
  public void setOffset2(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_offset2 == null)
      jcasType.jcas.throwFeatMissing("offset2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_offset2, v);}    
   
    
  //*--------------*
  //* Feature: classifier

  /** getter for classifier - gets 
   * @generated */
  public String getClassifier() {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_classifier == null)
      jcasType.jcas.throwFeatMissing("classifier", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_classifier);}
    
  /** setter for classifier - sets  
   * @generated */
  public void setClassifier(String v) {
    if (VecInstance_Type.featOkTst && ((VecInstance_Type)jcasType).casFeat_classifier == null)
      jcasType.jcas.throwFeatMissing("classifier", "edu.mayo.bmi.uima.coref.type.VecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((VecInstance_Type)jcasType).casFeatCode_classifier, v);}    
  }

    