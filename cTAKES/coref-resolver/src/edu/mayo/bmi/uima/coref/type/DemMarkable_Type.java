
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
public class DemMarkable_Type extends Markable_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DemMarkable_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DemMarkable_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DemMarkable(addr, DemMarkable_Type.this);
  			   DemMarkable_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DemMarkable(addr, DemMarkable_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DemMarkable.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.coref.type.DemMarkable");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DemMarkable_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    