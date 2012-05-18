package edu.mayo.bmi.fsm.ner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.ner.output.elements.StrengthUnitToken;

/**
 * Checks whether a token is a StrengthToken.
 * @author Mayo Clinic
 */
public class StrengthUnitCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof StrengthUnitToken)
		{
			return true;
		}
        	
		return false;
	}		
}
