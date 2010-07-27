
/* First created by JCasGen Tue Jul 20 20:16:45 CDT 2010 */
package edu.mayo.bmi.uima.core.type;

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
 * Updated by JCasGen Tue Jul 20 20:16:45 CDT 2010
 * @generated */
public class NamedEntity_Type extends IdentifiedAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NamedEntity_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NamedEntity_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NamedEntity(addr, NamedEntity_Type.this);
  			   NamedEntity_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NamedEntity(addr, NamedEntity_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = NamedEntity.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.NamedEntity");
 
  /** @generated */
  final Feature casFeat_discoveryTechnique;
  /** @generated */
  final int     casFeatCode_discoveryTechnique;
  /** @generated */ 
  public int getDiscoveryTechnique(int addr) {
        if (featOkTst && casFeat_discoveryTechnique == null)
      jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_discoveryTechnique);
  }
  /** @generated */    
  public void setDiscoveryTechnique(int addr, int v) {
        if (featOkTst && casFeat_discoveryTechnique == null)
      jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_discoveryTechnique, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ontologyConceptArr;
  /** @generated */
  final int     casFeatCode_ontologyConceptArr;
  /** @generated */ 
  public int getOntologyConceptArr(int addr) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr);
  }
  /** @generated */    
  public void setOntologyConceptArr(int addr, int v) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setRefValue(addr, casFeatCode_ontologyConceptArr, v);}
    
   /** @generated */
  public int getOntologyConceptArr(int addr, int i) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
  }
   
  /** @generated */ 
  public void setOntologyConceptArr(int addr, int i, int v) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.NamedEntity");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_status;
  /** @generated */
  final int     casFeatCode_status;
  /** @generated */ 
  public int getStatus(int addr) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_status);
  }
  /** @generated */    
  public void setStatus(int addr, int v) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_status, v);}
    
  
 
  /** @generated */
  final Feature casFeat_certainty;
  /** @generated */
  final int     casFeatCode_certainty;
  /** @generated */ 
  public int getCertainty(int addr) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_certainty);
  }
  /** @generated */    
  public void setCertainty(int addr, int v) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_certainty, v);}
    
  
 
  /** @generated */
  final Feature casFeat_typeID;
  /** @generated */
  final int     casFeatCode_typeID;
  /** @generated */ 
  public int getTypeID(int addr) {
        if (featOkTst && casFeat_typeID == null)
      jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_typeID);
  }
  /** @generated */    
  public void setTypeID(int addr, int v) {
        if (featOkTst && casFeat_typeID == null)
      jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_typeID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public float getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getFloatValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, float v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setFloatValue(addr, casFeatCode_confidence, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segmentID;
  /** @generated */
  final int     casFeatCode_segmentID;
  /** @generated */ 
  public String getSegmentID(int addr) {
        if (featOkTst && casFeat_segmentID == null)
      jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_segmentID);
  }
  /** @generated */    
  public void setSegmentID(int addr, String v) {
        if (featOkTst && casFeat_segmentID == null)
      jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.NamedEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_segmentID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public NamedEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_discoveryTechnique = jcas.getRequiredFeatureDE(casType, "discoveryTechnique", "uima.cas.Integer", featOkTst);
    casFeatCode_discoveryTechnique  = (null == casFeat_discoveryTechnique) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discoveryTechnique).getCode();

 
    casFeat_ontologyConceptArr = jcas.getRequiredFeatureDE(casType, "ontologyConceptArr", "uima.cas.FSArray", featOkTst);
    casFeatCode_ontologyConceptArr  = (null == casFeat_ontologyConceptArr) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ontologyConceptArr).getCode();

 
    casFeat_status = jcas.getRequiredFeatureDE(casType, "status", "uima.cas.Integer", featOkTst);
    casFeatCode_status  = (null == casFeat_status) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_status).getCode();

 
    casFeat_certainty = jcas.getRequiredFeatureDE(casType, "certainty", "uima.cas.Integer", featOkTst);
    casFeatCode_certainty  = (null == casFeat_certainty) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_certainty).getCode();

 
    casFeat_typeID = jcas.getRequiredFeatureDE(casType, "typeID", "uima.cas.Integer", featOkTst);
    casFeatCode_typeID  = (null == casFeat_typeID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_typeID).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Float", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

 
    casFeat_segmentID = jcas.getRequiredFeatureDE(casType, "segmentID", "uima.cas.String", featOkTst);
    casFeatCode_segmentID  = (null == casFeat_segmentID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segmentID).getCode();

  }
}



    