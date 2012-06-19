package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;

public class RelationExtractorTest {

	public static void main(String[] args) throws UIMAException, IOException {
		
		TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
				"../common-type-system/desc/common_type_system.xml");
    
		CollectionReader collectionReader = CollectionReaderFactory.createCollectionReaderFromPath(
				"../core/desc/collection_reader/FilesInDirectoryCollectionReader.xml",
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR,
				"/home/dima/sharp/cloud/mipacq/text/dev");
		
		AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
				"/home/dima/workspaces/ctakes/relation-extractor/desc/analysis_engine/RelationExtractorAggregate.xml",
				EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
				false,
				EntityMentionPairRelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
				0.5f,
				EntityMentionPairRelationExtractorAnnotator.PARAM_PRINT_ERRORS,
				false,
				EntityMentionPairRelationExtractorAnnotator.PARAM_IS_TRAINING,
				false);
    
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
    		XWriter.class,
    		typeSystemDescription,
    		XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
    		"/home/dima/temp/");
		
		SimplePipeline.runPipeline(collectionReader, relationExtractor, xWriter);
	}
}

