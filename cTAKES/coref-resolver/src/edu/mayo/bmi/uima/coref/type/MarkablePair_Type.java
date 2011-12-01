
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
import org.apache.uima.jcas.cas.TOP_Type;

/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class MarkablePair_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (MarkablePair_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = MarkablePair_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new MarkablePair(addr, MarkablePair_Type.this);
  			   MarkablePair_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new MarkablePair(addr, MarkablePair_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = MarkablePair.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.MarkablePair");
 
  /** @generated */
  final Feature casFeat_antecedent;
  /** @generated */
  final int     casFeatCode_antecedent;
  /** @generated */ 
  public int getAntecedent(int addr) {
        if (featOkTst && casFeat_antecedent == null)
      jcas.throwFeatMissing("antecedent", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_antecedent);
  }
  /** @generated */    
  public void setAntecedent(int addr, int v) {
        if (featOkTst && casFeat_antecedent == null)
      jcas.throwFeatMissing("antecedent", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    ll_cas.ll_setRefValue(addr, casFeatCode_antecedent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_anaphor;
  /** @generated */
  final int     casFeatCode_anaphor;
  /** @generated */ 
  public int getAnaphor(int addr) {
        if (featOkTst && casFeat_anaphor == null)
      jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return ll_cas.ll_getRefValue(addr, casFeatCode_anaphor);
  }
  /** @generated */    
  public void setAnaphor(int addr, int v) {
        if (featOkTst && casFeat_anaphor == null)
      jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    ll_cas.ll_setRefValue(addr, casFeatCode_anaphor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated */ 
  public boolean getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_label);
  }
  /** @generated */    
  public void setLabel(int addr, boolean v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "edu.mayo.bmi.uima.coref.type.MarkablePair");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_label, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public MarkablePair_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_antecedent = jcas.getRequiredFeatureDE(casType, "antecedent", "edu.mayo.bmi.uima.coref.type.Markable", featOkTst);
    casFeatCode_antecedent  = (null == casFeat_antecedent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_antecedent).getCode();

 
    casFeat_anaphor = jcas.getRequiredFeatureDE(casType, "anaphor", "edu.mayo.bmi.uima.coref.type.Markable", featOkTst);
    casFeatCode_anaphor  = (null == casFeat_anaphor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_anaphor).getCode();

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.Boolean", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

  }
}



    