package org.apache.ctakes.coreference.cc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class PreprocessAndWriteXmi {
	public static class Options extends Options_ImplBase {

		@Option(name = "-t", 
				aliases = "--textRoot", 
				usage = "specify the directory contraining the textFiles (for example /NLP/Corpus/Relations/mipacq/text/train",
				required = true)
				public String textRoot;

		// TODO - fix to use an xml collection reader instead of the hacky way it's done now...
		//		@Option(name = "-x",
		//				aliases = "--xmlRoot",
		//				usage = "specify the directory containing the knowtator xml files (for example: /NLP/Corpus/Relations/mipacq/xml/train",
		//        required = true)
		//		public File xmlRoot;

		@Option(name = "-o",
				aliases = "--outputRoot",
				usage = "specify the directory to write out CAS XMI files",
				required = true)
				public File outputRoot;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws UIMAException, IOException {
		Options options = new Options();
		options.parseOptions(args);

		File outputRoot = options.outputRoot;
		String inputRoot = options.textRoot;
//		TypeSystemDescription typeSystem = 
//			TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml", 
//																			 "../assertion/desc/medfactsTypeSystem.xml");

		AnalysisEngine ae = AnalysisEngineFactory.createAnalysisEngineFromPath("desc/analysis_engine/ODIESvmVectorCreator.xml");

		CollectionReader reader = CollectionReaderFactory.createCollectionReaderFromPath(
				"../ctakes-core/desc/collection_reader/FilesInDirectoryCollectionReader.xml",
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR,
				inputRoot);

		AnalysisEngine serializer = AnalysisEngineFactory.createPrimitive(
				PreprocessAndWriteXmi.SerializeDocumentToXMI.class,
//				typeSystem,
				PreprocessAndWriteXmi.SerializeDocumentToXMI.PARAM_OUTPUT_DIRECTORY, 
				outputRoot.getPath());

		SimplePipeline.runPipeline(reader, ae, serializer);	    
	}

	public static class SerializeDocumentToXMI extends JCasAnnotator_ImplBase {
		public static final String PARAM_OUTPUT_DIRECTORY = ConfigurationParameterFactory
		.createConfigurationParameterName(SerializeDocumentToXMI.class, "outputDirectory");

		@ConfigurationParameter(mandatory = true, description = "Specifies the output directory in which to write xmi files")
		private File outputDirectory;

		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
			super.initialize(context);
			if (!this.outputDirectory.exists()) {
				this.outputDirectory.mkdirs();
			}
		}

		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			try {
				// FIXME - not using this right now, just use default jcas
//				JCas goldView = jCas.getView(RelationExtractorEvaluation.GOLD_VIEW_NAME);
				JCas goldView = jCas;
				String documentID = DocumentIDAnnotationUtil.getDocumentID(goldView);
				if (documentID == null) {
					throw new IllegalArgumentException("No documentID for CAS:\n" + jCas);
				}
				File outFile = new File(this.outputDirectory, documentID + ".xmi");
				ContentHandler handler = new XMLSerializer(new FileOutputStream(outFile)).getContentHandler();
				new XmiCasSerializer(jCas.getTypeSystem()).serialize(jCas.getCas(), handler);
			} catch (CASRuntimeException e) {
				throw new AnalysisEngineProcessException(e);
			} catch (SAXException e) {
				throw new AnalysisEngineProcessException(e);
			} catch (FileNotFoundException e) {
				throw new AnalysisEngineProcessException(e);
//			} catch (CASException e) {
//				throw new AnalysisEngineProcessException(e);
			}	
		}

	}

}

