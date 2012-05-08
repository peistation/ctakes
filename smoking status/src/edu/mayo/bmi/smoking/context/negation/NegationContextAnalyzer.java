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
package edu.mayo.bmi.smoking.context.negation;

import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.fsm.output.NegationIndicator;
import edu.mayo.bmi.fsm.token.TextToken;
import edu.mayo.bmi.uima.context.ContextHit;
import edu.mayo.bmi.smoking.context.NamedEntityContextAnalyzer;
import edu.mayo.bmi.fsm.smoking.machine.NegationFSM;

public class NegationContextAnalyzer extends NamedEntityContextAnalyzer {
	private NegationFSM _negIndicatorFSM = new NegationFSM();

	/**
	 * This method analyzes a list of tokens looking for a negation pattern as
	 * specified by the class NegationFSM.
	 * 
	 * @see NegationFSM
	 */
	public ContextHit analyzeContext(List<? extends Annotation> contextTokens, int scopeOrientation)
			throws AnalysisEngineProcessException {
		List<TextToken> fsmTokenList = wrapAsFsmTokens(contextTokens);

		try {
			Set<NegationIndicator> s = _negIndicatorFSM.execute(fsmTokenList);

			if (s.size() > 0) {
				NegationIndicator neg = s.iterator().next();
				return new ContextHit(neg.getStartOffset(), neg.getEndOffset());
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}