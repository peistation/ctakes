/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.mayo.bmi.uima.termspotter.ae;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.apache.log4j.Logger;

//import uima.tt.TokenAnnotation;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.fsm.pad.machine.SubSectionPadIdFSM;
import edu.mayo.bmi.fsm.pad.output.SubSectionIndicator;
import edu.mayo.bmi.uima.core.ae.TokenizerAnnotator;
import edu.mayo.bmi.uima.core.fsm.adapters.ContractionTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.DecimalTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.IntegerTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.NewlineTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.PunctuationTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.SymbolTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.WordTokenAdapter;
import edu.mayo.bmi.fsm.token.BaseToken;
import edu.mayo.bmi.uima.core.resource.FileResource;
import org.apache.ctakes.typesystem.type.syntax.ContractionToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import edu.mayo.bmi.uima.core.util.FSUtil;
import edu.mayo.bmi.uima.pad.type.SubSection;

public class SubSectionAnnotator extends JCasAnnotator_ImplBase {
	public static Logger iv_logger = Logger
			.getLogger(SubSectionAnnotator.class);

	private SubSectionPadIdFSM iv_subMedSectionFSM;

	public void initialize(UimaContext aCtx)
			throws ResourceInitializationException {
		super.initialize(aCtx);
		iv_subMedSectionFSM = new SubSectionPadIdFSM();
		FileResource resrcExamTitle;
		try {
			resrcExamTitle = (FileResource) aCtx
					.getResourceObject(EXAM_TITLE_WORDS_FILE);
			File examTitleWordsFile = resrcExamTitle.getFile();
			loadExamTitleWords(examTitleWordsFile);
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	public void process(JCas jcas) throws AnalysisEngineProcessException {
		try {
			createSubSections(jcas);
		}

		catch (CASException ce) {
			throw new AnalysisEngineProcessException(ce);
		} catch (IllegalAccessException iae) {
			throw new AnalysisEngineProcessException(iae);
		} catch (NoSuchFieldException nsfe) {
			throw new AnalysisEngineProcessException(nsfe);
		} catch (ClassNotFoundException cnfe) {
			throw new AnalysisEngineProcessException(cnfe);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void createSubSections(JCas jcas) throws Exception {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator<?> subSectItr = indexes.getAnnotationIndex(
				org.apache.ctakes.typesystem.type.syntax.BaseToken.type).iterator();
		List<BaseToken> baseTokenList = new ArrayList<BaseToken>();
		while (subSectItr.hasNext()) {
			org.apache.ctakes.typesystem.type.syntax.BaseToken bta = (org.apache.ctakes.typesystem.type.syntax.BaseToken) subSectItr
					.next();
			baseTokenList.add(adaptToBaseToken(bta));
		}
		prepareSubSection(jcas, indexes,
				iv_subMedSectionFSM.execute(baseTokenList));
		boolean CTTypeExam = findSubSectionExamTitle(jcas);
	}

	/**
	 * Given the set of subsections to parse (via SubSectionIndicatorFSM)
	 * determine the offsets between adjacent subsections, end of major sections
	 * (e.g. 20104) or end of document
	 * 
	 * @param jcas
	 * @param indexes
	 * @param subSectionTokenSet
	 * @throws Exception
	 */
	private void prepareSubSection(JCas jcas, JFSIndexRepository indexes,
			Set<?> subSectionTokenSet) throws Exception {

		Iterator<Object> subSectionTokenItr = sortSubSectionItr(
				subSectionTokenSet.toArray()).iterator();

		Iterator<Object> subSectionMatchTokenItr = sortSubSectionItr(
				subSectionTokenSet.toArray()).iterator();

		// move to the next instance of the subSection entries
		if (subSectionMatchTokenItr.hasNext())
			subSectionMatchTokenItr.next();

		while (subSectionTokenItr.hasNext()) {
			boolean correctOrder = false;

			SubSectionIndicator subsectionNext = null;
			SubSectionIndicator subsectionThis = (SubSectionIndicator) subSectionTokenItr
					.next();

			while (subSectionMatchTokenItr.hasNext() && !correctOrder) {
				subsectionNext = (SubSectionIndicator) subSectionMatchTokenItr
						.next();
				if (subsectionThis.getStartOffset() < subsectionNext
						.getStartOffset()) {
					correctOrder = true;
				}
			}

			SubSection subsectionAnnotation = new SubSection(jcas,
					subsectionThis.getStartOffset(),
					subsectionThis.getEndOffset());
			// Always set the beginning of the subsection at the next position
			// of the subsectionAnnotation
			subsectionAnnotation.setSubSectionBodyBegin(subsectionAnnotation
					.getEnd() + 1);

			boolean foundEnd = false;
			Set segmentSet = new HashSet();
			Iterator<?> segmentItr = indexes.getAnnotationIndex(Segment.type)
					.iterator();
			while (segmentItr.hasNext()) {
				segmentSet.add(segmentItr.next());
			}
			Iterator<?> segItr = segmentSet.iterator();
			// While there are segments
			while (segItr.hasNext()) {

				Segment segmentAnnotation = (Segment) segItr.next();
				if (segmentAnnotation.getBegin() <= subsectionThis
						.getStartOffset()
						&& segmentAnnotation.getEnd() >= subsectionThis
								.getStartOffset()) {
					// Look at each sentence within the current segment
					Iterator<?> sentenceItr = FSUtil
							.getAnnotationsInSpanIterator(jcas, Sentence.type,
									segmentAnnotation.getBegin(),
									segmentAnnotation.getEnd());
					Sentence sentenceAnnotation = null;
					// while there are still sentences and current subsection
					// end isn't found
					while (sentenceItr.hasNext() && !foundEnd) {
						sentenceAnnotation = (Sentence) sentenceItr.next();
						if (!foundEnd && (subsectionNext != null)) {
							// correct order?
							if (subsectionNext.getStartOffset() > subsectionThis
									.getStartOffset()) {

								// If the starting offset of the next subsection
								// is
								// greater than or equal to the end of the
								// current sentence
								// (and the end of this sentence is greater than
								// the end of
								// the current subsectionAnnotation) and the
								// beginning of the
								// current sentence is not equal to the current
								// subsection end.
								if (subsectionNext.getStartOffset() >= sentenceAnnotation
										.getEnd()
										&& (sentenceAnnotation.getEnd() > subsectionAnnotation
												.getEnd())
										&& (sentenceAnnotation.getBegin() != subsectionThis
												.getStartOffset())) {
									int newLineCount = 0;
									Iterator<?> baseItr = FSUtil
											.getAnnotationsInSpanIterator(
													jcas,
													org.apache.ctakes.typesystem.type.syntax.BaseToken.type,
													sentenceAnnotation.getEnd(),
													subsectionNext
															.getStartOffset() - 1);
									while (baseItr.hasNext()) {
										org.apache.ctakes.typesystem.type.syntax.BaseToken checkToken = (org.apache.ctakes.typesystem.type.syntax.BaseToken) baseItr
												.next();
										if ((checkToken instanceof NewlineToken)
												|| (checkToken instanceof SymbolToken))
											newLineCount++;

									} // After taking in account the symbols and
										// new line characters see
										// if the ending of this sentence is
										// adjacent to the next subsection
										// then we found section end
									if ((sentenceAnnotation.getEnd() + 1 + newLineCount) == subsectionNext
											.getStartOffset()
											|| sentenceAnnotation.getEnd()
													+ newLineCount == subsectionNext
													.getStartOffset()) {

										foundEnd = true;
										// set the end of the current subsection
										// to sentence end and status
										subsectionAnnotation
												.setSubSectionBodyEnd(sentenceAnnotation
														.getEnd());
										subsectionAnnotation
												.setStatus(subsectionThis
														.getStatus());
										// test new window code
										subsectionAnnotation
												.setSubSectionHeaderBegin(subsectionAnnotation
														.getBegin());
										subsectionAnnotation
												.setSubSectionHeaderEnd(subsectionAnnotation
														.getEnd());
										subsectionAnnotation
												.setEnd(subsectionAnnotation
														.getSubSectionBodyEnd());
										subsectionAnnotation
												.setParentSectionId(segmentAnnotation
														.getId());
									}
									// The next subsection end (or the next
									// subsection plus one) is equal to the
									// current
									// sentence end then we found section end

								} else if (subsectionNext.getEndOffset() == sentenceAnnotation
										.getEnd()
										|| subsectionNext.getEndOffset() < sentenceAnnotation
												.getEnd()) {
									foundEnd = true;
									subsectionAnnotation
											.setSubSectionBodyEnd(subsectionNext
													.getStartOffset());
									subsectionAnnotation
											.setStatus(subsectionThis
													.getStatus());
									// test new window code
									subsectionAnnotation
											.setSubSectionHeaderBegin(subsectionAnnotation
													.getBegin());
									subsectionAnnotation
											.setSubSectionHeaderEnd(subsectionAnnotation
													.getEnd());
									subsectionAnnotation
											.setEnd(subsectionAnnotation
													.getSubSectionBodyEnd());
									subsectionAnnotation
											.setParentSectionId(segmentAnnotation
													.getId());
								}
								// if the current sentence ends at or before the
								// start of the current subsection
								// and the current sentence ends after the next
								// subsection ends (incorrect order?)
							} else if (subsectionThis.getStartOffset() <= sentenceAnnotation
									.getEnd()
									&& (sentenceAnnotation.getEnd() > subsectionNext
											.getEndOffset()))
								if ((sentenceAnnotation.getEnd() + 1) == subsectionThis
										.getStartOffset()
										|| sentenceAnnotation.getEnd() == subsectionThis
												.getStartOffset()) {
									foundEnd = true;
									subsectionAnnotation
											.setSubSectionBodyEnd(sentenceAnnotation
													.getEnd());
									subsectionAnnotation
											.setStatus(subsectionThis
													.getStatus());
									// test new window code
									subsectionAnnotation
											.setSubSectionHeaderBegin(subsectionAnnotation
													.getBegin());
									subsectionAnnotation
											.setSubSectionHeaderEnd(subsectionAnnotation
													.getEnd());
									subsectionAnnotation
											.setEnd(subsectionAnnotation
													.getSubSectionBodyEnd());
									subsectionAnnotation
											.setParentSectionId(segmentAnnotation
													.getId());
								}
							// No next subsection then
							// if there is a current subsection and end hasn't
							// been found and the subsection begin
							// is before the beginning of the current sentence,
							// but at or after the section beginning
							// and before the section ending then found end
						} else if ((subsectionThis != null)
								&& (!foundEnd)
								&& (subsectionThis.getStartOffset() < sentenceAnnotation
										.getBegin())
								&& (subsectionThis.getStartOffset() >= segmentAnnotation
										.getBegin())
								&& subsectionThis.getStartOffset() <= segmentAnnotation
										.getEnd()) {
							foundEnd = true;
							subsectionAnnotation
									.setSubSectionBodyEnd(sentenceAnnotation
											.getEnd());
							subsectionAnnotation.setStatus(subsectionThis
									.getStatus());
							// test new window code
							subsectionAnnotation
									.setSubSectionHeaderBegin(subsectionAnnotation
											.getBegin());
							subsectionAnnotation
									.setSubSectionHeaderEnd(subsectionAnnotation
											.getEnd());
							subsectionAnnotation.setEnd(subsectionAnnotation
									.getSubSectionBodyEnd());
							subsectionAnnotation
									.setParentSectionId(segmentAnnotation
											.getId());
							// No next subsection then
							// if there is a current subsection and end hasn't
							// been found and
							// the end of the subsection heading is equal to the
							// end of the sentence (weird case)
						} else if ((subsectionThis != null)
								&& (!foundEnd)
								&& (subsectionThis.getEndOffset() == sentenceAnnotation
										.getEnd())) {
							foundEnd = true;
							subsectionAnnotation
									.setSubSectionBodyEnd(sentenceAnnotation
											.getEnd());
							subsectionAnnotation.setStatus(subsectionThis
									.getStatus());
							// test new window code
							subsectionAnnotation
									.setSubSectionHeaderBegin(subsectionAnnotation
											.getBegin());
							subsectionAnnotation
									.setSubSectionHeaderEnd(subsectionAnnotation
											.getEnd());
							subsectionAnnotation.setEnd(subsectionAnnotation
									.getSubSectionBodyEnd());
							subsectionAnnotation
									.setParentSectionId(segmentAnnotation
											.getId());
						}

					}

				}
				if (foundEnd)
					subsectionAnnotation.addToIndexes();

			}

		}
		Iterator<?> segmentItrAgain = indexes.getAnnotationIndex(Segment.type)
				.iterator();
		while (segmentItrAgain.hasNext()) {
			Segment segmentScope = (Segment) segmentItrAgain.next();
			Iterator<?> subSectionItr = FSUtil.getAnnotationsInSpanIterator(
					jcas, SubSection.type, segmentScope.getBegin(),
					segmentScope.getEnd());
			while (subSectionItr.hasNext()) {
				SubSection checkSubsection = (SubSection) subSectionItr.next();
				// if the current segment is not one of the medical sections of
				// interest then only capture one sentence as the span of the
				// subsection when the sentence end
				// is greater then the subsection annotation end and only if the
				// subsection and sentence are w/in the section
				// boundaries
				if (/*
					 * !iv_medicalSections.contains(segmentScope.getId()) &&
					 */(segmentScope.getEnd() < checkSubsection
						.getSubSectionBodyEnd() || checkSubsection.getBegin() < segmentScope
						.getBegin())) {
					Iterator<?> sentenceSubSection = FSUtil
							.getAnnotationsInSpanIterator(jcas, Sentence.type,
									segmentScope.getBegin(),
									segmentScope.getEnd());
					boolean foundModifiedEnd = false;
					while (sentenceSubSection.hasNext() && !foundModifiedEnd) {
						Sentence checkSentence = (Sentence) sentenceSubSection
								.next();
						if (checkSentence.getBegin() >= checkSubsection
								.getBegin()
								&& checkSentence.getEnd() >= checkSubsection
										.getEnd()) {
							checkSubsection.setSubSectionBodyEnd(checkSentence
									.getEnd());
							foundModifiedEnd = true;
						}
					}

				} else if (!subSectionItr.hasNext()) {
					checkSubsection
							.setSubSectionBodyEnd(segmentScope.getEnd() - 1);
					checkSubsection.setEnd(segmentScope.getEnd() - 1);
				}
			}
		}
	}

	private List<Object> sortSubSectionItr(Object[] holdOutSet) {
		List<Object> holdList = new ArrayList<Object>();
		SubSectionIndicator tempSsi = null;
		for (int i = 0; i < holdOutSet.length - 1; i++) {
			SubSectionIndicator hos1 = (SubSectionIndicator) holdOutSet[i];
			SubSectionIndicator hos2 = (SubSectionIndicator) holdOutSet[i + 1];
			if (hos1.getStartOffset() > hos2.getStartOffset()) {
				tempSsi = hos2;
				holdOutSet[i + 1] = hos1;
				holdOutSet[i] = tempSsi;
				sortSubSectionItr(holdOutSet);
			}
		}
		for (int j = 0; j < holdOutSet.length; j++) {
			holdList.add(holdOutSet[j]);
		}

		return holdList;

	}

	private BaseToken adaptToBaseToken(org.apache.ctakes.typesystem.type.syntax.BaseToken bta)
			throws Exception {
		if (bta instanceof WordToken) {
			WordToken wta = (WordToken) bta;
			return new WordTokenAdapter(wta);
		} else if (bta instanceof NumToken) {
			NumToken nta = (NumToken) bta;
			if (nta.getNumType() == TokenizerAnnotator.TOKEN_NUM_TYPE_INTEGER) {
				return new IntegerTokenAdapter(nta);
			} else {
				return new DecimalTokenAdapter(nta);
			}
		} else if (bta instanceof PunctuationToken) {
			PunctuationToken pta = (PunctuationToken) bta;
			return new PunctuationTokenAdapter(pta);
		} else if (bta instanceof NewlineToken) {
			NewlineToken nta = (NewlineToken) bta;
			return new NewlineTokenAdapter(nta);
		} else if (bta instanceof ContractionToken) {
			ContractionToken cta = (ContractionToken) bta;
			return new ContractionTokenAdapter(cta);
		} else if (bta instanceof SymbolToken) {
			SymbolToken sta = (SymbolToken) bta;
			return new SymbolTokenAdapter(sta);
		}

		throw new Exception("No CDT adapter for class: " + bta.getClass());
	}

	private void loadExamTitleWords(File examTypeWordsFile)
			throws FileNotFoundException, IOException {
		String line;

		if (examTitleWords == null)
			examTitleWords = new ArrayList<String>();

		FileReader fr = new FileReader(examTypeWordsFile);
		BufferedReader br = new BufferedReader(fr);

		while ((line = br.readLine()) != null) {
			String examTypeWord = line.trim();
			examTitleWords.add(examTypeWord);
		}

	}

	private boolean findSubSectionExamTitle(JCas jcas) {

		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator<?> secItr = indexes.getAnnotationIndex(SubSection.type)
				.iterator();
		boolean hasUSExam = false;
		boolean hasLowerExt = false;
		boolean hasSoloLowerExt = false;
		boolean hasCTExt = false;
		while (secItr.hasNext()) {
			SubSection sa = (SubSection) secItr.next();

			Iterator listExamTitleIter = examTitleWords.iterator();
			while (listExamTitleIter.hasNext()) {
				String entryExamTitle = (String) listExamTitleIter.next();
				String[] spanExamTitle = entryExamTitle.split(",");
				Integer lastOffset = new Integer(0);

				if (spanExamTitle.length >= 4)
					lastOffset = new Integer(spanExamTitle[3]);
				if ((sa.getCoveredText().indexOf(spanExamTitle[0]) >= new Integer(
						spanExamTitle[2]) && (lastOffset == 0 || sa
						.getCoveredText().indexOf(spanExamTitle[0]) < lastOffset))
						|| (sa.getCoveredText().indexOf(spanExamTitle[0]) >= new Integer(
								spanExamTitle[2]) && (lastOffset == 0 || sa
								.getCoveredText().indexOf(spanExamTitle[0]) < lastOffset))) {

					if (spanExamTitle[1].equals("US_EXAM"))
						hasUSExam = true;
					if (spanExamTitle[1].equals("LOWER_EXT"))
						hasLowerExt = true;
					if (spanExamTitle[1].equals("US_LOWER_SOLO"))
						hasSoloLowerExt = true;
					else if (spanExamTitle[1].equals("CT_EXAM"))
						hasCTExt = true;

					if (hasUSExam && hasSoloLowerExt)
						sa.setParentSectionId("US_LOWER_SOLO");
					else if (hasLowerExt)
						sa.setParentSectionId("US_LOWER_EXT");
					else if (hasSoloLowerExt)
						sa.setParentSectionId("US_EXAM_SOLO");
					else
						sa.setParentSectionId(spanExamTitle[1]);
				}
			}

		}
		return hasCTExt;
	}

	private List<String> examTitleWords;
	private final String EXAM_TITLE_WORDS_FILE = "ExamTitleWordsFile";

}
