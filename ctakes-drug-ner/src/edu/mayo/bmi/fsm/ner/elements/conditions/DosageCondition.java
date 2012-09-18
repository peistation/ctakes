package edu.mayo.bmi.fsm.ner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.ner.output.elements.DosageToken;

/**
 * Checks whether a token is a DosageToken.
 * @author Mayo Clinic
 */
public class DosageCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof DosageToken)
		{
			return true;
		}
        	
		return false;
	}		
}
