
/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Thu Nov 18 10:02:32 CST 2010
 * @generated */
public class NominalAttributeValue_Type extends AttributeValue_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NominalAttributeValue_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NominalAttributeValue_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NominalAttributeValue(addr, NominalAttributeValue_Type.this);
  			   NominalAttributeValue_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NominalAttributeValue(addr, NominalAttributeValue_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NominalAttributeValue.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.libsvm.type.NominalAttributeValue");
 
  /** @generated */
  final Feature casFeat_nominalValue;
  /** @generated */
  final int     casFeatCode_nominalValue;
  /** @generated */ 
  public String getNominalValue(int addr) {
        if (featOkTst && casFeat_nominalValue == null)
      jcas.throwFeatMissing("nominalValue", "edu.mayo.bmi.uima.libsvm.type.NominalAttributeValue");
    return ll_cas.ll_getStringValue(addr, casFeatCode_nominalValue);
  }
  /** @generated */    
  public void setNominalValue(int addr, String v) {
        if (featOkTst && casFeat_nominalValue == null)
      jcas.throwFeatMissing("nominalValue", "edu.mayo.bmi.uima.libsvm.type.NominalAttributeValue");
    ll_cas.ll_setStringValue(addr, casFeatCode_nominalValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NominalAttributeValue_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_nominalValue = jcas.getRequiredFeatureDE(casType, "nominalValue", "uima.cas.String", featOkTst);
    casFeatCode_nominalValue  = (null == casFeat_nominalValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nominalValue).getCode();

  }
}



    