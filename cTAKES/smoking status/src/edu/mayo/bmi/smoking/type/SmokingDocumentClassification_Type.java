
/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.smoking.type;

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

/** Represents classification related to the document
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * @generated */
public class SmokingDocumentClassification_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SmokingDocumentClassification_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SmokingDocumentClassification_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SmokingDocumentClassification(addr, SmokingDocumentClassification_Type.this);
  			   SmokingDocumentClassification_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SmokingDocumentClassification(addr, SmokingDocumentClassification_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SmokingDocumentClassification.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
 
  /** @generated */
  final Feature casFeat_classification;
  /** @generated */
  final int     casFeatCode_classification;
  /** @generated */ 
  public String getClassification(int addr) {
        if (featOkTst && casFeat_classification == null)
      jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    return ll_cas.ll_getStringValue(addr, casFeatCode_classification);
  }
  /** @generated */    
  public void setClassification(int addr, String v) {
        if (featOkTst && casFeat_classification == null)
      jcas.throwFeatMissing("classification", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    ll_cas.ll_setStringValue(addr, casFeatCode_classification, v);}
    
  
 
  /** @generated */
  final Feature casFeat_recordID;
  /** @generated */
  final int     casFeatCode_recordID;
  /** @generated */ 
  public String getRecordID(int addr) {
        if (featOkTst && casFeat_recordID == null)
      jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    return ll_cas.ll_getStringValue(addr, casFeatCode_recordID);
  }
  /** @generated */    
  public void setRecordID(int addr, String v) {
        if (featOkTst && casFeat_recordID == null)
      jcas.throwFeatMissing("recordID", "edu.mayo.bmi.smoking.type.SmokingDocumentClassification");
    ll_cas.ll_setStringValue(addr, casFeatCode_recordID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SmokingDocumentClassification_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_classification = jcas.getRequiredFeatureDE(casType, "classification", "uima.cas.String", featOkTst);
    casFeatCode_classification  = (null == casFeat_classification) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_classification).getCode();

 
    casFeat_recordID = jcas.getRequiredFeatureDE(casType, "recordID", "uima.cas.String", featOkTst);
    casFeatCode_recordID  = (null == casFeat_recordID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_recordID).getCode();

  }
}



    