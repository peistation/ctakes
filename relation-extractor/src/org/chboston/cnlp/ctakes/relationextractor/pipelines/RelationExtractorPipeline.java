package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;

/**
 * A simple pipeline that runs relation extraction on all files in a directory and saves
 * the resulting annotations as XMI files. The core part of this pipeline is the aggregate
 * relation extractor AE which runs all the preprocessing that is necessary for relation
 * extraction as well as the AEs that extract relations.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorPipeline {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public String inputDirectory;
    
    @Option(
        name = "--output-dir",
        usage = "specify the path to the directory where the output xmi files are to be saved",
        required = true)
    public String outputDirectory;
  }
  
	public static void main(String[] args) throws UIMAException, IOException {
		
		Options options = new Options();
		options.parseOptions(args);

		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
				"../common-type-system/desc/common_type_system.xml");
    
		CollectionReader collectionReader = CollectionReaderFactory.createCollectionReaderFromPath(
				"../core/desc/collection_reader/FilesInDirectoryCollectionReader.xml",
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR,
				options.inputDirectory);

		// make sure the model parameters match those used for training
		AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
				"desc/analysis_engine/RelationExtractorAggregate.xml");
    
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
    		XWriter.class,
    		typeSystemDescription,
    		XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
    		options.outputDirectory);
		
		SimplePipeline.runPipeline(collectionReader, relationExtractor, xWriter);
	}
}
