

/* First created by JCasGen Thu Nov 18 10:02:31 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class UmlsConcept extends OntologyConcept {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(UmlsConcept.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected UmlsConcept() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public UmlsConcept(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public UmlsConcept(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: cui

  /** getter for cui - gets 
   * @generated */
  public String getCui() {
    if (UmlsConcept_Type.featOkTst && ((UmlsConcept_Type)jcasType).casFeat_cui == null)
      jcasType.jcas.throwFeatMissing("cui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((UmlsConcept_Type)jcasType).casFeatCode_cui);}
    
  /** setter for cui - sets  
   * @generated */
  public void setCui(String v) {
    if (UmlsConcept_Type.featOkTst && ((UmlsConcept_Type)jcasType).casFeat_cui == null)
      jcasType.jcas.throwFeatMissing("cui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((UmlsConcept_Type)jcasType).casFeatCode_cui, v);}    
   
    
  //*--------------*
  //* Feature: tui

  /** getter for tui - gets 
   * @generated */
  public String getTui() {
    if (UmlsConcept_Type.featOkTst && ((UmlsConcept_Type)jcasType).casFeat_tui == null)
      jcasType.jcas.throwFeatMissing("tui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((UmlsConcept_Type)jcasType).casFeatCode_tui);}
    
  /** setter for tui - sets  
   * @generated */
  public void setTui(String v) {
    if (UmlsConcept_Type.featOkTst && ((UmlsConcept_Type)jcasType).casFeat_tui == null)
      jcasType.jcas.throwFeatMissing("tui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((UmlsConcept_Type)jcasType).casFeatCode_tui, v);}    
  }

    