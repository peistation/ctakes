

/* First created by JCasGen Fri Aug 13 10:15:03 CDT 2010 */
package edu.mayo.bmi.smoking.i2b2.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Represents a single sentence from an I2B2 record.
 * Updated by JCasGen Thu Nov 18 10:02:27 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class RecordSentence extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(RecordSentence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected RecordSentence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public RecordSentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public RecordSentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public RecordSentence(JCas jcas, int begin, int end) {
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
  //* Feature: recordID

  /** getter for recordID - gets ID of record that this sentence belongs to.
   * @generated */
  public String getRecordID() {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordID == null)
      jcasType.jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordID);}
    
  /** setter for recordID - sets ID of record that this sentence belongs to. 
   * @generated */
  public void setRecordID(String v) {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordID == null)
      jcasType.jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    jcasType.ll_cas.ll_setStringValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordID, v);}    
   
    
  //*--------------*
  //* Feature: classification

  /** getter for classification - gets Smoking status classification for the given sentence.
   * @generated */
  public String getClassification() {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_classification == null)
      jcasType.jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_classification);}
    
  /** setter for classification - sets Smoking status classification for the given sentence. 
   * @generated */
  public void setClassification(String v) {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_classification == null)
      jcasType.jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    jcasType.ll_cas.ll_setStringValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_classification, v);}    
   
    
  //*--------------*
  //* Feature: recordTextBegin

  /** getter for recordTextBegin - gets Begin offset of the sentence relative to the entire record text.
   * @generated */
  public int getRecordTextBegin() {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordTextBegin == null)
      jcasType.jcas.throwFeatMissing("recordTextBegin", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordTextBegin);}
    
  /** setter for recordTextBegin - sets Begin offset of the sentence relative to the entire record text. 
   * @generated */
  public void setRecordTextBegin(int v) {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordTextBegin == null)
      jcasType.jcas.throwFeatMissing("recordTextBegin", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordTextBegin, v);}    
   
    
  //*--------------*
  //* Feature: recordTextEnd

  /** getter for recordTextEnd - gets End offset of the sentence relative to the entire record text.
   * @generated */
  public int getRecordTextEnd() {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordTextEnd == null)
      jcasType.jcas.throwFeatMissing("recordTextEnd", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordTextEnd);}
    
  /** setter for recordTextEnd - sets End offset of the sentence relative to the entire record text. 
   * @generated */
  public void setRecordTextEnd(int v) {
    if (RecordSentence_Type.featOkTst && ((RecordSentence_Type)jcasType).casFeat_recordTextEnd == null)
      jcasType.jcas.throwFeatMissing("recordTextEnd", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((RecordSentence_Type)jcasType).casFeatCode_recordTextEnd, v);}    
  }

    