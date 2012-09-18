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
/**
 * 
 */
package edu.mayo.bmi.uima.core.ci.test;

import java.util.ArrayList;

import org.junit.Test;

import edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl;

/**
 * @author Mayo Clinic
 *
 */
public class HyphenTextModifierImplTests {

	/**
	 * Test method for {@link edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl#HyphenTextModifierImpl(java.lang.String, int)}.
	 */
	//	@Test
	//	public void testHyphenTextModifierImpl() {
	//		fail("Not yet implemented"); // TODO
	//	}

	/**
	 * Test method for {@link edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl#modify(java.lang.String)}.
	 */
	//	@Test
	//	public void testModify() {
	//		fail("Not yet implemented"); // TODO
	//	}

	/**
	 * Test method for {@link edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl#test(edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl, java.lang.String)}.
	 * Performs two tests - one that has two blanks between some words that could be hyphenated. Since there are two blanks,
	 * the text is not modified (the blanks are not replaced by hyphens).
	 * For the second test, the appropriate blanks are replaced by hyphens. 
	 */
	@Test
	public void testTest() {

		// The messages about "UNSUPPORTED" go to stderr, and are not returned by test() 
		// Same for "New:  (new text not generated, see previous messages)"
		String [] expectedMessages = {
				// "UNSUPPORTED: TextModification with offset changes.",
				// "UNSUPPORTED: TextModification with offset changes.",
				// "UNSUPPORTED: TextModification with offset changes.",
				"Orig: Non  Hodgkin's the x  ray without any non small  cell complications.",
				// "New:  (new text not generated, see previous messages)",
				"Non-Hodgkin Orig: 0-12 New: 0-11",
				"x-ray Orig: 19-25 New: 18-23",
				"non-small-cell Orig: 38-53 New: 36-50",
				
				"Orig: Non Hodgkin's the x ray without any non small cell complications.",
				"New:  Non-Hodgkin's the x-ray without any non-small-cell complications.",
				"Non-Hodgkin Orig: 0-11 New: 0-11",
				"x-ray Orig: 18-23 New: 18-23",
				"non-small-cell Orig: 36-50 New: 36-50",
		};
		String [] expectedStderr = {
				"UNSUPPORTED: TextModification with offset changes.",
				"UNSUPPORTED: TextModification with offset changes.",
				"UNSUPPORTED: TextModification with offset changes.",
				"New:  (new text not generated, see previous messages)",
		};
		String [] receivedMessages = new String[expectedMessages.length];

		int errCount = expectedStderr.length;
		System.out.println("OK to see the following " + errCount + " had been written to stderr:");
		for (String s: expectedStderr) {
			System.out.println(s);
		}
		
		String filename = "resources/tokenizer/hyphenated.txt";
		HyphenTextModifierImpl tm = new HyphenTextModifierImpl(filename, 7);
		ArrayList<String> messages;
		String t = "Non  Hodgkin's the x  ray without any non small  cell complications.";
		messages = HyphenTextModifierImpl.test(tm, t); // extra blanks
		assert(messages.toArray(receivedMessages).equals(expectedMessages));
		t = t.replace("  ", " "); // change text to only have single blanks between words
		// t = "Non Hodgkin's the x ray without any non small cell complications.";
		messages = HyphenTextModifierImpl.test(tm, t); // single blanks

	}

	/**
	 * Test method for {@link edu.mayo.bmi.uima.core.ci.HyphenTextModifierImpl#main(java.lang.String[])}.
	 */
	//	@Test
	//	public void testMain() {
	//		fail("Not yet implemented"); // TODO
	//	}

}
