
/* First created by JCasGen Thu Jan 19 17:46:27 EST 2012 */
package edu.mayo.bmi.uima.core.type.textsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** Equivalent to cTAKES: edu.mayo.bmi.uima.cdt.type.RomanNumeralAnnotation
 * Updated by JCasGen Fri Jan 20 10:31:55 EST 2012
 * @generated */
public class RomanNumeralAnnotation_Type extends IdentifiedAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (RomanNumeralAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = RomanNumeralAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new RomanNumeralAnnotation(addr, RomanNumeralAnnotation_Type.this);
  			   RomanNumeralAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new RomanNumeralAnnotation(addr, RomanNumeralAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = RomanNumeralAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.textsem.RomanNumeralAnnotation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public RomanNumeralAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    