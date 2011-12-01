
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

/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class CoreferenceRelation_Type extends BinaryRelation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CoreferenceRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CoreferenceRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CoreferenceRelation(addr, CoreferenceRelation_Type.this);
  			   CoreferenceRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CoreferenceRelation(addr, CoreferenceRelation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = CoreferenceRelation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mipacq.annotation.type.CoreferenceRelation");
 
  /** @generated */
  final Feature casFeat_coref_prob;
  /** @generated */
  final int     casFeatCode_coref_prob;
  /** @generated */ 
  public double getCoref_prob(int addr) {
        if (featOkTst && casFeat_coref_prob == null)
      jcas.throwFeatMissing("coref_prob", "org.mipacq.annotation.type.CoreferenceRelation");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_coref_prob);
  }
  /** @generated */    
  public void setCoref_prob(int addr, double v) {
        if (featOkTst && casFeat_coref_prob == null)
      jcas.throwFeatMissing("coref_prob", "org.mipacq.annotation.type.CoreferenceRelation");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_coref_prob, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CoreferenceRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_coref_prob = jcas.getRequiredFeatureDE(casType, "coref_prob", "uima.cas.Double", featOkTst);
    casFeatCode_coref_prob  = (null == casFeat_coref_prob) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_coref_prob).getCode();

  }
}



    