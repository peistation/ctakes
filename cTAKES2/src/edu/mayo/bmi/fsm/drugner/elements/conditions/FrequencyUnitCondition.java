package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.elements.FrequencyUnitToken;

/**
 * Checks whether a token is a FrequencyUnitToken.
 * @author Mayo Clinic
 */
public class FrequencyUnitCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof FrequencyUnitToken)
		{
			return true;
		}
        	
		return false;
	}		
}
