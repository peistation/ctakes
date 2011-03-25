
/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.uima.SmokingStatus.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import edu.mayo.bmi.uima.core.type.NamedEntity_Type;

/** 
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * @generated */
public class SmokerNamedEntityAnnotation_Type extends NamedEntity_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SmokerNamedEntityAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SmokerNamedEntityAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SmokerNamedEntityAnnotation(addr, SmokerNamedEntityAnnotation_Type.this);
  			   SmokerNamedEntityAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SmokerNamedEntityAnnotation(addr, SmokerNamedEntityAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SmokerNamedEntityAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.SmokingStatus.type.SmokerNamedEntityAnnotation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SmokerNamedEntityAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    