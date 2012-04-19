package org.chboston.cnlp.ctakes.relationextractor.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation;
import org.chboston.cnlp.ctakes.relationextractor.eval.RelationExtractorEvaluation.XMIReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
	    
		System.out.println("Reading XMI files from: " + options.inputRoot);
		System.out.println("Writing training data files to: " + options.outputRoot);
	    
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("../common-type-system/desc/common_type_system.xml");
		
		// Search input directory for xmis and convert to list of strings (which will later be converted to an array of strings)
		String[] extensions = {"xmi"};
		List<String> xmiFiles = Lists.newArrayList(Iterables.transform(FileUtils.listFiles(options.inputRoot, extensions, false), 
				new Function<File, String>() {
					public String apply(File file) {
						return file.getPath();
					}
				}
		));
		

	    // Create a collection reader to read specified XMI files 
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				RelationExtractorEvaluation.XMIReader.class,
				typeSystem,
				XMIReader.PARAM_FILES,
				xmiFiles.toArray(new String[xmiFiles.size()]));

		// Create a RelationExtractorAnnotator that will write out the training data
		AnalysisEngineDescription relationExtractor = AnalysisEngineFactory.createPrimitiveDescription(
				RelationExtractorAnnotator.class,
				RelationExtractorAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				RelationExtractorEvaluation.MultiClassLIBSVMDataWriterFactory.class.getName(),
				RelationExtractorAnnotator.PARAM_IS_TRAINING,
				true,
				RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
				RelationExtractorEvaluation.GOLD_VIEW_NAME,
				RelationExtractorEvaluation.MultiClassLIBSVMDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
				options.outputRoot.getPath());
		
		// Read and run
		SimplePipeline.runPipeline(reader, relationExtractor);

	}
}
