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
package clear.ftr;

/**
 * Feature library.
 * @author Jinho D. Choi
 * <b>Last update:</b> 6/30/2010
 */
public class FtrLib
{
	/** Null feature tag */
	static public final String TAG_NULL   = "#$NULL$#";
	/** Delimiter between joined feature tags */
	static public final String TAG_DELIM  = "_";
	/** Delimiter between joined feature tags for rules */
	static public final String RULE_DELIM = " ";
	
	/** Name of the file containing class labels */
	static public final String FILE_LABEL             = "label.txt";
	/** Name of the file containing forms */
	static public final String FILE_FORM              = "form.txt";
	/** Name of the file containing lemmas */
	static public final String FILE_LEMMA             = "lemma.txt";
	/** Name of the file containing pos-tags */
	static public final String FILE_POS               = "pos.txt";
	/** Name of the file containing dependency labels */
	static public final String FILE_DEPREL            = "deprel.txt";
	/** Name of the file containing "pos_lemma" for a single token */
	static public final String FILE_POS_LEMMA_1GRAM   = "pos_lemma_1gram.txt";
	/** Name of the file containing "pos1_pos2" for token1 and token2 */
	static public final String FILE_POS_POS_2GRAM     = "pos_pos_2gram.txt";
	/** Name of the file containing "pos1_lemma2" for token1 and token2 */
	static public final String FILE_POS_LEMMA_2GRAM   = "pos_lemma_2gram.txt";
	/** Name of the file containing "lemma1_pos2" for token1 and token2 */
	static public final String FILE_LEMMA_POS_2GRAM   = "lemma_pos_2gram.txt";
	/** Name of the file containing "lemma1_lemma2" for token1 and token2 */
	static public final String FILE_LEMMA_LEMMA_2GRAM = "lemma_lemma_2gram.txt";
	/** Name of the file containing "pos1_pos2_pos3" for token[1..3] */
	static public final String FILE_POS_POS_POS_3GRAM = "pos_pos_pos_3gram.txt";
	/** Name of the file containing punctuation */
	static public final String FILE_PUNCTUATION       = "punctuation.txt";
	/** Name of the file containing "pos_pos" rule for token1 and token2 */
	static public final String FILE_POS_POS_DEP_RULE  = "pos_pos_rule.txt";
	
	static public final String FILE_CHUNK_POS         = "chunk_pos.txt";
}


