package edu.mayo.bmi.fsm.ner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.ner.output.elements.FormToken;

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
