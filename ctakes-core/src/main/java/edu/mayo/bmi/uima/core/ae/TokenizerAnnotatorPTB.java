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

package edu.mayo.bmi.uima.core.ae;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.nlp.tokenizer.TokenizerPTB;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.syntax.NewlineToken;
import edu.mayo.bmi.uima.core.type.textspan.Segment;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;
import edu.mayo.bmi.uima.core.util.ParamUtil;

/**
 * UIMA annotator that tokenizes based on Penn Treebank rules.
 * 
 * @author Mayo Clinic
 */
public class TokenizerAnnotatorPTB extends JCasAnnotator_ImplBase
{
	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * Value is "SegmentsToSkip".  This parameter specifies which segments to skip.  The parameter should be
	 * of type String, should be multi-valued and optional. 
	 */
	public static final String PARAM_SEGMENTS_TO_SKIP = "SegmentsToSkip";


	private UimaContext context;
	private Set<String> skipSegmentsSet;

	private TokenizerPTB tokenizer;

	private int tokenCount = 0;

	public void initialize(UimaContext aContext) throws ResourceInitializationException {

		super.initialize(aContext);

		logger.info("Initializing " + this.getClass().getName());
		context = aContext;
		try {
			configInit();
		} catch (ResourceAccessException e) {
			throw new ResourceInitializationException(e);
		} finally {};
	}

	/**
	 * Reads configuration parameters.
	 * @throws ResourceAccessException 
	 */
	private void configInit() throws ResourceAccessException {

		skipSegmentsSet = ParamUtil.getStringParameterValuesSet(PARAM_SEGMENTS_TO_SKIP, context); 

		tokenizer = new TokenizerPTB();

	}

	/**
	 * Entry point for processing.
	 */
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		logger.info("process(JCas) in " + this.getClass().getName());

		tokenCount = 0;

		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		FSIterator<Annotation> segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();
		while (segmentItr.hasNext()) {
			Segment sa = (Segment) segmentItr.next();
			String segmentID = sa.getId();
			if (!skipSegmentsSet.contains(segmentID)) { 
				try {
					annotateRange(jcas, sa.getBegin(), sa.getEnd());
				} catch (AnnotatorProcessException e) {
					throw new AnalysisEngineProcessException(e);
				}
			}
		}
	}


	static char CR = '\r';
	static char LF = '\n';

	/**
	 * Tokenizes a range of text, adding the tokens to the CAS
	 * Tokenizes one sentence at a time. Only tokenizes what is within Sentence annotation.
	 * There must have been Sentence annotations created beforehand in order for this method
	 * to tokenize anything.
	 */
	protected void annotateRange(JCas jcas, int rangeBegin, int rangeEnd) throws AnnotatorProcessException {

		// int tokenCount = 0; // can't start with tokenCount=0 here because this method can be called multiple times
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();

		// First look for all newlines and carriage returns (which are not contained within sentences)
		String docText = jcas.getDocumentText();
		for (int i = rangeBegin; i<rangeEnd; i++) {

			if (docText.charAt(i)==CR) {

				NewlineToken nta;
				if (i+1<rangeEnd && docText.charAt(i+1)==LF) {
					// single NewlineToken for the 2 characters
					nta = new NewlineToken(jcas, i, i+2);
					i++; // skip past the LF
				} else {
					nta = new NewlineToken(jcas, i, i+1);
				}
				nta.addToIndexes();

			} else if (docText.charAt(i)==LF) {

				NewlineToken nta = new NewlineToken(jcas, i, i+1);
				nta.addToIndexes();

			}

		}

		// Now process each sentence
		FSIterator sentencesIter = indexes.getAnnotationIndex(Sentence.type).iterator();
		// Tokenize each sentence, adding the tokens to the cas index
		while (sentencesIter.hasNext()) {
			Sentence sentence = (Sentence) sentencesIter.next();
			if (sentence.getBegin() < rangeBegin || sentence.getEnd() > rangeEnd) {
				continue;
			}
			List<BaseToken> tokens = (List<BaseToken>)tokenizer.tokenizeTextSegment(jcas, sentence.getCoveredText(), sentence.getBegin(), true);
			for (BaseToken bta: tokens) {
				if (bta==null) {
					Exception e = new RuntimeException("bta==null tokenCount=" + tokenCount + " tokens.size()==" + tokens.size());
					e.printStackTrace();
				} else{
					//logger.info("Token #" + tokenCount + " len = " + bta.getCoveredText().length() + " " + bta.getCoveredText());
					// add the BaseToken to CAS index
					bta.addToIndexes();
					//tokenCount++;
				}
			}

		}

		// Now add the tokenNumber in the order of offsets
		FSIterator baseTokenIter = indexes.getAnnotationIndex(BaseToken.type).iterator();
		while (baseTokenIter.hasNext()) {
			BaseToken bta = (BaseToken) baseTokenIter.next();
			if (bta.getBegin()>=rangeBegin && bta.getBegin()<rangeEnd) {
				bta.setTokenNumber(tokenCount);
				tokenCount++;
			}
		}

	}
}	


// pseudo code:
// ** find first non white space or first newline. this starts token 1
// if eof before next newline or next nonwhite space, done.
// if nothing but whitespace before the newline, create NewlineToken tokenNumber=0 for first
// repeat until find non white space. this starts the first BaseToken that is
// not a NewlineToken and it will have tokenNumber = (#NewlineTokens + 0)

// once found start of a token (other than NewlineToken) process as follows to find end of token:
//  if char 2 is a whitespace or no char 2 (eof), then token len = 1, and  go back to looking for next token for nonwhitespace (**)
//  if 1st char of token is "." (period) could be a number or an ellipsis CALL startsWithPeriod
//  if 1st char of token is "'" (apostrophe) could be the start of a name CALL startsWithApostrophe
//  if 1st char of token is dash/minus sign, could be a number, CALL startsWithMinusSign
//  if 1st char of token is any other punctuation, it is a one-char token.  go back to looking for nonwhitespace (**)
//  if 1st char of token is alphanum, then follow these rules
//     stop when hit a whitespace or EOF
//     include any alphanum if just alphanums
//     if a hyphen/dash/minus
//		-- if part of hyphen list, 
//			then don't stop ****
//			else stop, found end of token (and start of next token)
//				Can't be a minus sign since wasn't first character.
//				and if it's a dash, should be a separate token
//				so if wasn't part of hyphen list, stop, found end of token
//     include comma only part of a number (if all others are digits or commas or a single period)
//     include period if
//		--- part of abbreviation
//		--- part of a number (if all others are digits or commas or a single period)



// From http://www.seas.upenn.edu/~jmott/2009_addendum.pdf
// modified by Task1.4.4_adoptedConventions[AL]_Feb28_2011.doc

//All strings separated by white space are treated as separate tokens. 

//Also, no token can contain white space. 

//Most hyphenated words are split into multiple tokens.
//Hyphenated interjections and affixes in the following list are not split into multiple tokens.

//All other punctuation not described above triggers a break in tokenization, with the ex-
//ceptions outlined below.
//Note that for present purposes, all non-alphanumeric characters
//are considered `punctuation'.

//The tokenization of punctuation in webtext is deter-
//mined by whitespace boundaries.



// The following is the list of punctuation exceptions that do not cause end of token
//Periods marking abbreviations.
//Punctuation in web addresses.
// - URLs or email address
//Ellipses, when encoded as a string of periods.
//Complex numerals.
//Telephone numbers and postal codes.
//Single quotation marks as parts of names.



//    Hyphenated interjections and affixes in the following list are not split into multiple tokens.
//    For example, uh-oh and e-mail are both single tokens: uh-oh, e-mail.
//    e-
//    a-
//    u-
//    x-
//    agro-
//    ante-
//    anti-
//    arch-
//    be-
//    bi-
//    bio-
//    co-
//    counter-
//    cross-
//    cyber-
//    de-
//    eco-
//    -esque
//    -ette
//    ex-
//    extra-
//    -fest
//    -fold
//    -gate
//    inter-
//    intra-
//    -itis
//    -less
//    macro-
//    mega-
//    micro-
//    4
//    mid-
//    mini-
//    mm-hm
//    mm-mm
//    -most
//    multi-
//    neo-
//    non-
//    o-kay
//    -o-torium
//    over-
//    pan-
//    para-
//    peri-
//    post-
//    pre-
//    pro-
//    pseudo-
//    quasi-
//    -rama
//    re-
//    semi-
//    sub-
//    super-
//    tri-
//    uh-huh
//    uh-oh
//    ultra-
//    un-
//    uni-
//    vice-
//    -wise

