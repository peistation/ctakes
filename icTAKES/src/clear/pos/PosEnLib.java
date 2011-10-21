/**
* Copyright (c) 2009, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.pos;

import java.util.Arrays;
import java.util.HashSet;

/**
 * English part-of-speech tag library.
 * @author Jinho D. Choi
 * <b>Last update:</b> 4/14/2010
 */
public class PosEnLib
{
	static public String[] PHRAE_POS_ARR = {"S","SBAR","SBARQ","SINV","SQ","ADJP","ADVP","CONJP","FRAG","INTJ","LST","NAC","NP","NX","PP","PRN","PRT","QP","RRC","UCP","VP","WHADJP","WHADVP","WHNP","WHPP","X"};
	static public HashSet<String> PHRASE_POS_SET = new HashSet<String>(Arrays.asList(PHRAE_POS_ARR));
	
	/** Pos-tag of nouns, singular or mass */
	static public String NOUN   = "NN";
	/** Pos-tag of verbs, base form */
	static public String VERB   = "VB";
	/** Pos-tag of adjectives */
	static public String ADJ    = "JJ";
	/** Pos-tag of adverbs */
	static public String ADV    = "RB";
	/** Pos-tag of modals */
	static public String MODAL  = "MD";
	/** Pos-tag of wh-determiners */
	static public String WH_DET = "WDT";
	/** Pos-tag of wh-pronouns */
	static public String WH_PRO = "WP";
	/** Pos-tag of wh-adverbs */
	static public String WH_ADV = "WRB";
	/** Pos-tag of preposition */
	static public String IN     = "IN";
	
	/** @return true if <code>pos</code> is a noun. */
	static public boolean isNoun(String pos)
	{
		return pos.startsWith(NOUN);
	}
	
	/** @return true if <code>pos</code> is a verb. */
	static public boolean isVerb(String pos)
	{
		return pos.startsWith(VERB);
	}
	
	/** @return true if <code>pos</code> is an adjective. */
	static public boolean isAdjective(String pos)
	{
		return pos.startsWith(ADJ);
	}
	
	/** @return true if <code>pos</code> is an adverb. */
	static public boolean isAdverb(String pos)
	{
		return pos.startsWith(ADV);
	}
	
	/** @return true if <code>pos</code> is a modal. */
	static public boolean isModal(String pos)
	{
		return pos.equals(MODAL);
	}
	
	/** @return coarse-grained pos of <code>pos</code>. */
	static public String cpos(String pos)
	{
		return (pos.length() < 2) ? pos : pos.substring(0, 2);
	}
	
	static public boolean isPhrase(String pos)
	{
		return PHRASE_POS_SET.contains(pos);
	}
}
