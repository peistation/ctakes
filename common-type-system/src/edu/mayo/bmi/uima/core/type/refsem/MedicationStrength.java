

/* First created by JCasGen Wed Jan 11 14:37:37 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Wed Jan 11 14:37:37 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class MedicationStrength extends Attribute {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(MedicationStrength.class);
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
  protected MedicationStrength() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public MedicationStrength(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public MedicationStrength(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: number

  /** getter for number - gets 
   * @generated */
  public String getNumber() {
    if (MedicationStrength_Type.featOkTst && ((MedicationStrength_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "edu.mayo.bmi.uima.core.type.refsem.MedicationStrength");
    return jcasType.ll_cas.ll_getStringValue(addr, ((MedicationStrength_Type)jcasType).casFeatCode_number);}
    
  /** setter for number - sets  
   * @generated */
  public void setNumber(String v) {
    if (MedicationStrength_Type.featOkTst && ((MedicationStrength_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "edu.mayo.bmi.uima.core.type.refsem.MedicationStrength");
    jcasType.ll_cas.ll_setStringValue(addr, ((MedicationStrength_Type)jcasType).casFeatCode_number, v);}    
   
    
  //*--------------*
  //* Feature: unit

  /** getter for unit - gets 
   * @generated */
  public String getUnit() {
    if (MedicationStrength_Type.featOkTst && ((MedicationStrength_Type)jcasType).casFeat_unit == null)
      jcasType.jcas.throwFeatMissing("unit", "edu.mayo.bmi.uima.core.type.refsem.MedicationStrength");
    return jcasType.ll_cas.ll_getStringValue(addr, ((MedicationStrength_Type)jcasType).casFeatCode_unit);}
    
  /** setter for unit - sets  
   * @generated */
  public void setUnit(String v) {
    if (MedicationStrength_Type.featOkTst && ((MedicationStrength_Type)jcasType).casFeat_unit == null)
      jcasType.jcas.throwFeatMissing("unit", "edu.mayo.bmi.uima.core.type.refsem.MedicationStrength");
    jcasType.ll_cas.ll_setStringValue(addr, ((MedicationStrength_Type)jcasType).casFeatCode_unit, v);}    
  }

    