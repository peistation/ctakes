
/* First created by JCasGen Mon Jun 27 14:30:05 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * @generated */
public class Sentence_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Sentence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Sentence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Sentence(addr, Sentence_Type.this);
  			   Sentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Sentence(addr, Sentence_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Sentence.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.Sentence");
 
  /** @generated */
  final Feature casFeat_sentenceNumber;
  /** @generated */
  final int     casFeatCode_sentenceNumber;
  /** @generated */ 
  public int getSentenceNumber(int addr) {
        if (featOkTst && casFeat_sentenceNumber == null)
      jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.uima.core.type.Sentence");
    return ll_cas.ll_getIntValue(addr, casFeatCode_sentenceNumber);
  }
  /** @generated */    
  public void setSentenceNumber(int addr, int v) {
        if (featOkTst && casFeat_sentenceNumber == null)
      jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.uima.core.type.Sentence");
    ll_cas.ll_setIntValue(addr, casFeatCode_sentenceNumber, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segmentId;
  /** @generated */
  final int     casFeatCode_segmentId;
  /** @generated */ 
  public String getSegmentId(int addr) {
        if (featOkTst && casFeat_segmentId == null)
      jcas.throwFeatMissing("segmentId", "edu.mayo.bmi.uima.core.type.Sentence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_segmentId);
  }
  /** @generated */    
  public void setSegmentId(int addr, String v) {
        if (featOkTst && casFeat_segmentId == null)
      jcas.throwFeatMissing("segmentId", "edu.mayo.bmi.uima.core.type.Sentence");
    ll_cas.ll_setStringValue(addr, casFeatCode_segmentId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_topicDistribution;
  /** @generated */
  final int     casFeatCode_topicDistribution;
  /** @generated */ 
  public int getTopicDistribution(int addr) {
        if (featOkTst && casFeat_topicDistribution == null)
      jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    return ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution);
  }
  /** @generated */    
  public void setTopicDistribution(int addr, int v) {
        if (featOkTst && casFeat_topicDistribution == null)
      jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    ll_cas.ll_setRefValue(addr, casFeatCode_topicDistribution, v);}
    
   /** @generated */
  public float getTopicDistribution(int addr, int i) {
        if (featOkTst && casFeat_topicDistribution == null)
      jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getFloatArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i);
  return ll_cas.ll_getFloatArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i);
  }
   
  /** @generated */ 
  public void setTopicDistribution(int addr, int i, float v) {
        if (featOkTst && casFeat_topicDistribution == null)
      jcas.throwFeatMissing("topicDistribution", "edu.mayo.bmi.uima.core.type.Sentence");
    if (lowLevelTypeChecks)
      ll_cas.ll_setFloatArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i);
    ll_cas.ll_setFloatArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_topicDistribution), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_parse;
  /** @generated */
  final int     casFeatCode_parse;
  /** @generated */ 
  public String getParse(int addr) {
        if (featOkTst && casFeat_parse == null)
      jcas.throwFeatMissing("parse", "edu.mayo.bmi.uima.core.type.Sentence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_parse);
  }
  /** @generated */    
  public void setParse(int addr, String v) {
        if (featOkTst && casFeat_parse == null)
      jcas.throwFeatMissing("parse", "edu.mayo.bmi.uima.core.type.Sentence");
    ll_cas.ll_setStringValue(addr, casFeatCode_parse, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Sentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_sentenceNumber = jcas.getRequiredFeatureDE(casType, "sentenceNumber", "uima.cas.Integer", featOkTst);
    casFeatCode_sentenceNumber  = (null == casFeat_sentenceNumber) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceNumber).getCode();

 
    casFeat_segmentId = jcas.getRequiredFeatureDE(casType, "segmentId", "uima.cas.String", featOkTst);
    casFeatCode_segmentId  = (null == casFeat_segmentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segmentId).getCode();

 
    casFeat_topicDistribution = jcas.getRequiredFeatureDE(casType, "topicDistribution", "uima.cas.FloatArray", featOkTst);
    casFeatCode_topicDistribution  = (null == casFeat_topicDistribution) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_topicDistribution).getCode();

 
    casFeat_parse = jcas.getRequiredFeatureDE(casType, "parse", "uima.cas.String", featOkTst);
    casFeatCode_parse  = (null == casFeat_parse) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parse).getCode();

  }
}



    