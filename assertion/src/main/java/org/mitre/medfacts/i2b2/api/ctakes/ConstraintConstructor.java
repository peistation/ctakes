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
    System.err.println("annotation type: " + annotationType);
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
