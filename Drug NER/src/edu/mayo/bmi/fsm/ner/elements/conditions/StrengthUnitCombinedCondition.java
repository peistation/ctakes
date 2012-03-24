package edu.mayo.bmi.fsm.ner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.ner.output.elements.StrengthUnitCombinedToken;

/**
 * Checks whether a token is a StrengthToken.
 * @author Mayo Clinic
 */
public class StrengthUnitCombinedCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof StrengthUnitCombinedToken)
		{
			return true;
		}
        	
		return false;
	}		
}
