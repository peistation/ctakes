

/* First created by JCasGen Wed Jan 11 14:37:38 EST 2012 */
package edu.mayo.bmi.uima.core.type.util;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;


/** A brute force "hash" that stores multiple Pairs in a list. Equivalent to cTAKES: 
edu.mayo.bmi.uima.core.type.Properties
 * Updated by JCasGen Wed Jan 11 14:37:38 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class Hash extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Hash.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Hash() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Hash(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Hash(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: pairs

  /** getter for pairs - gets 
   * @generated */
  public FSList getPairs() {
    if (Hash_Type.featOkTst && ((Hash_Type)jcasType).casFeat_pairs == null)
      jcasType.jcas.throwFeatMissing("pairs", "edu.mayo.bmi.uima.core.type.util.Hash");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Hash_Type)jcasType).casFeatCode_pairs)));}
    
  /** setter for pairs - sets  
   * @generated */
  public void setPairs(FSList v) {
    if (Hash_Type.featOkTst && ((Hash_Type)jcasType).casFeat_pairs == null)
      jcasType.jcas.throwFeatMissing("pairs", "edu.mayo.bmi.uima.core.type.util.Hash");
    jcasType.ll_cas.ll_setRefValue(addr, ((Hash_Type)jcasType).casFeatCode_pairs, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    