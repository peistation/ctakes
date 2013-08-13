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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.JCas;

import edu.mayo.bmi.uima.core.util.JCasUtil;
import edu.mayo.bmi.uima.core.util.ParamUtil;

/**
 * Utility annotator that copy's data from an existing JCas object into a new
 * JCas object.
 * 
 * @author Mayo Clinic
 * 
 */
public class CopyAnnotator extends JTextAnnotator_ImplBase
{
	private int iv_srcType;

	// constructor used to create a new instance of the destination
	// JCas object
	private Constructor iv_destContr;

	// key = source getter method (java.lang.reflect.Method)
	// val = destination setter method (java.lang.reflect.Method)
	private Map iv_getSetMap;

	public void initialize(AnnotatorContext annotCtx)
			throws AnnotatorInitializationException,
			AnnotatorConfigurationException
	{
		super.initialize(annotCtx);

		try
		{
			String className;
			className = (String) annotCtx.getConfigParameterValue("srcObjClass");
			Class srcClass = Class.forName(className);
			iv_srcType = JCasUtil.getType(className);

			className = (String) annotCtx.getConfigParameterValue("destObjClass");
			Class destClass = Class.forName(className);
			Class[] constrArgs = { JCas.class };
			iv_destContr = destClass.getConstructor(constrArgs);

			Map m = ParamUtil.getStringParameterValuesMap(
					"dataBindMap",
					annotCtx,
					"|");
			iv_getSetMap = new HashMap();
			Iterator getterItr = m.keySet().iterator();
			while (getterItr.hasNext())
			{
				String getterMethName = (String) getterItr.next();
				String setterMethName = (String) m.get(getterMethName);

				Method getterMeth = srcClass.getMethod(getterMethName, (Class[]) null);

				// get corresponding setter that has compatible args
				Class[] setterArgs = { getterMeth.getReturnType() };
				Method setterMeth = destClass.getMethod(
						setterMethName,
						setterArgs);

				iv_getSetMap.put(getterMeth, setterMeth);
			}
		}
		catch (Exception e)
		{
			throw new AnnotatorInitializationException(e);
		}
	}

	public void process(JCas jcas, ResultSpecification rs)
			throws AnnotatorProcessException
	{
    	System.out.println("CopyAnnotator process(JCas ResultSpecification)" );
    	
		// iterate over source objects in JCas
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator srcObjItr = indexes.getAnnotationIndex(iv_srcType).iterator();
		while (srcObjItr.hasNext())
		{
			TOP srcObj = (TOP) srcObjItr.next();

			Object[] constrArgVals = { jcas };
			try
			{
				// create new destination object
				TOP destObj = (TOP) iv_destContr.newInstance(constrArgVals);

				// copy data from source to destination
				Iterator getterItr = iv_getSetMap.keySet().iterator();
				while (getterItr.hasNext())
				{
					Method getterMeth = (Method) getterItr.next();
					Method setterMeth = (Method) iv_getSetMap.get(getterMeth);

					Object val = getterMeth.invoke(srcObj, (Object[]) null);
					Object[] setterArgs = { val };
					setterMeth.invoke(destObj, setterArgs);
				}
				// add new destination object to JCas
				destObj.addToIndexes();
			}
			catch (Exception e)
			{
				throw new AnnotatorProcessException(e);
			}
		}
	}
}