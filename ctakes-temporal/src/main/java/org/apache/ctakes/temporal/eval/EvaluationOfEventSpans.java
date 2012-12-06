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
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

import org.apache.ctakes.temporal.ae.EventAnnotator;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.util.JCasUtil;

import com.lexicalscope.jewel.cli.CliFactory;

public class EvaluationOfEventSpans extends EvaluationOfAnnotationSpans_ImplBase {

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    EvaluationOfEventSpans evaluation = new EvaluationOfEventSpans(
        new File("target/eval"),
        options.getRawTextDirectory(),
        options.getKnowtatorXMLDirectory(),
        options.getPatients().getList(),
        options.getDownSampleRatio(),
    	options.getFeatureSelect()); //control apply feature selection or not
    evaluation.setLogging(Level.FINE, new File("target/eval/ctakes-event-errors.log"));
    List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(4);
    for (AnnotationStatistics<String> stats : foldStats) {
      System.err.println(stats);
    }
    System.err.println("OVERALL");
    System.err.println(AnnotationStatistics.addAll(foldStats));
  }
  
  private float downratio;
  private float featureTrim;

  public EvaluationOfEventSpans(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      List<Integer> patientSets,
      float downratio, float featureSelect) {
    super(
        baseDirectory,
        rawTextDirectory,
        knowtatorXMLDirectory,
        patientSets,
        EnumSet.of(AnnotatorType.PART_OF_SPEECH_TAGS,
        //AnnotatorType.UMLS_NAMED_ENTITIES,
//        AnnotatorType.LEXICAL_VARIANTS,
        AnnotatorType.DEPENDENCIES,
        AnnotatorType.SEMANTIC_ROLES));
    this.downratio = downratio;
    this.featureTrim = featureSelect;
  }

  @Override
  protected AnalysisEngineDescription getDataWriterDescription(File directory)
      throws ResourceInitializationException {
	if(this.featureTrim > 0){
		return EventAnnotator.createDataWriterDescription(
		    	InstanceDataWriter.class.getName(),
		        directory,
		        this.downratio,
		        this.featureTrim);
	}
	return EventAnnotator.createDataWriterDescription(
	        LIBSVMStringOutcomeDataWriter.class.getName(),
	        directory,
	        this.downratio,
	        this.featureTrim);
	
    
  }

  @Override
  protected void trainAndPackage(File directory) throws Exception {
    JarClassifierBuilder.trainAndPackage(directory, "-c", "10000");
  }

  @Override
  protected List<Class<? extends TOP>> getAnnotationClassesThatShouldBeGoldAtTestTime() {
    List<Class<? extends TOP>> result = super.getAnnotationClassesThatShouldBeGoldAtTestTime();
    result.add(EntityMention.class);
    return result;
  }

  @Override
  protected AnalysisEngineDescription getAnnotatorDescription(File directory)
      throws ResourceInitializationException {
    return EventAnnotator.createAnnotatorDescription(directory);
  }

  @Override
  protected Collection<? extends Annotation> getGoldAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, EventMention.class);
  }

  @Override
  protected Collection<? extends Annotation> getSystemAnnotations(JCas jCas) {
    return JCasUtil.select(jCas, EventMention.class);
  }
}
