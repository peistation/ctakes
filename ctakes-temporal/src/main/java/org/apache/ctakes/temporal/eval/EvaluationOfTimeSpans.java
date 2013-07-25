/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.temporal.eval;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.BackwardsTimeAnnotator;
import org.apache.ctakes.temporal.ae.CRFTimeAnnotator;
import org.apache.ctakes.temporal.ae.ConstituencyBasedTimeAnnotator;
import org.apache.ctakes.temporal.ae.MetaTimeAnnotator;
import org.apache.ctakes.temporal.ae.TimeAnnotator;
import org.apache.ctakes.temporal.ae.feature.selection.FeatureSelection;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Instance;
//import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.crfsuite.CRFSuiteStringOutcomeDataWriter;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class EvaluationOfTimeSpans extends EvaluationOfAnnotationSpans_ImplBase {

	static interface Options extends Evaluation_ImplBase.Options {

		@Option(longName = "featureSelectionThreshold", defaultValue = "0")
		public float getFeatureSelectionThreshold();
	}

	public static void main(String[] args) throws Exception {
		Options options = CliFactory.parseArguments(Options.class, args);
		List<Integer> patientSets = options.getPatients().getList();
		List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
		List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);

		// specify the annotator classes to use
		List<Class<? extends JCasAnnotator_ImplBase>> annotatorClasses = Lists.newArrayList();
		annotatorClasses.add(BackwardsTimeAnnotator.class);
		annotatorClasses.add(TimeAnnotator.class);
		annotatorClasses.add(ConstituencyBasedTimeAnnotator.class);
		annotatorClasses.add(CRFTimeAnnotator.class);
		annotatorClasses.add(MetaTimeAnnotator.class);
		Map<Class<? extends JCasAnnotator_ImplBase>, String[]> annotatorTrainingArguments = Maps.newHashMap();
		annotatorTrainingArguments.put(BackwardsTimeAnnotator.class, new String[]{"-c", "0.1"});
		annotatorTrainingArguments.put(TimeAnnotator.class, new String[]{"-c", "0.1"});
		annotatorTrainingArguments.put(ConstituencyBasedTimeAnnotator.class, new String[]{"-c", "0.1"});
		annotatorTrainingArguments.put(CRFTimeAnnotator.class, new String[]{"-p", "c2=0.1"});
		annotatorTrainingArguments.put(MetaTimeAnnotator.class, new String[]{"-p", "c2=0.1"});

		// run one evaluation per annotator class
		final Map<Class<?>, AnnotationStatistics<?>> annotatorStats = Maps.newHashMap();
		for (Class<? extends JCasAnnotator_ImplBase> annotatorClass : annotatorClasses) {
			EvaluationOfTimeSpans evaluation = new EvaluationOfTimeSpans(
					new File("target/eval/time-spans"),
					options.getRawTextDirectory(),
					options.getXMLDirectory(),
					options.getXMLFormat(),
					options.getXMIDirectory(),
					options.getTreebankDirectory(),
					options.getFeatureSelectionThreshold(),
					annotatorClass,
					options.getPrintOverlappingSpans(),
					annotatorTrainingArguments.get(annotatorClass));
			evaluation.prepareXMIsFor(patientSets);
			String name = String.format("%s.errors", annotatorClass.getSimpleName());
			evaluation.setLogging(Level.FINE, new File("target/eval", name));
			AnnotationStatistics<String> stats = evaluation.trainAndTest(trainItems, devItems);
			annotatorStats.put(annotatorClass, stats);
		}

		// allow ordering of models by F1
		Ordering<Class<? extends JCasAnnotator_ImplBase>> byF1 = Ordering.natural().onResultOf(
				new Function<Class<? extends JCasAnnotator_ImplBase>, Double>() {
					@Override
					public Double apply(
							Class<? extends JCasAnnotator_ImplBase> annotatorClass) {
						return annotatorStats.get(annotatorClass).f1();
					}
				});

		// print out models, ordered by F1
		for (Class<?> annotatorClass : byF1.sortedCopy(annotatorClasses)) {
			System.err.printf("===== %s =====\n", annotatorClass.getSimpleName());
			System.err.println(annotatorStats.get(annotatorClass));
		}
	}

	private Class<? extends JCasAnnotator_ImplBase> annotatorClass;

	private String[] trainingArguments;
	
	private float featureSelectionThreshold;

	public EvaluationOfTimeSpans(
			File baseDirectory,
			File rawTextDirectory,
			File xmlDirectory,
			XMLFormat xmlFormat,
			File xmiDirectory,
			File treebankDirectory,
			float featureSelectionThreshold,
			Class<? extends JCasAnnotator_ImplBase> annotatorClass,
					boolean printOverlapping,
					String[] trainingArguments) {
		super(baseDirectory, rawTextDirectory, xmlDirectory, xmlFormat, xmiDirectory, treebankDirectory, TimeMention.class);
		this.annotatorClass = annotatorClass;
		this.featureSelectionThreshold = featureSelectionThreshold;
		this.trainingArguments = trainingArguments;
		this.printOverlapping = printOverlapping;
	}

	@Override
	protected AnalysisEngineDescription getDataWriterDescription(File directory)
			throws ResourceInitializationException {
		if(MetaTimeAnnotator.class.isAssignableFrom(this.annotatorClass)){
			return MetaTimeAnnotator.getDataWriterDescription(CRFSuiteStringOutcomeDataWriter.class, directory);          
		}else if(CleartkAnnotator.class.isAssignableFrom(this.annotatorClass)){
			//limit feature selection only to TimeAnnotator
			if("org.apache.ctakes.temporal.ae.TimeAnnotator".equals(this.annotatorClass.getName())){
				Class<?> dataWriterClass = this.featureSelectionThreshold > 0f
				        ? InstanceDataWriter.class
				        : LIBLINEARStringOutcomeDataWriter.class;
				return TimeAnnotator.createDataWriterDescription(
						dataWriterClass,
						this.getModelDirectory(directory),
						this.featureSelectionThreshold);
			}
			return AnalysisEngineFactory.createPrimitiveDescription(
					this.annotatorClass,
					CleartkAnnotator.PARAM_IS_TRAINING,
					true,
					DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
					LIBLINEARStringOutcomeDataWriter.class,
					DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
					this.getModelDirectory(directory));
			
		}else if(CleartkSequenceAnnotator.class.isAssignableFrom(this.annotatorClass)){
			return AnalysisEngineFactory.createPrimitiveDescription(
					this.annotatorClass,
					CleartkSequenceAnnotator.PARAM_IS_TRAINING,
					true,
					DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
					CRFSuiteStringOutcomeDataWriter.class,
					DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
					this.getModelDirectory(directory));
		}else{
			throw new ResourceInitializationException("Annotator class was not recognized as an acceptable class!", new Object[]{});
		}
	}

	@Override
	protected void trainAndPackage(File directory) throws Exception {
		if (this.featureSelectionThreshold > 0 && "org.apache.ctakes.temporal.ae.TimeAnnotator".equals(this.annotatorClass.getName()) ) {
			// Extracting features and writing instances
			Iterable<Instance<String>> instances = InstanceStream.loadFromDirectory(this.getModelDirectory(directory));
			// Collect MinMax stats for feature normalization
			FeatureSelection<String> featureSelection = TimeAnnotator.createFeatureSelection(this.featureSelectionThreshold);
			featureSelection.train(instances);
			featureSelection.save(TimeAnnotator.createFeatureSelectionURI(this.getModelDirectory(directory)));
			// now write in the libsvm format
			LIBLINEARStringOutcomeDataWriter dataWriter = new LIBLINEARStringOutcomeDataWriter(this.getModelDirectory(directory));
			for (Instance<String> instance : instances) {
				dataWriter.write(featureSelection.transform(instance));
			}
			dataWriter.finish();
		}
		JarClassifierBuilder.trainAndPackage(this.getModelDirectory(directory), this.trainingArguments);
	}

	@Override
	protected AnalysisEngineDescription getAnnotatorDescription(File directory)
			throws ResourceInitializationException {
		if(MetaTimeAnnotator.class.isAssignableFrom(this.annotatorClass)){
			return MetaTimeAnnotator.getAnnotatorDescription(directory);
		}else if("org.apache.ctakes.temporal.ae.TimeAnnotator".equals(this.annotatorClass.getName() )){
			return TimeAnnotator.createAnnotatorDescription(this.getModelDirectory(directory));
		}
		return AnalysisEngineFactory.createPrimitiveDescription(
				this.annotatorClass,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				new File(this.getModelDirectory(directory), "model.jar"));
	}

	@Override
	protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas, Segment segment) {
		return selectExact(jCas, TimeMention.class, segment);
	}

	@Override
	protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas, Segment segment) {
		return selectExact(jCas, TimeMention.class, segment);
	}

	private File getModelDirectory(File directory) {
		return new File(directory, this.annotatorClass.getSimpleName());
	}
}
