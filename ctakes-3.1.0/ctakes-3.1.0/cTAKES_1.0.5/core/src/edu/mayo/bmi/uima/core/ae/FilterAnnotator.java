/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.core.ae;
/**
 * This simple implementation is intended to remove annotations other 
 * than the one specified form CAS.
 * 
 * @author m039575 
 */
import java.util.Iterator;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.NamedEntity;

public class FilterAnnotator extends JTextAnnotator_ImplBase
{
  //TODO parameterize retainAttrTypeId = DISORDER_ANNOTATIONS = 2
  private static int DISORDER_ANNOTATIONS = 2;
  
  public FilterAnnotator()
  { 
    super();
    
    retainAnnType = NamedEntity.type;
    retainAttrTypeId = DISORDER_ANNOTATIONS;
  }

  public void initialize(AnnotatorContext annotCtx)
  throws AnnotatorInitializationException, AnnotatorConfigurationException
  { 
    super.initialize(annotCtx);
    
    removeList = new java.util.ArrayList();
  }

  /**
   * Checks if the annotation is of the type to be retained. If not, 
   * removes it from the index. 
   * Uses helper method isValid(Annotation).
   */
  public void process(JCas jcas, ResultSpecification resultSpec)
      throws AnnotatorProcessException
  {
    removeList.clear();
    
    // iterate over source objects in JCas
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator srcObjItr = indexes.getAnnotationIndex(retainAnnType).iterator();
    
    while(srcObjItr.hasNext())
    {
      Annotation ann = (Annotation)srcObjItr.next();
      if(!isValid(ann))
        removeList.add(ann);
    }
    
    for(int i = 0; i< removeList.size(); i++)
      ((Annotation)removeList.get(i)).removeFromIndexes();
    
  }

  private boolean isValid(Annotation ann)
  {
    if( ((NamedEntity)ann).getTypeID() != retainAttrTypeId )
      return false;
    
    return true;
  }
  
  //-- private datamembers ----
  private int retainAnnType;          //annotation type you want to retain
  private int retainAttrTypeId;       //annotation type id you want to retain
  private java.util.List removeList;  //collection to hold annotations to be removed
}
