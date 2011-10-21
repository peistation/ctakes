package edu.mayo.bmi.fsm.drugner.machines.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;
import edu.mayo.bmi.fsm.condition.IntegerValueCondition;
import edu.mayo.bmi.fsm.condition.NumberCondition;
import edu.mayo.bmi.fsm.condition.PunctuationValueCondition;
import edu.mayo.bmi.fsm.drugner.output.util.DecimalStrengthToken;
import edu.mayo.bmi.fsm.state.NamedState;
import edu.mayo.bmi.fsm.token.BaseToken;

/**
 * Uses one or more finite state machines to detect decimal fractions in the given
 * input of tokens.
 * @author Mayo Clinic
 */
public class DecimalStrengthFSM
{
	Set iv_fullTextSet = new HashSet();

	Set iv_shortTextSet = new HashSet();
    // contains the finite state machines
    private Set iv_machineSet = new HashSet();

    /**
     * 
     * Constructor
     *
     */
    public DecimalStrengthFSM()
    {

        iv_machineSet.add(getDecimalStrengthMachine());
       
    }

    /**
     * Gets a finite state machine that detects the following:
     * <ol>
     * 		<li>0.5*</li>
     * </ol>
     * @return
     */
    private Machine getDecimalStrengthMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");
        endState.setEndStateFlag(true);

        Machine m = new Machine(startState);
        State zeroNumState = new NamedState("ZERO_NUM");
        State fractionTextState = new NamedState("NUMERATOR_TEXT");
        State dashState = new NamedState("DASH");
        startState.addTransition(new IntegerValueCondition(0), zeroNumState);
        startState.addTransition(new AnyCondition(), startState);

        zeroNumState.addTransition(new PunctuationValueCondition('.'), fractionTextState);
		zeroNumState.addTransition(new AnyCondition(), startState);

		fractionTextState.addTransition(new NumberCondition(), dashState);
		fractionTextState.addTransition(new AnyCondition(), startState);
		
		dashState.addTransition(new PunctuationValueCondition('-'), endState);
		dashState.addTransition(new AnyCondition(), startState);
		
        endState.addTransition(new AnyCondition(), startState);

        return m;
    }

    /**
     * Executes the finite state machines.
     * @param tokens
     * @return Set of FractionToken objects.
     * @throws Exception
     */
    public Set execute(List tokens) throws Exception
    {
        Set fractionSet = new HashSet();

        // maps a fsm to a token start index
        // key = fsm , value = token start index
        Map tokenStartMap = new HashMap();

        for (int i = 0; i < tokens.size(); i++)
        {
            BaseToken token = (BaseToken) tokens.get(i);

            Iterator machineItr = iv_machineSet.iterator();
            while (machineItr.hasNext())
            {
                Machine fsm = (Machine) machineItr.next();

                fsm.input(token);

                State currentState = fsm.getCurrentState();
                if (currentState.getStartStateFlag())
                {
                    tokenStartMap.put(fsm, new Integer(i));
                }
                if (currentState.getEndStateFlag())
                {
                    Object o = tokenStartMap.get(fsm);
					int tokenStartIndex;
					if (o == null)
					{
						// By default, all machines start with
						// token zero.
						tokenStartIndex = 0;
					}
					else
					{
						tokenStartIndex = ((Integer) o).intValue();
						// skip ahead over single token we don't want
						tokenStartIndex++;						
					}
                    BaseToken startToken =
                        (BaseToken) tokens.get(tokenStartIndex);
                    BaseToken endToken = token;
                    DecimalStrengthToken fractionToken =
                        new DecimalStrengthToken(
                            startToken.getStartOffset(),
                            endToken.getEndOffset());
                    fractionSet.add(fractionToken);
                    fsm.reset();
                }
            }
        }

        // cleanup
        tokenStartMap.clear();

        // reset machines
        Iterator itr = iv_machineSet.iterator();
        while (itr.hasNext())
        {
            Machine fsm = (Machine) itr.next();
            fsm.reset();
        }

        return fractionSet;
    }
}
