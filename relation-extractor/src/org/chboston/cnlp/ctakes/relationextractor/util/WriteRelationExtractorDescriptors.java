package org.chboston.cnlp.ctakes.relationextractor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
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
		
		@Option(name = "-me",
				aliases = "--modelFileEmPair",
				usage = "specify the path to the relation extractor model jar file",
				required = false)
		public File modelFileEmPair = new File("resources/models/em_pair/model.jar");
		
		@Option(name = "-md",
				aliases = "--modelFileDegreeOf",
				usage = "specify the path to the relation extractor model jar file",
				required = false)
		public File modelFileDegreeOf = new File("resources/models/degree_of/model.jar");
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
	    File modelFileEmPair = options.modelFileEmPair;
	    File modelFileDegreeOf = options.modelFileDegreeOf;
	    
	    AnalysisEngineDescription entityMentionPairRelationExtractorDesc = AnalysisEngineFactory.createPrimitiveDescription(
	    		EntityMentionPairRelationExtractorAnnotator.class,
	    		GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	    		modelFileEmPair.getPath());
	    
	    FileOutputStream output = new FileOutputStream(new File(outputRoot, "EntityMentionPairRelationExtractorAnnotator.xml"));
	    entityMentionPairRelationExtractorDesc.toXML(output);
	    output.close();
	    
	    AnalysisEngineDescription degreeOfRelationExtractorDesc = AnalysisEngineFactory.createPrimitiveDescription(
	    		DegreeOfRelationExtractorAnnotator.class,
	    		GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
	    		modelFileDegreeOf.getPath());
	    
	    output = new FileOutputStream(new File(outputRoot, "DegreeOfRelationExtractorAnnotator.xml"));
	    degreeOfRelationExtractorDesc.toXML(output);
	    output.close();
	    
	}
		
}
