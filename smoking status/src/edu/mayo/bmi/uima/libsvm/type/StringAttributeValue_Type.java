
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
public class StringAttributeValue_Type extends AttributeValue_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (StringAttributeValue_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = StringAttributeValue_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new StringAttributeValue(addr, StringAttributeValue_Type.this);
  			   StringAttributeValue_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new StringAttributeValue(addr, StringAttributeValue_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = StringAttributeValue.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.libsvm.type.StringAttributeValue");
 
  /** @generated */
  final Feature casFeat_attributeStringValue;
  /** @generated */
  final int     casFeatCode_attributeStringValue;
  /** @generated */ 
  public String getAttributeStringValue(int addr) {
        if (featOkTst && casFeat_attributeStringValue == null)
      jcas.throwFeatMissing("attributeStringValue", "edu.mayo.bmi.uima.libsvm.type.StringAttributeValue");
    return ll_cas.ll_getStringValue(addr, casFeatCode_attributeStringValue);
  }
  /** @generated */    
  public void setAttributeStringValue(int addr, String v) {
        if (featOkTst && casFeat_attributeStringValue == null)
      jcas.throwFeatMissing("attributeStringValue", "edu.mayo.bmi.uima.libsvm.type.StringAttributeValue");
    ll_cas.ll_setStringValue(addr, casFeatCode_attributeStringValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public StringAttributeValue_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_attributeStringValue = jcas.getRequiredFeatureDE(casType, "attributeStringValue", "uima.cas.String", featOkTst);
    casFeatCode_attributeStringValue  = (null == casFeat_attributeStringValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_attributeStringValue).getCode();

  }
}



    