

/* First created by JCasGen Mon Jun 27 14:30:05 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * XML source: /home/tmill/Projects/cNLP/cTakes-Ext/Consituency Parser/desc/ConstituencyParserAnnotator.xml
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

    