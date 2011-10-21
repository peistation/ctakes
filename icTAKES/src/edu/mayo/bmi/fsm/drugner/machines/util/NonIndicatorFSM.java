package edu.mayo.bmi.fsm.drugner.machines.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;
import edu.mayo.bmi.fsm.condition.TextSetCondition;
import edu.mayo.bmi.fsm.drugner.output.util.NonIndicator;
import edu.mayo.bmi.fsm.state.NamedState;
import edu.mayo.bmi.fsm.state.NonTerminalEndState;
import edu.mayo.bmi.fsm.token.BaseToken;

/**
 * Uses one or more finite state machines to detect dates in the given input of
 * tokens.
 * 
 * @author DUFFP
 */
public class NonIndicatorFSM
{

 
    private Set iv_negDeterminersSet = new HashSet();

    // contains the finite state machines
    private Set iv_machineSet = new HashSet();
    // beginning with capitalize. E.g 'No'
    private Set iv_negInitialDeterminersSet = new HashSet();
    
    private Machine iv_negInitialDetermineMachine = new Machine();
    /**
     * 
     * Constructor
     *  
     */
    public NonIndicatorFSM()
    {

        iv_negDeterminersSet.add("non");
        
        iv_negInitialDeterminersSet.add("No");
        
        iv_negInitialDetermineMachine = getInitialNegIndicatorMachine();

        iv_machineSet.add(getAdjNegIndicatorMachine());
        iv_machineSet.add(iv_negInitialDetermineMachine);

    }

    private Machine getInitialNegIndicatorMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");

        State ntEndState = new NonTerminalEndState("NON TERMINAL END");
        endState.setEndStateFlag(true);
        ntEndState.setEndStateFlag(true);

        Machine m = new Machine(startState);
       
        State negDetState = new NamedState("NEG_DET");
        Condition negInitDetC = new TextSetCondition(iv_negInitialDeterminersSet, true);
        startState.addTransition(negInitDetC, negDetState); // start with a modal
        
        startState.addTransition(new AnyCondition(), startState);

        negDetState.addTransition(new AnyCondition(), startState);

        endState.addTransition(new AnyCondition(), startState);
        return m;
    }

    private Machine getAdjNegIndicatorMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");
           endState.setEndStateFlag(true);
        

        Machine m = new Machine(startState);

      
        State negAdjState = new NamedState("NEG_ADJ");


        Condition negDetC = new TextSetCondition(iv_negDeterminersSet, false);

        startState.addTransition(negDetC, negAdjState); 
        startState.addTransition(new AnyCondition(), startState);
        
        negAdjState.addTransition(new AnyCondition(), startState);

     

        return m;
    }

    /**
     * Executes the finite state machines.
     * 
     * @param tokens
     * @return Set of DateToken objects.
     * @throws Exception
     */
    public Set execute(List tokens) throws Exception
    {
        Set outSet = new HashSet();

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
                    BaseToken endToken = null;
                    if (currentState instanceof NonTerminalEndState)
                    {
                        endToken = (BaseToken) tokens.get(i - 1);
                    }
                    else
                    {
                        endToken = token;
                    }

                    BaseToken startToken = (BaseToken) tokens
                            .get(tokenStartIndex);
                    NonIndicator neg = null;
                    if (fsm.equals(iv_negInitialDetermineMachine))
                    {
                        neg =  new NonIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset());
                    }
                    else neg = new NonIndicator(
                            startToken.getStartOffset(),
                            endToken.getEndOffset());
                    outSet.add(neg);
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

        return outSet;
    }
}