
/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.relation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Fri Jan 20 10:31:37 EST 2012
 * @generated */
public class CollectionTextRelation_Type extends Relation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CollectionTextRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CollectionTextRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CollectionTextRelation(addr, CollectionTextRelation_Type.this);
  			   CollectionTextRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CollectionTextRelation(addr, CollectionTextRelation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = CollectionTextRelation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.relation.CollectionTextRelation");
 
  /** @generated */
  final Feature casFeat_members;
  /** @generated */
  final int     casFeatCode_members;
  /** @generated */ 
  public int getMembers(int addr) {
        if (featOkTst && casFeat_members == null)
      jcas.throwFeatMissing("members", "edu.mayo.bmi.uima.core.type.relation.CollectionTextRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_members);
  }
  /** @generated */    
  public void setMembers(int addr, int v) {
        if (featOkTst && casFeat_members == null)
      jcas.throwFeatMissing("members", "edu.mayo.bmi.uima.core.type.relation.CollectionTextRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_members, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CollectionTextRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_members = jcas.getRequiredFeatureDE(casType, "members", "uima.cas.FSList", featOkTst);
    casFeatCode_members  = (null == casFeat_members) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_members).getCode();

  }
}



    