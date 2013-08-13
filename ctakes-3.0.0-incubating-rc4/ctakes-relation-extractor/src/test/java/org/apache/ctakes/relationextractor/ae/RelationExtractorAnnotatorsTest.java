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
package org.apache.ctakes.relationextractor.ae;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.junit.Test;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

public class RelationExtractorAnnotatorsTest {

  @Test
  public void test() throws Exception {
    // create the pipeline
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(this.findDescription(ModifierExtractorAnnotator.class));
    builder.add(this.findDescription(DegreeOfRelationExtractorAnnotator.class));
    builder.add(this.findDescription(EntityMentionPairRelationExtractorAnnotator.class));
    AnalysisEngine engine = builder.createAggregate();
    JCas jCas = engine.newJCas();

    // populate the CAS with an example sentence
    // TODO: add annotations to support phrase chunk and dependency features
    TokenBuilder<BaseToken, Sentence> tokenBuilder = new TokenBuilder<BaseToken, Sentence>(
        BaseToken.class,
        Sentence.class,
        "partOfSpeech",
        null);
    tokenBuilder.buildTokens(
        jCas,
        "He had a slight fracture in the proximal right fibula.",
        "He had a slight fracture in the proximal right fibula .",
        "PRP VBD DT JJ NN IN DT JJ JJ NN .");
    EntityMention fracture = new EntityMention(jCas, 16, 24);
    fracture.setTypeID(CONST.NE_TYPE_ID_DISORDER);
    fracture.addToIndexes();
    assertEquals("fracture", fracture.getCoveredText());
    EntityMention fibula = new EntityMention(jCas, 32, 53);
    fibula.setTypeID(CONST.NE_TYPE_ID_ANATOMICAL_SITE);
    fibula.addToIndexes();
    assertEquals("proximal right fibula", fibula.getCoveredText());

    // run the analysis engine
    engine.process(jCas);

    // test the modifier annotator
    Collection<Modifier> modifiers = JCasUtil.select(jCas, Modifier.class);
    assertEquals(1, modifiers.size());
    Modifier slight = modifiers.iterator().next();
    assertEquals("slight", slight.getCoveredText());

    // test the relation annotators
    Collection<BinaryTextRelation> relations = JCasUtil.select(jCas, BinaryTextRelation.class);
    assertEquals(2, relations.size());
    Iterator<BinaryTextRelation> iterator = relations.iterator();
    BinaryTextRelation slightFracture = iterator.next();
    assertEquals("degree_of", slightFracture.getCategory());
    assertEquals(fracture, slightFracture.getArg1().getArgument());
    assertEquals(slight, slightFracture.getArg2().getArgument());
    BinaryTextRelation fractureFibula = iterator.next();
    assertEquals("location_of", fractureFibula.getCategory());
    // TODO: this seems backwards, but maybe that's how it's supposed to be?
    assertEquals(fibula, fractureFibula.getArg1().getArgument());
    assertEquals(fracture, fractureFibula.getArg2().getArgument());
  }

  private AnalysisEngineDescription findDescription(Class<? extends JCasAnnotator_ImplBase> cls)
      throws Exception {
    File directory = new File("desc/analysis_engine");
    File file = new File(directory, cls.getSimpleName() + ".xml");
    XMLParser parser = UIMAFramework.getXMLParser();
    XMLInputSource source = new XMLInputSource(file);
    return parser.parseAnalysisEngineDescription(source);
  }

}
