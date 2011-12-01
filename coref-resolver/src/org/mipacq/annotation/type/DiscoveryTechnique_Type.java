
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
public class DiscoveryTechnique_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiscoveryTechnique_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiscoveryTechnique_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiscoveryTechnique(addr, DiscoveryTechnique_Type.this);
  			   DiscoveryTechnique_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiscoveryTechnique(addr, DiscoveryTechnique_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DiscoveryTechnique.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mipacq.annotation.type.DiscoveryTechnique");
 
  /** @generated */
  final Feature casFeat_technique;
  /** @generated */
  final int     casFeatCode_technique;
  /** @generated */ 
  public String getTechnique(int addr) {
        if (featOkTst && casFeat_technique == null)
      jcas.throwFeatMissing("technique", "org.mipacq.annotation.type.DiscoveryTechnique");
    return ll_cas.ll_getStringValue(addr, casFeatCode_technique);
  }
  /** @generated */    
  public void setTechnique(int addr, String v) {
        if (featOkTst && casFeat_technique == null)
      jcas.throwFeatMissing("technique", "org.mipacq.annotation.type.DiscoveryTechnique");
    ll_cas.ll_setStringValue(addr, casFeatCode_technique, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "org.mipacq.annotation.type.DiscoveryTechnique");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "org.mipacq.annotation.type.DiscoveryTechnique");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DiscoveryTechnique_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_technique = jcas.getRequiredFeatureDE(casType, "technique", "uima.cas.String", featOkTst);
    casFeatCode_technique  = (null == casFeat_technique) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_technique).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

  }
}



    