

/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class DateAttributeValue extends AttributeValue {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DateAttributeValue.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DateAttributeValue() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DateAttributeValue(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DateAttributeValue(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DateAttributeValue(JCas jcas, int begin, int end) {
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
  //* Feature: dateValue

  /** getter for dateValue - gets 
   * @generated */
  public String getDateValue() {
    if (DateAttributeValue_Type.featOkTst && ((DateAttributeValue_Type)jcasType).casFeat_dateValue == null)
      jcasType.jcas.throwFeatMissing("dateValue", "edu.mayo.bmi.uima.libsvm.type.DateAttributeValue");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DateAttributeValue_Type)jcasType).casFeatCode_dateValue);}
    
  /** setter for dateValue - sets  
   * @generated */
  public void setDateValue(String v) {
    if (DateAttributeValue_Type.featOkTst && ((DateAttributeValue_Type)jcasType).casFeat_dateValue == null)
      jcasType.jcas.throwFeatMissing("dateValue", "edu.mayo.bmi.uima.libsvm.type.DateAttributeValue");
    jcasType.ll_cas.ll_setStringValue(addr, ((DateAttributeValue_Type)jcasType).casFeatCode_dateValue, v);}    
  }

    