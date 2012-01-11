

/* First created by JCasGen Wed Jan 11 14:37:37 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** A normalized form for dates, namely with day, month, and year.  A subtype of Element but may not use all inherited attributes.
 * Updated by JCasGen Wed Jan 11 14:37:37 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class Date extends Element {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Date.class);
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
  protected Date() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Date(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Date(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: day

  /** getter for day - gets 
   * @generated */
  public String getDay() {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_day == null)
      jcasType.jcas.throwFeatMissing("day", "edu.mayo.bmi.uima.core.type.refsem.Date");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Date_Type)jcasType).casFeatCode_day);}
    
  /** setter for day - sets  
   * @generated */
  public void setDay(String v) {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_day == null)
      jcasType.jcas.throwFeatMissing("day", "edu.mayo.bmi.uima.core.type.refsem.Date");
    jcasType.ll_cas.ll_setStringValue(addr, ((Date_Type)jcasType).casFeatCode_day, v);}    
   
    
  //*--------------*
  //* Feature: month

  /** getter for month - gets 
   * @generated */
  public String getMonth() {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_month == null)
      jcasType.jcas.throwFeatMissing("month", "edu.mayo.bmi.uima.core.type.refsem.Date");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Date_Type)jcasType).casFeatCode_month);}
    
  /** setter for month - sets  
   * @generated */
  public void setMonth(String v) {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_month == null)
      jcasType.jcas.throwFeatMissing("month", "edu.mayo.bmi.uima.core.type.refsem.Date");
    jcasType.ll_cas.ll_setStringValue(addr, ((Date_Type)jcasType).casFeatCode_month, v);}    
   
    
  //*--------------*
  //* Feature: year

  /** getter for year - gets 
   * @generated */
  public String getYear() {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_year == null)
      jcasType.jcas.throwFeatMissing("year", "edu.mayo.bmi.uima.core.type.refsem.Date");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Date_Type)jcasType).casFeatCode_year);}
    
  /** setter for year - sets  
   * @generated */
  public void setYear(String v) {
    if (Date_Type.featOkTst && ((Date_Type)jcasType).casFeat_year == null)
      jcasType.jcas.throwFeatMissing("year", "edu.mayo.bmi.uima.core.type.refsem.Date");
    jcasType.ll_cas.ll_setStringValue(addr, ((Date_Type)jcasType).casFeatCode_year, v);}    
  }

    