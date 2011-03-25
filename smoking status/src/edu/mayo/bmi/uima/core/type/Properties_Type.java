
/* First created by JCasGen Thu Nov 18 10:02:31 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * @generated */
public class Properties_Type extends Annotation_Type {
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
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.Properties");
 
  /** @generated */
  final Feature casFeat_propArr;
  /** @generated */
  final int     casFeatCode_propArr;
  /** @generated */ 
  public int getPropArr(int addr) {
        if (featOkTst && casFeat_propArr == null)
      jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    return ll_cas.ll_getRefValue(addr, casFeatCode_propArr);
  }
  /** @generated */    
  public void setPropArr(int addr, int v) {
        if (featOkTst && casFeat_propArr == null)
      jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    ll_cas.ll_setRefValue(addr, casFeatCode_propArr, v);}
    
   /** @generated */
  public int getPropArr(int addr, int i) {
        if (featOkTst && casFeat_propArr == null)
      jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i);
  }
   
  /** @generated */ 
  public void setPropArr(int addr, int i, int v) {
        if (featOkTst && casFeat_propArr == null)
      jcas.throwFeatMissing("propArr", "edu.mayo.bmi.uima.core.type.Properties");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_propArr), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Properties_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_propArr = jcas.getRequiredFeatureDE(casType, "propArr", "uima.cas.FSArray", featOkTst);
    casFeatCode_propArr  = (null == casFeat_propArr) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_propArr).getCode();

  }
}



    