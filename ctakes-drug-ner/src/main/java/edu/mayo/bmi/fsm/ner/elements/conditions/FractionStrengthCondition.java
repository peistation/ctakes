package edu.mayo.bmi.fsm.ner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.ner.output.util.FractionStrengthToken;

/**
 * Checks whether a token is a FractionStrengthToken.
 * @author DUFFP
 */
public class FractionStrengthCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof FractionStrengthToken)
		{
			return true;
		}
        	
		return false;
	}		
}
