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
