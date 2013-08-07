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
package edu.mayo.bmi.uima.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;

public class ParamUtil {

	/**
	 * 
	 * @param parameterName
	 * @param annotatorContext
	 * @return does not return null - but will return empty set if parameter is
	 *         optional or not set.
	 * @throws AnnotatorContextException
	 */
	public static Set<String> getStringParameterValuesSet(String parameterName, AnnotatorContext annotatorContext)
			throws AnnotatorContextException {
		Set<String> returnValues = new HashSet<String>();
		String[] strings = (String[]) annotatorContext.getConfigParameterValue(parameterName);
		if (strings == null)
			return returnValues;

		for (int i = 0; i < strings.length; i++) {
			returnValues.add(strings[i]);
		}
		return returnValues;
	}

	public static Map<String, String> getStringParameterValuesMap(String parameterName,
			AnnotatorContext annotatorContext, String keyValueDelimiter) throws AnnotatorContextException {
		String[] paramValues = (String[]) annotatorContext.getConfigParameterValue(parameterName);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < paramValues.length; i++) {
			int delimiterIndex = paramValues[i].lastIndexOf(keyValueDelimiter);
			if (delimiterIndex == -1)
				continue;
			String key = paramValues[i].substring(0, delimiterIndex);
			String value = paramValues[i].substring(delimiterIndex + 1);
			map.put(key, value);
		}
		return map;
	}

}
