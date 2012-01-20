

/* First created by JCasGen Thu Jan 19 17:46:27 EST 2012 */
package edu.mayo.bmi.uima.core.type.syntax;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Equivalent to cTAKES: edu.mayo.bmi.uima.core.type.NumToken
 * Updated by JCasGen Fri Jan 20 10:31:49 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class NumToken extends BaseToken {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NumToken.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NumToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NumToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NumToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NumToken(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: numType

  /** getter for numType - gets 
   * @generated */
  public int getNumType() {
    if (NumToken_Type.featOkTst && ((NumToken_Type)jcasType).casFeat_numType == null)
      jcasType.jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.syntax.NumToken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NumToken_Type)jcasType).casFeatCode_numType);}
    
  /** setter for numType - sets  
   * @generated */
  public void setNumType(int v) {
    if (NumToken_Type.featOkTst && ((NumToken_Type)jcasType).casFeat_numType == null)
      jcasType.jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.syntax.NumToken");
    jcasType.ll_cas.ll_setIntValue(addr, ((NumToken_Type)jcasType).casFeatCode_numType, v);}    
  }

    