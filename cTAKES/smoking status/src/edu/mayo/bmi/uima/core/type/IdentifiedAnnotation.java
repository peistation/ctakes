

/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class IdentifiedAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(IdentifiedAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected IdentifiedAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public IdentifiedAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public IdentifiedAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public IdentifiedAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: uid

  /** getter for uid - gets Unique identifier of this annotation.
   * @generated */
  public int getUid() {
    if (IdentifiedAnnotation_Type.featOkTst && ((IdentifiedAnnotation_Type)jcasType).casFeat_uid == null)
      jcasType.jcas.throwFeatMissing("uid", "edu.mayo.bmi.uima.core.type.IdentifiedAnnotation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((IdentifiedAnnotation_Type)jcasType).casFeatCode_uid);}
    
  /** setter for uid - sets Unique identifier of this annotation. 
   * @generated */
  public void setUid(int v) {
    if (IdentifiedAnnotation_Type.featOkTst && ((IdentifiedAnnotation_Type)jcasType).casFeat_uid == null)
      jcasType.jcas.throwFeatMissing("uid", "edu.mayo.bmi.uima.core.type.IdentifiedAnnotation");
    jcasType.ll_cas.ll_setIntValue(addr, ((IdentifiedAnnotation_Type)jcasType).casFeatCode_uid, v);}    
  }

    