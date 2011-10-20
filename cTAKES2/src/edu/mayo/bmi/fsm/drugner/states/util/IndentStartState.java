package edu.mayo.bmi.fsm.drugner.states.util;

import edu.mayo.bmi.fsm.state.NamedState;


/**
 *
 * @author Mayo Clinic
 */
public class IndentStartState extends NamedState
{
	public IndentStartState(String name)
	{
		super(name);
	}

    /* (non-Javadoc)
     * @see net.openai.util.fsm.State#enter(java.lang.Object)
     */
    public void enter(Object arg0)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.openai.util.fsm.State#exit()
     */
    public Object exit()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
