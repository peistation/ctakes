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
package edu.mayo.bmi.nlp.parser.ae;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import liblinear.Linear;
import liblinear.Model;
import liblinear.Parameter;
import liblinear.Problem;
import liblinear.Train;
import liblinear.SolverType;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepParser;
import clear.dep.DepTree;
import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.resource.FileLocator;
import edu.mayo.bmi.uima.core.type.Sentence;

public class ClearTrainerAE extends JCasAnnotator_ImplBase{

	// LOG4J logger based on class name
	public Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * "ParserModelFile" is a required, single, string parameter that contains the
	 * file name of the parser's grammar model.
	 */
	public static final String DEPENDENCY_MODEL_FILE_PARAM = "DependencyModelFile";
	//public static final String PARAM_GRAMMAR_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(BerkeleyParserAE.class, "grammarFileName");
	//private String grammarFileName;

	public static final String LEXICON_DIR_PARAM        = "LexiconDirectory";
	
	public static final String FEATURE_TEMPLATE_PARAM   = "FeatureTemplateFile";

	public static final String LIBLINEAR_PATH_PARAM    = "TrainerPath";
	public static final String LIBLINEAR_SOLVER_PARAM  = "TrainerSolver";
	public static final String LIBLINEAR_COST_PARAM    = "TrainerCost";
	public static final String LIBLINEAR_EPSILON_PARAM = "TrainerEpsilon";
	public static final String LIBLINEAR_BIAS_PARAM    = "TrainerBias";
	    
	
	protected DepParser parser;
	
	private int parseFailureCount = 0;
	private int sentenceCount = 0;
	
	private String absParserModelPath      = null;
	private String absLexiconDirectoryPath = null;
	private String absFeatureTemplateFile  = null;
	private String absFeatureDataFile      = null;

	private String trainerPath    = null;
    private int    trainerSolver  = 3;
    private double trainerCost    = 0.1;
    private double trainerEpsilon = 0.1;
    private double trainerBias    = -1;
	
    StringBuilder liblinearCommand = new StringBuilder();
    
	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);

		
		try {
		    		    
		    String parserModelPath = (String) uimaContext.getConfigParameterValue(DEPENDENCY_MODEL_FILE_PARAM);
			File parserModelFile = FileLocator.locateFile(parserModelPath);
			absParserModelPath = parserModelFile.getAbsolutePath();
			logger.info("parser model file: " + absParserModelPath);
			absFeatureDataFile = absParserModelPath+".ftr";
            String lexiconDirectoryPath = (String) uimaContext.getConfigParameterValue(LEXICON_DIR_PARAM);
			File lexiconDirectoryFile = FileLocator.locateFile(lexiconDirectoryPath);
			absLexiconDirectoryPath = lexiconDirectoryFile.getAbsolutePath();
            logger.info("lexicon directory: " + absLexiconDirectoryPath);
            String featureTemplatePath = (String) uimaContext.getConfigParameterValue(FEATURE_TEMPLATE_PARAM);
			File featureTemplateFile = FileLocator.locateFile(featureTemplatePath);
			absFeatureTemplateFile = featureTemplateFile.getAbsolutePath();
		    logger.info("feature template file: " + absFeatureTemplateFile);

		    if (uimaContext.getConfigParameterValue(LIBLINEAR_PATH_PARAM)!=null)
		        trainerPath    = (String)  uimaContext.getConfigParameterValue(LIBLINEAR_PATH_PARAM);    // "~/work/liblinear-1.7/train";
            if (uimaContext.getConfigParameterValue(LIBLINEAR_SOLVER_PARAM)!=null)
                trainerSolver  = (Integer) uimaContext.getConfigParameterValue(LIBLINEAR_SOLVER_PARAM);  //3;
            if (uimaContext.getConfigParameterValue(LIBLINEAR_COST_PARAM)!=null)
                trainerCost    = (Float)   uimaContext.getConfigParameterValue(LIBLINEAR_COST_PARAM);    //0.1;
            if (uimaContext.getConfigParameterValue(LIBLINEAR_EPSILON_PARAM)!=null)
                trainerEpsilon = (Float)   uimaContext.getConfigParameterValue(LIBLINEAR_EPSILON_PARAM); //0.1;
            if (uimaContext.getConfigParameterValue(LIBLINEAR_BIAS_PARAM)!=null)
                trainerBias    = (Float)   uimaContext.getConfigParameterValue(LIBLINEAR_BIAS_PARAM);    //-1;
            
		    System.out.print("starting Clear parser trainer ... ");
            						
		    System.out.println("done.");

		} catch (Exception e) {
			logger.info("Error initializing parser model: " + absParserModelPath); 
			throw new ResourceInitializationException(e);
		}
	    
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		logger.info(" process(JCas)");
        List<ConllDependencyNode> nodes = new ArrayList<ConllDependencyNode>();

		
		for (int i=0; i<2; i++){
	        AnnotationIndex nodeIndex = jCas.getAnnotationIndex(ConllDependencyNode.type);
	        FSIterator sentences = jCas.getAnnotationIndex(Sentence.type).iterator();

		    if (i == 0)
		        parser = new DepParser(absLexiconDirectoryPath,absFeatureDataFile,absFeatureTemplateFile, DepLib.FLAG_PRINT_LEXICON);
		    else
		        parser = new DepParser(absLexiconDirectoryPath,absFeatureDataFile,absFeatureTemplateFile, DepLib.FLAG_PRINT_INSTANCE);
		    
		    while (sentences.hasNext()) {
	            Sentence sentence = (Sentence) sentences.next();

	            nodes.clear();

	            FSIterator nodeIterator = nodeIndex.subiterator(sentence);
	            while (nodeIterator.hasNext()) {
	                ConllDependencyNode node = (ConllDependencyNode) nodeIterator.next();
	                if (node.getID()!=0) {
	                    nodes.add(node);
	                }
	            }
	        
	            DepTree tree = new DepTree();
	            
	            for (ConllDependencyNode cNode : nodes)
	            {
	                DepNode node = new DepNode();
	                node.id = cNode.getID();
	                node.form = cNode.getFORM();
	                node.lemma = cNode.getLEMMA();
	                node.pos = cNode.getPOSTAG();
	                node.headId = cNode.getHEAD().getID();
	                node.deprel = cNode.getDEPREL();
	                
	                tree.add(node);
	            }
	            
	            parser.parse(tree);

	        }
		    
		    if (i == 0)   parser.saveTags(absLexiconDirectoryPath);
		}
                
		/** Use C version of liblinear */
		if ( (new File(trainerPath)).exists() ) {
            liblinearCommand.append(trainerPath);
            liblinearCommand.append(" -s "+trainerSolver);
            liblinearCommand.append(" -c "+trainerCost);
            liblinearCommand.append(" -e "+trainerEpsilon);
            liblinearCommand.append(" -B "+trainerBias);
            liblinearCommand.append(" "+absFeatureDataFile);
            liblinearCommand.append(" "+absParserModelPath);
            
            String cmd = liblinearCommand.toString();
            System.out.println("- Command : "+cmd);

            try
            {
                Runtime rt = Runtime.getRuntime();
                long    st = System.currentTimeMillis();
                Process ps = rt.exec(cmd);  ps.waitFor();
                long  time = System.currentTimeMillis() - st;
                System.out.printf("- Duration: %d hours, %d minutes\n", time/(1000*60*60), time/(1000*60));
            }
            catch (Exception e) {e.printStackTrace();}
        }
		
		/** Use Java version of liblinear */
		else {
		    Train trainer = new Train();
		    System.out.println("- Training with Java liblinear...");
		    try
		    {
		        Problem   prob  = trainer.readProblem(new File(absFeatureDataFile), trainerBias);
		        Parameter param = new Parameter(SolverType.values()[trainerSolver], trainerCost, trainerEpsilon);
		        Model     model = Linear.train(prob, param);
		        Linear.saveModel(new File(absParserModelPath), model);
		    }
		    catch (Exception e) {e.printStackTrace();}
		}
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println("total number of sentences that were not parsed was: "+parseFailureCount+" out of "+sentenceCount);
	}
}
