
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class MarkablePairSet_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (MarkablePairSet_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = MarkablePairSet_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new MarkablePairSet(addr, MarkablePairSet_Type.this);
  			   MarkablePairSet_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new MarkablePairSet(addr, MarkablePairSet_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = MarkablePairSet.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.MarkablePairSet");
 
  /** @generated */
  final Feature casFeat_anaphor;
  /** @generated */
  final int     casFeatCode_anaphor;
  /** @generated */ 
  public int getAnaphor(int addr) {
        if (featOkTst && casFeat_anaphor == null)
      jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    return ll_cas.ll_getRefValue(addr, casFeatCode_anaphor);
  }
  /** @generated */    
  public void setAnaphor(int addr, int v) {
        if (featOkTst && casFeat_anaphor == null)
      jcas.throwFeatMissing("anaphor", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    ll_cas.ll_setRefValue(addr, casFeatCode_anaphor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_antecedentList;
  /** @generated */
  final int     casFeatCode_antecedentList;
  /** @generated */ 
  public int getAntecedentList(int addr) {
        if (featOkTst && casFeat_antecedentList == null)
      jcas.throwFeatMissing("antecedentList", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    return ll_cas.ll_getRefValue(addr, casFeatCode_antecedentList);
  }
  /** @generated */    
  public void setAntecedentList(int addr, int v) {
        if (featOkTst && casFeat_antecedentList == null)
      jcas.throwFeatMissing("antecedentList", "edu.mayo.bmi.uima.coref.type.MarkablePairSet");
    ll_cas.ll_setRefValue(addr, casFeatCode_antecedentList, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public MarkablePairSet_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_anaphor = jcas.getRequiredFeatureDE(casType, "anaphor", "edu.mayo.bmi.uima.coref.type.Markable", featOkTst);
    casFeatCode_anaphor  = (null == casFeat_anaphor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_anaphor).getCode();

 
    casFeat_antecedentList = jcas.getRequiredFeatureDE(casType, "antecedentList", "uima.cas.FSList", featOkTst);
    casFeatCode_antecedentList  = (null == casFeat_antecedentList) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_antecedentList).getCode();

  }
}



    