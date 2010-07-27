

/* First created by JCasGen Tue Jul 20 20:16:45 CDT 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Tue Jul 20 20:16:45 CDT 2010
 * XML source: C:/general_workspace/PAD term spotter/desc/type_system/PADSiteAndTerm.xml
 * @generated */
public class Properties extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Properties.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Properties() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Properties(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Properties(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Properties(JCas jcas, int begin, int end) {
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
  //* Feature: propArr

  /** getter for propArr - gets 
   * @generated */
  public FSArray getPropArr() {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_propArr == null)
      jcasType.jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr)));}
    
  /** setter for propArr - sets  
   * @generated */
  public void setPropArr(FSArray v) {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_propArr == null)
      jcasType.jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    jcasType.ll_cas.ll_setRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for propArr - gets an indexed value - 
   * @generated */
  public Property getPropArr(int i) {
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_propArr == null)
      jcasType.jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr), i);
    return (Property)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr), i)));}

  /** indexed setter for propArr - sets an indexed value - 
   * @generated */
  public void setPropArr(int i, Property v) { 
    if (Properties_Type.featOkTst && ((Properties_Type)jcasType).casFeat_propArr == null)
      jcasType.jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Properties_Type)jcasType).casFeatCode_propArr), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    