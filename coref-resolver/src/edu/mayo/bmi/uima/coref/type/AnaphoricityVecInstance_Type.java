
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
public class AnaphoricityVecInstance_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AnaphoricityVecInstance_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AnaphoricityVecInstance_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AnaphoricityVecInstance(addr, AnaphoricityVecInstance_Type.this);
  			   AnaphoricityVecInstance_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AnaphoricityVecInstance(addr, AnaphoricityVecInstance_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = AnaphoricityVecInstance.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
 
  /** @generated */
  final Feature casFeat_vector;
  /** @generated */
  final int     casFeatCode_vector;
  /** @generated */ 
  public String getVector(int addr) {
        if (featOkTst && casFeat_vector == null)
      jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_vector);
  }
  /** @generated */    
  public void setVector(int addr, String v) {
        if (featOkTst && casFeat_vector == null)
      jcas.throwFeatMissing("vector", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_vector, v);}
    
  
 
  /** @generated */
  final Feature casFeat_m;
  /** @generated */
  final int     casFeatCode_m;
  /** @generated */ 
  public String getM(int addr) {
        if (featOkTst && casFeat_m == null)
      jcas.throwFeatMissing("m", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_m);
  }
  /** @generated */    
  public void setM(int addr, String v) {
        if (featOkTst && casFeat_m == null)
      jcas.throwFeatMissing("m", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_m, v);}
    
  
 
  /** @generated */
  final Feature casFeat_offset;
  /** @generated */
  final int     casFeatCode_offset;
  /** @generated */ 
  public String getOffset(int addr) {
        if (featOkTst && casFeat_offset == null)
      jcas.throwFeatMissing("offset", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    return ll_cas.ll_getStringValue(addr, casFeatCode_offset);
  }
  /** @generated */    
  public void setOffset(int addr, String v) {
        if (featOkTst && casFeat_offset == null)
      jcas.throwFeatMissing("offset", "edu.mayo.bmi.uima.coref.type.AnaphoricityVecInstance");
    ll_cas.ll_setStringValue(addr, casFeatCode_offset, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public AnaphoricityVecInstance_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_vector = jcas.getRequiredFeatureDE(casType, "vector", "uima.cas.String", featOkTst);
    casFeatCode_vector  = (null == casFeat_vector) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_vector).getCode();

 
    casFeat_m = jcas.getRequiredFeatureDE(casType, "m", "uima.cas.String", featOkTst);
    casFeatCode_m  = (null == casFeat_m) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_m).getCode();

 
    casFeat_offset = jcas.getRequiredFeatureDE(casType, "offset", "uima.cas.String", featOkTst);
    casFeatCode_offset  = (null == casFeat_offset) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_offset).getCode();

  }
}



    