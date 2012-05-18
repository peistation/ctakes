package edu.mayo.bmi.fsm.ner.machines.util;

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
import edu.mayo.bmi.fsm.condition.IntegerCondition;
import edu.mayo.bmi.fsm.condition.PunctuationValueCondition;
import edu.mayo.bmi.fsm.condition.WordSetCondition;
import edu.mayo.bmi.fsm.ner.elements.conditions.StrengthCondition;
import edu.mayo.bmi.fsm.ner.output.util.SuffixStrengthToken;
import edu.mayo.bmi.fsm.state.NamedState;
import edu.mayo.bmi.fsm.token.BaseToken;

/**
 * Uses one or more finite state machines to detect strength suffix information
 * adjoined to dosage data in the given input of tokens.
 * 
 * @author Mayo Clinic
 */
public class SuffixStrengthFSM {
	// text fractions
	Set iv_textSuffixSet = new HashSet();

	// contains the finite state machines
	private Set iv_machineSet = new HashSet();

	/**
	 * 
	 * Constructor
	 * 
	 */
	public SuffixStrengthFSM() {
		iv_textSuffixSet.add("gtts");
		iv_textSuffixSet.add("ui");
		iv_textSuffixSet.add("iu");
		iv_textSuffixSet.add("meq");
		iv_textSuffixSet.add("mcg");
		iv_textSuffixSet.add("gr");
		iv_textSuffixSet.add("tsp");
		iv_textSuffixSet.add("tbsp");
		iv_textSuffixSet.add("g");
		iv_textSuffixSet.add("mg");
		iv_textSuffixSet.add("dl");
		iv_textSuffixSet.add("cl");
		iv_textSuffixSet.add("ml");
		iv_textSuffixSet.add("kg");
		iv_textSuffixSet.add("cc");
		iv_textSuffixSet.add("ou");

		iv_machineSet.add(getDashMachine());

	}

	/**
	 * Gets a finite state machine that detects the following:
	 * <ol>
	 * <li>25.4-30.4</li>
	 * <li>32.1-three</li>
	 * </ol>
	 * 
	 * @return
	 */
	private Machine getDashMachine() {
		State startState = new NamedState("START");
		State endState = new NamedState("END");
		endState.setEndStateFlag(true);

		Machine m = new Machine(startState);

		State leftNumTextState = new NamedState("LEFT_DOSAGE");
		State rightNumTextState = new NamedState("RIGHT_DOSAGE");
		State fslashState = new NamedState("FSLASH");
		Condition dashCondition = new PunctuationValueCondition('/');

		Condition leftNumTextCondition = new StrengthCondition();
		Condition rightIntegerCondition = new IntegerCondition();
		Condition rightNumTextCondition = new WordSetCondition(
				iv_textSuffixSet, false);

		startState.addTransition(leftNumTextCondition, leftNumTextState);
		startState.addTransition(new AnyCondition(), startState);

		leftNumTextState.addTransition(dashCondition, fslashState);
		leftNumTextState.addTransition(new AnyCondition(), startState);

		fslashState.addTransition(rightNumTextCondition, endState);
		fslashState.addTransition(rightIntegerCondition, rightNumTextState);
		fslashState.addTransition(new AnyCondition(), startState);

		rightNumTextState.addTransition(rightNumTextCondition, endState);
		rightNumTextState.addTransition(new AnyCondition(), startState);
		endState.addTransition(new AnyCondition(), startState);

		return m;
	}

	/**
	 * Executes the finite state machines.
	 * 
	 * @param tokens
	 * @return Set of RangeToken objects.
	 * @throws Exception
	 */
	public Set execute(List tokens, Set overrideSet) throws Exception {
		Set rangeSet = new HashSet();

		// maps a fsm to a token start index
		// key = fsm , value = token start index
		Map tokenStartMap = new HashMap();

		Iterator overrideTokenItr = overrideSet.iterator();
		// key = start offset, value = override BaseToken object
		Map overrideTokenMap = new HashMap();
		while (overrideTokenItr.hasNext()) {
			BaseToken t = (BaseToken) overrideTokenItr.next();
			Integer key = new Integer(t.getStartOffset());
			overrideTokenMap.put(key, t);
		}

		boolean overrideOn = false;
		int overrideEndOffset = -1;
		for (int i = 0; i < tokens.size(); i++) {
			BaseToken token = (BaseToken) tokens.get(i);

			Integer key = new Integer(token.getStartOffset());

			if (overrideOn) {
				if (token.getStartOffset() >= overrideEndOffset) {
					overrideOn = false;
					overrideEndOffset = -1;
				} else {
					// step to next iteration of for loop
					continue;
				}
			} else {
				if (overrideTokenMap.containsKey(key)) {
					// override one or more tokens until the override
					// token is complete
					token = (BaseToken) overrideTokenMap.get(key);
					overrideOn = true;
					overrideEndOffset = token.getEndOffset();
				}
			}

			Iterator machineItr = iv_machineSet.iterator();
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
					BaseToken startToken = (BaseToken) tokens
							.get(tokenStartIndex);
					BaseToken endToken = token;
					SuffixStrengthToken segmentToken = new SuffixStrengthToken(
							startToken.getStartOffset(), endToken
									.getEndOffset());
					rangeSet.add(segmentToken);
					fsm.reset();
				}
			}
		}

		// cleanup
		tokenStartMap.clear();

		// reset machines
		Iterator itr = iv_machineSet.iterator();
		while (itr.hasNext()) {
			Machine fsm = (Machine) itr.next();
			fsm.reset();
		}

		return rangeSet;
	}
}
