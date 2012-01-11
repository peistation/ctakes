
/* First created by JCasGen Wed Jan 11 14:37:38 EST 2012 */
package edu.mayo.bmi.uima.core.type.util;

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

/** A brute force "hash" that stores multiple Pairs in a list. Equivalent to cTAKES: 
edu.mayo.bmi.uima.core.type.Properties
 * Updated by JCasGen Wed Jan 11 14:37:38 EST 2012
 * @generated */
public class Hash_Type extends TOP_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Hash_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Hash_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Hash(addr, Hash_Type.this);
  			   Hash_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Hash(addr, Hash_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Hash.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.util.Hash");
 
  /** @generated */
  final Feature casFeat_pairs;
  /** @generated */
  final int     casFeatCode_pairs;
  /** @generated */ 
  public int getPairs(int addr) {
        if (featOkTst && casFeat_pairs == null)
      jcas.throwFeatMissing("pairs", "edu.mayo.bmi.uima.core.type.util.Hash");
    return ll_cas.ll_getRefValue(addr, casFeatCode_pairs);
  }
  /** @generated */    
  public void setPairs(int addr, int v) {
        if (featOkTst && casFeat_pairs == null)
      jcas.throwFeatMissing("pairs", "edu.mayo.bmi.uima.core.type.util.Hash");
    ll_cas.ll_setRefValue(addr, casFeatCode_pairs, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Hash_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_pairs = jcas.getRequiredFeatureDE(casType, "pairs", "uima.cas.FSList", featOkTst);
    casFeatCode_pairs  = (null == casFeat_pairs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pairs).getCode();

  }
}



    