

/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class CoreferenceRelation extends BinaryRelation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(CoreferenceRelation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected CoreferenceRelation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CoreferenceRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CoreferenceRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: coref_prob

  /** getter for coref_prob - gets 
   * @generated */
  public double getCoref_prob() {
    if (CoreferenceRelation_Type.featOkTst && ((CoreferenceRelation_Type)jcasType).casFeat_coref_prob == null)
      jcasType.jcas.throwFeatMissing("coref_prob", "org.mipacq.annotation.type.CoreferenceRelation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((CoreferenceRelation_Type)jcasType).casFeatCode_coref_prob);}
    
  /** setter for coref_prob - sets  
   * @generated */
  public void setCoref_prob(double v) {
    if (CoreferenceRelation_Type.featOkTst && ((CoreferenceRelation_Type)jcasType).casFeat_coref_prob == null)
      jcasType.jcas.throwFeatMissing("coref_prob", "org.mipacq.annotation.type.CoreferenceRelation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((CoreferenceRelation_Type)jcasType).casFeatCode_coref_prob, v);}    
  }

    