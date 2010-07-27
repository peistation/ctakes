
/* First created by JCasGen Tue Jul 20 20:16:46 CDT 2010 */
package edu.mayo.bmi.uima.pad.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import edu.mayo.bmi.uima.core.type.NamedEntity_Type;

/** 
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * @generated */
public class PADLocation_Type extends NamedEntity_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PADLocation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PADLocation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PADLocation(addr, PADLocation_Type.this);
  			   PADLocation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new PADLocation(addr, PADLocation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = PADLocation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.pad.type.PADLocation");
 
  /** @generated */
  final Feature casFeat_hitDictionaryValue;
  /** @generated */
  final int     casFeatCode_hitDictionaryValue;
  /** @generated */ 
  public String getHitDictionaryValue(int addr) {
        if (featOkTst && casFeat_hitDictionaryValue == null)
      jcas.throwFeatMissing("hitDictionaryValue", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_hitDictionaryValue);
  }
  /** @generated */    
  public void setHitDictionaryValue(int addr, String v) {
        if (featOkTst && casFeat_hitDictionaryValue == null)
      jcas.throwFeatMissing("hitDictionaryValue", "edu.mayo.bmi.uima.pad.type.PADLocation");
    ll_cas.ll_setStringValue(addr, casFeatCode_hitDictionaryValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_isStandAlone;
  /** @generated */
  final int     casFeatCode_isStandAlone;
  /** @generated */ 
  public int getIsStandAlone(int addr) {
        if (featOkTst && casFeat_isStandAlone == null)
      jcas.throwFeatMissing("isStandAlone", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_isStandAlone);
  }
  /** @generated */    
  public void setIsStandAlone(int addr, int v) {
        if (featOkTst && casFeat_isStandAlone == null)
      jcas.throwFeatMissing("isStandAlone", "edu.mayo.bmi.uima.pad.type.PADLocation");
    ll_cas.ll_setIntValue(addr, casFeatCode_isStandAlone, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relatedTokens;
  /** @generated */
  final int     casFeatCode_relatedTokens;
  /** @generated */ 
  public int getRelatedTokens(int addr) {
        if (featOkTst && casFeat_relatedTokens == null)
      jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens);
  }
  /** @generated */    
  public void setRelatedTokens(int addr, int v) {
        if (featOkTst && casFeat_relatedTokens == null)
      jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    ll_cas.ll_setRefValue(addr, casFeatCode_relatedTokens, v);}
    
   /** @generated */
  public int getRelatedTokens(int addr, int i) {
        if (featOkTst && casFeat_relatedTokens == null)
      jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i);
  }
   
  /** @generated */ 
  public void setRelatedTokens(int addr, int i, int v) {
        if (featOkTst && casFeat_relatedTokens == null)
      jcas.throwFeatMissing("relatedTokens", "edu.mayo.bmi.uima.pad.type.PADLocation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_relatedTokens), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public PADLocation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_hitDictionaryValue = jcas.getRequiredFeatureDE(casType, "hitDictionaryValue", "uima.cas.String", featOkTst);
    casFeatCode_hitDictionaryValue  = (null == casFeat_hitDictionaryValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_hitDictionaryValue).getCode();

 
    casFeat_isStandAlone = jcas.getRequiredFeatureDE(casType, "isStandAlone", "uima.cas.Integer", featOkTst);
    casFeatCode_isStandAlone  = (null == casFeat_isStandAlone) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_isStandAlone).getCode();

 
    casFeat_relatedTokens = jcas.getRequiredFeatureDE(casType, "relatedTokens", "uima.cas.FSArray", featOkTst);
    casFeatCode_relatedTokens  = (null == casFeat_relatedTokens) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relatedTokens).getCode();

  }
}



    