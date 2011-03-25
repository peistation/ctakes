

/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.smoking.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Represents classification related to the document
 * Updated by JCasGen Thu Nov 18 10:02:28 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class SmokingDocumentClassification extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SmokingDocumentClassification.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SmokingDocumentClassification() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SmokingDocumentClassification(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SmokingDocumentClassification(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SmokingDocumentClassification(JCas jcas, int begin, int end) {
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
  //* Feature: classification

  /** getter for classification - gets represents the document level classification that RecordResolutionAnnotator assigns
   * @generated */
  public String getClassification() {
    if (SmokingDocumentClassification_Type.featOkTst && ((SmokingDocumentClassification_Type)jcasType).casFeat_classification == null)
      jcasType.jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SmokingDocumentClassification_Type)jcasType).casFeatCode_classification);}
    
  /** setter for classification - sets represents the document level classification that RecordResolutionAnnotator assigns 
   * @generated */
  public void setClassification(String v) {
    if (SmokingDocumentClassification_Type.featOkTst && ((SmokingDocumentClassification_Type)jcasType).casFeat_classification == null)
      jcasType.jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    jcasType.ll_cas.ll_setStringValue(addr, ((SmokingDocumentClassification_Type)jcasType).casFeatCode_classification, v);}    
   
    
  //*--------------*
  //* Feature: recordID

  /** getter for recordID - gets Represents the document record id
   * @generated */
  public String getRecordID() {
    if (SmokingDocumentClassification_Type.featOkTst && ((SmokingDocumentClassification_Type)jcasType).casFeat_recordID == null)
      jcasType.jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SmokingDocumentClassification_Type)jcasType).casFeatCode_recordID);}
    
  /** setter for recordID - sets Represents the document record id 
   * @generated */
  public void setRecordID(String v) {
    if (SmokingDocumentClassification_Type.featOkTst && ((SmokingDocumentClassification_Type)jcasType).casFeat_recordID == null)
      jcasType.jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    jcasType.ll_cas.ll_setStringValue(addr, ((SmokingDocumentClassification_Type)jcasType).casFeatCode_recordID, v);}    
  }

    