
/* First created by JCasGen Wed Jan 11 14:37:38 EST 2012 */
package edu.mayo.bmi.uima.core.type.textsem;

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

/** More generalized from cTAKES: edu.mayo.bmi.uima.core.type.IdentifiedAnnotation. Any span of text that has been discovered or flagged for some reason, such as a Named Entity.  Allows for mapping to an ontology.
 * Updated by JCasGen Wed Jan 11 14:37:38 EST 2012
 * @generated */
public class IdentifiedAnnotation_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (IdentifiedAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = IdentifiedAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new IdentifiedAnnotation(addr, IdentifiedAnnotation_Type.this);
  			   IdentifiedAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new IdentifiedAnnotation(addr, IdentifiedAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = IdentifiedAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
 
  /** @generated */
  final Feature casFeat_id;
  /** @generated */
  final int     casFeatCode_id;
  /** @generated */ 
  public int getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_id);
  }
  /** @generated */    
  public void setId(int addr, int v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_id, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ontologyConceptArr;
  /** @generated */
  final int     casFeatCode_ontologyConceptArr;
  /** @generated */ 
  public int getOntologyConceptArr(int addr) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr);
  }
  /** @generated */    
  public void setOntologyConceptArr(int addr, int v) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_ontologyConceptArr, v);}
    
   /** @generated */
  public int getOntologyConceptArr(int addr, int i) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
  }
   
  /** @generated */ 
  public void setOntologyConceptArr(int addr, int i, int v) {
        if (featOkTst && casFeat_ontologyConceptArr == null)
      jcas.throwFeatMissing("ontologyConceptArr", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ontologyConceptArr), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_typeID;
  /** @generated */
  final int     casFeatCode_typeID;
  /** @generated */ 
  public int getTypeID(int addr) {
        if (featOkTst && casFeat_typeID == null)
      jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_typeID);
  }
  /** @generated */    
  public void setTypeID(int addr, int v) {
        if (featOkTst && casFeat_typeID == null)
      jcas.throwFeatMissing("typeID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_typeID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_segmentID;
  /** @generated */
  final int     casFeatCode_segmentID;
  /** @generated */ 
  public String getSegmentID(int addr) {
        if (featOkTst && casFeat_segmentID == null)
      jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_segmentID);
  }
  /** @generated */    
  public void setSegmentID(int addr, String v) {
        if (featOkTst && casFeat_segmentID == null)
      jcas.throwFeatMissing("segmentID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_segmentID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sentenceID;
  /** @generated */
  final int     casFeatCode_sentenceID;
  /** @generated */ 
  public String getSentenceID(int addr) {
        if (featOkTst && casFeat_sentenceID == null)
      jcas.throwFeatMissing("sentenceID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sentenceID);
  }
  /** @generated */    
  public void setSentenceID(int addr, String v) {
        if (featOkTst && casFeat_sentenceID == null)
      jcas.throwFeatMissing("sentenceID", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_sentenceID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_category;
  /** @generated */
  final int     casFeatCode_category;
  /** @generated */ 
  public String getCategory(int addr) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_category);
  }
  /** @generated */    
  public void setCategory(int addr, String v) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_category, v);}
    
  
 
  /** @generated */
  final Feature casFeat_discoveryTechnique;
  /** @generated */
  final int     casFeatCode_discoveryTechnique;
  /** @generated */ 
  public int getDiscoveryTechnique(int addr) {
        if (featOkTst && casFeat_discoveryTechnique == null)
      jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_discoveryTechnique);
  }
  /** @generated */    
  public void setDiscoveryTechnique(int addr, int v) {
        if (featOkTst && casFeat_discoveryTechnique == null)
      jcas.throwFeatMissing("discoveryTechnique", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setIntValue(addr, casFeatCode_discoveryTechnique, v);}
    
  
 
  /** @generated */
  final Feature casFeat_confidence;
  /** @generated */
  final int     casFeatCode_confidence;
  /** @generated */ 
  public float getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    return ll_cas.ll_getFloatValue(addr, casFeatCode_confidence);
  }
  /** @generated */    
  public void setConfidence(int addr, float v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation");
    ll_cas.ll_setFloatValue(addr, casFeatCode_confidence, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public IdentifiedAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.Integer", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_ontologyConceptArr = jcas.getRequiredFeatureDE(casType, "ontologyConceptArr", "uima.cas.FSArray", featOkTst);
    casFeatCode_ontologyConceptArr  = (null == casFeat_ontologyConceptArr) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ontologyConceptArr).getCode();

 
    casFeat_typeID = jcas.getRequiredFeatureDE(casType, "typeID", "uima.cas.Integer", featOkTst);
    casFeatCode_typeID  = (null == casFeat_typeID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_typeID).getCode();

 
    casFeat_segmentID = jcas.getRequiredFeatureDE(casType, "segmentID", "uima.cas.String", featOkTst);
    casFeatCode_segmentID  = (null == casFeat_segmentID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_segmentID).getCode();

 
    casFeat_sentenceID = jcas.getRequiredFeatureDE(casType, "sentenceID", "uima.cas.String", featOkTst);
    casFeatCode_sentenceID  = (null == casFeat_sentenceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceID).getCode();

 
    casFeat_category = jcas.getRequiredFeatureDE(casType, "category", "uima.cas.String", featOkTst);
    casFeatCode_category  = (null == casFeat_category) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_category).getCode();

 
    casFeat_discoveryTechnique = jcas.getRequiredFeatureDE(casType, "discoveryTechnique", "uima.cas.Integer", featOkTst);
    casFeatCode_discoveryTechnique  = (null == casFeat_discoveryTechnique) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_discoveryTechnique).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Float", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

  }
}



    