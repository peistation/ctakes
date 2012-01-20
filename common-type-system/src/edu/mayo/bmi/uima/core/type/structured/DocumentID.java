

/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.structured;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** Equivalent to cTAKES: edu.mayo.bmi.uima.core.type.DocumentID, but its supertype changed to uima.cas.TOP
 * Updated by JCasGen Fri Jan 20 10:31:42 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class DocumentID extends TOP {
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
  protected DocumentID() {/* intentionally empty block */}
    
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
      jcasType.jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.core.type.structured.DocumentID");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentID_Type)jcasType).casFeatCode_documentID);}
    
  /** setter for documentID - sets  
   * @generated */
  public void setDocumentID(String v) {
    if (DocumentID_Type.featOkTst && ((DocumentID_Type)jcasType).casFeat_documentID == null)
      jcasType.jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.core.type.structured.DocumentID");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentID_Type)jcasType).casFeatCode_documentID, v);}    
  }

    