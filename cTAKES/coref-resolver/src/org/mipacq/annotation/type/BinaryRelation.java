

/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class BinaryRelation extends Relation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BinaryRelation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BinaryRelation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BinaryRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BinaryRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: arg1

  /** getter for arg1 - gets 
   * @generated */
  public RelationArgument getArg1() {
    if (BinaryRelation_Type.featOkTst && ((BinaryRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "org.mipacq.annotation.type.BinaryRelation");
    return (RelationArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BinaryRelation_Type)jcasType).casFeatCode_arg1)));}
    
  /** setter for arg1 - sets  
   * @generated */
  public void setArg1(RelationArgument v) {
    if (BinaryRelation_Type.featOkTst && ((BinaryRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "org.mipacq.annotation.type.BinaryRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((BinaryRelation_Type)jcasType).casFeatCode_arg1, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: arg2

  /** getter for arg2 - gets 
   * @generated */
  public RelationArgument getArg2() {
    if (BinaryRelation_Type.featOkTst && ((BinaryRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "org.mipacq.annotation.type.BinaryRelation");
    return (RelationArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BinaryRelation_Type)jcasType).casFeatCode_arg2)));}
    
  /** setter for arg2 - sets  
   * @generated */
  public void setArg2(RelationArgument v) {
    if (BinaryRelation_Type.featOkTst && ((BinaryRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "org.mipacq.annotation.type.BinaryRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((BinaryRelation_Type)jcasType).casFeatCode_arg2, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    