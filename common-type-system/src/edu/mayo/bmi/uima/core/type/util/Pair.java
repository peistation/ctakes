

/* First created by JCasGen Thu Jan 19 17:46:27 EST 2012 */
package edu.mayo.bmi.uima.core.type.util;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** An Attribute-Value tuple. Equivalent to cTAKES: 
edu.mayo.bmi.uima.core.type.Property
 * Updated by JCasGen Fri Jan 20 10:31:57 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
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
  protected Pair() {/* intentionally empty block */}
    
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
  //* Feature: attribute

  /** getter for attribute - gets 
   * @generated */
  public TOP getAttribute() {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_attribute == null)
      jcasType.jcas.throwFeatMissing("attribute", "edu.mayo.bmi.uima.core.type.util.Pair");
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pair_Type)jcasType).casFeatCode_attribute)));}
    
  /** setter for attribute - sets  
   * @generated */
  public void setAttribute(TOP v) {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_attribute == null)
      jcasType.jcas.throwFeatMissing("attribute", "edu.mayo.bmi.uima.core.type.util.Pair");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pair_Type)jcasType).casFeatCode_attribute, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   * @generated */
  public TOP getValue() {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.util.Pair");
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pair_Type)jcasType).casFeatCode_value)));}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(TOP v) {
    if (Pair_Type.featOkTst && ((Pair_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.util.Pair");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pair_Type)jcasType).casFeatCode_value, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    