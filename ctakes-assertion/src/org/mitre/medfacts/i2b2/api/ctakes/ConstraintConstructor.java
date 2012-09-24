/*
 * Copyright: (c) 2012   The MITRE Corporation. All rights reserved.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MITRE  as the author of this software, the trade names, trademarks, service
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

package org.mitre.medfacts.i2b2.api.ctakes;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import java.util.Iterator;

public abstract class ConstraintConstructor
{
  protected JCas jcas;
  
  public ConstraintConstructor()
  {
  }
  
  public ConstraintConstructor(JCas jcas)
  {
    this.jcas = jcas;
  }
  
  /**
   * @param problemBegin
   * @param problemEnd
   * @param sentenceType
   * @return
   */
  public FSIterator<Annotation> createFilteredIterator(
      int problemBegin, int problemEnd, Type sentenceType)
  {
    ConstraintFactory cf = jcas.getConstraintFactory();
    TypeSystem ts = jcas.getTypeSystem();
    Iterator<Type> it = ts.getTypeIterator();
    //Type annotationType = ts.getType(Annotation.class.getName()); // this returns "org.apache.uima.tcas.Annotation" which isn't in type system
    Type annotationType = ts.getType("uima.tcas.Annotation"); // should be safe to hard code this
    //System.err.println("annotation type: " + annotationType);
    Feature sentenceBeginFeature = annotationType.getFeatureByBaseName("begin");
    FeaturePath sentenceBeginFeaturePath = jcas.createFeaturePath();
    sentenceBeginFeaturePath.addFeature(sentenceBeginFeature);
    
    Feature sentenceEndFeature = annotationType.getFeatureByBaseName("end");
    FeaturePath sentenceEndFeaturePath = jcas.createFeaturePath();
    sentenceEndFeaturePath.addFeature(sentenceEndFeature);
    
    FSMatchConstraint beginAndEnd = constructConstraintByBeginEnd(
        problemBegin, problemEnd, cf, sentenceBeginFeaturePath,
        sentenceEndFeaturePath);
    
    
    FSTypeConstraint sentenceTypeConstraint = cf.createTypeConstraint();
    sentenceTypeConstraint.add(sentenceType);
    
    FSMatchConstraint beginAndEndAndType = cf.and(beginAndEnd, sentenceTypeConstraint);
    
    FSIterator<Annotation> filteredIterator =
        jcas.createFilteredIterator(jcas.getAnnotationIndex().iterator(),  beginAndEndAndType);
    return filteredIterator;
  }

  /**
   * @param problemBegin
   * @param problemEnd
   * @param cf
   * @param sentenceBeginFeaturePath
   * @param sentenceEndFeaturePath
   * @return
   */
  public abstract FSMatchConstraint constructConstraintByBeginEnd(int problemBegin,
      int problemEnd, ConstraintFactory cf,
      FeaturePath sentenceBeginFeaturePath, FeaturePath sentenceEndFeaturePath);
  
  public JCas getJcas()
  {
    return jcas;
  }

  public void setJcas(JCas jcas)
  {
    this.jcas = jcas;
  }

}
