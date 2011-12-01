

/* First created by JCasGen Mon Jun 27 14:30:05 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.FloatArray;


/** 
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * XML source: /home/tmill/Projects/cNLP/cTakes-Ext/Consituency Parser/desc/ConstituencyParserAnnotator.xml
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
   
    
  //*--------------*
  //* Feature: topicDistribution

  /** getter for topicDistribution - gets Topic id as assigned by lsa/lda
   * @generated */
  public FloatArray getTopicDistribution() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_topicDistribution == null)
      jcasType.jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    return (FloatArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution)));}
    
  /** setter for topicDistribution - sets Topic id as assigned by lsa/lda 
   * @generated */
  public void setTopicDistribution(FloatArray v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_topicDistribution == null)
      jcasType.jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.ll_cas.ll_setRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for topicDistribution - gets an indexed value - Topic id as assigned by lsa/lda
   * @generated */
  public float getTopicDistribution(int i) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_topicDistribution == null)
      jcasType.jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution), i);
    return jcasType.ll_cas.ll_getFloatArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution), i);}

  /** indexed setter for topicDistribution - sets an indexed value - Topic id as assigned by lsa/lda
   * @generated */
  public void setTopicDistribution(int i, float v) { 
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_topicDistribution == null)
      jcasType.jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution), i);
    jcasType.ll_cas.ll_setFloatArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Sentence_Type)jcasType).casFeatCode_topicDistribution), i, v);}
   
    
  //*--------------*
  //* Feature: parse

  /** getter for parse - gets 
   * @generated */
  public String getParse() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_parse == null)
      jcasType.jcas.throwFeatMissing("parse", "edu.mayo.bmi.uima.core.type.Sentence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_parse);}
    
  /** setter for parse - sets  
   * @generated */
  public void setParse(String v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_parse == null)
      jcasType.jcas.throwFeatMissing("parse", "edu.mayo.bmi.uima.core.type.Sentence");
    jcasType.ll_cas.ll_setStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_parse, v);}    
  }

    