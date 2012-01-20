

/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.structured;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** Demographic information about the patient in a clinical document.  Attributes come from Clinical Element Models (CEMs).
 * Updated by JCasGen Fri Jan 20 10:31:41 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class Demographics extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Demographics.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Demographics() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Demographics(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Demographics(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: birthDate

  /** getter for birthDate - gets 
   * @generated */
  public String getBirthDate() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_birthDate == null)
      jcasType.jcas.throwFeatMissing("birthDate", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_birthDate);}
    
  /** setter for birthDate - sets  
   * @generated */
  public void setBirthDate(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_birthDate == null)
      jcasType.jcas.throwFeatMissing("birthDate", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_birthDate, v);}    
   
    
  //*--------------*
  //* Feature: deathDate

  /** getter for deathDate - gets 
   * @generated */
  public String getDeathDate() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_deathDate == null)
      jcasType.jcas.throwFeatMissing("deathDate", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_deathDate);}
    
  /** setter for deathDate - sets  
   * @generated */
  public void setDeathDate(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_deathDate == null)
      jcasType.jcas.throwFeatMissing("deathDate", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_deathDate, v);}    
   
    
  //*--------------*
  //* Feature: gender

  /** getter for gender - gets 
   * @generated */
  public String getGender() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_gender);}
    
  /** setter for gender - sets  
   * @generated */
  public void setGender(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_gender, v);}    
   
    
  //*--------------*
  //* Feature: firstName

  /** getter for firstName - gets 
   * @generated */
  public String getFirstName() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_firstName == null)
      jcasType.jcas.throwFeatMissing("firstName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_firstName);}
    
  /** setter for firstName - sets  
   * @generated */
  public void setFirstName(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_firstName == null)
      jcasType.jcas.throwFeatMissing("firstName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_firstName, v);}    
   
    
  //*--------------*
  //* Feature: middleName

  /** getter for middleName - gets 
   * @generated */
  public String getMiddleName() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_middleName == null)
      jcasType.jcas.throwFeatMissing("middleName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_middleName);}
    
  /** setter for middleName - sets  
   * @generated */
  public void setMiddleName(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_middleName == null)
      jcasType.jcas.throwFeatMissing("middleName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_middleName, v);}    
   
    
  //*--------------*
  //* Feature: lastName

  /** getter for lastName - gets 
   * @generated */
  public String getLastName() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_lastName == null)
      jcasType.jcas.throwFeatMissing("lastName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_lastName);}
    
  /** setter for lastName - sets  
   * @generated */
  public void setLastName(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_lastName == null)
      jcasType.jcas.throwFeatMissing("lastName", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_lastName, v);}    
   
    
  //*--------------*
  //* Feature: firstNameSoundex

  /** getter for firstNameSoundex - gets 
   * @generated */
  public String getFirstNameSoundex() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_firstNameSoundex == null)
      jcasType.jcas.throwFeatMissing("firstNameSoundex", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_firstNameSoundex);}
    
  /** setter for firstNameSoundex - sets  
   * @generated */
  public void setFirstNameSoundex(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_firstNameSoundex == null)
      jcasType.jcas.throwFeatMissing("firstNameSoundex", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_firstNameSoundex, v);}    
   
    
  //*--------------*
  //* Feature: lastNameSoundex

  /** getter for lastNameSoundex - gets 
   * @generated */
  public String getLastNameSoundex() {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_lastNameSoundex == null)
      jcasType.jcas.throwFeatMissing("lastNameSoundex", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_lastNameSoundex);}
    
  /** setter for lastNameSoundex - sets  
   * @generated */
  public void setLastNameSoundex(String v) {
    if (Demographics_Type.featOkTst && ((Demographics_Type)jcasType).casFeat_lastNameSoundex == null)
      jcasType.jcas.throwFeatMissing("lastNameSoundex", "edu.mayo.bmi.uima.core.type.structured.Demographics");
    jcasType.ll_cas.ll_setStringValue(addr, ((Demographics_Type)jcasType).casFeatCode_lastNameSoundex, v);}    
  }

    