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
package edu.mayo.bmi.fsm.token.adapter;

import edu.mayo.bmi.fsm.token.NumberToken;
import edu.mayo.bmi.nlp.tokenizer.Token;

public class NumberTokenAdapter extends BaseTokenAdapter implements NumberToken {
	private boolean iv_isPositive = true;

	public NumberTokenAdapter(Token tok) {
		super(tok);
		if (tok.getText().charAt(0) == '-') {
			iv_isPositive = false;
		}
	}

	public boolean getPositive() {
		return iv_isPositive;
	}

	protected String removeCommas(String str) {
		StringBuffer sb = new StringBuffer(str);
		for (int i = 0; i < sb.length(); i++) {
			char currentChar = sb.charAt(i);
			if (currentChar == ',') {
				sb.deleteCharAt(i);
				i--;
			}
		}
		return sb.toString();
	}
}
