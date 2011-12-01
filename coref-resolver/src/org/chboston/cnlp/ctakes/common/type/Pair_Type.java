
/* First created by JCasGen Tue Nov 22 14:28:23 EST 2011 */
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
public class Pair_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Pair_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Pair_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Pair(addr, Pair_Type.this);
  			   Pair_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Pair(addr, Pair_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Pair.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.chboston.cnlp.ctakes.common.type.Pair");
 
  /** @generated */
  final Feature casFeat_p1;
  /** @generated */
  final int     casFeatCode_p1;
  /** @generated */ 
  public int getP1(int addr) {
        if (featOkTst && casFeat_p1 == null)
      jcas.throwFeatMissing("p1", "org.chboston.cnlp.ctakes.common.type.Pair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_p1);
  }
  /** @generated */    
  public void setP1(int addr, int v) {
        if (featOkTst && casFeat_p1 == null)
      jcas.throwFeatMissing("p1", "org.chboston.cnlp.ctakes.common.type.Pair");
    ll_cas.ll_setRefValue(addr, casFeatCode_p1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_p2;
  /** @generated */
  final int     casFeatCode_p2;
  /** @generated */ 
  public int getP2(int addr) {
        if (featOkTst && casFeat_p2 == null)
      jcas.throwFeatMissing("p2", "org.chboston.cnlp.ctakes.common.type.Pair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_p2);
  }
  /** @generated */    
  public void setP2(int addr, int v) {
        if (featOkTst && casFeat_p2 == null)
      jcas.throwFeatMissing("p2", "org.chboston.cnlp.ctakes.common.type.Pair");
    ll_cas.ll_setRefValue(addr, casFeatCode_p2, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Pair_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_p1 = jcas.getRequiredFeatureDE(casType, "p1", "uima.cas.TOP", featOkTst);
    casFeatCode_p1  = (null == casFeat_p1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_p1).getCode();

 
    casFeat_p2 = jcas.getRequiredFeatureDE(casType, "p2", "uima.cas.TOP", featOkTst);
    casFeatCode_p2  = (null == casFeat_p2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_p2).getCode();

  }
}



    