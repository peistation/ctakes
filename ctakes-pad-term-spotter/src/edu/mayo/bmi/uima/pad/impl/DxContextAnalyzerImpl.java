package edu.mayo.bmi.uima.pad.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.uima.jcas.tcas.Annotation;
import edu.mayo.bmi.fsm.pad.machine.DxIndicatorFSM;
import edu.mayo.bmi.fsm.pad.output.IllnessDxIndicator;
import edu.mayo.bmi.fsm.token.TextToken;
import edu.mayo.bmi.uima.context.ContextAnalyzer;
import edu.mayo.bmi.uima.context.ContextAnnotator;
import edu.mayo.bmi.uima.context.NamedEntityContextAnalyzer;
import edu.mayo.bmi.uima.context.ContextHit;

/**
 * @author Mayo Clinic
 */
public class DxContextAnalyzerImpl extends NamedEntityContextAnalyzer
		implements ContextAnalyzer {
	public static final String CTX_HIT_KEY_ILLNESS_TYPE = "ILLNESS_TYPE";

	private DxIndicatorFSM iv_statusDxIndicatorFSM = new DxIndicatorFSM();

	public ContextHit analyzeContext(List<? extends Annotation> tokenList,
			int scope) {
		List<TextToken> fsmTokenList = wrapAsFsmTokens(tokenList);

		Set<IllnessDxIndicator> s;
		try {
			s = iv_statusDxIndicatorFSM.execute(fsmTokenList);

			if (s.size() > 0) {
				IllnessDxIndicator finalSi = null;
				Iterator <IllnessDxIndicator>siItr = s.iterator();
				while (siItr.hasNext()) {
					IllnessDxIndicator si = (IllnessDxIndicator) siItr.next();
					if (finalSi == null) {
						finalSi = si;
					} else if ((si.getStatus() == IllnessDxIndicator.BRAIN_STATUS)
							&& (finalSi.getStatus() == IllnessDxIndicator.COLON_STATUS)) {
						// not implemented yet
						finalSi = si;
					} else if ((scope == ContextAnnotator.LEFT_SCOPE)
							&& (si.getEndOffset() > finalSi.getEndOffset())) {
						// pick one w/ closest proximity to focus
						finalSi = si;
					} else if ((scope == ContextAnnotator.RIGHT_SCOPE)
							&& (si.getStartOffset() < finalSi.getStartOffset())) {
						// pick one w/ closest proximity to focus
						finalSi = si;
					}
				}
				ContextHit ctxHit = new ContextHit(finalSi.getStartOffset(),
						finalSi.getEndOffset());

				ctxHit.addMetaData(CTX_HIT_KEY_ILLNESS_TYPE, new Integer(
						finalSi.getStatus()));

				return ctxHit;
			} else {
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}