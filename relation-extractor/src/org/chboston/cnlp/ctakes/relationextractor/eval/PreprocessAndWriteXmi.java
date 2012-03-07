package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.apache.uima.util.XMLSerializer;
import org.chboston.cnlp.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.DocumentIDAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
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

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;


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
	    		FilesCollectionReader.PARAM_ROOT_FILE, textRoot.getPath());
	    
	    File preprocessDescFile = new File("desc/analysis_engine/RelationExtractorPreprocessor.xml");
	    AnalysisEngine preprocessing = createPreprocessingAE(preprocessDescFile);
	    
	    AnalysisEngine goldAnnotator = createGoldAnnotator(xmlRoot);
	    
	    AnalysisEngine serializer = AnalysisEngineFactory.createPrimitive(
				PreprocessAndWriteXmi.SerializeDocumentToXMI.class, 
				PreprocessAndWriteXmi.SerializeDocumentToXMI.PARAM_OUTPUT_DIRECTORY_NAME, 
				outputRoot.getPath());
				
	    SimplePipeline.runPipeline(reader, preprocessing, goldAnnotator, serializer);
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
	
	public static class SerializeDocumentToXMI extends JCasAnnotator_ImplBase {
		public static final String PARAM_OUTPUT_DIRECTORY_NAME = ConfigurationParameterFactory
		.createConfigurationParameterName(SerializeDocumentToXMI.class, "outputDirectoryName");

		@ConfigurationParameter(mandatory = true, description = "Specifies the output directory in which to write xmi files")
		private String outputDirectoryName;

		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			try {
			  String documentID = DocumentIDAnnotationUtil.getDocumentID(jCas);
			   File outFile = new File(this.outputDirectoryName, documentID + ".xmi");
			   ContentHandler handler = new XMLSerializer(new FileOutputStream(outFile)).getContentHandler();
				new XmiCasSerializer(jCas.getTypeSystem()).serialize(jCas.getCas(), handler);
			} catch (CASRuntimeException e) {
				throw new AnalysisEngineProcessException(e);
			} catch (SAXException e) {
				throw new AnalysisEngineProcessException(e);
			} catch (FileNotFoundException e) {
				throw new AnalysisEngineProcessException(e);
			}	
		}
		
	}
	
}
