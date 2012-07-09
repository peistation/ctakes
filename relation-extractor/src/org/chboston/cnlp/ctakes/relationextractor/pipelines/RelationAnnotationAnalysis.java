package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.eval.XMIReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * 
 * Analyze the annotations obtained by running the relation extractor on clinical notes.
 * The results of running the relation extractor are expected to be saved in xmi files.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationAnnotationAnalysis {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public File inputDirectory;
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
				"../common-type-system/desc/common_type_system.xml");
		
		List<File> trainFiles = Arrays.asList(options.inputDirectory.listFiles());
    CollectionReader collectionReader = getCollectionReader(trainFiles, typeSystemDescription);
		
    AnalysisEngine relationExtractorConsumer = AnalysisEngineFactory.createPrimitive(
    		RelationExtractorConsumer.class);
    		
		SimplePipeline.runPipeline(collectionReader, relationExtractorConsumer);
	}
	
  private static CollectionReader getCollectionReader(List<File> items, 
  		TypeSystemDescription typeSystemDescription) throws Exception {

  	// convert the List<File> to a String[]
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }
    
    // return a reader that will load each of the XMI files
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        typeSystemDescription,
        XMIReader.PARAM_FILES,
        paths);
  }
}
