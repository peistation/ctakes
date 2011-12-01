

/* First created by JCasGen Thu Dec 01 13:26:31 EST 2011 */
package org.chboston.cnlp.ctakes.parser.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Thu Dec 01 13:26:42 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/Constituency Parser/desc/TypeSystem.xml
 * @generated */
public class TopTreebankNode extends TreebankNode {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(TopTreebankNode.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TopTreebankNode() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public TopTreebankNode(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public TopTreebankNode(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public TopTreebankNode(JCas jcas, int begin, int end) {
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
  //* Feature: treebankParse

  /** getter for treebankParse - gets 
   * @generated */
  public String getTreebankParse() {
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_treebankParse == null)
      jcasType.jcas.throwFeatMissing("treebankParse", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_treebankParse);}
    
  /** setter for treebankParse - sets  
   * @generated */
  public void setTreebankParse(String v) {
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_treebankParse == null)
      jcasType.jcas.throwFeatMissing("treebankParse", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    jcasType.ll_cas.ll_setStringValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_treebankParse, v);}    
   
    
  //*--------------*
  //* Feature: terminals

  /** getter for terminals - gets 
   * @generated */
  public FSArray getTerminals() {
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_terminals == null)
      jcasType.jcas.throwFeatMissing("terminals", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals)));}
    
  /** setter for terminals - sets  
   * @generated */
  public void setTerminals(FSArray v) {
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_terminals == null)
      jcasType.jcas.throwFeatMissing("terminals", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    jcasType.ll_cas.ll_setRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for terminals - gets an indexed value - 
   * @generated */
  public TerminalTreebankNode getTerminals(int i) {
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_terminals == null)
      jcasType.jcas.throwFeatMissing("terminals", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals), i);
    return (TerminalTreebankNode)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals), i)));}

  /** indexed setter for terminals - sets an indexed value - 
   * @generated */
  public void setTerminals(int i, TerminalTreebankNode v) { 
    if (TopTreebankNode_Type.featOkTst && ((TopTreebankNode_Type)jcasType).casFeat_terminals == null)
      jcasType.jcas.throwFeatMissing("terminals", "org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopTreebankNode_Type)jcasType).casFeatCode_terminals), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    