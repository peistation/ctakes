package org.apache.ctakes.temporal.eval;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.NEPredicateEventAnnotator;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.NoOpAnnotator;
import org.uimafit.factory.AnalysisEngineFactory;

import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfNEPredicateEventSpans extends
		EvaluationOfAnnotationSpans_ImplBase {

	public EvaluationOfNEPredicateEventSpans(File baseDirectory,
			File rawTextDirectory, File knowtatorXMLDirectory,
			File xmiDirectory, File treebankDirectory,
			Class<? extends Annotation> annotationClass) {
		super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory,
				xmiDirectory, treebankDirectory, annotationClass);
		// TODO Auto-generated constructor stub
	}

	public EvaluationOfNEPredicateEventSpans(File baseDirectory,
			File rawTextDirectory, File knowtatorXMLDirectory,
			File xmiDirectory) {
		super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory,
				xmiDirectory, EventMention.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AnalysisEngineDescription getDataWriterDescription(File directory)
			throws ResourceInitializationException {
		// not training a model - just using the NEs and predicates
	    return AnalysisEngineFactory.createPrimitiveDescription(NoOpAnnotator.class);
	}

	@Override
	protected void trainAndPackage(File directory) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected AnalysisEngineDescription getAnnotatorDescription(File directory)
			throws ResourceInitializationException {
		// not training a model - just using the NEs and predicates
//		return AnalysisEngineFactory.createPrimitiveDescription(NoOpAnnotator.class);
	    return NEPredicateEventAnnotator.createAnnotatorDescription(directory);
	}

	@Override
	protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas, Segment segment) {
		return selectExact(jCas, EventMention.class, segment);
	}

	@Override
	protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas, Segment segment) {
		return selectExact(jCas, EventMention.class, segment);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Options options = CliFactory.parseArguments(Options.class, args);
	    List<Integer> patientSets = options.getPatients().getList();
	    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
	    List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
	    EvaluationOfNEPredicateEventSpans evaluation = new EvaluationOfNEPredicateEventSpans(
	        new File("target/eval/event-spans"),
	        options.getRawTextDirectory(),
	        options.getKnowtatorXMLDirectory(),
	        options.getXMIDirectory());
	    evaluation.prepareXMIsFor(patientSets);
	    evaluation.setLogging(Level.FINE, new File("target/eval/ctakes-event-errors.log"));
	    AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
	    System.err.println(stats);

	}

}
