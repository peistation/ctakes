
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
import org.apache.uima.jcas.cas.TOP_Type;

/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * @generated */
public class OntologyConcept_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (OntologyConcept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = OntologyConcept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new OntologyConcept(addr, OntologyConcept_Type.this);
  			   OntologyConcept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new OntologyConcept(addr, OntologyConcept_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = OntologyConcept.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.OntologyConcept");
 
  /** @generated */
  final Feature casFeat_codingScheme;
  /** @generated */
  final int     casFeatCode_codingScheme;
  /** @generated */ 
  public String getCodingScheme(int addr) {
        if (featOkTst && casFeat_codingScheme == null)
      jcas.throwFeatMissing("codingScheme", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_codingScheme);
  }
  /** @generated */    
  public void setCodingScheme(int addr, String v) {
        if (featOkTst && casFeat_codingScheme == null)
      jcas.throwFeatMissing("codingScheme", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_codingScheme, v);}
    
  
 
  /** @generated */
  final Feature casFeat_code;
  /** @generated */
  final int     casFeatCode_code;
  /** @generated */ 
  public String getCode(int addr) {
        if (featOkTst && casFeat_code == null)
      jcas.throwFeatMissing("code", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_code);
  }
  /** @generated */    
  public void setCode(int addr, String v) {
        if (featOkTst && casFeat_code == null)
      jcas.throwFeatMissing("code", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_code, v);}
    
  
 
  /** @generated */
  final Feature casFeat_oid;
  /** @generated */
  final int     casFeatCode_oid;
  /** @generated */ 
  public String getOid(int addr) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_oid);
  }
  /** @generated */    
  public void setOid(int addr, String v) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "edu.mayo.bmi.uima.core.type.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_oid, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public OntologyConcept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_codingScheme = jcas.getRequiredFeatureDE(casType, "codingScheme", "uima.cas.String", featOkTst);
    casFeatCode_codingScheme  = (null == casFeat_codingScheme) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_codingScheme).getCode();

 
    casFeat_code = jcas.getRequiredFeatureDE(casType, "code", "uima.cas.String", featOkTst);
    casFeatCode_code  = (null == casFeat_code) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_code).getCode();

 
    casFeat_oid = jcas.getRequiredFeatureDE(casType, "oid", "uima.cas.String", featOkTst);
    casFeatCode_oid  = (null == casFeat_oid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_oid).getCode();

  }
}



    