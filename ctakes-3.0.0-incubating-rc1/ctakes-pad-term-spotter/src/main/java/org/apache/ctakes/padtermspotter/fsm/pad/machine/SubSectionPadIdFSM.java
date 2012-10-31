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

import org.apache.ctakes.core.fsm.condition.PunctuationValueCondition;
import org.apache.ctakes.core.fsm.condition.SymbolValueCondition;
import org.apache.ctakes.core.fsm.condition.TextSetCondition;
import org.apache.ctakes.core.fsm.condition.WordValueCondition;
import org.apache.ctakes.core.fsm.state.NamedState;
import org.apache.ctakes.core.fsm.state.NonTerminalEndState;
import org.apache.ctakes.core.fsm.token.BaseToken;
import org.apache.ctakes.padtermspotter.fsm.output.SubSectionIndicator;

import net.openai.util.fsm.AnyCondition;
import net.openai.util.fsm.Condition;
import net.openai.util.fsm.Machine;
import net.openai.util.fsm.State;

/**
 * Uses one or more finite state machines to detect sub-sections in the given input of
 * tokens.
 * 		- terms; "smh","rmh","gonda","romayo" are indicative of names of buildings on the Mayo campus which are used to mark 
 *	subsection begin/end
 *		- terms; "indications","bleindications","exam","showing" are special terms which often contain the terms being screened for 
 *		relating to PAD, but since they are titles of examinations, revision sections, and generalized screenings they are to be 
 *		ignored in the Mayo cohort.
 *
 * @author Mayo Clinic
 */

public class SubSectionPadIdFSM {

	// contains the finite state machines
	private Set<Machine>iv_machineSet = new HashSet<Machine>();

	private Set<String>iv_probableSubSet = new HashSet<String>();
	
	private Set<String>iv_probableSubSetNL = new HashSet<String>();
	
	private Set<String>iv_probableSubBeginSet = new HashSet<String>();
	
	private Set<String>iv_probableSubEndSet = new HashSet<String>();
	
	private Set<String>iv_negateSubSet = new HashSet<String>();
	
	private Set<String>iv_confirmedSubBeginSet = new HashSet<String>();

	private Set<String>iv_confirmedSubNextSet = new HashSet<String>();
	
	private Set<String>iv_confirmedSubTailSet = new HashSet<String>();
	
	private Set<String> iv_confirmedSpecialFirstSubSet = new HashSet<String>();
	
	private Set<String> iv_confirmedSpecialNextSubSet = new HashSet<String>();
	
	private Machine iv_subSectionIDConfirmMachine = new Machine();
	
	private Machine iv_subSectionIDProbableMachine = new Machine();
	
	private Machine iv_subSectionIDNegateMachine = new Machine();

	
	/**
	 * 
	 * Constructor
	 * 
	 */
	public SubSectionPadIdFSM() {

		iv_probableSubSet.add("left");
		iv_probableSubSet.add("right");
		iv_probableSubSet.add("cta");
		iv_probableSubSet.add("reconstructions");
		iv_probableSubSet.add("pelvis");
		iv_probableSubSet.add("impression");
		iv_probableSubSet.add("summary");
		iv_probableSubSet.add("analysis");
		
		iv_probableSubSetNL.add("smh");// Mayo Clinic specific term *Site*
		iv_probableSubSetNL.add("rmh");// Mayo Clinic specific term *Site*
		iv_probableSubSetNL.add("gonda");// Mayo Clinic specific term *Site*
		iv_probableSubSetNL.add("romayo");// Mayo Clinic specific term *Site*

		iv_probableSubBeginSet.add("original");
		iv_probableSubBeginSet.add("additional");
		iv_probableSubEndSet.add("report");
		iv_probableSubEndSet.add("findings");
		
		iv_negateSubSet.add("indications");// Mayo Clinic specific term *Header*
		iv_negateSubSet.add("bleindications");// Mayo Clinic specific term *Header*
		iv_negateSubSet.add("exam");// Mayo Clinic specific term *Header*
		iv_negateSubSet.add("showing");// Mayo Clinic specific term *Header*
				
		iv_confirmedSubBeginSet.add("right");
		iv_confirmedSubBeginSet.add("left");
		iv_confirmedSubBeginSet.add("runoff");
		iv_confirmedSubBeginSet.add("bilateral");
		iv_confirmedSubBeginSet.add("lower");
		iv_confirmedSubBeginSet.add("ultrasound");
		iv_confirmedSubBeginSet.add("v");
		
		iv_confirmedSubNextSet.add("&");
		iv_confirmedSubNextSet.add("leg");
		iv_confirmedSubNextSet.add("lower");
		iv_confirmedSubNextSet.add("extremity");
		iv_confirmedSubNextSet.add("extrem");
		iv_confirmedSubNextSet.add("examination");
		iv_confirmedSubNextSet.add("evaluation");
		
		iv_confirmedSubTailSet.add("extremity");
		iv_confirmedSubTailSet.add("extremities");
		iv_confirmedSubTailSet.add("extrem");
		iv_confirmedSubTailSet.add("artery");
		iv_confirmedSubTailSet.add("art");
		iv_confirmedSubTailSet.add("of");
		iv_confirmedSubTailSet.add("irad");

		iv_confirmedSpecialFirstSubSet.add("graft");
		
		iv_confirmedSpecialNextSubSet.add("art");
		
		iv_subSectionIDConfirmMachine = getConfirmSubSectionMachine();
		iv_subSectionIDNegateMachine = getNegateSubSectionMachine();
		iv_subSectionIDProbableMachine = getProbableSubSectionMachine();
		iv_machineSet.add(iv_subSectionIDConfirmMachine);
		iv_machineSet.add(iv_subSectionIDNegateMachine);
		iv_machineSet.add(iv_subSectionIDProbableMachine);
	}


	public Set<SubSectionIndicator> execute(List<? extends BaseToken> tokens) throws Exception {
		Set<SubSectionIndicator> outSet = new HashSet<SubSectionIndicator>();

		// maps a fsm to a token start index
		// key = fsm , value = token start index
		Map<Machine, Integer>  tokenStartMap = new HashMap<Machine, Integer> ();

		for (int i = 0; i < tokens.size(); i++) {
			BaseToken token = (BaseToken) tokens.get(i);

			Iterator<Machine> machineItr = iv_machineSet.iterator();
			while (machineItr.hasNext()) {
				Machine fsm = (Machine) machineItr.next();

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
					BaseToken endToken = null;
					if (currentState instanceof NonTerminalEndState) {
						endToken = (BaseToken) tokens.get(i - 1);
					} else {
						endToken = token;
					}

					BaseToken startToken = (BaseToken) tokens
							.get(tokenStartIndex);
					SubSectionIndicator subs = null;
					if (fsm.equals(iv_subSectionIDConfirmMachine)) {
						subs = new SubSectionIndicator(startToken
								.getStartOffset(), endToken.getEndOffset(),
								SubSectionIndicator.CONFIRMED_STATUS);
					} else if (fsm.equals(iv_subSectionIDNegateMachine)) {
						subs = new SubSectionIndicator(startToken
								.getStartOffset(), endToken.getEndOffset(),
								SubSectionIndicator.NEGATED_STATUS);
					} else if (fsm.equals(iv_subSectionIDProbableMachine)) {
						subs = new SubSectionIndicator(startToken
								.getStartOffset(), endToken.getEndOffset(),
								SubSectionIndicator.PROBABLE_STATUS);
					}
					outSet.add(subs);

					fsm.reset();
				}
			}
		}

		// cleanup
		tokenStartMap.clear();

		// reset machines
		Iterator<Machine> itr = iv_machineSet.iterator();
		while (itr.hasNext()) {
			Machine fsm = (Machine) itr.next();
			fsm.reset();
		}
	
		return outSet;
	}

	private Machine getConfirmSubSectionMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		State medState = new NamedState("MED");
		State tailState = new NamedState("TAIL");
		State caseState = new NamedState("COLON");
		State nonCaseState = new NamedState("NOCOLON");
		State fslashState = new NamedState("FSLASH");
		State orState = new NamedState("OR");
		State altTailState = new NamedState("ALTTAIL");
		State finishAltState = new NamedState("EXTREMITY");
		
		State ntEndState = new NonTerminalEndState("NON TERMINAL END");
		endState.setEndStateFlag(true);
		ntEndState.setEndStateFlag(true);
	
		Machine m = new Machine(startState);
	
		Condition subFirstBegin = new TextSetCondition(iv_confirmedSubBeginSet,
				false);
		Condition subFirstNext = new TextSetCondition(iv_confirmedSubNextSet,
				false);
		Condition subFirstTail = new TextSetCondition(iv_confirmedSubTailSet,
				false);
		Condition altSubFirstBegin = new TextSetCondition(iv_confirmedSubBeginSet,
				false);
		Condition altSubFirstNext = new TextSetCondition(iv_confirmedSubNextSet,
				false);
		Condition altSubFirstTail = new TextSetCondition(iv_confirmedSubTailSet,
				false);
		Condition subSecondBegin = new TextSetCondition(iv_confirmedSpecialFirstSubSet,
				false);
		Condition subSecondNext = new TextSetCondition(iv_confirmedSpecialNextSubSet,
				false);

		startState.addTransition(subFirstBegin, medState);
	    startState.addTransition(new AnyCondition(), startState);

	    medState.addTransition(new PunctuationValueCondition(':'), endState);
		medState.addTransition(subFirstNext, tailState);
		medState.addTransition(new SymbolValueCondition('&'), tailState);
		medState.addTransition(new PunctuationValueCondition('&'), tailState);
		medState.addTransition(new AnyCondition(), startState);
		
		tailState.addTransition(new PunctuationValueCondition(':'), endState);
		tailState.addTransition(subFirstTail, caseState);
		tailState.addTransition(new AnyCondition(), startState);
		
		caseState.addTransition(new WordValueCondition("and", false), nonCaseState);
		caseState.addTransition(new WordValueCondition("the", false), nonCaseState);
		caseState.addTransition(new PunctuationValueCondition(':'), endState);
		caseState.addTransition(new PunctuationValueCondition('-'), endState);
		caseState.addTransition(new AnyCondition(), startState);
		
		nonCaseState.addTransition(new PunctuationValueCondition('/'), fslashState);
		nonCaseState.addTransition(altSubFirstBegin, altTailState);
		nonCaseState.addTransition(new AnyCondition(), startState);
		
		altTailState.addTransition(altSubFirstNext, finishAltState);
		altTailState.addTransition(new AnyCondition(), startState);
		
		finishAltState.addTransition(altSubFirstTail, endState);
		finishAltState.addTransition(new AnyCondition(), startState);
		
		fslashState.addTransition(new WordValueCondition("or", false), orState);
		fslashState.addTransition(subSecondBegin, orState);
		fslashState.addTransition(new AnyCondition(), startState);
		
		orState.addTransition(subSecondNext, endState);
		orState.addTransition(new AnyCondition(), startState);
		
		endState.addTransition(new AnyCondition(), startState);
		return m;
	}


	private Machine getProbableSubSectionMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
	    State nonCaseState = new NamedState("NOCOLON");
		State casenState = new NamedState("COLON");
		
		State ntEndState = new NonTerminalEndState("NON TERMINAL END");
		endState.setEndStateFlag(true);
		ntEndState.setEndStateFlag(true);
	
		Machine m = new Machine(startState);
	
		Condition subFirstSolo = new TextSetCondition(iv_probableSubSet,
				false);
		Condition subFirstBegin = new TextSetCondition(iv_probableSubBeginSet,
				false);
		Condition subFirstEnd = new TextSetCondition(iv_probableSubEndSet,
				false);
		Condition subFirstSoloNL = new TextSetCondition(iv_probableSubSetNL,
				false);
		
		startState.addTransition(subFirstSolo, casenState);
		startState.addTransition(subFirstBegin, nonCaseState);
		startState.addTransition(subFirstSoloNL, ntEndState);
	    startState.addTransition(new AnyCondition(), startState);
	    
	    nonCaseState.addTransition(subFirstEnd, endState);
	    nonCaseState.addTransition(new AnyCondition(), startState);
		
		casenState.addTransition(new PunctuationValueCondition(':'), endState);
		casenState.addTransition(new AnyCondition(), startState);
		
		endState.addTransition(new AnyCondition(), startState);
		return m;
	}


	private Machine getNegateSubSectionMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		State caseState = new NamedState("COLON");
		State nonCaseState = new NamedState("NOCOLON");
		
		State ntEndState = new NonTerminalEndState("NON TERMINAL END");
		endState.setEndStateFlag(true);
		ntEndState.setEndStateFlag(true);
	
		Machine m = new Machine(startState);
	
		Condition subFirstBegin = new TextSetCondition(iv_negateSubSet,
				false);
	
		startState.addTransition(subFirstBegin, caseState);
	    startState.addTransition(new AnyCondition(), startState);
	
		
		caseState.addTransition(new PunctuationValueCondition(':'), endState);
		caseState.addTransition(new WordValueCondition("revision", false), nonCaseState);
		caseState.addTransition(new AnyCondition(), startState);
		
		nonCaseState.addTransition(new WordValueCondition("changes", false), endState);
		nonCaseState.addTransition(new AnyCondition(), startState);
		
		endState.addTransition(new AnyCondition(), startState);
		return m;
	}

}