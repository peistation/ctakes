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
package edu.mayo.bmi.uima.context.status;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.fsm.machine.StatusIndicatorFSM;
import edu.mayo.bmi.fsm.output.StatusIndicator;
import edu.mayo.bmi.fsm.token.TextToken;
import edu.mayo.bmi.uima.context.ContextAnnotator;
import edu.mayo.bmi.uima.context.ContextHit;
import edu.mayo.bmi.uima.context.ContextAnalyzer;
import edu.mayo.bmi.uima.context.NamedEntityContextAnalyzer;

public class StatusContextAnalyzer extends NamedEntityContextAnalyzer implements ContextAnalyzer {
	public static final String CTX_HIT_KEY_STATUS_TYPE = "STATUS_TYPE";

	private StatusIndicatorFSM _statusIndicatorFSM = new StatusIndicatorFSM();

	/**
	 * Analyze a list of tokens looking for a status pattern as
	 * specified by the class StatusIndicatorFSM.
	 * An indication of family history found within the scope takes precedence over 
	 * an indication of history of, even if evidence of history of is closer.
	 * Otherwise, the closest indicator of status is used.
	 * 
	 * @see StatusIndicatorFSM
	 */

	public ContextHit analyzeContext(List<? extends Annotation> tokenList, int scope) throws AnalysisEngineProcessException{
		List<TextToken> fsmTokenList = wrapAsFsmTokens(tokenList);

		try {
			Set<StatusIndicator> s = _statusIndicatorFSM.execute(fsmTokenList);

			if (s.size() > 0) {
				StatusIndicator finalSi = null;
				Iterator<StatusIndicator> siItr = s.iterator();
				while (siItr.hasNext()) {
					StatusIndicator si = siItr.next();
					if (finalSi == null) {
						finalSi = si;
					} else if ((si.getStatus() == StatusIndicator.FAMILY_HISTORY_STATUS)
							&& (finalSi.getStatus() == StatusIndicator.HISTORY_STATUS)) {
						// family history always overrides history
						finalSi = si;
					} else if ((scope == ContextAnnotator.LEFT_SCOPE) && (si.getEndOffset() > finalSi.getEndOffset())) {
						// pick one with closest proximity to focus
						finalSi = si;
					} else if ((scope == ContextAnnotator.RIGHT_SCOPE)
							&& (si.getStartOffset() < finalSi.getStartOffset())) {
						// pick one w/ closest proximity to focus
						finalSi = si;
					}
				}
				ContextHit ctxHit = new ContextHit(finalSi.getStartOffset(), finalSi.getEndOffset());

				ctxHit.addMetaData(CTX_HIT_KEY_STATUS_TYPE, new Integer(finalSi.getStatus()));

				return ctxHit;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}