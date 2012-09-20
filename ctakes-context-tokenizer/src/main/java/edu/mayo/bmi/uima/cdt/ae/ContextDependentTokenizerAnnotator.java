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
package edu.mayo.bmi.uima.cdt.ae;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.fsm.machine.DateFSM;
import edu.mayo.bmi.fsm.machine.FractionFSM;
import edu.mayo.bmi.fsm.machine.MeasurementFSM;
import edu.mayo.bmi.fsm.machine.PersonTitleFSM;
import edu.mayo.bmi.fsm.machine.RangeFSM;
import edu.mayo.bmi.fsm.machine.RomanNumeralFSM;
import edu.mayo.bmi.fsm.machine.TimeFSM;
import edu.mayo.bmi.fsm.output.DateToken;
import edu.mayo.bmi.fsm.output.FractionToken;
import edu.mayo.bmi.fsm.output.MeasurementToken;
import edu.mayo.bmi.fsm.output.PersonTitleToken;
import edu.mayo.bmi.fsm.output.RangeToken;
import edu.mayo.bmi.fsm.output.RomanNumeralToken;
import edu.mayo.bmi.fsm.output.TimeToken;
import edu.mayo.bmi.fsm.token.BaseToken;
import edu.mayo.bmi.uima.core.ae.TokenizerAnnotator;
import edu.mayo.bmi.uima.core.fsm.adapters.ContractionTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.DecimalTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.IntegerTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.NewlineTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.PunctuationTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.SymbolTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.WordTokenAdapter;
import org.apache.ctakes.typesystem.type.syntax.ContractionToken;
import org.apache.ctakes.typesystem.type.syntax.NewlineToken;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
import org.apache.ctakes.typesystem.type.syntax.PunctuationToken;
import org.apache.ctakes.typesystem.type.syntax.SymbolToken;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textsem.DateAnnotation;
import org.apache.ctakes.typesystem.type.textsem.FractionAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MeasurementAnnotation;
import org.apache.ctakes.typesystem.type.textsem.PersonTitleAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RangeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RomanNumeralAnnotation;
import org.apache.ctakes.typesystem.type.textsem.TimeAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;

/**
 * Finds tokens based on context.
 * 
 * @author Mayo Clinic
 */
public class ContextDependentTokenizerAnnotator extends JCasAnnotator_ImplBase {
	// LOG4J logger based on class name
	private Logger iv_logger = Logger.getLogger(getClass().getName());

	private DateFSM iv_dateFSM;
	private TimeFSM iv_timeFSM;
	private FractionFSM iv_fractionFSM;
	private RomanNumeralFSM iv_romanNumeralFSM;
	private RangeFSM iv_rangeFSM;
	private MeasurementFSM iv_measurementFSM;
	private PersonTitleFSM iv_personTitleFSM;

	public void initialize(UimaContext annotCtx) throws ResourceInitializationException {
		super.initialize(annotCtx);

		iv_dateFSM = new DateFSM();
		iv_timeFSM = new TimeFSM();
		iv_fractionFSM = new FractionFSM();
		iv_romanNumeralFSM = new RomanNumeralFSM();
		iv_rangeFSM = new RangeFSM();
		iv_measurementFSM = new MeasurementFSM();
		iv_personTitleFSM = new PersonTitleFSM();
		iv_logger.info("Finite state machines loaded.");
	}

	public void process(JCas jcas) throws AnalysisEngineProcessException {

		try {
			
	    	iv_logger.info("process(JCas)");

			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
			Iterator<?> sentItr = indexes.getAnnotationIndex(Sentence.type).iterator();
			AnnotationIndex baseTokenIndex = jcas.getJFSIndexRepository().getAnnotationIndex(
					org.apache.ctakes.typesystem.type.syntax.BaseToken.type);
			
			while (sentItr.hasNext()) {
				Sentence sentAnnot = (Sentence) sentItr.next();
				FSIterator btaItr = baseTokenIndex.subiterator(sentAnnot);

				// adapt JCas objects into objects expected by the Finite state
				// machines
				List<BaseToken> baseTokenList = new ArrayList<BaseToken>();
				while (btaItr.hasNext()) {
					org.apache.ctakes.typesystem.type.syntax.BaseToken bta = (org.apache.ctakes.typesystem.type.syntax.BaseToken) btaItr
							.next();
					baseTokenList.add(adaptToBaseToken(bta));
				}

				// execute FSM logic
				executeFSMs(jcas, baseTokenList);
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void executeFSMs(JCas jcas, List<? extends BaseToken> baseTokenList) throws AnalysisEngineProcessException {
		try {
			Set<DateToken> dateTokenSet = iv_dateFSM.execute(baseTokenList);
			Iterator<DateToken> dateTokenItr = dateTokenSet.iterator();
			while (dateTokenItr.hasNext()) {
				DateToken dt = dateTokenItr.next();
				DateAnnotation dta = new DateAnnotation(jcas, dt.getStartOffset(), dt.getEndOffset());
				dta.addToIndexes();
			}

			Set<TimeToken> timeTokenSet = iv_timeFSM.execute(baseTokenList);
			Iterator<TimeToken> timeTokenItr = timeTokenSet.iterator();
			while (timeTokenItr.hasNext()) {
				TimeToken tt = timeTokenItr.next();
				TimeAnnotation ta = new TimeAnnotation(jcas, tt.getStartOffset(), tt.getEndOffset());
				ta.addToIndexes();
			}

			Set<RomanNumeralToken> romanNumeralTokenSet = iv_romanNumeralFSM.execute(baseTokenList);
			Iterator<RomanNumeralToken> romanNumeralTokenItr = romanNumeralTokenSet.iterator();
			while (romanNumeralTokenItr.hasNext()) {
				RomanNumeralToken rnt = romanNumeralTokenItr.next();
				RomanNumeralAnnotation rna = new RomanNumeralAnnotation(jcas, rnt.getStartOffset(), rnt.getEndOffset());
				rna.addToIndexes();
			}

			Set<FractionToken> fractionTokenSet = iv_fractionFSM.execute(baseTokenList);
			Iterator<FractionToken> fractionTokenItr = fractionTokenSet.iterator();
			while (fractionTokenItr.hasNext()) {
				FractionToken ft = fractionTokenItr.next();
				FractionAnnotation fa = new FractionAnnotation(jcas, ft.getStartOffset(), ft.getEndOffset());
				fa.addToIndexes();
			}

			Set<RangeToken> rangeTokenSet = iv_rangeFSM.execute(baseTokenList, romanNumeralTokenSet);
			Iterator<RangeToken> rangeTokenItr = rangeTokenSet.iterator();
			while (rangeTokenItr.hasNext()) {
				RangeToken rt = rangeTokenItr.next();
				RangeAnnotation ra = new RangeAnnotation(jcas, rt.getStartOffset(), rt.getEndOffset());
				ra.addToIndexes();
			}

			Set<MeasurementToken> measurementTokenSet = iv_measurementFSM.execute(baseTokenList, rangeTokenSet);
			Iterator<MeasurementToken> measurementTokenItr = measurementTokenSet.iterator();
			while (measurementTokenItr.hasNext()) {
				MeasurementToken mt = measurementTokenItr.next();
				MeasurementAnnotation ma = new MeasurementAnnotation(jcas, mt.getStartOffset(), mt.getEndOffset());
				ma.addToIndexes();
			}

			Set<PersonTitleToken> personTitleTokenSet = iv_personTitleFSM.execute(baseTokenList);
			Iterator<PersonTitleToken> personTitleTokenItr = personTitleTokenSet.iterator();
			while (personTitleTokenItr.hasNext()) {
				PersonTitleToken ptt = personTitleTokenItr.next();
				PersonTitleAnnotation pta = new PersonTitleAnnotation(jcas, ptt.getStartOffset(), ptt.getEndOffset());
				pta.addToIndexes();
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Adapts JCas objects to BaseToken interfaces expected by the Finite State
	 * Machines.
	 * 
	 * @param obj
	 * @return
	 */
	private BaseToken adaptToBaseToken(org.apache.ctakes.typesystem.type.syntax.BaseToken obj) throws Exception {
		if (obj instanceof WordToken) {
			WordToken wta = (WordToken) obj;
			return new WordTokenAdapter(wta);
		} else if (obj instanceof NumToken) {
			NumToken nta = (NumToken) obj;
			if (nta.getNumType() == TokenizerAnnotator.TOKEN_NUM_TYPE_INTEGER) {
				return new IntegerTokenAdapter(nta);
			} else {
				return new DecimalTokenAdapter(nta);
			}
		} else if (obj instanceof PunctuationToken) {
			PunctuationToken pta = (PunctuationToken) obj;
			return new PunctuationTokenAdapter(pta);
		} else if (obj instanceof NewlineToken) {
			NewlineToken nta = (NewlineToken) obj;
			return new NewlineTokenAdapter(nta);
		} else if (obj instanceof ContractionToken) {
			ContractionToken cta = (ContractionToken) obj;
			return new ContractionTokenAdapter(cta);
		} else if (obj instanceof SymbolToken) {
			SymbolToken sta = (SymbolToken) obj;
			return new SymbolTokenAdapter(sta);
		}

		throw new Exception("No Context Dependent Tokenizer adapter for class: " + obj.getClass());
	}
}
