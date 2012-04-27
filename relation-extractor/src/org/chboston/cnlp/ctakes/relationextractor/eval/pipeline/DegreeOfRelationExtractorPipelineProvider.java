package org.chboston.cnlp.ctakes.relationextractor.eval.pipeline;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.uimafit.factory.AnalysisEngineFactory;

public class DegreeOfRelationExtractorPipelineProvider extends RelationExtractionPipelineProvider {

	public DegreeOfRelationExtractorPipelineProvider(
			File modelsDirectory,
			Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
			Object... additionalParameters) throws UIMAException, IOException {
		super(modelsDirectory, dataWriterFactoryClass, additionalParameters);
	}

	@Override
	protected AnalysisEngineDescription getClassifierAnnotatorDescription()
			throws ResourceInitializationException {

		return AnalysisEngineFactory.createPrimitiveDescription(
				DegreeOfRelationExtractorAnnotator.class,
				this.additionalParameters);
	}
	
}
