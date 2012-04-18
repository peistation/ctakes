package org.chboston.cnlp.ctakes.relationextractor.util;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.XMIReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

import com.google.common.io.Files;

/**
 * Simple class for running and writing out features for the RelationExtractorAnnotator
 * This was written to enable debug of features, while also allowing us to avoid the pain
 * and misery associated with a CPE.
 * 
 * @author Lee Becker
 *
 */
public class SimpleRunRelationExtractor {
	
	public static class Options extends Options_ImplBase {
		@Option(name = "-i",
				aliases = "--inputRoot",
				usage = "specify the directory containing the input xmis",
				required = true)
		public File inputRoot;
		
		@Option(name = "-o",
				aliases = "--outputRoot",
				usage = "specify the output directory to write training data",
				required = false)
		public File outputRoot = Files.createTempDir();
	}

	public static void main(String[] args ) throws UIMAException, IOException {
	    Options options = new Options();
	    options.parseOptions(args);

	    // Read XMI files from a directory
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				RelationExtractorEvaluation.XMIReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml"),
				XMIReader.PARAM_FILES,
				options.inputRoot);

		// Set RelationExtractorAnnotator in training and to write to libsvm format
		AnalysisEngineDescription relationExtractor = AnalysisEngineFactory.createPrimitiveDescription(
				RelationExtractorAnnotator.class,
				RelationExtractorAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				RelationExtractorEvaluation.MultiClassLIBSVMDataWriterFactory.class.getName(),
				RelationExtractorAnnotator.PARAM_IS_TRAINING,
				"true",
				RelationExtractorEvaluation.MultiClassLIBSVMDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
				options.outputRoot);
		
		// Read and run
		SimplePipeline.runPipeline(reader, relationExtractor);

	}
}
