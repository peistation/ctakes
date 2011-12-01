

/* First created by JCasGen Mon Jun 27 17:09:54 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * XML source: /home/tmill/Projects/cNLP/cTakes-Ext/Consituency Parser/desc/ConstituencyParserAnnotator.xml
 * @generated */
public class Property extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Property.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Property() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Property(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Property(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Property(JCas jcas, int begin, int end) {
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
  //* Feature: key

  /** getter for key - gets 
   * @generated */
  public String getKey() {
    if (Property_Type.featOkTst && ((Property_Type)jcasType).casFeat_key == null)
      jcasType.jcas.throwFeatMissing("key", "edu.mayo.bmi.uima.core.type.Property");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Property_Type)jcasType).casFeatCode_key);}
    
  /** setter for key - sets  
   * @generated */
  public void setKey(String v) {
    if (Property_Type.featOkTst && ((Property_Type)jcasType).casFeat_key == null)
      jcasType.jcas.throwFeatMissing("key", "edu.mayo.bmi.uima.core.type.Property");
    jcasType.ll_cas.ll_setStringValue(addr, ((Property_Type)jcasType).casFeatCode_key, v);}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   * @generated */
  public String getValue() {
    if (Property_Type.featOkTst && ((Property_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.Property");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Property_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(String v) {
    if (Property_Type.featOkTst && ((Property_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.Property");
    jcasType.ll_cas.ll_setStringValue(addr, ((Property_Type)jcasType).casFeatCode_value, v);}    
  }

    