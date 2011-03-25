

/* First created by JCasGen Thu Nov 18 10:02:31 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class Sentence extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Sentence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Sentence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Sentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Sentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Sentence(JCas jcas, int begin, int end) {
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
  //* Feature: sentenceNumber

  /** getter for sentenceNumber - gets 
   * @generated */
  public int getSentenceNumber() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_sentenceNumber == null)
      jcasType.jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.uima.core.type.Sentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((Sentence_Type)jcasType).casFeatCode_sentenceNumber);}
    
  /** setter for sentenceNumber - sets  
   * @generated */
  public void setSentenceNumber(int v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_sentenceNumber == null)
      jcasType.jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((Sentence_Type)jcasType).casFeatCode_sentenceNumber, v);}    
   
    
  //*--------------*
  //* Feature: segmentId

  /** getter for segmentId - gets 
   * @generated */
  public String getSegmentId() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_segmentId == null)
      jcasType.jcas.throwFeatMissing("segmentId", "edu.mayo.bmi.uima.core.type.Sentence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_segmentId);}
    
  /** setter for segmentId - sets  
   * @generated */
  public void setSegmentId(String v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_segmentId == null)
      jcasType.jcas.throwFeatMissing("segmentId", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.ll_cas.ll_setStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_segmentId, v);}    
  }

    