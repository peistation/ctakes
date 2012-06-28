package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.chboston.cnlp.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.JCasFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import edu.mayo.bmi.uima.core.cr.FilesInDirectoryCollectionReader;

/**
 * Run relation module on a single sentence.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorSingleCasTest {

	public static class Options extends Options_ImplBase {

		public static void main(String[] args) throws UIMAException, IOException {

			String sampleSentence = "Abdomen: Soft, nontender without hepatosplenomegaly or masses";
			
			TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
					"../common-type-system/desc/common_type_system.xml");

			JCas jCas = JCasFactory.createJCas(typeSystemDescription);
			jCas.setDocumentText(sampleSentence);

			// the parameters must match the training parameters
			AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
					"desc/analysis_engine/RelationExtractorAggregate.xml",
					EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
					true,
					EntityMentionPairRelationExtractorAnnotator.PARAM_PRINT_ERRORS,
					false,
					EntityMentionPairRelationExtractorAnnotator.PARAM_IS_TRAINING,
					false);

		  AnalysisEngine relationConsumer = AnalysisEngineFactory.createPrimitive(
	    		RelationExtractorConsumer.class);

			SimplePipeline.runPipeline(jCas, relationExtractor, relationConsumer);
		}
	}
}
