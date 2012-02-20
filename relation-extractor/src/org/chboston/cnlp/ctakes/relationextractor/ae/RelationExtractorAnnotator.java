/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
@author
 * 
 */
package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.textspan.Sentence;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class RelationExtractorAnnotator extends CleartkAnnotator<String> {

  /**
   * The list of feature extractors used by the classifier.
   */
  private List<RelationFeaturesExtractor> featureExtractors = Arrays.asList(
      new TokenFeaturesExtractor(),
      new PartOfSpeechFeaturesExtractor(),
      new ChunkerExtractor(),
      new NamedEntityFeaturesExtractor(),
      new DependencyTreeFeaturesExtractor(),
      new DependencyPathFeaturesExtractor());

  /*
   * Implement the standard UIMA process method.
   */
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    // walk through each sentence in the text
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      
      // collect all possible relation arguments from the sentence
      List<IdentifiedAnnotation> args = JCasUtil.selectCovered(
          jCas,
          IdentifiedAnnotation.class,
          sentence);
      
      // walk through the pairs
      for (int i = 0; i < args.size(); ++i) {
      	IdentifiedAnnotation arg1 = args.get(i);
        for (int j = i + 1; j < args.size(); ++j) {
      	  IdentifiedAnnotation arg2 = args.get(j);
  
          // apply all the feature extractors to extract the list of features
          List<Feature> features = new ArrayList<Feature>();
          for (RelationFeaturesExtractor extractor : this.featureExtractors) {
            features.addAll(extractor.extract(jCas, arg1, arg2));
          }
                  
          // during training, feed the features to the data writer
          if (this.isTraining()) {
            // TODO: load the relation label from the CAS
            String category = "NO_LABEL_FOR_NOW";
  
            // create a classification instance and write it to the training data
            this.dataWriter.write(new Instance<String>(category, features));
          }
  
          // during classification feed the features to the classifier and create annotations
          else {
            String category = this.classifier.classify(features);
            if (category != null) {
              // TODO: add relation to CAS
            }
          }
        }
      }
    }
  }
}