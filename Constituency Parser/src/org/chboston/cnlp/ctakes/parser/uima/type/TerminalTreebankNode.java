

/* First created by JCasGen Thu Dec 01 13:26:31 EST 2011 */
package org.chboston.cnlp.ctakes.parser.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Dec 01 13:26:42 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/Constituency Parser/desc/TypeSystem.xml
 * @generated */
public class TerminalTreebankNode extends TreebankNode {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(TerminalTreebankNode.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TerminalTreebankNode() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TerminalTreebankNode(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TerminalTreebankNode(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TerminalTreebankNode(JCas jcas, int begin, int end) {
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
  //* Feature: index

  /** getter for index - gets 
   * @generated */
  public int getIndex() {
    if (TerminalTreebankNode_Type.featOkTst && ((TerminalTreebankNode_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode");
    return jcasType.ll_cas.ll_getIntValue(addr, ((TerminalTreebankNode_Type)jcasType).casFeatCode_index);}
    
  /** setter for index - sets  
   * @generated */
  public void setIndex(int v) {
    if (TerminalTreebankNode_Type.featOkTst && ((TerminalTreebankNode_Type)jcasType).casFeat_index == null)
      jcasType.jcas.throwFeatMissing("index", "org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode");
    jcasType.ll_cas.ll_setIntValue(addr, ((TerminalTreebankNode_Type)jcasType).casFeatCode_index, v);}    
   
    
  //*--------------*
  //* Feature: tokenIndex

  /** getter for tokenIndex - gets 
   * @generated */
  public int getTokenIndex() {
    if (TerminalTreebankNode_Type.featOkTst && ((TerminalTreebankNode_Type)jcasType).casFeat_tokenIndex == null)
      jcasType.jcas.throwFeatMissing("tokenIndex", "org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode");
    return jcasType.ll_cas.ll_getIntValue(addr, ((TerminalTreebankNode_Type)jcasType).casFeatCode_tokenIndex);}
    
  /** setter for tokenIndex - sets  
   * @generated */
  public void setTokenIndex(int v) {
    if (TerminalTreebankNode_Type.featOkTst && ((TerminalTreebankNode_Type)jcasType).casFeat_tokenIndex == null)
      jcasType.jcas.throwFeatMissing("tokenIndex", "org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode");
    jcasType.ll_cas.ll_setIntValue(addr, ((TerminalTreebankNode_Type)jcasType).casFeatCode_tokenIndex, v);}    
  }

    