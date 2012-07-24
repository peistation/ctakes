package edu.mayo.bmi.uima.pad.impl;

import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.fsm.output.NegationIndicator;
import edu.mayo.bmi.fsm.pad.machine.NegDxIndicatorFSM;
import edu.mayo.bmi.fsm.token.TextToken;
import edu.mayo.bmi.uima.context.ContextAnalyzer;
import edu.mayo.bmi.uima.context.NamedEntityContextAnalyzer;
import edu.mayo.bmi.uima.context.ContextHit;

/**
 * @author Mayo Clinic
 */
public class NegativeContextAnalyzerImpl extends NamedEntityContextAnalyzer
        implements ContextAnalyzer
{
    private NegDxIndicatorFSM iv_negIndicatorFSM = new NegDxIndicatorFSM();

    public ContextHit analyzeContext(List<? extends Annotation> tokenList, int scope)
    {
        List<TextToken> fsmTokenList = wrapAsFsmTokens(tokenList);

        try
        {
            Set<NegationIndicator> s = iv_negIndicatorFSM.execute(fsmTokenList);

            if (s.size() > 0)
            {
                NegationIndicator neg = (NegationIndicator) s.iterator().next();
                return new ContextHit(neg.getStartOffset(), neg.getEndOffset());
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
		return null;
    }
}