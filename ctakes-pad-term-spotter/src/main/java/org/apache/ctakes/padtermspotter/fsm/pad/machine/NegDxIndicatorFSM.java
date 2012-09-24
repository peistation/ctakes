/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.padtermspotter.fsm.pad.machine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.core.fsm.condition.TextSetCondition;
import org.apache.ctakes.core.fsm.output.NegationIndicator;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.core.fsm.state.NonTerminalEndState;
import org.apache.ctakes.core.fsm.token.BaseToken;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;

/**
 * Uses one or more finite state machines to detect dates in the given input of
 * tokens.
 * 
 * @author Mayo Clinic
 */
public class NegDxIndicatorFSM
{

    private Set<String>iv_negCol1of2PartSet = new HashSet<String>();
    
    private Set<String>iv_negCol2of2PartSet = new HashSet<String>();
    
    private Set<String>iv_negColAnyOf3PartSet = new HashSet<String>();
    
    private Set<String>iv_negCol1Of3PartSet = new HashSet<String>();
    private Set<String>iv_negCol2Of3PartSet = new HashSet<String>();
    private Set<String>iv_negCol3Of3PartSet = new HashSet<String>();
    
    private Set<String>iv_negCol1Of3ArterialPartSet = new HashSet<String>();
    private Set<String>iv_negCol2Of3ArterialPartSet = new HashSet<String>();
    private Set<String>iv_negCol3Of3ArterialPartSet = new HashSet<String>();
    // beginning with capitalize. E.g 'No'
    private Set<String>iv_negInitialDeterminersSet = new HashSet<String>();
    // contains the finite state machines
    private Set<Machine>iv_machineSet = new HashSet<Machine>();

    
    private Machine iv_negInitialDetermineMachine = new Machine();
    /**
     * 
     * Constructor
     *  
     */
    public NegDxIndicatorFSM()
    {

        iv_negInitialDeterminersSet.add("patent");
        iv_negInitialDeterminersSet.add("endograft");
        iv_negInitialDeterminersSet.add("minimal");
        iv_negInitialDeterminersSet.add("celiac");
        iv_negInitialDeterminersSet.add("hypogastric");
        iv_negInitialDeterminersSet.add("ectatic");
        iv_negInitialDeterminersSet.add("mild");
        iv_negInitialDeterminersSet.add("vein");
        iv_negInitialDeterminersSet.add("veins");
        iv_negInitialDeterminersSet.add("ima");
        iv_negInitialDeterminersSet.add("nonstenotic");
        
        iv_negCol1of2PartSet.add("mild");
        iv_negCol1of2PartSet.add("minimal");
        iv_negCol1of2PartSet.add("articular");
        iv_negCol1of2PartSet.add("with");
        iv_negCol1of2PartSet.add("normal");
        iv_negCol1of2PartSet.add("thoracic");
        iv_negCol1of2PartSet.add("abdominal");
        
        iv_negCol2of2PartSet.add("atherosclerotic");
        iv_negCol2of2PartSet.add("narrowing");
        iv_negCol2of2PartSet.add("calcification");
        iv_negCol2of2PartSet.add("diffuse");
        iv_negCol2of2PartSet.add("coils");
        iv_negCol2of2PartSet.add("flow");
        iv_negCol2of2PartSet.add("aorta");
        
        iv_negCol1Of3ArterialPartSet.add("aneurysmal");
        iv_negCol1Of3ArterialPartSet.add("peri");
        iv_negCol1Of3ArterialPartSet.add("para");
        
        iv_negCol2Of3ArterialPartSet.add("popliteal");
        iv_negCol2Of3ArterialPartSet.add("articular");
        
        iv_negCol3Of3ArterialPartSet.add("artery");
        iv_negCol3Of3ArterialPartSet.add("arteries");
        iv_negCol3Of3ArterialPartSet.add("calcification"); 
        
        iv_negCol1Of3PartSet.add("mild");
        iv_negCol1Of3PartSet.add("dilatation");
        iv_negCol1Of3PartSet.add("small");
        iv_negCol1Of3PartSet.add("no");

        iv_negCol1Of3PartSet.add("soft");
        iv_negCol1Of3PartSet.add("atheromatous");
        
        
        iv_negCol2Of3PartSet.add("focal");
        iv_negCol2Of3PartSet.add("calcified");
        iv_negCol2Of3PartSet.add("significant");
        iv_negCol2Of3PartSet.add("ulcerative");
        
        iv_negCol3Of3PartSet.add("narrowing");
        iv_negCol3Of3PartSet.add("plaque"); 
        iv_negCol3Of3PartSet.add("plaques"); 

        
        iv_negInitialDetermineMachine = getInitialNegIndicatorMachine();
        iv_machineSet.add(iv_negInitialDetermineMachine);

    }

    private Machine getInitialNegIndicatorMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");
        State twoPartState = new NamedState("TWOSTATE");
        State twoOfThreePartState = new NamedState("TWOOFTHREESTATE");
        State threePartState = new NamedState("THREESTATE");
        State twoArterialPartState = new NamedState("TWOSTATEART");
        State threeArterialPartState = new NamedState("THREESTATEART");
        State ntEndState = new NonTerminalEndState("NON TERMINAL END");
        endState.setEndStateFlag(true);
        ntEndState.setEndStateFlag(true);

        Machine m = new Machine(startState);
 

        Condition negInitDetC = new TextSetCondition(iv_negInitialDeterminersSet, false);
        Condition negInit1of2DetC = new TextSetCondition(iv_negCol1of2PartSet, false);
        Condition negInit2of2DetC = new TextSetCondition(iv_negCol2of2PartSet, false);
        Condition negInit1of3DetC = new TextSetCondition(iv_negCol1Of3PartSet, false);
        Condition negInit2of3DetC = new TextSetCondition(iv_negCol2Of3PartSet, false);
        Condition negInit3of3DetC = new TextSetCondition(iv_negCol3Of3PartSet, false);
        Condition negInit1of3ArterialDetC = new TextSetCondition(iv_negCol1Of3ArterialPartSet, false);
        Condition negInit2of3ArterialDetC = new TextSetCondition(iv_negCol2Of3ArterialPartSet, false);
        Condition negInit3of3ArterialDetC = new TextSetCondition(iv_negCol3Of3ArterialPartSet, false);
        Condition negInitNtEndDetC = new TextSetCondition(iv_negColAnyOf3PartSet, false);
        
        startState.addTransition(negInitDetC, endState); // start with a modal
        startState.addTransition(negInit1of2DetC, twoPartState);
        startState.addTransition(negInit1of3DetC, twoOfThreePartState); 
        startState.addTransition(negInit1of3ArterialDetC, twoArterialPartState); 
        startState.addTransition(negInitNtEndDetC, ntEndState); 
        startState.addTransition(new AnyCondition(), startState);

        twoPartState.addTransition(negInit2of2DetC, endState);
        twoPartState.addTransition(new AnyCondition(), startState);
        
        twoOfThreePartState.addTransition(negInit2of3DetC, threePartState);
        twoOfThreePartState.addTransition(new AnyCondition(), startState);
        
        twoArterialPartState.addTransition(negInit2of3ArterialDetC, threeArterialPartState);
        twoArterialPartState.addTransition(new AnyCondition(), startState);

        threePartState.addTransition(negInit3of3DetC, endState);
        threePartState.addTransition(new AnyCondition(), startState);
        
        threeArterialPartState.addTransition(negInit3of3ArterialDetC, endState);
        threeArterialPartState.addTransition(new AnyCondition(), startState);
        
        ntEndState.addTransition(negInitNtEndDetC, ntEndState);
        ntEndState.addTransition(new AnyCondition(), endState);
        
        endState.addTransition(new AnyCondition(), startState);
        return m;
    }

    /**
     * Executes the finite state machines.
     * 
     * @param tokens
     * @return Set of DateToken objects.
     * @throws Exception
     */
    public Set<NegationIndicator> execute(List<? extends BaseToken> tokens) throws Exception
    {
        Set<NegationIndicator> outSet = new HashSet<NegationIndicator>();

        // maps a fsm to a token start index
        // key = fsm , value = token start index
        Map<Machine, Integer> tokenStartMap = new HashMap<Machine, Integer>();

        for (int i = 0; i < tokens.size(); i++)
        {
            BaseToken token = (BaseToken) tokens.get(i);

            Iterator<Machine> machineItr = iv_machineSet.iterator();
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
                    NegationIndicator neg = null;
                    if (fsm.equals(iv_negInitialDetermineMachine))
                    {
                        neg =  new NegationIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset());
                    }
                    else neg = new NegationIndicator(
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
        Iterator<Machine> itr = iv_machineSet.iterator();
        while (itr.hasNext())
        {
            Machine fsm = (Machine) itr.next();
            fsm.reset();
        }

        return outSet;
    }
}