package org.apache.ctakes.temporal.ae;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * for every cTAKES JCas, create a ClearTK Document Creation Time Annotation, fake span, no attribute. 
 * @author Chen Lin
 *
 */
public class ClearTKDocumentCreationTimeAnnotator extends JCasAnnotator_ImplBase {

	public ClearTKDocumentCreationTimeAnnotator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(JCas jCas)
			throws AnalysisEngineProcessException {
		// create a cleartk dct object
		DocumentCreationTime dct = new DocumentCreationTime(jCas);

		dct.addToIndexes();
	}

	public static AnalysisEngineDescription getAnnotatorDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(ClearTKDocumentCreationTimeAnnotator.class);
	}

}
