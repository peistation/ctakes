package edu.mayo.bmi.nlp.parser.ae.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.mayo.bmi.nlp.parser.ae.ClearParserDependencyParserAE;
import edu.mayo.bmi.nlp.parser.ae.ClearParserSemanticRoleLabelerAE;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;


/**
 * Use this to do batch preprocessing and conversion to CAS XMI files for use in experiments.
 * @author lbecker
 *
 */
public class TestAnalysisEngines {

	public static class Options extends Options_ImplBase {
		@Option(name = "-o",
				aliases = "--outputRoot",
				usage = "specify the directory to write out descriptor files",
				required = false)
		public File outputRoot = new File("/tmp");

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
		
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml");
				
		String[] paths = { "/Users/lbecker/Desktop/mipacq_relations/Relations/mipacq/xmi/test/3257136710_5.xmi" };
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				XMIReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml"),
				XMIReader.PARAM_FILES,
				paths);

		AnalysisEngine clearparserSRL = AnalysisEngineFactory.createPrimitive(
				ClearParserSemanticRoleLabelerAE.class,
				typeSystem,
				ClearParserSemanticRoleLabelerAE.PARAM_PARSER_MODEL_FILE_NAME,
				new File("src/resources/srl/dummy.srl.mod.jar").toString()
				);
		
		AnalysisEngine clearparserDepParser = AnalysisEngineFactory.createPrimitive(
				ClearParserDependencyParserAE.class,
				typeSystem,
				ClearParserDependencyParserAE.PARAM_PARSER_MODEL_FILE_NAME,
				new File("src/resources/dependency/dummy.dep.mod.jar").toString()
				);
	
		
		
		SimplePipeline.runPipeline(reader, clearparserDepParser);
		SimplePipeline.runPipeline(reader, clearparserSRL);
	}


	public static AnalysisEngine createPreprocessingAE(File preprocessDescFile) throws IOException, InvalidXMLException, ResourceInitializationException {
		// create the pre-processing pipeline
		XMLParser parser = UIMAFramework.getXMLParser();
		XMLInputSource source = new XMLInputSource(preprocessDescFile);
		AnalysisEngineDescription desc = parser.parseAnalysisEngineDescription(source);
		return UIMAFramework.produceAnalysisEngine(desc);
	}

}
