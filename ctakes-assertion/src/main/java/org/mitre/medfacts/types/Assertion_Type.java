
/* First created by JCasGen Mon May 23 12:04:34 EDT 2011 */
package org.mitre.medfacts.types;

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
 * Updated by JCasGen Tue Feb 28 11:18:13 EST 2012
 * @generated */
public class Assertion_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Assertion_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Assertion_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Assertion(addr, Assertion_Type.this);
  			   Assertion_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Assertion(addr, Assertion_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Assertion.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mitre.medfacts.types.Assertion");
 
  /** @generated */
  final Feature casFeat_assertionType;
  /** @generated */
  final int     casFeatCode_assertionType;
  /** @generated */ 
  public String getAssertionType(int addr) {
        if (featOkTst && casFeat_assertionType == null)
      jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    return ll_cas.ll_getStringValue(addr, casFeatCode_assertionType);
  }
  /** @generated */    
  public void setAssertionType(int addr, String v) {
        if (featOkTst && casFeat_assertionType == null)
      jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    ll_cas.ll_setStringValue(addr, casFeatCode_assertionType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_associatedConcept;
  /** @generated */
  final int     casFeatCode_associatedConcept;
  /** @generated */ 
  public int getAssociatedConcept(int addr) {
        if (featOkTst && casFeat_associatedConcept == null)
      jcas.throwFeatMissing("associatedConcept", "org.mitre.medfacts.types.Assertion");
    return ll_cas.ll_getRefValue(addr, casFeatCode_associatedConcept);
  }
  /** @generated */    
  public void setAssociatedConcept(int addr, int v) {
        if (featOkTst && casFeat_associatedConcept == null)
      jcas.throwFeatMissing("associatedConcept", "org.mitre.medfacts.types.Assertion");
    ll_cas.ll_setRefValue(addr, casFeatCode_associatedConcept, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Assertion_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_assertionType = jcas.getRequiredFeatureDE(casType, "assertionType", "uima.cas.String", featOkTst);
    casFeatCode_assertionType  = (null == casFeat_assertionType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_assertionType).getCode();

 
    casFeat_associatedConcept = jcas.getRequiredFeatureDE(casType, "associatedConcept", "org.mitre.medfacts.types.Concept", featOkTst);
    casFeatCode_associatedConcept  = (null == casFeat_associatedConcept) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_associatedConcept).getCode();

  }
}



    