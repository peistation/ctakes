

/* First created by JCasGen Wed Jan 11 14:37:38 EST 2012 */
package edu.mayo.bmi.uima.core.type.syntax;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Equivalent to cTAKES: edu.mayo.bmi.uima.core.type.WordToken
 * Updated by JCasGen Wed Jan 11 14:37:38 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class WordToken extends BaseToken {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(WordToken.class);
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
  protected WordToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public WordToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public WordToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public WordToken(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: capitalization

  /** getter for capitalization - gets 
   * @generated */
  public int getCapitalization() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_capitalization == null)
      jcasType.jcas.throwFeatMissing("capitalization", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WordToken_Type)jcasType).casFeatCode_capitalization);}
    
  /** setter for capitalization - sets  
   * @generated */
  public void setCapitalization(int v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_capitalization == null)
      jcasType.jcas.throwFeatMissing("capitalization", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    jcasType.ll_cas.ll_setIntValue(addr, ((WordToken_Type)jcasType).casFeatCode_capitalization, v);}    
   
    
  //*--------------*
  //* Feature: numPosition

  /** getter for numPosition - gets 
   * @generated */
  public int getNumPosition() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_numPosition == null)
      jcasType.jcas.throwFeatMissing("numPosition", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WordToken_Type)jcasType).casFeatCode_numPosition);}
    
  /** setter for numPosition - sets  
   * @generated */
  public void setNumPosition(int v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_numPosition == null)
      jcasType.jcas.throwFeatMissing("numPosition", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    jcasType.ll_cas.ll_setIntValue(addr, ((WordToken_Type)jcasType).casFeatCode_numPosition, v);}    
   
    
  //*--------------*
  //* Feature: suggestion

  /** getter for suggestion - gets 
   * @generated */
  public String getSuggestion() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_suggestion == null)
      jcasType.jcas.throwFeatMissing("suggestion", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordToken_Type)jcasType).casFeatCode_suggestion);}
    
  /** setter for suggestion - sets  
   * @generated */
  public void setSuggestion(String v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_suggestion == null)
      jcasType.jcas.throwFeatMissing("suggestion", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordToken_Type)jcasType).casFeatCode_suggestion, v);}    
   
    
  //*--------------*
  //* Feature: canonicalForm

  /** getter for canonicalForm - gets 
   * @generated */
  public String getCanonicalForm() {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_canonicalForm == null)
      jcasType.jcas.throwFeatMissing("canonicalForm", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordToken_Type)jcasType).casFeatCode_canonicalForm);}
    
  /** setter for canonicalForm - sets  
   * @generated */
  public void setCanonicalForm(String v) {
    if (WordToken_Type.featOkTst && ((WordToken_Type)jcasType).casFeat_canonicalForm == null)
      jcasType.jcas.throwFeatMissing("canonicalForm", "edu.mayo.bmi.uima.core.type.syntax.WordToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordToken_Type)jcasType).casFeatCode_canonicalForm, v);}    
  }

    