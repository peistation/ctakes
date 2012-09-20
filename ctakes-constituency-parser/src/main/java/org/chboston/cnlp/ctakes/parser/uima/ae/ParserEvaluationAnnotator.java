package org.chboston.cnlp.ctakes.parser.uima.ae;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// import opennlp.tools.lang.english.TreebankParser; // no longer part of OpenNLP as of 1.5
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import opennlp.tools.cmdline.parser.ParserTool;

public class ParserEvaluationAnnotator extends JCasAnnotator_ImplBase {

	Parser parser = null;
	private boolean useTagDictionary = true;
	private boolean useCaseSensitiveTagDictionary = true;
	private String parseStr = "";
	
	@Override
	public void initialize(org.apache.uima.UimaContext aContext) throws org.apache.uima.resource.ResourceInitializationException {
		String modelFileOrDirname = (String) aContext.getConfigParameterValue("modelDir");
		try {
			FileInputStream fis = new FileInputStream(new File(modelFileOrDirname));
			ParserModel model = new ParserModel(fis);
			parser = ParserFactory.create(model, AbstractBottomUpParser.defaultBeamSize, AbstractBottomUpParser.defaultAdvancePercentage); //TreebankParser.getParser(modelFileOrDirname, useTagDictionary, useCaseSensitiveTagDictionary, AbstractBottomUpParser.defaultBeamSize, AbstractBottomUpParser.defaultAdvancePercentage);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String sent = jcas.getDocumentText();
		StringBuffer buff = new StringBuffer();
		Parse parse = null;
		try{
			parse = ParserTool.parseLine(sent, parser, 1)[0];
			parse.show(buff);
		}catch(NullPointerException e){
			System.err.println("Parser error... no parse found... check POS tags of missed sentence (immediately above)");
		}
		if(parse == null){
			buff.append("(S null)");
		}

		TopTreebankNode ttn = new TopTreebankNode(jcas);
		ttn.setTreebankParse(buff.toString());
		ttn.addToIndexes();
	}
}
