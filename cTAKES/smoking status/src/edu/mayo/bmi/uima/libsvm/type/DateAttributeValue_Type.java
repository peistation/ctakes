
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
public class DateAttributeValue_Type extends AttributeValue_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DateAttributeValue_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DateAttributeValue_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DateAttributeValue(addr, DateAttributeValue_Type.this);
  			   DateAttributeValue_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DateAttributeValue(addr, DateAttributeValue_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DateAttributeValue.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.libsvm.type.DateAttributeValue");
 
  /** @generated */
  final Feature casFeat_dateValue;
  /** @generated */
  final int     casFeatCode_dateValue;
  /** @generated */ 
  public String getDateValue(int addr) {
        if (featOkTst && casFeat_dateValue == null)
      jcas.throwFeatMissing("dateValue", "edu.mayo.bmi.uima.libsvm.type.DateAttributeValue");
    return ll_cas.ll_getStringValue(addr, casFeatCode_dateValue);
  }
  /** @generated */    
  public void setDateValue(int addr, String v) {
        if (featOkTst && casFeat_dateValue == null)
      jcas.throwFeatMissing("dateValue", "edu.mayo.bmi.uima.libsvm.type.DateAttributeValue");
    ll_cas.ll_setStringValue(addr, casFeatCode_dateValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DateAttributeValue_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_dateValue = jcas.getRequiredFeatureDE(casType, "dateValue", "uima.cas.String", featOkTst);
    casFeatCode_dateValue  = (null == casFeat_dateValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dateValue).getCode();

  }
}



    