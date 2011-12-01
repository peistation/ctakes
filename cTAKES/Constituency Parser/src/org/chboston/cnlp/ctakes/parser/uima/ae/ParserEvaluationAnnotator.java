/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
* @author Tim Miller
*/
package org.chboston.cnlp.ctakes.parser.uima.ae;

import java.io.IOException;

import opennlp.tools.lang.english.TreebankParser;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.chboston.cnlp.ctakes.parser.uima.type.TopTreebankNode;

public class ParserEvaluationAnnotator extends JCasAnnotator_ImplBase {

	Parser parser = null;
	private boolean useTagDictionary = true;
	private boolean useCaseSensitiveTagDictionary = true;
	private String parseStr = "";
	
	@Override
	public void initialize(org.apache.uima.UimaContext aContext) throws org.apache.uima.resource.ResourceInitializationException {
		String modelFileOrDirname = (String) aContext.getConfigParameterValue("modelDir");
		try {
			parser = TreebankParser.getParser(modelFileOrDirname, useTagDictionary, useCaseSensitiveTagDictionary, AbstractBottomUpParser.defaultBeamSize, AbstractBottomUpParser.defaultAdvancePercentage);
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
			parse = TreebankParser.parseLine(sent, parser, 1)[0];
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
