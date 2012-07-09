package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;

/**
 * 
 * A class for testing the reader for the gold standard relation data. 
 * Currently this class runs the reader and saves the resulting annotations as xmi files.
 * 
 * @author dmitriy dligach
 *
 */
public class TestGoldEntityAndRelationReader {

	public static void main(String[] args) throws UIMAException, IOException {
		
		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
				"/home/dima/workspaces/ctakes/common-type-system/desc/common_type_system.xml");
		
		CollectionReaderDescription collectionReader = CollectionReaderFactory.createDescription(
				FilesInDirectoryCollectionReader.class,
				typeSystemDescription,
				"InputDirectory",
				"/home/dima/sharp/cloud/sharp/text/train/");
		
		AnalysisEngineDescription goldAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
				GoldEntityAndRelationReader.class,
				typeSystemDescription,
				"InputDirectory",
				"/home/dima/sharp/cloud/sharp/xml/train/");

    AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
        XWriter.class,
        typeSystemDescription,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        "/home/dima/temp/");
    
		SimplePipeline.runPipeline(collectionReader, goldAnnotator, xWriter);
	}
}
