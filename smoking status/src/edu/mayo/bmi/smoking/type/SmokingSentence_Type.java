
/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.smoking.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** SentenceAnnotation will be copied to Smoking sentence to adjust the boundaries
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * @generated */
public class SmokingSentence_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SmokingSentence_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SmokingSentence_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SmokingSentence(addr, SmokingSentence_Type.this);
  			   SmokingSentence_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SmokingSentence(addr, SmokingSentence_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SmokingSentence.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.smoking.type.SmokingSentence");
 
  /** @generated */
  final Feature casFeat_sentenceNumber;
  /** @generated */
  final int     casFeatCode_sentenceNumber;
  /** @generated */ 
  public int getSentenceNumber(int addr) {
        if (featOkTst && casFeat_sentenceNumber == null)
      jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.smoking.type.SmokingSentence");
    return ll_cas.ll_getIntValue(addr, casFeatCode_sentenceNumber);
  }
  /** @generated */    
  public void setSentenceNumber(int addr, int v) {
        if (featOkTst && casFeat_sentenceNumber == null)
      jcas.throwFeatMissing("sentenceNumber", "edu.mayo.bmi.smoking.type.SmokingSentence");
    ll_cas.ll_setIntValue(addr, casFeatCode_sentenceNumber, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SmokingSentence_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_sentenceNumber = jcas.getRequiredFeatureDE(casType, "sentenceNumber", "uima.cas.Integer", featOkTst);
    casFeatCode_sentenceNumber  = (null == casFeat_sentenceNumber) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceNumber).getCode();

  }
}



    