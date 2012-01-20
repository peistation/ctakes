
/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Fri Jan 20 10:31:31 EST 2012
 * @generated */
public class Lab_Type extends Event_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Lab_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Lab_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Lab(addr, Lab_Type.this);
  			   Lab_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Lab(addr, Lab_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Lab.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.refsem.Lab");
 
  /** @generated */
  final Feature casFeat_abnormalInterpretation;
  /** @generated */
  final int     casFeatCode_abnormalInterpretation;
  /** @generated */ 
  public int getAbnormalInterpretation(int addr) {
        if (featOkTst && casFeat_abnormalInterpretation == null)
      jcas.throwFeatMissing("abnormalInterpretation", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    return ll_cas.ll_getRefValue(addr, casFeatCode_abnormalInterpretation);
  }
  /** @generated */    
  public void setAbnormalInterpretation(int addr, int v) {
        if (featOkTst && casFeat_abnormalInterpretation == null)
      jcas.throwFeatMissing("abnormalInterpretation", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    ll_cas.ll_setRefValue(addr, casFeatCode_abnormalInterpretation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_deltaFlag;
  /** @generated */
  final int     casFeatCode_deltaFlag;
  /** @generated */ 
  public int getDeltaFlag(int addr) {
        if (featOkTst && casFeat_deltaFlag == null)
      jcas.throwFeatMissing("deltaFlag", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    return ll_cas.ll_getRefValue(addr, casFeatCode_deltaFlag);
  }
  /** @generated */    
  public void setDeltaFlag(int addr, int v) {
        if (featOkTst && casFeat_deltaFlag == null)
      jcas.throwFeatMissing("deltaFlag", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    ll_cas.ll_setRefValue(addr, casFeatCode_deltaFlag, v);}
    
  
 
  /** @generated */
  final Feature casFeat_labValue;
  /** @generated */
  final int     casFeatCode_labValue;
  /** @generated */ 
  public int getLabValue(int addr) {
        if (featOkTst && casFeat_labValue == null)
      jcas.throwFeatMissing("labValue", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    return ll_cas.ll_getRefValue(addr, casFeatCode_labValue);
  }
  /** @generated */    
  public void setLabValue(int addr, int v) {
        if (featOkTst && casFeat_labValue == null)
      jcas.throwFeatMissing("labValue", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    ll_cas.ll_setRefValue(addr, casFeatCode_labValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ordinalInterpretation;
  /** @generated */
  final int     casFeatCode_ordinalInterpretation;
  /** @generated */ 
  public int getOrdinalInterpretation(int addr) {
        if (featOkTst && casFeat_ordinalInterpretation == null)
      jcas.throwFeatMissing("ordinalInterpretation", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ordinalInterpretation);
  }
  /** @generated */    
  public void setOrdinalInterpretation(int addr, int v) {
        if (featOkTst && casFeat_ordinalInterpretation == null)
      jcas.throwFeatMissing("ordinalInterpretation", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    ll_cas.ll_setRefValue(addr, casFeatCode_ordinalInterpretation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_referenceRangeNarrative;
  /** @generated */
  final int     casFeatCode_referenceRangeNarrative;
  /** @generated */ 
  public int getReferenceRangeNarrative(int addr) {
        if (featOkTst && casFeat_referenceRangeNarrative == null)
      jcas.throwFeatMissing("referenceRangeNarrative", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    return ll_cas.ll_getRefValue(addr, casFeatCode_referenceRangeNarrative);
  }
  /** @generated */    
  public void setReferenceRangeNarrative(int addr, int v) {
        if (featOkTst && casFeat_referenceRangeNarrative == null)
      jcas.throwFeatMissing("referenceRangeNarrative", "edu.mayo.bmi.uima.core.type.refsem.Lab");
    ll_cas.ll_setRefValue(addr, casFeatCode_referenceRangeNarrative, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Lab_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_abnormalInterpretation = jcas.getRequiredFeatureDE(casType, "abnormalInterpretation", "edu.mayo.bmi.uima.core.type.relation.DegreeOf", featOkTst);
    casFeatCode_abnormalInterpretation  = (null == casFeat_abnormalInterpretation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_abnormalInterpretation).getCode();

 
    casFeat_deltaFlag = jcas.getRequiredFeatureDE(casType, "deltaFlag", "edu.mayo.bmi.uima.core.type.refsem.LabDeltaFlag", featOkTst);
    casFeatCode_deltaFlag  = (null == casFeat_deltaFlag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_deltaFlag).getCode();

 
    casFeat_labValue = jcas.getRequiredFeatureDE(casType, "labValue", "edu.mayo.bmi.uima.core.type.refsem.LabValue", featOkTst);
    casFeatCode_labValue  = (null == casFeat_labValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labValue).getCode();

 
    casFeat_ordinalInterpretation = jcas.getRequiredFeatureDE(casType, "ordinalInterpretation", "edu.mayo.bmi.uima.core.type.relation.DegreeOf", featOkTst);
    casFeatCode_ordinalInterpretation  = (null == casFeat_ordinalInterpretation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ordinalInterpretation).getCode();

 
    casFeat_referenceRangeNarrative = jcas.getRequiredFeatureDE(casType, "referenceRangeNarrative", "edu.mayo.bmi.uima.core.type.refsem.LabReferenceRange", featOkTst);
    casFeatCode_referenceRangeNarrative  = (null == casFeat_referenceRangeNarrative) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_referenceRangeNarrative).getCode();

  }
}



    