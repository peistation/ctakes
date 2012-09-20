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
package edu.mayo.bmi.uima.pad.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;

import org.apache.ctakes.typesystem.type.syntax.WordToken;
import edu.mayo.bmi.uima.lookup.ae.BaseLookupConsumerImpl;
import edu.mayo.bmi.uima.lookup.ae.LookupConsumer;
import edu.mayo.bmi.uima.pad.type.SubSection;


public abstract class  PADConsumerImpl 
  extends BaseLookupConsumerImpl
  implements LookupConsumer
{

  public abstract void consumeHits(JCas jcas, Iterator lookupHitItr)
  throws AnalysisEngineProcessException;

  protected List<WordToken> getTokenData(JCas jcas, Annotation locationTerm)
  {
    List<WordToken> list = new ArrayList<WordToken>();
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> annotItr = indexes.getAnnotationIndex(WordToken.type).iterator();
    
    while (annotItr.hasNext())
    {
      WordToken ann = (WordToken)annotItr.next();

      if(ann.getBegin() >= locationTerm.getBegin() && ann.getEnd() <= locationTerm.getEnd())
        list.add(ann);
    }

    return list;
  }

  
  public String getSegmentIdContaining(Annotation ann, JCas jcas)
  {
    int begin = ann.getBegin();
    int end = ann.getEnd();
    
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> secItr = indexes.getAnnotationIndex(SubSection.type).iterator();
      
    while(secItr.hasNext())
    {
      SubSection sa = (SubSection)secItr.next();
      if(sa.getBegin() <= begin && end <= sa.getEnd())
        return String.valueOf(sa.getParentSectionId());
      //if NE spans 2 or more sentences, use the begining sentence
      else if(sa.getBegin() <= begin && begin < sa.getEnd())
        return String.valueOf(sa.getParentSectionId());
    }
    return "-1";
  }
  
  public int findOriginalReportSubSection( JCas jcas)
  {
    
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> secItr = indexes.getAnnotationIndex(SubSection.type).iterator();
      
    while(secItr.hasNext())
    {
    	SubSection sa = (SubSection)secItr.next();

    	if (sa.getStatus() == 1 & sa.getCoveredText().startsWith(revisionHeader) ) {
    		return sa.getSubSectionBodyBegin();
    	}
    	
    }
    return 0;
  } 
  
public String getSubSectionStatus(Annotation ann, JCas jcas, int cutOff)
  {
    int begin = ann.getBegin();
    int end = ann.getEnd();
    
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> secItr = indexes.getAnnotationIndex(SubSection.type).iterator();
      
    while(secItr.hasNext())
    {
    	SubSection sa = (SubSection) secItr.next();
			// Don't look in sections after a cut off revision point
			if (cutOff == 0  || (cutOff > 0 && (sa.getSubSectionBodyBegin() < cutOff))) {
				// since entire subsections will be used to determine if
				// location is used remove mentions overlapping in the header
				if (sa.getSubSectionHeaderBegin() <= begin
						&& sa.getSubSectionHeaderEnd() >= end)
					return "1";

				else if (sa.getBegin() <= begin && end <= sa.getEnd()) {
					if (sa.getStatus() == 1
							& sa.getCoveredText().startsWith(indicationsHeader)
							&& sa.getBegin() > maxSubsectionSize)
						return "-1";

					return String.valueOf(sa.getStatus());
				}
				// if NE spans 2 or more sentences, use the beginning sentence
				else if (sa.getBegin() <= begin && begin < sa.getEnd())
					return String.valueOf(sa.getStatus());
				else if (begin < sa.getSubSectionHeaderBegin()
						&& end < sa.getSubSectionHeaderEnd())
					return "1";
			} else
				return "1";
    }
    return "-1";
  } 
	private static String revisionHeader = "showing";
	private static String indicationsHeader = "INDICATIONS:";
	private static int maxSubsectionSize = 300;
}
