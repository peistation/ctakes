package edu.mayo.bmi.fsm.drugner.elements.conditions;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.drugner.output.elements.DrugChangeStatusToken;

/**
 * Checks whether a token is a DrugChangeStatusToken.
 * @author Mayo Clinic
 */
public class DrugChangeStatusCondition extends Condition
{
	public boolean satisfiedBy(Object conditional)
	{
		if (conditional instanceof DrugChangeStatusToken)
		{
			return true;
		}
        	
		return false;
	}		
}
