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
 * A simple pipeline that first runs all the components that are necessary for the relation extraction
 * module. Next, it runs the relation extraction annotator. Finally, it saves the annotations as xmi files.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorTest {

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

		// the parameters must match the training parameters
		AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
				"desc/analysis_engine/RelationExtractorAggregate.xml",
				EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
				true,
				EntityMentionPairRelationExtractorAnnotator.PARAM_PRINT_ERRORS,
				false,
				EntityMentionPairRelationExtractorAnnotator.PARAM_IS_TRAINING,
				false);
    
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
    		XWriter.class,
    		typeSystemDescription,
    		XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
    		options.outputDirectory);
		
		SimplePipeline.runPipeline(collectionReader, relationExtractor, xWriter);
	}
}
