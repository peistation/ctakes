
/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** Hypothesized but negative example of coreference.  Used for debugging coreference model.
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * @generated */
public class NonCoreferenceRelation_Type extends CoreferenceRelation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NonCoreferenceRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NonCoreferenceRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NonCoreferenceRelation(addr, NonCoreferenceRelation_Type.this);
  			   NonCoreferenceRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NonCoreferenceRelation(addr, NonCoreferenceRelation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NonCoreferenceRelation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mipacq.annotation.type.NonCoreferenceRelation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NonCoreferenceRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    