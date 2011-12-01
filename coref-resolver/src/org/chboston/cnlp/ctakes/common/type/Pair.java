

/* First created by JCasGen Tue Nov 22 14:28:23 EST 2011 */
package org.chboston.cnlp.ctakes.common.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * XML source: /home/tmill/cleanroomworkspace/ctakes/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class Pair extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Pair.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Pair() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Pair(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Pair(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: p1

  /** getter for p1 - gets 
   * @generated */
  public TOP getP1() {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_p1 == null)
      jcasType.jcas.throwFeatMissing("p1", "org.chboston.cnlp.ctakes.common.type.Pair");
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pair_Type)jcasType).casFeatCode_p1)));}
    
  /** setter for p1 - sets  
   * @generated */
  public void setP1(TOP v) {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_p1 == null)
      jcasType.jcas.throwFeatMissing("p1", "org.chboston.cnlp.ctakes.common.type.Pair");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pair_Type)jcasType).casFeatCode_p1, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: p2

  /** getter for p2 - gets 
   * @generated */
  public TOP getP2() {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_p2 == null)
      jcasType.jcas.throwFeatMissing("p2", "org.chboston.cnlp.ctakes.common.type.Pair");
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pair_Type)jcasType).casFeatCode_p2)));}
    
  /** setter for p2 - sets  
   * @generated */
  public void setP2(TOP v) {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_p2 == null)
      jcasType.jcas.throwFeatMissing("p2", "org.chboston.cnlp.ctakes.common.type.Pair");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pair_Type)jcasType).casFeatCode_p2, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    