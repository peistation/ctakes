

/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Fri Jan 20 10:31:30 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class BodySide extends Attribute {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BodySide.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BodySide() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BodySide(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BodySide(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   * @generated */
  public String getValue() {
    if (BodySide_Type.featOkTst && ((BodySide_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.refsem.BodySide");
    return jcasType.ll_cas.ll_getStringValue(addr, ((BodySide_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(String v) {
    if (BodySide_Type.featOkTst && ((BodySide_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "edu.mayo.bmi.uima.core.type.refsem.BodySide");
    jcasType.ll_cas.ll_setStringValue(addr, ((BodySide_Type)jcasType).casFeatCode_value, v);}    
  }

    