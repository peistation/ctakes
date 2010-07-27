
/* First created by JCasGen Tue Jul 20 20:16:46 CDT 2010 */
package edu.mayo.bmi.uima.pad.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import edu.mayo.bmi.uima.core.type.NamedEntity_Type;

/** Would represent one of the following
Term (in the case of a stand alone term)
Term + Location
Location + term
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * @generated */
public class PADHit_Type extends NamedEntity_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PADHit_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PADHit_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PADHit(addr, PADHit_Type.this);
  			   PADHit_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new PADHit(addr, PADHit_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = PADHit.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.pad.type.PADHit");
 
  /** @generated */
  final Feature casFeat_uaTerm;
  /** @generated */
  final int     casFeatCode_uaTerm;
  /** @generated */ 
  public int getUaTerm(int addr) {
        if (featOkTst && casFeat_uaTerm == null)
      jcas.throwFeatMissing("uaTerm", "edu.mayo.bmi.uima.pad.type.PADHit");
    return ll_cas.ll_getRefValue(addr, casFeatCode_uaTerm);
  }
  /** @generated */    
  public void setUaTerm(int addr, int v) {
        if (featOkTst && casFeat_uaTerm == null)
      jcas.throwFeatMissing("uaTerm", "edu.mayo.bmi.uima.pad.type.PADHit");
    ll_cas.ll_setRefValue(addr, casFeatCode_uaTerm, v);}
    
  
 
  /** @generated */
  final Feature casFeat_uaLocation;
  /** @generated */
  final int     casFeatCode_uaLocation;
  /** @generated */ 
  public int getUaLocation(int addr) {
        if (featOkTst && casFeat_uaLocation == null)
      jcas.throwFeatMissing("uaLocation", "edu.mayo.bmi.uima.pad.type.PADHit");
    return ll_cas.ll_getRefValue(addr, casFeatCode_uaLocation);
  }
  /** @generated */    
  public void setUaLocation(int addr, int v) {
        if (featOkTst && casFeat_uaLocation == null)
      jcas.throwFeatMissing("uaLocation", "edu.mayo.bmi.uima.pad.type.PADHit");
    ll_cas.ll_setRefValue(addr, casFeatCode_uaLocation, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public PADHit_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_uaTerm = jcas.getRequiredFeatureDE(casType, "uaTerm", "edu.mayo.bmi.uima.pad.type.PADTerm", featOkTst);
    casFeatCode_uaTerm  = (null == casFeat_uaTerm) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uaTerm).getCode();

 
    casFeat_uaLocation = jcas.getRequiredFeatureDE(casType, "uaLocation", "edu.mayo.bmi.uima.pad.type.PADLocation", featOkTst);
    casFeatCode_uaLocation  = (null == casFeat_uaLocation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uaLocation).getCode();

  }
}



    