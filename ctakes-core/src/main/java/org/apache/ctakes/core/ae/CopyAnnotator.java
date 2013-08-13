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
package org.apache.ctakes.core.ae;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ctakes.core.util.JCasUtil;
import org.apache.ctakes.core.util.ParamUtil;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;


/**
 * Utility annotator that copy's data from an existing JCas object into a new
 * JCas object.
 * 
 * @author Mayo Clinic
 * 
 */
public class CopyAnnotator extends JCasAnnotator_ImplBase {
	private int iv_srcType;

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	// constructor used to create a new instance of the destination
	// JCas object
	private Constructor<?> iv_destContr;

	// key = source getter method (java.lang.reflect.Method)
	// val = destination setter method (java.lang.reflect.Method)
	private Map<Method, Method> iv_getSetMap;

	public void initialize(UimaContext annotCtx)
			throws ResourceInitializationException {
		super.initialize(annotCtx);

		try {
			String className;
			className = (String) annotCtx
					.getConfigParameterValue("srcObjClass");
			Class<?> srcClass = Class.forName(className);
			iv_srcType = JCasUtil.getType(className);

			className = (String) annotCtx
					.getConfigParameterValue("destObjClass");
			Class<?> destClass = Class.forName(className);
			Class<?>[] constrArgs = { JCas.class };
			iv_destContr = destClass.getConstructor(constrArgs);

			Map<String, String> m = ParamUtil.getStringParameterValuesMap(
					"dataBindMap", annotCtx, "|");
			iv_getSetMap = new HashMap<Method, Method>();
			Iterator<String> getterItr = m.keySet().iterator();
			while (getterItr.hasNext()) {
				String getterMethName = (String) getterItr.next();
				String setterMethName = (String) m.get(getterMethName);

				Method getterMeth = srcClass.getMethod(getterMethName,
						(Class[]) null);

				// get corresponding setter that has compatible args
				Class<?>[] setterArgs = { getterMeth.getReturnType() };
				Method setterMeth = destClass.getMethod(setterMethName,
						setterArgs);

				iv_getSetMap.put(getterMeth, setterMeth);
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	public void process(JCas jcas) throws AnalysisEngineProcessException {
		logger.info("process(JCas)");

		// iterate over source objects in JCas
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		FSIterator<Annotation> srcObjItr = indexes.getAnnotationIndex(
				iv_srcType).iterator();
		while (srcObjItr.hasNext()) {
			TOP srcObj = (TOP) srcObjItr.next();

			Object[] constrArgVals = { jcas };
			try {
				// create new destination object
				TOP destObj = (TOP) iv_destContr.newInstance(constrArgVals);

				// copy data from source to destination
				Iterator<Method> getterItr = iv_getSetMap.keySet().iterator();
				while (getterItr.hasNext()) {
					Method getterMeth = (Method) getterItr.next();
					Method setterMeth = (Method) iv_getSetMap.get(getterMeth);

					Object val = getterMeth.invoke(srcObj, (Object[]) null);
					Object[] setterArgs = { val };
					setterMeth.invoke(destObj, setterArgs);
				}
				// add new destination object to JCas
				destObj.addToIndexes();
			} catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}

}