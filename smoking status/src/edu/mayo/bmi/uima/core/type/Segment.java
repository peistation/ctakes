

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
public class Segment extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Segment.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Segment() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Segment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Segment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Segment(JCas jcas, int begin, int end) {
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
  //* Feature: id

  /** getter for id - gets 
   * @generated */
  public String getId() {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.core.type.Segment");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Segment_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated */
  public void setId(String v) {
    if (Segment_Type.featOkTst && ((Segment_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.core.type.Segment");
    jcasType.ll_cas.ll_setStringValue(addr, ((Segment_Type)jcasType).casFeatCode_id, v);}    
  }

    