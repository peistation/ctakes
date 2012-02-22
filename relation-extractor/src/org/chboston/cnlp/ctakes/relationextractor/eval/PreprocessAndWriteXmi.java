package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.chboston.cnlp.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.DocumentIDAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;


/**
 * Use this to do batch preprocessing and conversion to CAS XMI files for use in experiments.
 * @author lbecker
 *
 */
public class PreprocessAndWriteXmi {

	
	public static class Options extends Options_ImplBase {

		@Option(name = "-t", 
				aliases = "--textRoot", 
				usage = "specify the directory contraining the textFiles (for example /Volumes/sharp/NLP/Corpus/MiPACQ/Text/")
		public File textRoot = new File("/Volumes/sharp/NLP/Corpus/MiPACQ/Text/");

		@Option(name = "-x",
				aliases = "--xmlRoot",
				usage = "specify the directory containing the knowtator xml files (for example: /Volumes/sharp/NLP/Corpus/MiPACQ/UMLS/XML_exported_corpus_1_2_show_all/")
		public File xmlRoot = new File("/Volumes/sharp/NLP/Corpus/MiPACQ/UMLS/XML_exported_corpus_1_2_show_all/");
		
		@Option(name = "-o",
				aliases = "--outputRoot",
				usage = "specify the directory to write out CAS XMI files")
		public File outputRoot;
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws IOException, UIMAException {
	    Options options = new Options();
	    options.parseOptions(args);
    
	    File textRoot = options.textRoot;
	    File xmlRoot = options.xmlRoot;
	    File outputRoot = options.outputRoot;
	    
	    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
	    		FilesCollectionReader.class,
	    		FilesCollectionReader.PARAM_ROOT_FILE, textRoot);
	    
	    File preprocessDescFile = new File("desc/analysis_engine/RelationExtractorPreprocessor.xml");
	    AnalysisEngine preprocessing = createPreprocessingAE(preprocessDescFile);
	    
	    AnalysisEngine goldAnnotator = createGoldAnnotator(xmlRoot);
	    
	    AnalysisEngine xwriter = AnalysisEngineFactory.createPrimitive(
				XWriter.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("edu.mayo.bmi.core.type.common_type_system"),
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME, 
				outputRoot.getPath());
				
	    SimplePipeline.runPipeline(reader, preprocessing, goldAnnotator, xwriter);
	}

	
	public static AnalysisEngine createPreprocessingAE(File preprocessDescFile) throws IOException, InvalidXMLException, ResourceInitializationException {
	    // create the pre-processing pipeline
	      XMLParser parser = UIMAFramework.getXMLParser();
	      XMLInputSource source = new XMLInputSource(preprocessDescFile);
	      AnalysisEngineDescription desc = parser.parseAnalysisEngineDescription(source);
	      return UIMAFramework.produceAnalysisEngine(desc);
	}
	
	
	public static AnalysisEngine createGoldAnnotator(File knowtatorXMLDirectory)
			throws ResourceInitializationException {
		// pipeline to read manual annotations into the gold view, not the default view
	      AggregateBuilder goldAnnotatorBuilder = new AggregateBuilder();
	      goldAnnotatorBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
	          ViewCreatorAnnotator.class,
	          ViewCreatorAnnotator.PARAM_VIEW_NAME,
	          RelationExtractorEvaluation.GOLD_VIEW_NAME));
	      goldAnnotatorBuilder.add(
	          AnalysisEngineFactory.createPrimitiveDescription(DocumentIDAnnotator.class),
	          CAS.NAME_DEFAULT_SOFA, RelationExtractorEvaluation.GOLD_VIEW_NAME);
	      goldAnnotatorBuilder.add(
	          AnalysisEngineFactory.createPrimitiveDescription(
	            GoldEntityAndRelationReader.class,
	            GoldEntityAndRelationReader.PARAM_INPUTDIR,
	            knowtatorXMLDirectory.getPath()),
	          CAS.NAME_DEFAULT_SOFA, RelationExtractorEvaluation.GOLD_VIEW_NAME);
	      AnalysisEngine goldAnnotator = goldAnnotatorBuilder.createAggregate();
	      return goldAnnotator;
	}
	
}
