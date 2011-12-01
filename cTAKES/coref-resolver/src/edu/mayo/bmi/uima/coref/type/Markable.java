

/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class Markable extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Markable.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Markable() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Markable(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Markable(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Markable(JCas jcas, int begin, int end) {
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
  //* Feature: content

  /** getter for content - gets The markable instance, i.e., a Named Entity, pronoun Token, etc.
   * @generated */
  public Annotation getContent() {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "edu.mayo.bmi.uima.coref.type.Markable");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Markable_Type)jcasType).casFeatCode_content)));}
    
  /** setter for content - sets The markable instance, i.e., a Named Entity, pronoun Token, etc. 
   * @generated */
  public void setContent(Annotation v) {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_content == null)
      jcasType.jcas.throwFeatMissing("content", "edu.mayo.bmi.uima.coref.type.Markable");
    jcasType.ll_cas.ll_setRefValue(addr, ((Markable_Type)jcasType).casFeatCode_content, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public int getId() {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.coref.type.Markable");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Markable_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(int v) {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.coref.type.Markable");
    jcasType.ll_cas.ll_setIntValue(addr, ((Markable_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: anaphoric_prob

  /** getter for anaphoric_prob - gets 
   * @generated */
  public double getAnaphoric_prob() {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_anaphoric_prob == null)
      jcasType.jcas.throwFeatMissing("anaphoric_prob", "edu.mayo.bmi.uima.coref.type.Markable");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Markable_Type)jcasType).casFeatCode_anaphoric_prob);}
    
  /** setter for anaphoric_prob - sets  
   * @generated */
  public void setAnaphoric_prob(double v) {
    if (Markable_Type.featOkTst && ((Markable_Type)jcasType).casFeat_anaphoric_prob == null)
      jcasType.jcas.throwFeatMissing("anaphoric_prob", "edu.mayo.bmi.uima.coref.type.Markable");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Markable_Type)jcasType).casFeatCode_anaphoric_prob, v);}    
  }

    