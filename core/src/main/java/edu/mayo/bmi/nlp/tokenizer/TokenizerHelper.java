/*
 * Copyright: (c) 2011   Mayo Foundation for Medical Education and 
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

package edu.mayo.bmi.nlp.tokenizer;

import java.util.Date;

/**
 * 
 * @author Mayo Clinic
 */
public class TokenizerHelper {

    static final char APOSTROPHE = '\'';
    static final char PERIOD = '.';
    static final char HYPHEN_OR_MINUS_SIGN = '-';
    static final char NEWLINE = '\n';
    static final char CR = '\r';
    static final char COMMA = ',';

    /**
     * @return s.length() or index of nonalphanumeric character
     * Note does NOT return -1 if the rest are all alphanumeric, returns s.length in that case
     * Returns -1 if s == null. returns s.length() if fromIndex is too big
     */
    static public int findNextNonAlphaNum(String s, int fromIndex) {
	if (s==null) throw new IndexOutOfBoundsException("s==null, fromIndex = " + fromIndex);
	for (int i=fromIndex; i<s.length(); i++) {
	    if (!Character.isLetterOrDigit(s.charAt(i))) return i;
	}
	return s.length();
    }

    // returns true if starts with 'tis and either that's all or the next char is not a letter
    static boolean startsWithWithoutBeingFollowedByLetter(String s, String compareTo) {
	if (s.startsWith(compareTo)) {
	    if (s.length()==compareTo.length()) return true;
	    char next = s.charAt(compareTo.length());
	    if (Character.isLetter(next)) return false;
	    return true;
	} 
	return false;
    }

    // Copied isPunctuation from edu.mayo.bmi.nlp.tokenizer.Tokenizer
    static boolean isPunctuation(char c) {
	if ((c == ';') || (c == ':') || (c == ',') || (c == '.') || (c == '(')
		|| (c == ')') || (c == '[') || (c == ']') || (c == '{')
		|| (c == '}') || (c == '<') || (c == '>') || (c == '\'')
		|| (c == '"') || (c == '/') || (c == '\\') || (c == '-')) {
	    return true;
	} else {
	    return false;
	}
    }


    public static void main(String[] args) {

	Exception e = new Exception(); // array[0] = edu.mayo.bmi.nlp.tokenizer.TokenizerHelper.main(TokenizerHelper.java:66)
	StackTraceElement [] array = e.getStackTrace();
	StackTraceElement ste = array[0]; 
	
	System.out.println("Started " + ste.getClassName() + " at " + new Date());
	
	System.out.println(ste.getClassName());	//edu.mayo.bmi.nlp.tokenizer.TokenizerHelper
	System.out.println(ste.getFileName());	// TokenizerHelper.java
	System.out.println(ste.getMethodName());// main

	System.out.println("End " + ste.getClassName() + " at " + new Date());  
	
    }

}
