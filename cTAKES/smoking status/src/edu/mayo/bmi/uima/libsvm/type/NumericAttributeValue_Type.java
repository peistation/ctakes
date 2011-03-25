
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

/** Corresponds to the ARFF numeric attributes.
 * Updated by JCasGen Thu Nov 18 10:02:32 CST 2010
 * @generated */
public class NumericAttributeValue_Type extends AttributeValue_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NumericAttributeValue_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NumericAttributeValue_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NumericAttributeValue(addr, NumericAttributeValue_Type.this);
  			   NumericAttributeValue_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NumericAttributeValue(addr, NumericAttributeValue_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NumericAttributeValue.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.libsvm.type.NumericAttributeValue");
 
  /** @generated */
  final Feature casFeat_numericValue;
  /** @generated */
  final int     casFeatCode_numericValue;
  /** @generated */ 
  public String getNumericValue(int addr) {
        if (featOkTst && casFeat_numericValue == null)
      jcas.throwFeatMissing("numericValue", "edu.mayo.bmi.uima.libsvm.type.NumericAttributeValue");
    return ll_cas.ll_getStringValue(addr, casFeatCode_numericValue);
  }
  /** @generated */    
  public void setNumericValue(int addr, String v) {
        if (featOkTst && casFeat_numericValue == null)
      jcas.throwFeatMissing("numericValue", "edu.mayo.bmi.uima.libsvm.type.NumericAttributeValue");
    ll_cas.ll_setStringValue(addr, casFeatCode_numericValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NumericAttributeValue_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_numericValue = jcas.getRequiredFeatureDE(casType, "numericValue", "uima.cas.String", featOkTst);
    casFeatCode_numericValue  = (null == casFeat_numericValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_numericValue).getCode();

  }
}



    