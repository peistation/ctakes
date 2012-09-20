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

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.context.ContextHit;
import edu.mayo.bmi.uima.context.ContextHitConsumer;
import edu.mayo.bmi.uima.context.NamedEntityContextHitConsumer;
import org.apache.ctakes.typesystem.type.refsem.Element;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

/**
 * @author Mayo Clinic
 */
public class DxContextHitConsumerImpl extends NamedEntityContextHitConsumer
		implements ContextHitConsumer {
	public void consumeHit(JCas jcas, Annotation focusAnnot, int scope,
			ContextHit ctxHit) {
		Integer status = (Integer) ctxHit
				.getMetaData(DxContextAnalyzerImpl.CTX_HIT_KEY_ILLNESS_TYPE);
		if (focusAnnot instanceof IdentifiedAnnotation) {
			IdentifiedAnnotation neAnnot = (IdentifiedAnnotation) focusAnnot;
			if (neAnnot.getTypeID() == 6 || neAnnot.getTypeID() == 7
					|| neAnnot.getTypeID() == 0)
				// TODO: Status Family History, History Of, etc. are constant
				// strings?
				neAnnot.setUncertainty(status);
		}

		createContextAnnot(jcas, focusAnnot, scope, ctxHit).addToIndexes();
	}
}