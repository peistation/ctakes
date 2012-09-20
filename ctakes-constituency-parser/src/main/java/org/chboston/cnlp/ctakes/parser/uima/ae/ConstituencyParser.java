package org.chboston.cnlp.ctakes.parser.uima.ae;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.parser.MaxentParserWrapper;
import org.chboston.cnlp.ctakes.parser.ParserWrapper;

import edu.mayo.bmi.uima.core.resource.FileLocator;

public class ConstituencyParser extends JCasAnnotator_ImplBase {

	ParserWrapper parser = null;
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		String modelFileOrDirname = (String) aContext.getConfigParameterValue("modelFilename");
//		parser = new BerkeleyParserWrapper(modelFilename);
		try {
//			synchronized(this.getClass()){
//				if(parser == null){
					logger.info("Initializing parser...");
					parser = new MaxentParserWrapper(FileLocator.locateFile(modelFileOrDirname).getAbsolutePath());
//				}
//			}
//			parser = new ParallelParser(FileLocator.locateFile(modelFileOrDirname).getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Error reading parser model file/directory: " + e.getMessage());
		}
	}


	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
//		TreebankParser p = new TreebankParser();
//		synchronized(parser){
			parser.createAnnotations(jcas);
//		}
	}
}
