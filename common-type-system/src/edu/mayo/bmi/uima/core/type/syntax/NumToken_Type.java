
/* First created by JCasGen Wed Jan 11 14:37:38 EST 2012 */
package edu.mayo.bmi.uima.core.type.syntax;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Equivalent to cTAKES: edu.mayo.bmi.uima.core.type.NumToken
 * Updated by JCasGen Wed Jan 11 14:37:38 EST 2012
 * @generated */
public class NumToken_Type extends BaseToken_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NumToken_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NumToken_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NumToken(addr, NumToken_Type.this);
  			   NumToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NumToken(addr, NumToken_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = NumToken.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.syntax.NumToken");
 
  /** @generated */
  final Feature casFeat_numType;
  /** @generated */
  final int     casFeatCode_numType;
  /** @generated */ 
  public int getNumType(int addr) {
        if (featOkTst && casFeat_numType == null)
      jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.syntax.NumToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_numType);
  }
  /** @generated */    
  public void setNumType(int addr, int v) {
        if (featOkTst && casFeat_numType == null)
      jcas.throwFeatMissing("numType", "edu.mayo.bmi.uima.core.type.syntax.NumToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_numType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NumToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_numType = jcas.getRequiredFeatureDE(casType, "numType", "uima.cas.Integer", featOkTst);
    casFeatCode_numType  = (null == casFeat_numType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_numType).getCode();

  }
}



    