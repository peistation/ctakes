package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.elements.FormToken;

/**
 * Checks whether a token is a FormToken.
 * @author Mayo Clinic
 */
public class FormCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof FormToken)
		{
			return true;
		}
        	
		return false;
	}		
}
