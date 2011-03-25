
/* First created by JCasGen Fri Aug 13 10:15:03 CDT 2010 */
package edu.mayo.bmi.smoking.i2b2.type;

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

/** Represents a single sentence from an I2B2 record.
 * Updated by JCasGen Thu Nov 18 10:02:28 CST 2010
 * @generated */
public class RecordSentence_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (RecordSentence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = RecordSentence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new RecordSentence(addr, RecordSentence_Type.this);
  			   RecordSentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new RecordSentence(addr, RecordSentence_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = RecordSentence.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
 
  /** @generated */
  final Feature casFeat_recordID;
  /** @generated */
  final int     casFeatCode_recordID;
  /** @generated */ 
  public String getRecordID(int addr) {
        if (featOkTst && casFeat_recordID == null)
      jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_recordID);
  }
  /** @generated */    
  public void setRecordID(int addr, String v) {
        if (featOkTst && casFeat_recordID == null)
      jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    ll_cas.ll_setStringValue(addr, casFeatCode_recordID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_classification;
  /** @generated */
  final int     casFeatCode_classification;
  /** @generated */ 
  public String getClassification(int addr) {
        if (featOkTst && casFeat_classification == null)
      jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return ll_cas.ll_getStringValue(addr, casFeatCode_classification);
  }
  /** @generated */    
  public void setClassification(int addr, String v) {
        if (featOkTst && casFeat_classification == null)
      jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    ll_cas.ll_setStringValue(addr, casFeatCode_classification, v);}
    
  
 
  /** @generated */
  final Feature casFeat_recordTextBegin;
  /** @generated */
  final int     casFeatCode_recordTextBegin;
  /** @generated */ 
  public int getRecordTextBegin(int addr) {
        if (featOkTst && casFeat_recordTextBegin == null)
      jcas.throwFeatMissing("recordTextBegin", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return ll_cas.ll_getIntValue(addr, casFeatCode_recordTextBegin);
  }
  /** @generated */    
  public void setRecordTextBegin(int addr, int v) {
        if (featOkTst && casFeat_recordTextBegin == null)
      jcas.throwFeatMissing("recordTextBegin", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    ll_cas.ll_setIntValue(addr, casFeatCode_recordTextBegin, v);}
    
  
 
  /** @generated */
  final Feature casFeat_recordTextEnd;
  /** @generated */
  final int     casFeatCode_recordTextEnd;
  /** @generated */ 
  public int getRecordTextEnd(int addr) {
        if (featOkTst && casFeat_recordTextEnd == null)
      jcas.throwFeatMissing("recordTextEnd", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    return ll_cas.ll_getIntValue(addr, casFeatCode_recordTextEnd);
  }
  /** @generated */    
  public void setRecordTextEnd(int addr, int v) {
        if (featOkTst && casFeat_recordTextEnd == null)
      jcas.throwFeatMissing("recordTextEnd", "edu.mayo.bmi.smoking.i2b2.type.RecordSentence");
    ll_cas.ll_setIntValue(addr, casFeatCode_recordTextEnd, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public RecordSentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_recordID = jcas.getRequiredFeatureDE(casType, "recordID", "uima.cas.String", featOkTst);
    casFeatCode_recordID  = (null == casFeat_recordID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_recordID).getCode();

 
    casFeat_classification = jcas.getRequiredFeatureDE(casType, "classification", "uima.cas.String", featOkTst);
    casFeatCode_classification  = (null == casFeat_classification) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_classification).getCode();

 
    casFeat_recordTextBegin = jcas.getRequiredFeatureDE(casType, "recordTextBegin", "uima.cas.Integer", featOkTst);
    casFeatCode_recordTextBegin  = (null == casFeat_recordTextBegin) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_recordTextBegin).getCode();

 
    casFeat_recordTextEnd = jcas.getRequiredFeatureDE(casType, "recordTextEnd", "uima.cas.Integer", featOkTst);
    casFeatCode_recordTextEnd  = (null == casFeat_recordTextEnd) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_recordTextEnd).getCode();

  }
}



    