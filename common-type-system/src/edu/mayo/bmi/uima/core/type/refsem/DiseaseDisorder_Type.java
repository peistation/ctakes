
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

/** 
 * Updated by JCasGen Fri Jan 20 10:31:30 EST 2012
 * @generated */
public class DiseaseDisorder_Type extends Event_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DiseaseDisorder_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DiseaseDisorder_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DiseaseDisorder(addr, DiseaseDisorder_Type.this);
  			   DiseaseDisorder_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DiseaseDisorder(addr, DiseaseDisorder_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DiseaseDisorder.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
 
  /** @generated */
  final Feature casFeat_alleviatingFactor;
  /** @generated */
  final int     casFeatCode_alleviatingFactor;
  /** @generated */ 
  public int getAlleviatingFactor(int addr) {
        if (featOkTst && casFeat_alleviatingFactor == null)
      jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_alleviatingFactor);
  }
  /** @generated */    
  public void setAlleviatingFactor(int addr, int v) {
        if (featOkTst && casFeat_alleviatingFactor == null)
      jcas.throwFeatMissing("alleviatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_alleviatingFactor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_associatedSignSymptom;
  /** @generated */
  final int     casFeatCode_associatedSignSymptom;
  /** @generated */ 
  public int getAssociatedSignSymptom(int addr) {
        if (featOkTst && casFeat_associatedSignSymptom == null)
      jcas.throwFeatMissing("associatedSignSymptom", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_associatedSignSymptom);
  }
  /** @generated */    
  public void setAssociatedSignSymptom(int addr, int v) {
        if (featOkTst && casFeat_associatedSignSymptom == null)
      jcas.throwFeatMissing("associatedSignSymptom", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_associatedSignSymptom, v);}
    
  
 
  /** @generated */
  final Feature casFeat_bodyLaterality;
  /** @generated */
  final int     casFeatCode_bodyLaterality;
  /** @generated */ 
  public int getBodyLaterality(int addr) {
        if (featOkTst && casFeat_bodyLaterality == null)
      jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_bodyLaterality);
  }
  /** @generated */    
  public void setBodyLaterality(int addr, int v) {
        if (featOkTst && casFeat_bodyLaterality == null)
      jcas.throwFeatMissing("bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_bodyLaterality, v);}
    
  
 
  /** @generated */
  final Feature casFeat_bodySide;
  /** @generated */
  final int     casFeatCode_bodySide;
  /** @generated */ 
  public int getBodySide(int addr) {
        if (featOkTst && casFeat_bodySide == null)
      jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_bodySide);
  }
  /** @generated */    
  public void setBodySide(int addr, int v) {
        if (featOkTst && casFeat_bodySide == null)
      jcas.throwFeatMissing("bodySide", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_bodySide, v);}
    
  
 
  /** @generated */
  final Feature casFeat_bodyLocation;
  /** @generated */
  final int     casFeatCode_bodyLocation;
  /** @generated */ 
  public int getBodyLocation(int addr) {
        if (featOkTst && casFeat_bodyLocation == null)
      jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_bodyLocation);
  }
  /** @generated */    
  public void setBodyLocation(int addr, int v) {
        if (featOkTst && casFeat_bodyLocation == null)
      jcas.throwFeatMissing("bodyLocation", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_bodyLocation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_course;
  /** @generated */
  final int     casFeatCode_course;
  /** @generated */ 
  public int getCourse(int addr) {
        if (featOkTst && casFeat_course == null)
      jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_course);
  }
  /** @generated */    
  public void setCourse(int addr, int v) {
        if (featOkTst && casFeat_course == null)
      jcas.throwFeatMissing("course", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_course, v);}
    
  
 
  /** @generated */
  final Feature casFeat_duration;
  /** @generated */
  final int     casFeatCode_duration;
  /** @generated */ 
  public int getDuration(int addr) {
        if (featOkTst && casFeat_duration == null)
      jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_duration);
  }
  /** @generated */    
  public void setDuration(int addr, int v) {
        if (featOkTst && casFeat_duration == null)
      jcas.throwFeatMissing("duration", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_duration, v);}
    
  
 
  /** @generated */
  final Feature casFeat_endTime;
  /** @generated */
  final int     casFeatCode_endTime;
  /** @generated */ 
  public int getEndTime(int addr) {
        if (featOkTst && casFeat_endTime == null)
      jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_endTime);
  }
  /** @generated */    
  public void setEndTime(int addr, int v) {
        if (featOkTst && casFeat_endTime == null)
      jcas.throwFeatMissing("endTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_endTime, v);}
    
  
 
  /** @generated */
  final Feature casFeat_exacerbatingFactor;
  /** @generated */
  final int     casFeatCode_exacerbatingFactor;
  /** @generated */ 
  public int getExacerbatingFactor(int addr) {
        if (featOkTst && casFeat_exacerbatingFactor == null)
      jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_exacerbatingFactor);
  }
  /** @generated */    
  public void setExacerbatingFactor(int addr, int v) {
        if (featOkTst && casFeat_exacerbatingFactor == null)
      jcas.throwFeatMissing("exacerbatingFactor", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_exacerbatingFactor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_startTime;
  /** @generated */
  final int     casFeatCode_startTime;
  /** @generated */ 
  public int getStartTime(int addr) {
        if (featOkTst && casFeat_startTime == null)
      jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_startTime);
  }
  /** @generated */    
  public void setStartTime(int addr, int v) {
        if (featOkTst && casFeat_startTime == null)
      jcas.throwFeatMissing("startTime", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_startTime, v);}
    
  
 
  /** @generated */
  final Feature casFeat_relativeTemporalContext;
  /** @generated */
  final int     casFeatCode_relativeTemporalContext;
  /** @generated */ 
  public int getRelativeTemporalContext(int addr) {
        if (featOkTst && casFeat_relativeTemporalContext == null)
      jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relativeTemporalContext);
  }
  /** @generated */    
  public void setRelativeTemporalContext(int addr, int v) {
        if (featOkTst && casFeat_relativeTemporalContext == null)
      jcas.throwFeatMissing("relativeTemporalContext", "edu.mayo.bmi.uima.core.type.refsem.DiseaseDisorder");
    ll_cas.ll_setRefValue(addr, casFeatCode_relativeTemporalContext, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DiseaseDisorder_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_alleviatingFactor = jcas.getRequiredFeatureDE(casType, "alleviatingFactor", "edu.mayo.bmi.uima.core.type.relation.ElementRelation", featOkTst);
    casFeatCode_alleviatingFactor  = (null == casFeat_alleviatingFactor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_alleviatingFactor).getCode();

 
    casFeat_associatedSignSymptom = jcas.getRequiredFeatureDE(casType, "associatedSignSymptom", "edu.mayo.bmi.uima.core.type.relation.ManifestationOf", featOkTst);
    casFeatCode_associatedSignSymptom  = (null == casFeat_associatedSignSymptom) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_associatedSignSymptom).getCode();

 
    casFeat_bodyLaterality = jcas.getRequiredFeatureDE(casType, "bodyLaterality", "edu.mayo.bmi.uima.core.type.refsem.BodyLaterality", featOkTst);
    casFeatCode_bodyLaterality  = (null == casFeat_bodyLaterality) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bodyLaterality).getCode();

 
    casFeat_bodySide = jcas.getRequiredFeatureDE(casType, "bodySide", "edu.mayo.bmi.uima.core.type.refsem.BodySide", featOkTst);
    casFeatCode_bodySide  = (null == casFeat_bodySide) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bodySide).getCode();

 
    casFeat_bodyLocation = jcas.getRequiredFeatureDE(casType, "bodyLocation", "edu.mayo.bmi.uima.core.type.relation.LocationOf", featOkTst);
    casFeatCode_bodyLocation  = (null == casFeat_bodyLocation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bodyLocation).getCode();

 
    casFeat_course = jcas.getRequiredFeatureDE(casType, "course", "edu.mayo.bmi.uima.core.type.refsem.Course", featOkTst);
    casFeatCode_course  = (null == casFeat_course) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_course).getCode();

 
    casFeat_duration = jcas.getRequiredFeatureDE(casType, "duration", "edu.mayo.bmi.uima.core.type.relation.TemporalRelation", featOkTst);
    casFeatCode_duration  = (null == casFeat_duration) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_duration).getCode();

 
    casFeat_endTime = jcas.getRequiredFeatureDE(casType, "endTime", "edu.mayo.bmi.uima.core.type.refsem.Time", featOkTst);
    casFeatCode_endTime  = (null == casFeat_endTime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_endTime).getCode();

 
    casFeat_exacerbatingFactor = jcas.getRequiredFeatureDE(casType, "exacerbatingFactor", "edu.mayo.bmi.uima.core.type.relation.ElementRelation", featOkTst);
    casFeatCode_exacerbatingFactor  = (null == casFeat_exacerbatingFactor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_exacerbatingFactor).getCode();

 
    casFeat_startTime = jcas.getRequiredFeatureDE(casType, "startTime", "edu.mayo.bmi.uima.core.type.refsem.Time", featOkTst);
    casFeatCode_startTime  = (null == casFeat_startTime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_startTime).getCode();

 
    casFeat_relativeTemporalContext = jcas.getRequiredFeatureDE(casType, "relativeTemporalContext", "edu.mayo.bmi.uima.core.type.relation.TemporalRelation", featOkTst);
    casFeatCode_relativeTemporalContext  = (null == casFeat_relativeTemporalContext) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relativeTemporalContext).getCode();

  }
}



    