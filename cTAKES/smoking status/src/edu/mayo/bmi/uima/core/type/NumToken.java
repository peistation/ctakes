

/* First created by JCasGen Thu Nov 18 10:02:30 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 18 10:02:30 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
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
  protected NumToken() {}
    
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
      jcasType.jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.NumToken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NumToken_Type)jcasType).casFeatCode_numType);}
    
  /** setter for numType - sets  
   * @generated */
  public void setNumType(int v) {
    if (NumToken_Type.featOkTst && ((NumToken_Type)jcasType).casFeat_numType == null)
      jcasType.jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.NumToken");
    jcasType.ll_cas.ll_setIntValue(addr, ((NumToken_Type)jcasType).casFeatCode_numType, v);}    
  }

    