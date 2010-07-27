
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * @generated */
public class SubSection_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SubSection_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SubSection_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SubSection(addr, SubSection_Type.this);
  			   SubSection_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SubSection(addr, SubSection_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SubSection.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.pad.type.SubSection");
 
  /** @generated */
  final Feature casFeat_parentSectionId;
  /** @generated */
  final int     casFeatCode_parentSectionId;
  /** @generated */ 
  public String getParentSectionId(int addr) {
        if (featOkTst && casFeat_parentSectionId == null)
      jcas.throwFeatMissing("parentSectionId", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getStringValue(addr, casFeatCode_parentSectionId);
  }
  /** @generated */    
  public void setParentSectionId(int addr, String v) {
        if (featOkTst && casFeat_parentSectionId == null)
      jcas.throwFeatMissing("parentSectionId", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setStringValue(addr, casFeatCode_parentSectionId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subSectionBodyBegin;
  /** @generated */
  final int     casFeatCode_subSectionBodyBegin;
  /** @generated */ 
  public int getSubSectionBodyBegin(int addr) {
        if (featOkTst && casFeat_subSectionBodyBegin == null)
      jcas.throwFeatMissing("subSectionBodyBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getIntValue(addr, casFeatCode_subSectionBodyBegin);
  }
  /** @generated */    
  public void setSubSectionBodyBegin(int addr, int v) {
        if (featOkTst && casFeat_subSectionBodyBegin == null)
      jcas.throwFeatMissing("subSectionBodyBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setIntValue(addr, casFeatCode_subSectionBodyBegin, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subSectionBodyEnd;
  /** @generated */
  final int     casFeatCode_subSectionBodyEnd;
  /** @generated */ 
  public int getSubSectionBodyEnd(int addr) {
        if (featOkTst && casFeat_subSectionBodyEnd == null)
      jcas.throwFeatMissing("subSectionBodyEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getIntValue(addr, casFeatCode_subSectionBodyEnd);
  }
  /** @generated */    
  public void setSubSectionBodyEnd(int addr, int v) {
        if (featOkTst && casFeat_subSectionBodyEnd == null)
      jcas.throwFeatMissing("subSectionBodyEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setIntValue(addr, casFeatCode_subSectionBodyEnd, v);}
    
  
 
  /** @generated */
  final Feature casFeat_status;
  /** @generated */
  final int     casFeatCode_status;
  /** @generated */ 
  public int getStatus(int addr) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getIntValue(addr, casFeatCode_status);
  }
  /** @generated */    
  public void setStatus(int addr, int v) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setIntValue(addr, casFeatCode_status, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subSectionHeaderBegin;
  /** @generated */
  final int     casFeatCode_subSectionHeaderBegin;
  /** @generated */ 
  public int getSubSectionHeaderBegin(int addr) {
        if (featOkTst && casFeat_subSectionHeaderBegin == null)
      jcas.throwFeatMissing("subSectionHeaderBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getIntValue(addr, casFeatCode_subSectionHeaderBegin);
  }
  /** @generated */    
  public void setSubSectionHeaderBegin(int addr, int v) {
        if (featOkTst && casFeat_subSectionHeaderBegin == null)
      jcas.throwFeatMissing("subSectionHeaderBegin", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setIntValue(addr, casFeatCode_subSectionHeaderBegin, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subSectionHeaderEnd;
  /** @generated */
  final int     casFeatCode_subSectionHeaderEnd;
  /** @generated */ 
  public int getSubSectionHeaderEnd(int addr) {
        if (featOkTst && casFeat_subSectionHeaderEnd == null)
      jcas.throwFeatMissing("subSectionHeaderEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    return ll_cas.ll_getIntValue(addr, casFeatCode_subSectionHeaderEnd);
  }
  /** @generated */    
  public void setSubSectionHeaderEnd(int addr, int v) {
        if (featOkTst && casFeat_subSectionHeaderEnd == null)
      jcas.throwFeatMissing("subSectionHeaderEnd", "edu.mayo.bmi.uima.pad.type.SubSection");
    ll_cas.ll_setIntValue(addr, casFeatCode_subSectionHeaderEnd, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SubSection_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_parentSectionId = jcas.getRequiredFeatureDE(casType, "parentSectionId", "uima.cas.String", featOkTst);
    casFeatCode_parentSectionId  = (null == casFeat_parentSectionId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parentSectionId).getCode();

 
    casFeat_subSectionBodyBegin = jcas.getRequiredFeatureDE(casType, "subSectionBodyBegin", "uima.cas.Integer", featOkTst);
    casFeatCode_subSectionBodyBegin  = (null == casFeat_subSectionBodyBegin) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subSectionBodyBegin).getCode();

 
    casFeat_subSectionBodyEnd = jcas.getRequiredFeatureDE(casType, "subSectionBodyEnd", "uima.cas.Integer", featOkTst);
    casFeatCode_subSectionBodyEnd  = (null == casFeat_subSectionBodyEnd) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subSectionBodyEnd).getCode();

 
    casFeat_status = jcas.getRequiredFeatureDE(casType, "status", "uima.cas.Integer", featOkTst);
    casFeatCode_status  = (null == casFeat_status) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_status).getCode();

 
    casFeat_subSectionHeaderBegin = jcas.getRequiredFeatureDE(casType, "subSectionHeaderBegin", "uima.cas.Integer", featOkTst);
    casFeatCode_subSectionHeaderBegin  = (null == casFeat_subSectionHeaderBegin) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subSectionHeaderBegin).getCode();

 
    casFeat_subSectionHeaderEnd = jcas.getRequiredFeatureDE(casType, "subSectionHeaderEnd", "uima.cas.Integer", featOkTst);
    casFeatCode_subSectionHeaderEnd  = (null == casFeat_subSectionHeaderEnd) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subSectionHeaderEnd).getCode();

  }
}



    