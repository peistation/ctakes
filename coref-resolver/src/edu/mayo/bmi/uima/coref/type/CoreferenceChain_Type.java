
/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
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
import org.mipacq.annotation.type.CollectionRelation_Type;

/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class CoreferenceChain_Type extends CollectionRelation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CoreferenceChain_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CoreferenceChain_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CoreferenceChain(addr, CoreferenceChain_Type.this);
  			   CoreferenceChain_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CoreferenceChain(addr, CoreferenceChain_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = CoreferenceChain.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.CoreferenceChain");
 
  /** @generated */
  final Feature casFeat_size;
  /** @generated */
  final int     casFeatCode_size;
  /** @generated */ 
  public int getSize(int addr) {
        if (featOkTst && casFeat_size == null)
      jcas.throwFeatMissing("size", "edu.mayo.bmi.uima.coref.type.CoreferenceChain");
    return ll_cas.ll_getIntValue(addr, casFeatCode_size);
  }
  /** @generated */    
  public void setSize(int addr, int v) {
        if (featOkTst && casFeat_size == null)
      jcas.throwFeatMissing("size", "edu.mayo.bmi.uima.coref.type.CoreferenceChain");
    ll_cas.ll_setIntValue(addr, casFeatCode_size, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CoreferenceChain_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_size = jcas.getRequiredFeatureDE(casType, "size", "uima.cas.Integer", featOkTst);
    casFeatCode_size  = (null == casFeat_size) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_size).getCode();

  }
}



    