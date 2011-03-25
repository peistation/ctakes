
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
public class UnknownSmokerNamedEntityAnnotation_Type extends NamedEntity_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (UnknownSmokerNamedEntityAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = UnknownSmokerNamedEntityAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new UnknownSmokerNamedEntityAnnotation(addr, UnknownSmokerNamedEntityAnnotation_Type.this);
  			   UnknownSmokerNamedEntityAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new UnknownSmokerNamedEntityAnnotation(addr, UnknownSmokerNamedEntityAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = UnknownSmokerNamedEntityAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.SmokingStatus.type.UnknownSmokerNamedEntityAnnotation");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public UnknownSmokerNamedEntityAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    