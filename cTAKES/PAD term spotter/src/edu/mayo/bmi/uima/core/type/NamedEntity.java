

/* First created by JCasGen Tue Jul 20 20:16:45 CDT 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.cas.FSArray;


/** 
 * Updated by JCasGen Tue Jul 20 20:16:45 CDT 2010
 * XML source: C:/general_workspace/PAD term spotter/desc/type_system/PADSiteAndTerm.xml
 * @generated */
public class NamedEntity extends IdentifiedAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NamedEntity.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NamedEntity() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NamedEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NamedEntity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NamedEntity(JCas jcas, int begin, int end) {
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
  //* Feature: discoveryTechnique

  /** getter for discoveryTechnique - gets 
   * @generated */
  public int getDiscoveryTechnique() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_discoveryTechnique == null)
      jcasType.jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_discoveryTechnique);}
    
  /** setter for discoveryTechnique - sets  
   * @generated */
  public void setDiscoveryTechnique(int v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_discoveryTechnique == null)
      jcasType.jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_discoveryTechnique, v);}    
   
    
  //*--------------*
  //* Feature: ontologyConceptArr

  /** getter for ontologyConceptArr - gets 
   * @generated */
  public FSArray getOntologyConceptArr() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_ontologyConceptArr == null)
      jcasType.jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr)));}
    
  /** setter for ontologyConceptArr - sets  
   * @generated */
  public void setOntologyConceptArr(FSArray v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_ontologyConceptArr == null)
      jcasType.jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for ontologyConceptArr - gets an indexed value - 
   * @generated */
  public TOP getOntologyConceptArr(int i) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_ontologyConceptArr == null)
      jcasType.jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr), i);
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr), i)));}

  /** indexed setter for ontologyConceptArr - sets an indexed value - 
   * @generated */
  public void setOntologyConceptArr(int i, TOP v) { 
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_ontologyConceptArr == null)
      jcasType.jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_ontologyConceptArr), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: status

  /** getter for status - gets 
   * @generated */
  public int getStatus() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_status);}
    
  /** setter for status - sets  
   * @generated */
  public void setStatus(int v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_status, v);}    
   
    
  //*--------------*
  //* Feature: certainty

  /** getter for certainty - gets 
   * @generated */
  public int getCertainty() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_certainty);}
    
  /** setter for certainty - sets  
   * @generated */
  public void setCertainty(int v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_certainty, v);}    
   
    
  //*--------------*
  //* Feature: typeID

  /** getter for typeID - gets The type of named entity (e.g. drug, disorder, ...)
   * @generated */
  public int getTypeID() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_typeID == null)
      jcasType.jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_typeID);}
    
  /** setter for typeID - sets The type of named entity (e.g. drug, disorder, ...) 
   * @generated */
  public void setTypeID(int v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_typeID == null)
      jcasType.jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setIntValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_typeID, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets The confidence of the annotation.
   * @generated */
  public float getConfidence() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getFloatValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets The confidence of the annotation. 
   * @generated */
  public void setConfidence(float v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setFloatValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_confidence, v);}    
   
    
  //*--------------*
  //* Feature: segmentID

  /** getter for segmentID - gets 
   * @generated */
  public String getSegmentID() {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_segmentID == null)
      jcasType.jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_segmentID);}
    
  /** setter for segmentID - sets  
   * @generated */
  public void setSegmentID(String v) {
    if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_segmentID == null)
      jcasType.jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_segmentID, v);}    
  }

    