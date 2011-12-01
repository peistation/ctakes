

/* First created by JCasGen Mon Jun 27 14:30:05 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * XML source: /home/tmill/Projects/cNLP/cTakes-Ext/Consituency Parser/desc/ConstituencyParserAnnotator.xml
 * @generated */
public class DocumentID extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocumentID.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentID() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentID(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentID(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentID(JCas jcas, int begin, int end) {
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
  //* Feature: documentID

  /** getter for documentID - gets 
   * @generated */
  public String getDocumentID() {
    if (DocumentID_Type.featOkTst && ((DocumentID_Type)jcasType).casFeat_documentID == null)
      jcasType.jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.core.type.DocumentID");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentID_Type)jcasType).casFeatCode_documentID);}
    
  /** setter for documentID - sets  
   * @generated */
  public void setDocumentID(String v) {
    if (DocumentID_Type.featOkTst && ((DocumentID_Type)jcasType).casFeat_documentID == null)
      jcasType.jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.core.type.DocumentID");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentID_Type)jcasType).casFeatCode_documentID, v);}    
  }

    