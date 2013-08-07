

/* First created by JCasGen Mon May 23 12:04:33 EDT 2011 */
package org.mitre.medfacts.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Feb 28 11:18:13 EST 2012
 * XML source: /work/medfacts/sandbox/svn/medfact_branch/medfacts-uima-assertion-pear/desc/medfactsTypeSystem.xml
 * @generated */
public class Assertion extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Assertion.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Assertion() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Assertion(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Assertion(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Assertion(JCas jcas, int begin, int end) {
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
  //* Feature: assertionType

  /** getter for assertionType - gets assertion type -- one of present, absent, possible, conditional, hypothetical, or assocated_with_someone_else
   * @generated */
  public String getAssertionType() {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_assertionType == null)
      jcasType.jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Assertion_Type)jcasType).casFeatCode_assertionType);}
    
  /** setter for assertionType - sets assertion type -- one of present, absent, possible, conditional, hypothetical, or assocated_with_someone_else 
   * @generated */
  public void setAssertionType(String v) {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_assertionType == null)
      jcasType.jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    jcasType.ll_cas.ll_setStringValue(addr, ((Assertion_Type)jcasType).casFeatCode_assertionType, v);}    
   
    
  //*--------------*
  //* Feature: associatedConcept

  /** getter for associatedConcept - gets 
   * @generated */
  public Concept getAssociatedConcept() {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_associatedConcept == null)
      jcasType.jcas.throwFeatMissing("associatedConcept", "org.mitre.medfacts.types.Assertion");
    return (Concept)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Assertion_Type)jcasType).casFeatCode_associatedConcept)));}
    
  /** setter for associatedConcept - sets  
   * @generated */
  public void setAssociatedConcept(Concept v) {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_associatedConcept == null)
      jcasType.jcas.throwFeatMissing("associatedConcept", "org.mitre.medfacts.types.Assertion");
    jcasType.ll_cas.ll_setRefValue(addr, ((Assertion_Type)jcasType).casFeatCode_associatedConcept, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    