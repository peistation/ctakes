package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.output.TimeToken;

/**
 * Checks whether a token is a TimeToken.
 * @author Mayo Clinic
 */
public class TimeCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof TimeToken)
		{
			return true;
		}
        	
		return false;
	}		
}
