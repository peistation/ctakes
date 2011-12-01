

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
public class DiscoveryTechnique extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DiscoveryTechnique.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DiscoveryTechnique() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DiscoveryTechnique(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DiscoveryTechnique(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: technique

  /** getter for technique - gets 
   * @generated */
  public String getTechnique() {
    if (DiscoveryTechnique_Type.featOkTst && ((DiscoveryTechnique_Type)jcasType).casFeat_technique == null)
      jcasType.jcas.throwFeatMissing("technique", "org.mipacq.annotation.type.DiscoveryTechnique");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DiscoveryTechnique_Type)jcasType).casFeatCode_technique);}
    
  /** setter for technique - sets  
   * @generated */
  public void setTechnique(String v) {
    if (DiscoveryTechnique_Type.featOkTst && ((DiscoveryTechnique_Type)jcasType).casFeat_technique == null)
      jcasType.jcas.throwFeatMissing("technique", "org.mipacq.annotation.type.DiscoveryTechnique");
    jcasType.ll_cas.ll_setStringValue(addr, ((DiscoveryTechnique_Type)jcasType).casFeatCode_technique, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets 
   * @generated */
  public double getConfidence() {
    if (DiscoveryTechnique_Type.featOkTst && ((DiscoveryTechnique_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "org.mipacq.annotation.type.DiscoveryTechnique");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((DiscoveryTechnique_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets  
   * @generated */
  public void setConfidence(double v) {
    if (DiscoveryTechnique_Type.featOkTst && ((DiscoveryTechnique_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "org.mipacq.annotation.type.DiscoveryTechnique");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((DiscoveryTechnique_Type)jcasType).casFeatCode_confidence, v);}    
  }

    