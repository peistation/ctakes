
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
public class Markable_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Markable_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Markable_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Markable(addr, Markable_Type.this);
  			   Markable_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Markable(addr, Markable_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Markable.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.Markable");
 
  /** @generated */
  final Feature casFeat_content;
  /** @generated */
  final int     casFeatCode_content;
  /** @generated */ 
  public int getContent(int addr) {
        if (featOkTst && casFeat_content == null)
      jcas.throwFeatMissing("content", "edu.mayo.bmi.uima.coref.type.Markable");
    return ll_cas.ll_getRefValue(addr, casFeatCode_content);
  }
  /** @generated */    
  public void setContent(int addr, int v) {
        if (featOkTst && casFeat_content == null)
      jcas.throwFeatMissing("content", "edu.mayo.bmi.uima.coref.type.Markable");
    ll_cas.ll_setRefValue(addr, casFeatCode_content, v);}
    
  
 
  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public int getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.coref.type.Markable");
    return ll_cas.ll_getIntValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, int v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.coref.type.Markable");
    ll_cas.ll_setIntValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_anaphoric_prob;
  /** @generated */
  final int     casFeatCode_anaphoric_prob;
  /** @generated */ 
  public double getAnaphoric_prob(int addr) {
        if (featOkTst && casFeat_anaphoric_prob == null)
      jcas.throwFeatMissing("anaphoric_prob", "edu.mayo.bmi.uima.coref.type.Markable");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_anaphoric_prob);
  }
  /** @generated */    
  public void setAnaphoric_prob(int addr, double v) {
        if (featOkTst && casFeat_anaphoric_prob == null)
      jcas.throwFeatMissing("anaphoric_prob", "edu.mayo.bmi.uima.coref.type.Markable");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_anaphoric_prob, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Markable_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_content = jcas.getRequiredFeatureDE(casType, "content", "uima.tcas.Annotation", featOkTst);
    casFeatCode_content  = (null == casFeat_content) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_content).getCode();

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.Integer", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_anaphoric_prob = jcas.getRequiredFeatureDE(casType, "anaphoric_prob", "uima.cas.Double", featOkTst);
    casFeatCode_anaphoric_prob  = (null == casFeat_anaphoric_prob) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_anaphoric_prob).getCode();

  }
}



    