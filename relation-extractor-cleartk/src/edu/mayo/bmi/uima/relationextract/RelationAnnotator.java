package edu.mayo.bmi.uima.relationextract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.relation.RelationArgument;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class RelationAnnotator extends CleartkAnnotator<String> {

  /**
   * The list of feature extractors used by the classifier. (See below for the interface.)
   */
  private List<FeatureExtractor> featureExtractors = Arrays.asList(
      new CoveredTextExtractor(),
      new SurroundingWordsExtractor());

  /*
   * Implement the standard UIMA process method.
   */
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // map arguments to relations
    Map<Annotation, Map<Annotation, BinaryTextRelation>> relationLookup;
    relationLookup = this.getRelationLookup(jCas);

    // collect all possible relation arguments from the CAS (using UimaFIT for simplicity)
    Collection<IdentifiedAnnotation> args = JCasUtil.select(jCas, IdentifiedAnnotation.class);

    // walk through all pairs of arguments
    for (IdentifiedAnnotation arg1 : args) {
      for (IdentifiedAnnotation arg2 : args) {

        // apply all the feature extractors to extract the list of features
        List<Feature> features = new ArrayList<Feature>();
        for (FeatureExtractor extractor : this.featureExtractors) {
          features.addAll(extractor.extract(jCas, arg1, arg2));
        }

        // during training, feed the features to the data writer
        if (this.isTraining()) {

          // look up the relation (if any) for this pair of arguments
          BinaryTextRelation relation = null;
          if (relationLookup.containsKey(arg1)) {
            relation = relationLookup.get(arg1).get(arg2);
          }

          // create a classification instance and write it to the training data
          this.dataWriter.write(new Instance<String>(relation.getCategory(), features));
        }

        // during classification feed the features to the classifier and create annotations
        else {
          String category = this.classifier.classify(features);
          if (category != null) {

            // do the dance to create a relation in the CAS under the crazy SHARP type system
            RelationArgument relArg1 = new RelationArgument(jCas);
            relArg1.setArgument(arg1);
            relArg1.addToIndexes();
            RelationArgument relArg2 = new RelationArgument(jCas);
            relArg2.setArgument(arg2);
            relArg2.addToIndexes();
            BinaryTextRelation relation = new BinaryTextRelation(jCas);
            relation.setArg1(relArg1);
            relation.setArg2(relArg2);
            relation.setCategory(category);
            relation.addToIndexes();
          }
        }
      }
    }
  }

  /**
   * Map arguments to relations for easy lookup.
   * 
   * @param jCas
   *          The CAS containing the relations and their arguments.
   * @return A map from arg1 to arg2 to relation.
   */
  private Map<Annotation, Map<Annotation, BinaryTextRelation>> getRelationLookup(JCas jCas) {
    // (this whole method would be 1 line of Scala code, but Java sucks)
    Map<Annotation, Map<Annotation, BinaryTextRelation>> relationLookup;
    relationLookup = new HashMap<Annotation, Map<Annotation, BinaryTextRelation>>();
    for (BinaryTextRelation relation : JCasUtil.select(jCas, BinaryTextRelation.class)) {
      RelationArgument arg1 = relation.getArg1();
      RelationArgument arg2 = relation.getArg2();
      if (arg1 != null && arg2 != null) {
        if (!relationLookup.containsKey(arg1)) {
          relationLookup.put(arg1.getArgument(), new HashMap<Annotation, BinaryTextRelation>());
        }
        relationLookup.get(arg1).put(arg2.getArgument(), relation);
      }
    }
    return relationLookup;
  }
}