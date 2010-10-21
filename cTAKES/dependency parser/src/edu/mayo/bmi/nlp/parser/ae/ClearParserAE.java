/*
 * Copyright: (c) 2010   Mayo Foundation for Medical Education and 
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
package edu.mayo.bmi.nlp.parser.ae;import java.io.File;import java.io.FileNotFoundException;import java.util.ArrayList;import java.util.Arrays;import java.util.List;import org.apache.log4j.Logger;import org.apache.uima.UimaContext;import org.apache.uima.analysis_engine.AnalysisEngineProcessException;import org.apache.uima.jcas.JCas;import org.apache.uima.resource.ResourceInitializationException;import edu.mayo.bmi.uima.core.resource.FileLocator;import edu.mayo.bmi.uima.core.type.BaseToken;import edu.mayo.bmi.uima.core.type.Sentence;import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;import org.apache.uima.cas.FSIterator;import org.apache.uima.cas.text.AnnotationIndex;//import org.cleartk.type.Sentence;//import org.cleartk.type.Token;//import org.cleartk.util.AnnotationRetrieval;//import org.uimafit.component.JCasAnnotator_ImplBase;//import org.uimafit.descriptor.ConfigurationParameter;//import org.uimafit.factory.ConfigurationParameterFactory;import clear.dep.DepLib;import clear.dep.DepNode;import clear.dep.DepParser;import clear.dep.DepTree;import clear.model.AbstractModel;import clear.model.LiblinearModel;import clear.morph.MorphEnAnalyzer;import clear.util.ClearDependencyUtility;public class ClearParserAE extends JCasAnnotator_ImplBase{	// LOG4J logger based on class name	public Logger logger = Logger.getLogger(getClass().getName());	/**	 * "ParserModelFile" is a required, single, string parameter that contains the	 * file name of the parser's grammar model.	 */	public static final String DEPENDENCY_MODEL_FILE_PARAM = "DependencyModelFile";	//public static final String PARAM_GRAMMAR_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(BerkeleyParserAE.class, "grammarFileName");	//private String grammarFileName;	public static final String LEXICON_DIR_PARAM = "LexiconDirectory";		public static final String FEATURE_TEMPLATE_PARAM = "FeatureTemplateFile";    	public static final String MORPH_DICT_PARAM = "MorphDictionaryDirectory";
		protected DepParser parser;		protected MorphEnAnalyzer morph;

	private boolean useMorphy = false;
	private String morphDictionaryPath;		private int parseFailureCount = 0;	private int sentenceCount = 0;		@Override	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {		super.initialize(uimaContext);		String parserModelPath = null;		String lexiconDirectoryPath = null;		String featureTemplateFile = null;		try {		    		    		    morphDictionaryPath = (String) uimaContext.getConfigParameterValue(MORPH_DICT_PARAM);
		    useMorphy = (new File(morphDictionaryPath)).exists() ? (new File(morphDictionaryPath)).isDirectory() : false;
		    if (useMorphy) 
		    	morph = new MorphEnAnalyzer( morphDictionaryPath );
		    logger.info("using Morphy analysis? "+useMorphy);
		    
		    parserModelPath = (String) uimaContext.getConfigParameterValue(DEPENDENCY_MODEL_FILE_PARAM);			logger.info("parser model file: " + parserModelPath);            lexiconDirectoryPath = (String) uimaContext.getConfigParameterValue(LEXICON_DIR_PARAM);            logger.info("lexicon directory: " + parserModelPath);            featureTemplateFile = (String) uimaContext.getConfigParameterValue(FEATURE_TEMPLATE_PARAM);		    logger.info("feature template file: " + featureTemplateFile);            logger.info("loading Clear Parser model file: "+featureTemplateFile+" ... ");            								    parser = new DepParser(lexiconDirectoryPath,parserModelPath,featureTemplateFile, DepLib.FLAG_PREDICT);		    logger.info("done.");
		} catch (Exception e) {			logger.info("Error initializing parser model: " + parserModelPath); 			throw new ResourceInitializationException(e);		}	    	}	@Override	public void process(JCas jCas) throws AnalysisEngineProcessException {		logger.info(" process(JCas)");		List<BaseToken> tokens = new ArrayList<BaseToken>();		AnnotationIndex baseTokenIndex = jCas.getAnnotationIndex(BaseToken.type);		FSIterator sentences = jCas.getAnnotationIndex(Sentence.type).iterator();		while (sentences.hasNext()) {			Sentence sentence = (Sentence) sentences.next();			tokens.clear();			FSIterator tokenIterator = baseTokenIndex.subiterator(sentence);			while (tokenIterator.hasNext()) {				BaseToken token = (BaseToken) tokenIterator.next();				tokens.add(token);			}						String[] words = new String[tokens.size()];			String[] tags  = new String[tokens.size()];            String[] lemmas = new String[tokens.size()];			String[] conlltokens = new String[tokens.size()];			for (int i = 0; i < tokens.size(); i++) {				words[i] = (tokens.get(i).getCoveredText());				tags[i] = (tokens.get(i).getPartOfSpeech());			    if ( useMorphy ) 
			    	lemmas[i] = (tokens.get(i).getNormalizedForm());                conlltokens[i] = (i+1)+"\t"+words[i]+"\t_\t"+tags[i]+"\t"+tags[i]+"\t"+"_";			}			DepTree tree = new DepTree();                        for (int i=0; i<words.length; i++)            {                int    id    = i + 1;                String form  = words[i];                String pos   = tags[i];                String lemma = useMorphy ? morph.getLemma(form, pos) : lemmas[i];                                DepNode node = new DepNode();                node.id = id;                node.form = form;
                node.lemma = (lemma==null) ? form.toLowerCase() : lemma;                node.pos = (pos==null) ? "_" : pos;                                tree.add(node);            }//            System.out.println("going to parse:\n"+tree.toString());            parser.parse(tree);            ClearDependencyUtility.convert(jCas, tree, sentence, tokens);		}	}		@Override	public void collectionProcessComplete() throws AnalysisEngineProcessException {		System.out.println("total number of sentences that were not parsed was: "+parseFailureCount+" out of "+sentenceCount);	}}