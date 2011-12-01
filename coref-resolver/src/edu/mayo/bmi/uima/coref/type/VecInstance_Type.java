
/* First created by JCasGen Thu Dec 01 13:17:33 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP_Type;

/** 
 * Updated by JCasGen Thu Dec 01 13:17:33 EST 2011
 * @generated */
public class VecInstance_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (VecInstance_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = VecInstance_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new VecInstance(addr, VecInstance_Type.this);
  			   VecInstance_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new VecInstance(addr, VecInstance_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = VecInstance.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.VecInstance");
 
  /** @generated */
  final Feature casFeat_vector;
  /** @generated */
  final int     casFeatCode_vector;
  /** @generated */ 
  public String getVector(int addr) {
        if (featOkTst && casFeat_vector == null)
      jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_vector);
  }
  /** @generated */    
  public void setVector(int addr, String v) {
        if (featOkTst && casFeat_vector == null)
      jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_vector, v);}
    
  
 
  /** @generated */
  final Feature casFeat_a1;
  /** @generated */
  final int     casFeatCode_a1;
  /** @generated */ 
  public String getA1(int addr) {
        if (featOkTst && casFeat_a1 == null)
      jcas.throwFeatMissing("a1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_a1);
  }
  /** @generated */    
  public void setA1(int addr, String v) {
        if (featOkTst && casFeat_a1 == null)
      jcas.throwFeatMissing("a1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_a1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_a2;
  /** @generated */
  final int     casFeatCode_a2;
  /** @generated */ 
  public String getA2(int addr) {
        if (featOkTst && casFeat_a2 == null)
      jcas.throwFeatMissing("a2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_a2);
  }
  /** @generated */    
  public void setA2(int addr, String v) {
        if (featOkTst && casFeat_a2 == null)
      jcas.throwFeatMissing("a2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_a2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_offset1;
  /** @generated */
  final int     casFeatCode_offset1;
  /** @generated */ 
  public String getOffset1(int addr) {
        if (featOkTst && casFeat_offset1 == null)
      jcas.throwFeatMissing("offset1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_offset1);
  }
  /** @generated */    
  public void setOffset1(int addr, String v) {
        if (featOkTst && casFeat_offset1 == null)
      jcas.throwFeatMissing("offset1", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_offset1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_offset2;
  /** @generated */
  final int     casFeatCode_offset2;
  /** @generated */ 
  public String getOffset2(int addr) {
        if (featOkTst && casFeat_offset2 == null)
      jcas.throwFeatMissing("offset2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_offset2);
  }
  /** @generated */    
  public void setOffset2(int addr, String v) {
        if (featOkTst && casFeat_offset2 == null)
      jcas.throwFeatMissing("offset2", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_offset2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_classifier;
  /** @generated */
  final int     casFeatCode_classifier;
  /** @generated */ 
  public String getClassifier(int addr) {
        if (featOkTst && casFeat_classifier == null)
      jcas.throwFeatMissing("classifier", "edu.mayo.bmi.uima.coref.type.VecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_classifier);
  }
  /** @generated */    
  public void setClassifier(int addr, String v) {
        if (featOkTst && casFeat_classifier == null)
      jcas.throwFeatMissing("classifier", "edu.mayo.bmi.uima.coref.type.VecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_classifier, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public VecInstance_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_vector = jcas.getRequiredFeatureDE(casType, "vector", "uima.cas.String", featOkTst);
    casFeatCode_vector  = (null == casFeat_vector) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_vector).getCode();

 
    casFeat_a1 = jcas.getRequiredFeatureDE(casType, "a1", "uima.cas.String", featOkTst);
    casFeatCode_a1  = (null == casFeat_a1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_a1).getCode();

 
    casFeat_a2 = jcas.getRequiredFeatureDE(casType, "a2", "uima.cas.String", featOkTst);
    casFeatCode_a2  = (null == casFeat_a2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_a2).getCode();

 
    casFeat_offset1 = jcas.getRequiredFeatureDE(casType, "offset1", "uima.cas.String", featOkTst);
    casFeatCode_offset1  = (null == casFeat_offset1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_offset1).getCode();

 
    casFeat_offset2 = jcas.getRequiredFeatureDE(casType, "offset2", "uima.cas.String", featOkTst);
    casFeatCode_offset2  = (null == casFeat_offset2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_offset2).getCode();

 
    casFeat_classifier = jcas.getRequiredFeatureDE(casType, "classifier", "uima.cas.String", featOkTst);
    casFeatCode_classifier  = (null == casFeat_classifier) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_classifier).getCode();

  }
}



    