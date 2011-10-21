package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.util.FractionToken;

/**
 * Checks whether a token is a FractionToken.
 * @author Mayo Clinic
 */
public class FractionCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof FractionToken)
		{
			return true;
		}
        	
		return false;
	}		
}
