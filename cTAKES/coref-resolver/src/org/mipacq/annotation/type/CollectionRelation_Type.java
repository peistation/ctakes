
/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

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
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * @generated */
public class CollectionRelation_Type extends Relation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CollectionRelation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CollectionRelation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CollectionRelation(addr, CollectionRelation_Type.this);
  			   CollectionRelation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CollectionRelation(addr, CollectionRelation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = CollectionRelation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mipacq.annotation.type.CollectionRelation");
 
  /** @generated */
  final Feature casFeat_members;
  /** @generated */
  final int     casFeatCode_members;
  /** @generated */ 
  public int getMembers(int addr) {
        if (featOkTst && casFeat_members == null)
      jcas.throwFeatMissing("members", "org.mipacq.annotation.type.CollectionRelation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_members);
  }
  /** @generated */    
  public void setMembers(int addr, int v) {
        if (featOkTst && casFeat_members == null)
      jcas.throwFeatMissing("members", "org.mipacq.annotation.type.CollectionRelation");
    ll_cas.ll_setRefValue(addr, casFeatCode_members, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public CollectionRelation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_members = jcas.getRequiredFeatureDE(casType, "members", "uima.cas.FSList", featOkTst);
    casFeatCode_members  = (null == casFeat_members) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_members).getCode();

  }
}



    