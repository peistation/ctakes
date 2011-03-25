

/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 18 10:02:32 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class StringAttributeValue extends AttributeValue {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(StringAttributeValue.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected StringAttributeValue() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public StringAttributeValue(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public StringAttributeValue(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public StringAttributeValue(JCas jcas, int begin, int end) {
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
  //* Feature: attributeStringValue

  /** getter for attributeStringValue - gets 
   * @generated */
  public String getAttributeStringValue() {
    if (StringAttributeValue_Type.featOkTst && ((StringAttributeValue_Type)jcasType).casFeat_attributeStringValue == null)
      jcasType.jcas.throwFeatMissing("attributeStringValue", "edu.mayo.bmi.uima.libsvm.type.StringAttributeValue");
    return jcasType.ll_cas.ll_getStringValue(addr, ((StringAttributeValue_Type)jcasType).casFeatCode_attributeStringValue);}
    
  /** setter for attributeStringValue - sets  
   * @generated */
  public void setAttributeStringValue(String v) {
    if (StringAttributeValue_Type.featOkTst && ((StringAttributeValue_Type)jcasType).casFeat_attributeStringValue == null)
      jcasType.jcas.throwFeatMissing("attributeStringValue", "edu.mayo.bmi.uima.libsvm.type.StringAttributeValue");
    jcasType.ll_cas.ll_setStringValue(addr, ((StringAttributeValue_Type)jcasType).casFeatCode_attributeStringValue, v);}    
  }

    