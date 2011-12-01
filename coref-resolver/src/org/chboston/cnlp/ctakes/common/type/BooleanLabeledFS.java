

/* First created by JCasGen Tue Nov 22 14:28:43 EST 2011 */
package org.chboston.cnlp.ctakes.common.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * XML source: /home/tmill/cleanroomworkspace/ctakes/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class BooleanLabeledFS extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BooleanLabeledFS.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BooleanLabeledFS() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BooleanLabeledFS(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BooleanLabeledFS(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   * @generated */
  public boolean getLabel() {
    if (BooleanLabeledFS_Type.featOkTst && ((BooleanLabeledFS_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((BooleanLabeledFS_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   * @generated */
  public void setLabel(boolean v) {
    if (BooleanLabeledFS_Type.featOkTst && ((BooleanLabeledFS_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((BooleanLabeledFS_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: feature

  /** getter for feature - gets 
   * @generated */
  public TOP getFeature() {
    if (BooleanLabeledFS_Type.featOkTst && ((BooleanLabeledFS_Type)jcasType).casFeat_feature == null)
      jcasType.jcas.throwFeatMissing("feature", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BooleanLabeledFS_Type)jcasType).casFeatCode_feature)));}
    
  /** setter for feature - sets  
   * @generated */
  public void setFeature(TOP v) {
    if (BooleanLabeledFS_Type.featOkTst && ((BooleanLabeledFS_Type)jcasType).casFeat_feature == null)
      jcasType.jcas.throwFeatMissing("feature", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    jcasType.ll_cas.ll_setRefValue(addr, ((BooleanLabeledFS_Type)jcasType).casFeatCode_feature, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    