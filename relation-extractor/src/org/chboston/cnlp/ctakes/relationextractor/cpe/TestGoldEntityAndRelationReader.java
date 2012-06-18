package org.chboston.cnlp.ctakes.relationextractor.cpe;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.cr.GoldEntityAndRelationReader;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;

public class TestGoldEntityAndRelationReader {

	public static void main(String[] args) throws UIMAException, IOException {
		
		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
				"/home/dima/workspaces/ctakes/common-type-system/desc/common_type_system.xml");
		
		CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
				FilesInDirectoryCollectionReader.class,
				typeSystemDescription,
				"InputDirectory",
				"/home/dima/sharp/cloud/sharp/text/train/");
		
		AnalysisEngineDescription gold = AnalysisEngineFactory.createPrimitiveDescription(
				GoldEntityAndRelationReader.class,
				typeSystemDescription,
				"InputDirectory",
				"/home/dima/sharp/cloud/sharp/xml/train/");

		AnalysisEngineDescription consumer = AnalysisEngineFactory.createPrimitiveDescription(
				CasConsumer.class,
				typeSystemDescription);
		
		SimplePipeline.runPipeline(reader, gold, consumer);
		
		
	}
}
