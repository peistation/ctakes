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
package edu.mayo.bmi.uima.core.sentence;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.sentdetect.EndOfSentenceScanner;

/**
 * End of Sentence scanner with more candidate end-of-sentence
 * characters than the default.
 * @author Mayo Clinic
 */
public class EndOfSentenceScannerImpl implements EndOfSentenceScanner {

    private static final char[] eosCandidates =  {'.', '!', ')', ']', '>', '\'', '\"', ':', ';'};

	public EndOfSentenceScannerImpl() {
        super();
	}

	public char[] getEndOfSentenceCharacters() {
		return eosCandidates;
		
	}
	/**
	 * @see opennlp.tools.sentdetect.EndOfSentenceScanner#getPositions(java.lang.String)
	 */
	public List<Integer> getPositions(String s) {
        return getPositions(s.toCharArray());
	}

	/**
	 * @see opennlp.tools.sentdetect.EndOfSentenceScanner#getPositions(java.lang.StringBuffer)
	 */
	public List<Integer> getPositions(StringBuffer sb) {
        return getPositions(sb.toString().toCharArray());
	}

	/**
	 * @see opennlp.tools.sentdetect.EndOfSentenceScanner#getPositions(char[])
	 */
	public List<Integer> getPositions(char[] cb) {
		List<Integer> positions = new ArrayList<Integer>();

		for (int i=0; i<cb.length; i++) { // for each character in buffer
			for (int j=0; j<eosCandidates.length; j++) { // for each eosCandidate
				if (cb[i]==eosCandidates[j]) { 
					positions.add(new Integer(i)); // TODO - don't always create new, use a pool
					break; // can't match others if it matched eosCandidates[j]
				}
			}
		}
		 
		return positions;
	}

}
