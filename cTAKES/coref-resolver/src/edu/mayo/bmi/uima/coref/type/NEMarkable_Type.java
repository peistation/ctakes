
/* First created by JCasGen Thu Dec 01 13:16:44 EST 2011 */
package edu.mayo.bmi.uima.coref.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * @generated */
public class NEMarkable_Type extends Markable_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NEMarkable_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NEMarkable_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NEMarkable(addr, NEMarkable_Type.this);
  			   NEMarkable_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NEMarkable(addr, NEMarkable_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NEMarkable.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.NEMarkable");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NEMarkable_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    