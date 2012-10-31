/*

* Copyright: (c) 2012  Children's Hospital Boston
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @author Dmitriy Dligach
*/
package org.chboston.cnlp.ctakes.relationextractor.pipelines;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.util.Options_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.JCasFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Run relation extraction AE on a single sentence.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationExtractorPipelineSingleCas {

	public static class Options extends Options_ImplBase {

		public static void main(String[] args) throws UIMAException, IOException {

			String sampleSentence = "Abdomen: Soft, nontender without hepatosplenomegaly or masses";
			
			TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(
					"../common-type-system/desc/common_type_system.xml");

			JCas jCas = JCasFactory.createJCas(typeSystemDescription);
			jCas.setDocumentText(sampleSentence);

			AnalysisEngine relationExtractor = AnalysisEngineFactory.createAnalysisEngineFromPath(
					"desc/analysis_engine/RelationExtractorAggregate.xml");

		  AnalysisEngine relationConsumer = AnalysisEngineFactory.createPrimitive(
	    		RelationExtractorConsumer.class);

			SimplePipeline.runPipeline(jCas, relationExtractor, relationConsumer);
		}
	}
}
