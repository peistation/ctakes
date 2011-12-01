

/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.mipacq.annotation.type.CollectionRelation;


/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class CoreferenceChain extends CollectionRelation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(CoreferenceChain.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected CoreferenceChain() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CoreferenceChain(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CoreferenceChain(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: size

  /** getter for size - gets 
   * @generated */
  public int getSize() {
    if (CoreferenceChain_Type.featOkTst && ((CoreferenceChain_Type)jcasType).casFeat_size == null)
      jcasType.jcas.throwFeatMissing("size", "edu.mayo.bmi.uima.coref.type.CoreferenceChain");
    return jcasType.ll_cas.ll_getIntValue(addr, ((CoreferenceChain_Type)jcasType).casFeatCode_size);}
    
  /** setter for size - sets  
   * @generated */
  public void setSize(int v) {
    if (CoreferenceChain_Type.featOkTst && ((CoreferenceChain_Type)jcasType).casFeat_size == null)
      jcasType.jcas.throwFeatMissing("size", "edu.mayo.bmi.uima.coref.type.CoreferenceChain");
    jcasType.ll_cas.ll_setIntValue(addr, ((CoreferenceChain_Type)jcasType).casFeatCode_size, v);}    
  }

    