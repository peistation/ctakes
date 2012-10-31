/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.lookup.ae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.lookup.vo.LookupHit;

/**
 * Provides some base functionality for subclasses.
 * 
 * @author Mayo Clinic
 * 
 */
public abstract class BaseLookupConsumerImpl implements LookupConsumer
{
	/**
	 * Organizes the LookupHit objects by begin and end offsets.
	 * 
	 * @param lhItr
	 * @return Iterator over Set objects. Each Set object is a collection of
	 *         LookupHit objects with the same begin,end offsets.
	 */
	protected Iterator organizeByOffset(Iterator lhItr)
	{
		// key = begin,end key (java.lang.String)
		// val = Set of LookupHit objects corresponding to begin,end
		Map m = new HashMap();

		while (lhItr.hasNext())
		{
			LookupHit lh = (LookupHit) lhItr.next();
			String keyStr = getKeyString(lh.getStartOffset(), lh.getEndOffset());

			Set s = null;
			if (m.containsKey(keyStr))
			{
				s = (Set) m.get(keyStr);
			}
			else
			{
				s = new HashSet();
			}
			s.add(lh);
			m.put(keyStr, s);
		}

		return m.values().iterator();
	}

	private String getKeyString(int begin, int end)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(begin);
		sb.append(',');
		sb.append(end);
		return sb.toString();
	}

	public abstract void consumeHits(JCas jcas, Iterator lookupHitItr)
			throws AnalysisEngineProcessException;
}
