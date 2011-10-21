package edu.mayo.bmi.fsm.pad.machine;

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
import edu.mayo.bmi.fsm.condition.TextSetCondition;
import edu.mayo.bmi.fsm.token.BaseToken;
import edu.mayo.bmi.fsm.state.NamedState;
import edu.mayo.bmi.fsm.state.NonTerminalEndState;
import edu.mayo.bmi.fsm.pad.output.IllnessDxIndicator;


/**
 * Uses one or more finite state machines to detect ???
 * 
 * @author Mayo Clinic
 */
public class DxIndicatorFSM
{
    private Set<String>iv_breastDxSet = new HashSet<String>();
    private Set<String>iv_brainDxSet = new HashSet<String>();
    private Set<String>iv_colonDxSet = new HashSet<String>();
    private Set<String>iv_padDxSet = new HashSet<String>();
    private Set<String>iv_padDxHyphenBeginSet = new HashSet<String>();
    private Set<String>iv_padDxHyphenEndSet = new HashSet<String>();
    private Set<String>iv_padOneOfTwoDxSet = new HashSet<String>();
    private Set<String>iv_padTwoOfTwoDxSet = new HashSet<String>();
    private Set<String>iv_padOneOfThreeDxSet = new HashSet<String>();
    private Set<String>iv_padTwoOfThreeDxSet = new HashSet<String>();
    private Set<String>iv_padThreeOfThreeDxSet = new HashSet<String>();
    
    private Set<String>iv_padOneOfTwoNegConfirmDxSet = new HashSet<String>();
    private Set<String>iv_padTwoOfTwoNegConfirmDxSet = new HashSet<String>();
    
    // contains the finite state machines
    private Machine iv_breastDxMachine;
    private Machine iv_brainDxMachine;
    private Machine iv_colonDxMachine;
    private Machine iv_padDxMachine;
    private Set<Machine>iv_machineSet = new HashSet<Machine>();

    /**
     * 
     * Constructor
     *  
     */
    public DxIndicatorFSM()
    {
        iv_breastDxSet.add("breast");
        iv_breastDxSet.add("left-breast");
        iv_breastDxSet.add("right-breast");
        
        iv_brainDxSet.add("brain");
        
        iv_colonDxSet.add("colon");
        
        iv_padDxSet.add("stent");
        iv_padDxSet.add("stents");
        iv_padDxSet.add("stented");
        iv_padDxSet.add("stenting");
        iv_padDxSet.add("aortobifemoral");
        iv_padDxSet.add("angioplasty");
        iv_padDxSet.add("femoropopliteal");
        iv_padDxSet.add("anastomotic");
        iv_padDxSet.add("ileofemoral");
        iv_padDxSet.add("axillobifemoral");
        iv_padDxSet.add("aortobiiliac");
        iv_padDxSet.add("reconstitution");
        iv_padDxSet.add("reconstitute");
        iv_padDxSet.add("reconstitutes");
        iv_padDxSet.add("graft");
        iv_padDxSet.add("occluded");
        iv_padDxSet.add("occlude");
        iv_padDxSet.add("occludes");
        iv_padDxSet.add("occluding");
        iv_padDxSet.add("atherosclerosis");
        iv_padDxSet.add("collateral");
        iv_padDxSet.add("collaterals");
        iv_padDxSet.add("diffuse");
        iv_padDxSet.add("extensive");
        iv_padDxSet.add("plaque");
        iv_padDxSet.add("plaques");
        
        iv_padDxHyphenBeginSet.add("aortobi");
        
        iv_padDxHyphenEndSet.add("femoral");
        iv_padDxHyphenEndSet.add("iliac");
        
        iv_padOneOfTwoNegConfirmDxSet.add("not");
        
        iv_padTwoOfTwoNegConfirmDxSet.add("visualized");
        
        iv_padOneOfTwoDxSet.add("artery");
        iv_padOneOfTwoDxSet.add("bypass");
        iv_padOneOfTwoDxSet.add("outflow");
        iv_padOneOfTwoDxSet.add("critical");
        iv_padOneOfTwoDxSet.add("balloon");
        iv_padOneOfTwoDxSet.add("moderate");
        iv_padOneOfTwoDxSet.add("aortobifemoral");
        iv_padOneOfTwoDxSet.add("multi-focal");
        iv_padOneOfTwoDxSet.add("focal");
        iv_padOneOfTwoDxSet.add("vascular");
        iv_padOneOfTwoDxSet.add("arterial");
       
        iv_padTwoOfTwoDxSet.add("calcification");
        iv_padTwoOfTwoDxSet.add("calcifications");
        iv_padTwoOfTwoDxSet.add("narrowing");
        iv_padTwoOfTwoDxSet.add("graft");
        iv_padTwoOfTwoDxSet.add("grafts");
        iv_padTwoOfTwoDxSet.add("grafting");
        iv_padTwoOfTwoDxSet.add("artery");
        iv_padTwoOfTwoDxSet.add("bypass");
        iv_padTwoOfTwoDxSet.add("obstruction");
        iv_padTwoOfTwoDxSet.add("lesions");
        iv_padTwoOfTwoDxSet.add("angioplastied");
        iv_padTwoOfTwoDxSet.add("atherosclerotic");
        iv_padTwoOfTwoDxSet.add("stenoses");// Misspelling
        iv_padTwoOfTwoDxSet.add("plaque");
        iv_padTwoOfTwoDxSet.add("plaques");
        iv_padTwoOfTwoDxSet.add("stenosis");
        
       
        iv_padOneOfThreeDxSet.add("femoral");
        iv_padOneOfThreeDxSet.add("artery");
        iv_padOneOfThreeDxSet.add("aorto");
        iv_padOneOfThreeDxSet.add("possible");
        iv_padOneOfThreeDxSet.add("mild");
        iv_padOneOfThreeDxSet.add("lower");
        iv_padOneOfThreeDxSet.add("iliac");
        iv_padOneOfThreeDxSet.add("moderate");
        
        
        iv_padTwoOfThreeDxSet.add("bypass");
        iv_padTwoOfThreeDxSet.add("bifemoral");
        iv_padTwoOfThreeDxSet.add("bi-iliac");
        iv_padTwoOfThreeDxSet.add("stents");
        iv_padTwoOfThreeDxSet.add("outflow");
        iv_padTwoOfThreeDxSet.add("focal");
        iv_padTwoOfThreeDxSet.add("to");
        iv_padTwoOfThreeDxSet.add("artery");
        iv_padTwoOfThreeDxSet.add("aorta");
        iv_padTwoOfThreeDxSet.add("atheromatous");
        
        iv_padThreeOfThreeDxSet.add("graft");
        iv_padThreeOfThreeDxSet.add("grafts");
        iv_padThreeOfThreeDxSet.add("stents");
        iv_padThreeOfThreeDxSet.add("obstruction");
        iv_padThreeOfThreeDxSet.add("narrowing");
        iv_padThreeOfThreeDxSet.add("plaque");
        iv_padThreeOfThreeDxSet.add("moderate");
        
        iv_brainDxMachine = getBrainDxMachine();
        iv_breastDxMachine = getBreastDxMachine();
        iv_colonDxMachine = getColonDxMachine();
        iv_padDxMachine = getPADDxMachine();
        iv_machineSet.add(iv_brainDxMachine);
        iv_machineSet.add(iv_breastDxMachine);
        iv_machineSet.add(iv_colonDxMachine);
        iv_machineSet.add(iv_padDxMachine);
    }

    /**
     * Gets a finite state machine that detects the following:
     * <ol>
     * <li>breast</li>
     * <li>left-breast</li>
     * <li>right-breast</li>
     * </ol>
     * 
     * @return
     */
    private Machine getBreastDxMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");

        endState.setEndStateFlag(true);

        Machine m = new Machine(startState);

        Condition breastDxC = new TextSetCondition(iv_breastDxSet, false);

        startState.addTransition(breastDxC, endState);
        startState.addTransition(new AnyCondition(), startState);

        endState.addTransition(new AnyCondition(), startState);

        return m;
    }

    /**
     * Gets a finite state machine that detects the following:
     * <ol>
     * <li>brain</li>
     * </ol>
     * 
     * @return
     */
    private Machine getBrainDxMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");


   
        endState.setEndStateFlag(true);

        Machine m = new Machine(startState);

        Condition brainDxC = new TextSetCondition(iv_brainDxSet, false);

        startState.addTransition(brainDxC, endState);
        startState.addTransition(new AnyCondition(), startState);

        endState.addTransition(new AnyCondition(), startState);

        return m;
    }

    /**
     * Gets a finite state machine that detects the following:
     * <ol>
     * <li>colon</li>
     * </ol>
     * 
     * @return
     */
    private Machine getColonDxMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");

        endState.setEndStateFlag(true);

        Machine m = new Machine(startState);

        Condition colonDxC = new TextSetCondition(
                iv_colonDxSet,
                false);

        startState.addTransition(colonDxC, endState);
        startState.addTransition(new AnyCondition(), startState);


        endState.addTransition(new AnyCondition(), startState);

        return m;
    }
    /**
     * Gets a finite state machine that detects the following:
     * <ol>
     * <li>occluded</li>
     * <li>moderate narrowing</li>
     * <li>extensive collatoral formation</li>
     * </ol>
     * 
     * @return
     */
    private Machine getPADDxMachine()
    {
        State startState = new NamedState("START");
        State endState = new NamedState("END");
        State padHyphenBeginState = new NamedState("HYPHENB");
        State padHyphenEndState = new NamedState("HYPHENE");
        State pad2of2NegConfirmState = new NamedState("TWOOFTWONEG");
        State pad2of2State = new NamedState("TWOOFTWO");
        State pad2of3State = new NamedState("TWOOFTHREE");
        State pad3of3State = new NamedState("THREEOFTHREE");
        
        endState.setEndStateFlag(true);

        Machine m = new Machine(startState);

        Condition padDxC = new TextSetCondition(iv_padDxSet, false);
        Condition padDxHyphenBeginC = new TextSetCondition(iv_padDxHyphenBeginSet, false);
        Condition padDxHyphenEndC = new TextSetCondition(iv_padDxHyphenEndSet, false);
        Condition pad1of2DxC = new TextSetCondition(iv_padOneOfTwoDxSet, false);
        Condition pad2of2DxC = new TextSetCondition(iv_padTwoOfTwoDxSet, false);
        Condition pad1of3DxC = new TextSetCondition(iv_padOneOfThreeDxSet, false);
        Condition pad2of3DxC = new TextSetCondition(iv_padTwoOfThreeDxSet, false);
        Condition pad3of3DxC = new TextSetCondition(iv_padThreeOfThreeDxSet, false);

        Condition padNegConfirm1Of2DxC = new TextSetCondition(iv_padOneOfTwoNegConfirmDxSet, false);
        Condition padNegConfirm2Of2DxC = new TextSetCondition(iv_padTwoOfTwoNegConfirmDxSet, false);
        
        startState.addTransition(padDxC, endState);
        startState.addTransition(padNegConfirm1Of2DxC, pad2of2NegConfirmState);
        startState.addTransition(pad1of2DxC, pad2of2State);
        startState.addTransition(pad1of3DxC, pad2of3State);
        startState.addTransition(padDxHyphenBeginC, padHyphenBeginState);
        startState.addTransition(new AnyCondition(), startState);

        pad2of2NegConfirmState.addTransition(padNegConfirm2Of2DxC, endState);
        pad2of2NegConfirmState.addTransition(new AnyCondition(), startState);
        
        pad2of2State.addTransition(pad2of2DxC, endState);
        pad2of2State.addTransition(new AnyCondition(), startState);
        
        pad2of3State.addTransition(pad2of3DxC, pad3of3State);
        pad2of3State.addTransition(new AnyCondition(), startState);
        
        pad3of3State.addTransition(pad3of3DxC, endState);
        pad3of3State.addTransition(new AnyCondition(), startState);
        
        padHyphenBeginState.addTransition(new PunctuationValueCondition('-'), padHyphenEndState);
        padHyphenBeginState.addTransition(new AnyCondition(), startState);
        
        padHyphenEndState.addTransition(padDxHyphenEndC, endState);
        padHyphenEndState.addTransition(new AnyCondition(), startState);
        
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
    public Set<IllnessDxIndicator> execute(List<? extends BaseToken> tokens) throws Exception
    {
        Set <IllnessDxIndicator>outSet = new HashSet<IllnessDxIndicator>();

        // maps a fsm to a token start index
        // key = Machine , value = token start index
        Map <Machine, Integer>tokenStartMap = new HashMap<Machine, Integer>();

        for (int i = 0; i < tokens.size(); i++)
        {
            BaseToken token = (BaseToken) tokens.get(i);

            Iterator<Machine>machineItr = iv_machineSet.iterator();
            while (machineItr.hasNext())
            {
                Machine m = (Machine) machineItr.next();

                m.input(token);

                State currentState = m.getCurrentState();
                if (currentState.getStartStateFlag())
                {
                    tokenStartMap.put(m, new Integer(i));
                }
                if (currentState.getEndStateFlag())
                {
                    Object o = tokenStartMap.get(m);
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
                    IllnessDxIndicator si = null;
                    if (m.equals(iv_brainDxMachine))
                    {
                        si = new IllnessDxIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset(),
                                IllnessDxIndicator.BRAIN_STATUS);
                    }
                    else if (m.equals(iv_breastDxMachine))
                    {
                        si = new IllnessDxIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset(),
                                IllnessDxIndicator.BREAST_STATUS);
                    }
                    else if (m.equals(iv_colonDxMachine))
                    {
                        si = new IllnessDxIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset(),
                                IllnessDxIndicator.COLON_STATUS);
                    }
                    else if (m.equals(iv_padDxMachine))
                    {
                        si = new IllnessDxIndicator(
                                startToken.getStartOffset(),
                                endToken.getEndOffset(),
                                IllnessDxIndicator.PAD_STATUS);
                    }
                    outSet.add(si);
                    m.reset();
                }
            }
        }

 

        // cleanup
        tokenStartMap.clear();

        // reset machines
        Iterator<Machine>itr = iv_machineSet.iterator();
        while (itr.hasNext())
        {
            Machine m = (Machine) itr.next();
            m.reset();
        }

        return outSet;
    }
}