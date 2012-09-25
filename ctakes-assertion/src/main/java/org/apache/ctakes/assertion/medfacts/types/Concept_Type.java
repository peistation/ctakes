/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* First created by JCasGen Mon May 23 12:04:34 EDT 2011 */
package org.apache.ctakes.assertion.medfacts.types;

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
 * Updated by JCasGen Tue Feb 28 11:18:13 EST 2012
 * @generated */
public class Concept_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Concept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Concept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Concept(addr, Concept_Type.this);
  			   Concept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Concept(addr, Concept_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Concept.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mitre.medfacts.types.Concept");
 
  /** @generated */
  final Feature casFeat_conceptType;
  /** @generated */
  final int     casFeatCode_conceptType;
  /** @generated */ 
  public String getConceptType(int addr) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptType);
  }
  /** @generated */    
  public void setConceptType(int addr, String v) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_conceptText;
  /** @generated */
  final int     casFeatCode_conceptText;
  /** @generated */ 
  public String getConceptText(int addr) {
        if (featOkTst && casFeat_conceptText == null)
      jcas.throwFeatMissing("conceptText", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptText);
  }
  /** @generated */    
  public void setConceptText(int addr, String v) {
        if (featOkTst && casFeat_conceptText == null)
      jcas.throwFeatMissing("conceptText", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptText, v);}
    
  
 
  /** @generated */
  final Feature casFeat_externalId;
  /** @generated */
  final int     casFeatCode_externalId;
  /** @generated */ 
  public int getExternalId(int addr) {
        if (featOkTst && casFeat_externalId == null)
      jcas.throwFeatMissing("externalId", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getIntValue(addr, casFeatCode_externalId);
  }
  /** @generated */    
  public void setExternalId(int addr, int v) {
        if (featOkTst && casFeat_externalId == null)
      jcas.throwFeatMissing("externalId", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setIntValue(addr, casFeatCode_externalId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_originalEntityExternalId;
  /** @generated */
  final int     casFeatCode_originalEntityExternalId;
  /** @generated */ 
  public int getOriginalEntityExternalId(int addr) {
        if (featOkTst && casFeat_originalEntityExternalId == null)
      jcas.throwFeatMissing("originalEntityExternalId", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getIntValue(addr, casFeatCode_originalEntityExternalId);
  }
  /** @generated */    
  public void setOriginalEntityExternalId(int addr, int v) {
        if (featOkTst && casFeat_originalEntityExternalId == null)
      jcas.throwFeatMissing("originalEntityExternalId", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setIntValue(addr, casFeatCode_originalEntityExternalId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Concept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_conceptType = jcas.getRequiredFeatureDE(casType, "conceptType", "uima.cas.String", featOkTst);
    casFeatCode_conceptType  = (null == casFeat_conceptType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptType).getCode();

 
    casFeat_conceptText = jcas.getRequiredFeatureDE(casType, "conceptText", "uima.cas.String", featOkTst);
    casFeatCode_conceptText  = (null == casFeat_conceptText) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptText).getCode();

 
    casFeat_externalId = jcas.getRequiredFeatureDE(casType, "externalId", "uima.cas.Integer", featOkTst);
    casFeatCode_externalId  = (null == casFeat_externalId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_externalId).getCode();

 
    casFeat_originalEntityExternalId = jcas.getRequiredFeatureDE(casType, "originalEntityExternalId", "uima.cas.Integer", featOkTst);
    casFeatCode_originalEntityExternalId  = (null == casFeat_originalEntityExternalId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_originalEntityExternalId).getCode();

  }
}



    