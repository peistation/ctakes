

/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.smoking.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** SentenceAnnotation will be copied to Smoking sentence to adjust the boundaries
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class SmokingSentence extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SmokingSentence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SmokingSentence() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SmokingSentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SmokingSentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SmokingSentence(JCas jcas, int begin, int end) {
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
    if (SmokingSentence_Type.featOkTst && ((SmokingSentence_Type)jcasType).casFeat_sentenceNumber == null)
      jcasType.jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.smoking.type.SmokingSentence");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SmokingSentence_Type)jcasType).casFeatCode_sentenceNumber);}
    
  /** setter for sentenceNumber - sets  
   * @generated */
  public void setSentenceNumber(int v) {
    if (SmokingSentence_Type.featOkTst && ((SmokingSentence_Type)jcasType).casFeat_sentenceNumber == null)
      jcasType.jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.smoking.type.SmokingSentence");
    jcasType.ll_cas.ll_setIntValue(addr, ((SmokingSentence_Type)jcasType).casFeatCode_sentenceNumber, v);}    
  }

    