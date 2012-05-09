package org.chboston.cnlp.ctakes.relationextractor.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
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
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;
import edu.mayo.bmi.uima.core.type.structured.DocumentID;
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
				usage = "specify the directory contraining the textFiles (for example /NLP/Corpus/Relations/mipacq/text/train",
				required = true)
		public File textRoot;

		@Option(name = "-x",
				aliases = "--xmlRoot",
				usage = "specify the directory containing the knowtator xml files (for example: /NLP/Corpus/Relations/mipacq/xml/train",
        required = true)
		public File xmlRoot;
		
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
				PreprocessAndWriteXmi.SerializeDocumentToXMI.PARAM_OUTPUT_DIRECTORY, 
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
	      goldAnnotatorBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
	          CopyDocumentTextToGoldView.class));
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
	
  public static class CopyDocumentTextToGoldView extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      try {
        JCas goldView = jCas.getView(RelationExtractorEvaluation.GOLD_VIEW_NAME);
        goldView.setDocumentText(jCas.getDocumentText());
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
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
			  JCas goldView = jCas.getView(RelationExtractorEvaluation.GOLD_VIEW_NAME);
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
			} catch (CASException e) {
			  throw new AnalysisEngineProcessException(e);
      }	
		}
		
	}
	
  /**
   * Class for adding DocumentID annotations.
   * 
   * Needed because {@link FilesInDirectoryCollectionReader} creates {@link DocumentID} annotations
   * but doesn't allow specific files to be loaded, while {@link FilesCollectionReader} allows
   * specific files to be loaded but creates URIs instead of {@link DocumentID} annotations.
   */
  public static class DocumentIDAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String documentID = new File(ViewURIUtil.getURI(jCas)).getName();
      DocumentID documentIDAnnotation = new DocumentID(jCas);
      documentIDAnnotation.setDocumentID(documentID);
      documentIDAnnotation.addToIndexes();
    }

  }

}
