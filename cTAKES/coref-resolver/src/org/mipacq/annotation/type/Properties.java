

/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * XML source: /home/tmill/cleanroomworkspace/ctakes/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class Properties extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Properties.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Properties() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Properties(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Properties(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: polarity

  /** getter for polarity - gets 
   * @generated */
  public boolean getPolarity() {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "org.mipacq.annotation.type.Properties");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Properties_Type)jcasType).casFeatCode_polarity);}
    
  /** setter for polarity - sets  
   * @generated */
  public void setPolarity(boolean v) {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_polarity == null)
      jcasType.jcas.throwFeatMissing("polarity", "org.mipacq.annotation.type.Properties");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Properties_Type)jcasType).casFeatCode_polarity, v);}    
   
    
  //*--------------*
  //* Feature: certainty

  /** getter for certainty - gets 
   * @generated */
  public String getCertainty() {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "org.mipacq.annotation.type.Properties");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Properties_Type)jcasType).casFeatCode_certainty);}
    
  /** setter for certainty - sets  
   * @generated */
  public void setCertainty(String v) {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "org.mipacq.annotation.type.Properties");
    jcasType.ll_cas.ll_setStringValue(addr, ((Properties_Type)jcasType).casFeatCode_certainty, v);}    
  }

    