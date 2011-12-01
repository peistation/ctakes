

/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class MarkablePair extends TOP {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(MarkablePair.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected MarkablePair() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public MarkablePair(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public MarkablePair(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: antecedent

  /** getter for antecedent - gets Proposed antecedent
   * @generated */
  public Markable getAntecedent() {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_antecedent == null)
      jcasType.jcas.throwFeatMissing("antecedent", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return (Markable)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_antecedent)));}
    
  /** setter for antecedent - sets Proposed antecedent 
   * @generated */
  public void setAntecedent(Markable v) {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_antecedent == null)
      jcasType.jcas.throwFeatMissing("antecedent", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    jcasType.ll_cas.ll_setRefValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_antecedent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: anaphor

  /** getter for anaphor - gets 
   * @generated */
  public Markable getAnaphor() {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_anaphor == null)
      jcasType.jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return (Markable)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_anaphor)));}
    
  /** setter for anaphor - sets  
   * @generated */
  public void setAnaphor(Markable v) {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_anaphor == null)
      jcasType.jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    jcasType.ll_cas.ll_setRefValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_anaphor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets Is this pair coreferent?
   * @generated */
  public boolean getLabel() {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets Is this pair coreferent? 
   * @generated */
  public void setLabel(boolean v) {
    if (MarkablePair_Type.featOkTst && ((MarkablePair_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((MarkablePair_Type)jcasType).casFeatCode_label, v);}    
  }

    