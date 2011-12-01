

/* First created by JCasGen Thu Dec 01 13:17:32 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Thu Dec 01 13:17:33 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/VecInst.xml
 * @generated */
public class AnaphoricityVecInstance extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(AnaphoricityVecInstance.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AnaphoricityVecInstance() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public AnaphoricityVecInstance(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public AnaphoricityVecInstance(JCas jcas) {
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
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_vector);}
    
  /** setter for vector - sets  
   * @generated */
  public void setVector(String v) {
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_vector == null)
      jcasType.jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_vector, v);}    
   
    
  //*--------------*
  //* Feature: m

  /** getter for m - gets 
   * @generated */
  public String getM() {
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_m == null)
      jcasType.jcas.throwFeatMissing("m", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_m);}
    
  /** setter for m - sets  
   * @generated */
  public void setM(String v) {
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_m == null)
      jcasType.jcas.throwFeatMissing("m", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_m, v);}    
   
    
  //*--------------*
  //* Feature: offset

  /** getter for offset - gets 
   * @generated */
  public String getOffset() {
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_offset == null)
      jcasType.jcas.throwFeatMissing("offset", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_offset);}
    
  /** setter for offset - sets  
   * @generated */
  public void setOffset(String v) {
    if (AnaphoricityVecInstance_Type.featOkTst && ((AnaphoricityVecInstance_Type)jcasType).casFeat_offset == null)
      jcasType.jcas.throwFeatMissing("offset", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    jcasType.ll_cas.ll_setStringValue(addr, ((AnaphoricityVecInstance_Type)jcasType).casFeatCode_offset, v);}    
  }

    