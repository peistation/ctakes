package org.chboston.cnlp.ctakes.relationextractor.ae;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;

import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class NamedEntityFeaturesExtractorTest {

  @Test
  public void test() throws Exception {
    // create and populate a JCas with some EntityMention objects
    JCas jCas = JCasFactory.createJCasFromPath("../common-type-system/desc/common_type_system.xml");
    jCas.setDocumentText("aaa bbb ccc ddd");
    EntityMention e1 = new EntityMention(jCas, 0, 3);
    e1.setTypeID(42);
    e1.addToIndexes();
    EntityMention e2 = new EntityMention(jCas, 8, 11);
    e2.setTypeID(1);
    e2.addToIndexes();
    EntityMention between = new EntityMention(jCas, 4, 7);
    between.addToIndexes();
    
    // run the feature extractor over the JCas
    NamedEntityFeaturesExtractor extractor = new NamedEntityFeaturesExtractor();
    List<Feature> features = extractor.extract(jCas, e1, e2);
    
    // make sure that the features that we expect are there
    assertTrue(features.contains(new Feature("mention1_TypeID", "42")));
    assertTrue(features.contains(new Feature("mention2_TypeID", "1")));
    assertTrue(features.contains(new Feature("Distance_EntityMention", 1)));
    assertTrue(features.contains(new Feature("type1type2", "42_1")));
    assertTrue(features.contains(new Feature("mention1InMention2", false)));
    assertTrue(features.contains(new Feature("mention2InMention1", false)));
  }
}
