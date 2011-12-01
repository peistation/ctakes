
/* First created by JCasGen Tue Nov 22 14:28:43 EST 2011 */
package org.chboston.cnlp.ctakes.common.type;

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
public class BooleanLabeledFS_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (BooleanLabeledFS_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = BooleanLabeledFS_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new BooleanLabeledFS(addr, BooleanLabeledFS_Type.this);
  			   BooleanLabeledFS_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new BooleanLabeledFS(addr, BooleanLabeledFS_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = BooleanLabeledFS.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated */ 
  public boolean getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_label);
  }
  /** @generated */    
  public void setLabel(int addr, boolean v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_feature;
  /** @generated */
  final int     casFeatCode_feature;
  /** @generated */ 
  public int getFeature(int addr) {
        if (featOkTst && casFeat_feature == null)
      jcas.throwFeatMissing("feature", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    return ll_cas.ll_getRefValue(addr, casFeatCode_feature);
  }
  /** @generated */    
  public void setFeature(int addr, int v) {
        if (featOkTst && casFeat_feature == null)
      jcas.throwFeatMissing("feature", "org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS");
    ll_cas.ll_setRefValue(addr, casFeatCode_feature, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public BooleanLabeledFS_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.Boolean", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_feature = jcas.getRequiredFeatureDE(casType, "feature", "uima.cas.TOP", featOkTst);
    casFeatCode_feature  = (null == casFeat_feature) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_feature).getCode();

  }
}



    