/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.fsm.machine;

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
import edu.mayo.bmi.fsm.condition.PunctuationValueCondition;
import edu.mayo.bmi.fsm.condition.WordSetCondition;
import edu.mayo.bmi.fsm.output.PersonTitleToken;
import edu.mayo.bmi.fsm.state.NamedState;
import edu.mayo.bmi.fsm.token.BaseToken;

/**
 * Uses one or more finite state machines to detect measurements in the given
 * input of tokens.
 * 
 * @author Mayo Clinic
 */
public class PersonTitleFSM {
	// text fractions
	Set<String> iv_fullTextSet = new HashSet<String>();

	// contains the finite state machines
	private Set<Machine> iv_machineSet = new HashSet<Machine>();

	/**
	 * 
	 * Constructor
	 * 
	 */
	public PersonTitleFSM() {
		iv_fullTextSet.add("mr");
		iv_fullTextSet.add("ms");
		iv_fullTextSet.add("mrs");
		iv_fullTextSet.add("dr");

		iv_machineSet.add(getTitleMachine());
	}

	/**
	 * Gets a finite state machine that detects the following:
	 * <ol>
	 * <li>Dr.</li>
	 * </ol>
	 * 
	 * @return
	 */
	private Machine getTitleMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		endState.setEndStateFlag(true);

		Machine m = new Machine(startState);
		State titleNameState = new NamedState("TITLENAME");

		Condition titleNameCondition = new WordSetCondition(iv_fullTextSet,
				false);
		Condition periodCondition = new PunctuationValueCondition('.');

		startState.addTransition(titleNameCondition, titleNameState);
		startState.addTransition(new AnyCondition(), startState);

		titleNameState.addTransition(periodCondition, endState);
		titleNameState.addTransition(new AnyCondition(), startState);

		endState.addTransition(new AnyCondition(), startState);

		return m;
	}

	/**
	 * Executes the finite state machines.
	 * 
	 * @param tokens
	 * @return Set of FractionToken objects.
	 * @throws Exception
	 */
	public Set<PersonTitleToken> execute(List<? extends BaseToken> tokens)
			throws Exception {
		Set<PersonTitleToken> personTitleSet = new HashSet<PersonTitleToken>();

		// maps a fsm to a token start index
		// key = fsm , value = token start index
		Map<Machine, Integer> tokenStartMap = new HashMap<Machine, Integer>();

		for (int i = 0; i < tokens.size(); i++) {
			BaseToken token = tokens.get(i);

			Iterator<Machine> machineItr = iv_machineSet.iterator();
			while (machineItr.hasNext()) {
				Machine fsm = machineItr.next();

				fsm.input(token);

				State currentState = fsm.getCurrentState();
				if (currentState.getStartStateFlag()) {
					tokenStartMap.put(fsm, new Integer(i));
				}
				if (currentState.getEndStateFlag()) {
					Object o = tokenStartMap.get(fsm);
					int tokenStartIndex;
					if (o == null) {
						// By default, all machines start with
						// token zero.
						tokenStartIndex = 0;
					} else {
						tokenStartIndex = ((Integer) o).intValue();
						// skip ahead over single token we don't want
						tokenStartIndex++;
					}
					BaseToken startToken = tokens.get(tokenStartIndex);
					BaseToken endToken = token;
					PersonTitleToken ptToken = new PersonTitleToken(startToken
							.getStartOffset(), endToken.getEndOffset());
					personTitleSet.add(ptToken);
					fsm.reset();
				}
			}
		}

		// cleanup
		tokenStartMap.clear();

		// reset machines
		Iterator<Machine> itr = iv_machineSet.iterator();
		while (itr.hasNext()) {
			Machine fsm = itr.next();
			fsm.reset();
		}

		return personTitleSet;
	}
}
