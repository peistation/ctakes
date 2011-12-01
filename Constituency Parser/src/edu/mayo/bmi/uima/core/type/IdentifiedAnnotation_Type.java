
/* First created by JCasGen Mon Jun 27 17:09:54 EDT 2011 */
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
 * Updated by JCasGen Mon Jun 27 17:09:56 EDT 2011
 * @generated */
public class IdentifiedAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (IdentifiedAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = IdentifiedAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new IdentifiedAnnotation(addr, IdentifiedAnnotation_Type.this);
  			   IdentifiedAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new IdentifiedAnnotation(addr, IdentifiedAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = IdentifiedAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.IdentifiedAnnotation");
 
  /** @generated */
  final Feature casFeat_uid;
  /** @generated */
  final int     casFeatCode_uid;
  /** @generated */ 
  public int getUid(int addr) {
        if (featOkTst && casFeat_uid == null)
      jcas.throwFeatMissing("uid", "edu.mayo.bmi.uima.core.type.IdentifiedAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_uid);
  }
  /** @generated */    
  public void setUid(int addr, int v) {
        if (featOkTst && casFeat_uid == null)
      jcas.throwFeatMissing("uid", "edu.mayo.bmi.uima.core.type.IdentifiedAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_uid, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public IdentifiedAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_uid = jcas.getRequiredFeatureDE(casType, "uid", "uima.cas.Integer", featOkTst);
    casFeatCode_uid  = (null == casFeat_uid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uid).getCode();

  }
}



    