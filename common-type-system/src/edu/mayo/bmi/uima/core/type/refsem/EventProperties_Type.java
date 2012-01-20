
/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP_Type;

/** A set of mostly temporal properties that are unique to Events.
 * Updated by JCasGen Fri Jan 20 10:31:31 EST 2012
 * @generated */
public class EventProperties_Type extends TOP_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (EventProperties_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = EventProperties_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new EventProperties(addr, EventProperties_Type.this);
  			   EventProperties_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new EventProperties(addr, EventProperties_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = EventProperties.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.refsem.EventProperties");
 
  /** @generated */
  final Feature casFeat_contextualModality;
  /** @generated */
  final int     casFeatCode_contextualModality;
  /** @generated */ 
  public String getContextualModality(int addr) {
        if (featOkTst && casFeat_contextualModality == null)
      jcas.throwFeatMissing("contextualModality", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_contextualModality);
  }
  /** @generated */    
  public void setContextualModality(int addr, String v) {
        if (featOkTst && casFeat_contextualModality == null)
      jcas.throwFeatMissing("contextualModality", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_contextualModality, v);}
    
  
 
  /** @generated */
  final Feature casFeat_contextualAspect;
  /** @generated */
  final int     casFeatCode_contextualAspect;
  /** @generated */ 
  public String getContextualAspect(int addr) {
        if (featOkTst && casFeat_contextualAspect == null)
      jcas.throwFeatMissing("contextualAspect", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_contextualAspect);
  }
  /** @generated */    
  public void setContextualAspect(int addr, String v) {
        if (featOkTst && casFeat_contextualAspect == null)
      jcas.throwFeatMissing("contextualAspect", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_contextualAspect, v);}
    
  
 
  /** @generated */
  final Feature casFeat_permanence;
  /** @generated */
  final int     casFeatCode_permanence;
  /** @generated */ 
  public String getPermanence(int addr) {
        if (featOkTst && casFeat_permanence == null)
      jcas.throwFeatMissing("permanence", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_permanence);
  }
  /** @generated */    
  public void setPermanence(int addr, String v) {
        if (featOkTst && casFeat_permanence == null)
      jcas.throwFeatMissing("permanence", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_permanence, v);}
    
  
 
  /** @generated */
  final Feature casFeat_category;
  /** @generated */
  final int     casFeatCode_category;
  /** @generated */ 
  public String getCategory(int addr) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_category);
  }
  /** @generated */    
  public void setCategory(int addr, String v) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_category, v);}
    
  
 
  /** @generated */
  final Feature casFeat_aspect;
  /** @generated */
  final int     casFeatCode_aspect;
  /** @generated */ 
  public String getAspect(int addr) {
        if (featOkTst && casFeat_aspect == null)
      jcas.throwFeatMissing("aspect", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_aspect);
  }
  /** @generated */    
  public void setAspect(int addr, String v) {
        if (featOkTst && casFeat_aspect == null)
      jcas.throwFeatMissing("aspect", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_aspect, v);}
    
  
 
  /** @generated */
  final Feature casFeat_docTimeRel;
  /** @generated */
  final int     casFeatCode_docTimeRel;
  /** @generated */ 
  public String getDocTimeRel(int addr) {
        if (featOkTst && casFeat_docTimeRel == null)
      jcas.throwFeatMissing("docTimeRel", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_docTimeRel);
  }
  /** @generated */    
  public void setDocTimeRel(int addr, String v) {
        if (featOkTst && casFeat_docTimeRel == null)
      jcas.throwFeatMissing("docTimeRel", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_docTimeRel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_degree;
  /** @generated */
  final int     casFeatCode_degree;
  /** @generated */ 
  public String getDegree(int addr) {
        if (featOkTst && casFeat_degree == null)
      jcas.throwFeatMissing("degree", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getStringValue(addr, casFeatCode_degree);
  }
  /** @generated */    
  public void setDegree(int addr, String v) {
        if (featOkTst && casFeat_degree == null)
      jcas.throwFeatMissing("degree", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setStringValue(addr, casFeatCode_degree, v);}
    
  
 
  /** @generated */
  final Feature casFeat_polarity;
  /** @generated */
  final int     casFeatCode_polarity;
  /** @generated */ 
  public int getPolarity(int addr) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    return ll_cas.ll_getIntValue(addr, casFeatCode_polarity);
  }
  /** @generated */    
  public void setPolarity(int addr, int v) {
        if (featOkTst && casFeat_polarity == null)
      jcas.throwFeatMissing("polarity", "edu.mayo.bmi.uima.core.type.refsem.EventProperties");
    ll_cas.ll_setIntValue(addr, casFeatCode_polarity, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public EventProperties_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_contextualModality = jcas.getRequiredFeatureDE(casType, "contextualModality", "uima.cas.String", featOkTst);
    casFeatCode_contextualModality  = (null == casFeat_contextualModality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_contextualModality).getCode();

 
    casFeat_contextualAspect = jcas.getRequiredFeatureDE(casType, "contextualAspect", "uima.cas.String", featOkTst);
    casFeatCode_contextualAspect  = (null == casFeat_contextualAspect) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_contextualAspect).getCode();

 
    casFeat_permanence = jcas.getRequiredFeatureDE(casType, "permanence", "uima.cas.String", featOkTst);
    casFeatCode_permanence  = (null == casFeat_permanence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_permanence).getCode();

 
    casFeat_category = jcas.getRequiredFeatureDE(casType, "category", "uima.cas.String", featOkTst);
    casFeatCode_category  = (null == casFeat_category) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_category).getCode();

 
    casFeat_aspect = jcas.getRequiredFeatureDE(casType, "aspect", "uima.cas.String", featOkTst);
    casFeatCode_aspect  = (null == casFeat_aspect) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_aspect).getCode();

 
    casFeat_docTimeRel = jcas.getRequiredFeatureDE(casType, "docTimeRel", "uima.cas.String", featOkTst);
    casFeatCode_docTimeRel  = (null == casFeat_docTimeRel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_docTimeRel).getCode();

 
    casFeat_degree = jcas.getRequiredFeatureDE(casType, "degree", "uima.cas.String", featOkTst);
    casFeatCode_degree  = (null == casFeat_degree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_degree).getCode();

 
    casFeat_polarity = jcas.getRequiredFeatureDE(casType, "polarity", "uima.cas.Integer", featOkTst);
    casFeatCode_polarity  = (null == casFeat_polarity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_polarity).getCode();

  }
}



    