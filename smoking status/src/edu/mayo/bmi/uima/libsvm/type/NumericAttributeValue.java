

/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Corresponds to the ARFF numeric attributes.
 * Updated by JCasGen Thu Nov 18 10:02:32 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class NumericAttributeValue extends AttributeValue {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NumericAttributeValue.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NumericAttributeValue() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NumericAttributeValue(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NumericAttributeValue(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NumericAttributeValue(JCas jcas, int begin, int end) {
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
  //* Feature: numericValue

  /** getter for numericValue - gets 
   * @generated */
  public String getNumericValue() {
    if (NumericAttributeValue_Type.featOkTst && ((NumericAttributeValue_Type)jcasType).casFeat_numericValue == null)
      jcasType.jcas.throwFeatMissing("numericValue", "edu.mayo.bmi.uima.libsvm.type.NumericAttributeValue");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NumericAttributeValue_Type)jcasType).casFeatCode_numericValue);}
    
  /** setter for numericValue - sets  
   * @generated */
  public void setNumericValue(String v) {
    if (NumericAttributeValue_Type.featOkTst && ((NumericAttributeValue_Type)jcasType).casFeat_numericValue == null)
      jcasType.jcas.throwFeatMissing("numericValue", "edu.mayo.bmi.uima.libsvm.type.NumericAttributeValue");
    jcasType.ll_cas.ll_setStringValue(addr, ((NumericAttributeValue_Type)jcasType).casFeatCode_numericValue, v);}    
  }

    