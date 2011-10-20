package edu.mayo.bmi.fsm.drugner.elements.conditions;

import java.util.Set;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.token.TextToken;

/**
 * Finds numeric values w/in tokens containing mixed text (integers contained in a string). Will return true if the numeric value separated from the text
 * results in a unit value provided in Set 'c'
 * 
 * @author Mayo Clinic
 */
public class ContainsSetTextValueCondition extends Condition {
	private Set iv_TextSet;

	private boolean iv_isCaseSensitive;

	/**
	 * Constructor
	 * 
	 * @param t
	 */
	public ContainsSetTextValueCondition(Set c, boolean casesensative) {
		iv_TextSet = c;
		iv_isCaseSensitive = casesensative;
	}

	/**
	 * Called to check if the conditional meets the criteria defined by this
	 * state.
	 */
	public boolean satisfiedBy(Object conditional) {
		if (conditional instanceof TextToken) {
			TextToken t = (TextToken) conditional;
			String text = t.getText();
			String subText = "";
			boolean containsNums = false;
			boolean doneHere = false;
			int textSize = text.length();
			int pos = 0;
			Integer posInt = null;
			while (!doneHere && (textSize > pos) && (textSize > 1)) {
				try {
					String numString = text.substring(pos, pos + 1);

					Integer posNum = posInt.decode(numString);
					int checkInt = posNum.intValue();

					if ((checkInt >= 0) && (checkInt <= 9)) {
						containsNums = true;
						subText = text.substring(pos + 1, textSize);
						pos++;
					} else
						return false;
				}

				catch (NullPointerException npe) {
					return false;
				} catch (NumberFormatException nfe) {
					if (!containsNums)
						return false;
					else
						doneHere = true;

				}
			}
			if ((subText.length() > 1)
					&& (subText.substring(0, 1).compareTo("-") == 0))
				subText = subText.substring(1);
			if (!iv_isCaseSensitive) {
				subText = subText.toLowerCase();
			}

			if (iv_TextSet.contains(subText)) {

				return true;
			}

		}
		return false;
	}
}
