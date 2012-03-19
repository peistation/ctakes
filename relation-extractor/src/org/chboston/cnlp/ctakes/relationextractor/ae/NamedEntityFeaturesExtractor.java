package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.annotationpair.DistanceExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;

import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

public class NamedEntityFeaturesExtractor implements RelationFeaturesExtractor {

  private SimpleFeatureExtractor namedEntityType = new TypePathExtractor(
		  IdentifiedAnnotation.class,
      "typeID");

  /**
   * All extractors for mention 1, with features named to distinguish them from mention 2
   */
  private SimpleFeatureExtractor mention1FeaturesExtractor = new NamingExtractor(
      "mention1",
      namedEntityType);

  /**
   * All extractors for mention 2, with features named to distinguish them from mention 1
   */
  private SimpleFeatureExtractor mention2FeaturesExtractor = new NamingExtractor(
      "mention2",
      namedEntityType);

  /**
   * Number of named entities between the two mentions
   */
  private DistanceExtractor nEntityMentionsBetween = new DistanceExtractor(null, IdentifiedAnnotation.class);

  @Override
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2)
      throws AnalysisEngineProcessException {

    List<Feature> features = new ArrayList<Feature>();
    features.addAll(this.mention1FeaturesExtractor.extract(jCas, arg1));
    features.addAll(this.mention2FeaturesExtractor.extract(jCas, arg2));
    features.addAll(this.nEntityMentionsBetween.extract(jCas, arg1, arg2));

    // entity type of both mentions, concatenated
    int type1 = arg1.getTypeID();
    int type2 = arg2.getTypeID();
    features.add(new Feature("type1type2", String.format("%s_%s", type1, type2)));

    // is mention1 included in mention2?
    boolean begins1After2 = arg1.getBegin() >= arg2.getBegin();
    boolean ends1Before2 = arg1.getEnd() <= arg2.getEnd();
    features.add(new Feature("mention1InMention2", begins1After2 && ends1Before2));

    // is mention2 included in mention1?
    boolean begins2After1 = arg2.getBegin() >= arg1.getBegin();
    boolean ends2Before1 = arg2.getEnd() <= arg1.getEnd();
    features.add(new Feature("mention2InMention1", begins2After1 && ends2Before1));

    return features;
  }

}
