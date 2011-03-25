

/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class AttributeValue extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(AttributeValue.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AttributeValue() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public AttributeValue(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public AttributeValue(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public AttributeValue(JCas jcas, int begin, int end) {
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
  //* Feature: attributeName

  /** getter for attributeName - gets 
   * @generated */
  public String getAttributeName() {
    if (AttributeValue_Type.featOkTst && ((AttributeValue_Type)jcasType).casFeat_attributeName == null)
      jcasType.jcas.throwFeatMissing("attributeName", "edu.mayo.bmi.uima.libsvm.type.AttributeValue");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AttributeValue_Type)jcasType).casFeatCode_attributeName);}
    
  /** setter for attributeName - sets  
   * @generated */
  public void setAttributeName(String v) {
    if (AttributeValue_Type.featOkTst && ((AttributeValue_Type)jcasType).casFeat_attributeName == null)
      jcasType.jcas.throwFeatMissing("attributeName", "edu.mayo.bmi.uima.libsvm.type.AttributeValue");
    jcasType.ll_cas.ll_setStringValue(addr, ((AttributeValue_Type)jcasType).casFeatCode_attributeName, v);}    
  }

    