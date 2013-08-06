package org.chboston.cnlp.ctakes.relationextractor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.xml.sax.SAXException;


/**
 * Use this to do batch preprocessing and conversion to CAS XMI files for use in experiments.
 * @author lbecker
 *
 */
public class WriteRelationExtractorDescriptors {

	
	public static class Options extends Options_ImplBase {
		@Option(name = "-o",
				aliases = "--outputRoot",
				usage = "specify the directory to write out descriptor files",
				required = false)
		public File outputRoot = new File("desc/analysis_engine");
		
		@Option(name = "-m",
				aliases = "--modelFile",
				usage = "specify the path to the relation extractor model jar file",
				required = false)
		public File modelFile = new File("model.jar");
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, UIMAException, SAXException {
	    Options options = new Options();
	    options.parseOptions(args);
    
	    File outputRoot = options.outputRoot;
	    File modelFile = options.modelFile;
	    
	    AnalysisEngineDescription relationExtractorDesc = AnalysisEngineFactory.createPrimitiveDescription(
	    		RelationExtractorAnnotator.class,
	    		GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	    		modelFile.getPath());
	    
	    FileOutputStream output = new FileOutputStream(new File(outputRoot, "RelationExtractorAnnotator.xml"));
	    relationExtractorDesc.toXML(output);
	    output.close();
	}
		
}
