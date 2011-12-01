
/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

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
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class Properties_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Properties_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Properties_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Properties(addr, Properties_Type.this);
  			   Properties_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Properties(addr, Properties_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Properties.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mipacq.annotation.type.Properties");
 
  /** @generated */
  final Feature casFeat_polarity;
  /** @generated */
  final int     casFeatCode_polarity;
  /** @generated */ 
  public boolean getPolarity(int addr) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "org.mipacq.annotation.type.Properties");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_polarity);
  }
  /** @generated */    
  public void setPolarity(int addr, boolean v) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "org.mipacq.annotation.type.Properties");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_polarity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_certainty;
  /** @generated */
  final int     casFeatCode_certainty;
  /** @generated */ 
  public String getCertainty(int addr) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "org.mipacq.annotation.type.Properties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_certainty);
  }
  /** @generated */    
  public void setCertainty(int addr, String v) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "org.mipacq.annotation.type.Properties");
    ll_cas.ll_setStringValue(addr, casFeatCode_certainty, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Properties_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_polarity = jcas.getRequiredFeatureDE(casType, "polarity", "uima.cas.Boolean", featOkTst);
    casFeatCode_polarity  = (null == casFeat_polarity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_polarity).getCode();

 
    casFeat_certainty = jcas.getRequiredFeatureDE(casType, "certainty", "uima.cas.String", featOkTst);
    casFeatCode_certainty  = (null == casFeat_certainty) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_certainty).getCode();

  }
}



    