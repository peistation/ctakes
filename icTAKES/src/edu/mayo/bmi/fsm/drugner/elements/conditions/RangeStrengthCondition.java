package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.util.RangeStrengthToken;

/**
 * Checks whether a token is a RangeStrengthToken.
 * @author Mayo Clinic
 */
public class RangeStrengthCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof RangeStrengthToken)
		{
			return true;
		}
        	
		return false;
	}		
}
