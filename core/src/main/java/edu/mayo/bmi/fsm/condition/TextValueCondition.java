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
package edu.mayo.bmi.fsm.condition;

import net.openai.util.fsm.Condition;
import edu.mayo.bmi.fsm.token.TextToken;

/**
 * 
 * @author Mayo Clinic
 */
@SuppressWarnings("serial")
public class TextValueCondition extends Condition {
	private String iv_word;
	private boolean iv_isCaseSensitive;

	/**
	 * Constructor
	 * 
	 * @param t
	 */
	public TextValueCondition(String value, boolean isCaseSensitive) {
		iv_word = value;
	}

	/**
	 * Called to check if the conditional meets the criteria defined by this
	 * state.
	 */
	public boolean satisfiedBy(Object conditional) {
		if (conditional instanceof TextToken) {
			TextToken t = (TextToken) conditional;
			if (isEqual(t.getText(), iv_word)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param text1
	 * @param text2
	 * @return
	 */
	private boolean isEqual(String text1, String text2) {
		if (iv_isCaseSensitive) {
			return text1.equals(text2);
		} else {
			return text1.equalsIgnoreCase(text2);
		}
	}
}
