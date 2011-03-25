

/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.uima.context.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class ContextAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ContextAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ContextAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ContextAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ContextAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ContextAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: FocusText

  /** getter for FocusText - gets 
   * @generated */
  public String getFocusText() {
    if (ContextAnnotation_Type.featOkTst && ((ContextAnnotation_Type)jcasType).casFeat_FocusText == null)
      jcasType.jcas.throwFeatMissing("FocusText", "edu.mayo.bmi.uima.context.type.ContextAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ContextAnnotation_Type)jcasType).casFeatCode_FocusText);}
    
  /** setter for FocusText - sets  
   * @generated */
  public void setFocusText(String v) {
    if (ContextAnnotation_Type.featOkTst && ((ContextAnnotation_Type)jcasType).casFeat_FocusText == null)
      jcasType.jcas.throwFeatMissing("FocusText", "edu.mayo.bmi.uima.context.type.ContextAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ContextAnnotation_Type)jcasType).casFeatCode_FocusText, v);}    
   
    
  //*--------------*
  //* Feature: Scope

  /** getter for Scope - gets 
   * @generated */
  public String getScope() {
    if (ContextAnnotation_Type.featOkTst && ((ContextAnnotation_Type)jcasType).casFeat_Scope == null)
      jcasType.jcas.throwFeatMissing("Scope", "edu.mayo.bmi.uima.context.type.ContextAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ContextAnnotation_Type)jcasType).casFeatCode_Scope);}
    
  /** setter for Scope - sets  
   * @generated */
  public void setScope(String v) {
    if (ContextAnnotation_Type.featOkTst && ((ContextAnnotation_Type)jcasType).casFeat_Scope == null)
      jcasType.jcas.throwFeatMissing("Scope", "edu.mayo.bmi.uima.context.type.ContextAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((ContextAnnotation_Type)jcasType).casFeatCode_Scope, v);}    
  }

    