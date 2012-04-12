package org.mitre.medfacts.i2b2.api.ctakes;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.jcas.JCas;

public class ConstraintConstructorFindContainedBy extends ConstraintConstructor
{

  public ConstraintConstructorFindContainedBy()
  {
  }

  public ConstraintConstructorFindContainedBy(JCas jcas)
  {
    super(jcas);
  }

  @Override
  public FSMatchConstraint constructConstraintByBeginEnd(int problemBegin,
      int problemEnd, ConstraintFactory cf,
      FeaturePath sentenceBeginFeaturePath, FeaturePath sentenceEndFeaturePath)
  {
    return constructContainedByConstraint(problemBegin, problemEnd, cf, sentenceBeginFeaturePath, sentenceEndFeaturePath);
  }

  /**
   * @param problemBegin
   * @param problemEnd
   * @param cf
   * @param sentenceBeginFeaturePath
   * @param sentenceEndFeaturePath
   * @return
   */
  public FSMatchConstraint constructContainedByConstraint(int problemBegin,
      int problemEnd, ConstraintFactory cf,
      FeaturePath sentenceBeginFeaturePath, FeaturePath sentenceEndFeaturePath)
  {
    FSIntConstraint sentenceBeginIntConstraint = cf.createIntConstraint();
    sentenceBeginIntConstraint.leq(problemBegin);
    
    FSIntConstraint sentenceEndIntConstraint = cf.createIntConstraint();
    sentenceEndIntConstraint.geq(problemEnd);
    
    
    FSMatchConstraint begin = cf.embedConstraint(sentenceBeginFeaturePath, sentenceBeginIntConstraint);
    FSMatchConstraint end = cf.embedConstraint(sentenceEndFeaturePath, sentenceEndIntConstraint);
    
    FSMatchConstraint beginAndEnd = cf.and(begin, end);
    return beginAndEnd;
  }


}
