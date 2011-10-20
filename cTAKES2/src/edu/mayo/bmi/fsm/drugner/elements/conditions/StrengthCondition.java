package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.elements.StrengthToken;

/**
 * Checks whether a token is a StrengthToken.
 * @author Mayo Clinic
 */
public class StrengthCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof StrengthToken)
		{
			return true;
		}
        	
		return false;
	}		
}
