

/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class MarkablePairSet extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(MarkablePairSet.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected MarkablePairSet() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public MarkablePairSet(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public MarkablePairSet(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public MarkablePairSet(JCas jcas, int begin, int end) {
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
  //* Feature: anaphor

  /** getter for anaphor - gets 
   * @generated */
  public Markable getAnaphor() {
    if (MarkablePairSet_Type.featOkTst && ((MarkablePairSet_Type)jcasType).casFeat_anaphor == null)
      jcasType.jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    return (Markable)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MarkablePairSet_Type)jcasType).casFeatCode_anaphor)));}
    
  /** setter for anaphor - sets  
   * @generated */
  public void setAnaphor(Markable v) {
    if (MarkablePairSet_Type.featOkTst && ((MarkablePairSet_Type)jcasType).casFeat_anaphor == null)
      jcasType.jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    jcasType.ll_cas.ll_setRefValue(addr, ((MarkablePairSet_Type)jcasType).casFeatCode_anaphor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: antecedentList

  /** getter for antecedentList - gets 
   * @generated */
  public FSList getAntecedentList() {
    if (MarkablePairSet_Type.featOkTst && ((MarkablePairSet_Type)jcasType).casFeat_antecedentList == null)
      jcasType.jcas.throwFeatMissing("antecedentList", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MarkablePairSet_Type)jcasType).casFeatCode_antecedentList)));}
    
  /** setter for antecedentList - sets  
   * @generated */
  public void setAntecedentList(FSList v) {
    if (MarkablePairSet_Type.featOkTst && ((MarkablePairSet_Type)jcasType).casFeat_antecedentList == null)
      jcasType.jcas.throwFeatMissing("antecedentList", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    jcasType.ll_cas.ll_setRefValue(addr, ((MarkablePairSet_Type)jcasType).casFeatCode_antecedentList, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    