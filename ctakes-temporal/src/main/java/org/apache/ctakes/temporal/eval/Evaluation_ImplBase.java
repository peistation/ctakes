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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.DefaultChunkCreator;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.OverlapAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.resource.FileResourceImpl;
import org.apache.ctakes.core.resource.JdbcConnectionResourceImpl;
import org.apache.ctakes.core.resource.LuceneIndexReaderResourceImpl;
import org.apache.ctakes.core.resource.SuffixMaxentModelResourceImpl;
import org.apache.ctakes.dependency.parser.ae.ClearParserDependencyParserAE;
import org.apache.ctakes.dependency.parser.ae.ClearParserSemanticRoleLabelerAE;
import org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.lvg.resource.LvgCmdApiResourceImpl;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.temporal.ae.THYMEKnowtatorXMLReader;
import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textspan.LookupWindowAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.component.ViewTextCopierAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ExternalResourceFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;
import com.lexicalscope.jewel.cli.Option;

public abstract class Evaluation_ImplBase<STATISTICS_TYPE> extends
    org.cleartk.eval.Evaluation_ImplBase<Integer, STATISTICS_TYPE> {

  public enum AnnotatorType {
    PART_OF_SPEECH_TAGS, UMLS_NAMED_ENTITIES, LEXICAL_VARIANTS, DEPENDENCIES, SEMANTIC_ROLES
  }

  protected final String GOLD_VIEW_NAME = "GoldView";

  static interface Options {

    @Option(longName = "text")
    public File getRawTextDirectory();

    @Option(longName = "xml")
    public File getKnowtatorXMLDirectory();

    @Option(longName = "patients")
    public CommandLine.IntegerRanges getPatients();
    
    @Option(longName = "downratio")
	public float getDownSampleRatio();

    @Option(longName = "featureSelect")
    public float getFeatureSelect(); //get feature selection cut off threshold is it is > 0. apply no FS if featureSelect == 0 
  }

  protected File rawTextDirectory;

  protected File knowtatorXMLDirectory;

  protected List<Integer> patientSets;

  private Set<AnnotatorType> annotatorFlags;

  public Evaluation_ImplBase(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      List<Integer> patientSets,
      Set<AnnotatorType> annotatorFlags) {
    super(baseDirectory);
    this.rawTextDirectory = rawTextDirectory;
    this.knowtatorXMLDirectory = knowtatorXMLDirectory;
    this.patientSets = patientSets;
    this.annotatorFlags = annotatorFlags;
  }

  public List<STATISTICS_TYPE> crossValidation(int nFolds) throws Exception {
    return this.crossValidation(this.patientSets, nFolds);
  }

  @Override
  protected CollectionReader getCollectionReader(List<Integer> selectedPatientSets) throws Exception {
    List<File> files = new ArrayList<File>();
    for (Integer set : selectedPatientSets) {
      File setTextDirectory = new File(this.rawTextDirectory, "doc" + set);
      for (File file : setTextDirectory.listFiles()) {
        files.add(file);
      }
    }
    return UriCollectionReader.getCollectionReaderFromFiles(files);
  }

  protected AnalysisEngineDescription getPreprocessorTrainDescription() throws Exception {
    return this.getPreprocessorDescription(PipelineType.TRAIN);
  }

  protected AnalysisEngineDescription getPreprocessorTestDescription() throws Exception {
    return this.getPreprocessorDescription(PipelineType.TEST);
  }

  protected List<Class<? extends TOP>> getAnnotationClassesThatShouldBeGoldAtTestTime() {
    return new ArrayList<Class<? extends TOP>>();
  }

  private static enum PipelineType {
    TRAIN, TEST
  }

  private AnalysisEngineDescription getPreprocessorDescription(PipelineType pipelineType)
      throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(UriToDocumentTextAnnotator.getDescription());
    switch (pipelineType) {
      case TRAIN:
        aggregateBuilder.add(THYMEKnowtatorXMLReader.getDescription(this.knowtatorXMLDirectory));
        break;
      case TEST:
        aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
            ViewCreatorAnnotator.class,
            ViewCreatorAnnotator.PARAM_VIEW_NAME,
            GOLD_VIEW_NAME));
        aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
            ViewTextCopierAnnotator.class,
            ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME,
            CAS.NAME_DEFAULT_SOFA,
            ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME,
            GOLD_VIEW_NAME));
        aggregateBuilder.add(
            THYMEKnowtatorXMLReader.getDescription(this.knowtatorXMLDirectory),
            CAS.NAME_DEFAULT_SOFA,
            GOLD_VIEW_NAME);
        for (Class<? extends TOP> annotationClass : this.getAnnotationClassesThatShouldBeGoldAtTestTime()) {
          aggregateBuilder.add(AnnotationCopier.getDescription(
              GOLD_VIEW_NAME,
              CAS.NAME_DEFAULT_SOFA,
              annotationClass));
        }
        break;
    }
    // identify segments
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(SimpleSegmentAnnotator.class));
    // identify sentences
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        SentenceDetector.class,
        "MaxentModel",
        ExternalResourceFactory.createExternalResourceDescription(
            SuffixMaxentModelResourceImpl.class,
            SentenceDetector.class.getResource("../sentdetect/sdmed.mod"))));
    // identify tokens
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(TokenizerAnnotatorPTB.class));
    // merge some tokens
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ContextDependentTokenizerAnnotator.class));

    // identify part-of-speech tags if requested
    if (this.annotatorFlags.contains(AnnotatorType.PART_OF_SPEECH_TAGS)) {
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          POSTagger.class,
          POSTagger.POS_MODEL_FILE_PARAM,
          "org/apache/ctakes/postagger/models/mayo-pos.zip",
          POSTagger.TAG_DICTIONARY_PARAM,
          "org/apache/ctakes/postagger/models/tag.dictionary.txt",
          POSTagger.CASE_SENSITIVE_PARAM,
          true));
    }

    // identify UMLS named entities if requested
    if (this.annotatorFlags.contains(AnnotatorType.UMLS_NAMED_ENTITIES)) {
      // remove gold mentions if they're there (we'll add cTAKES mentions later instead)
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(EntityMentionRemover.class));
      // identify chunks
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          Chunker.class,
          Chunker.CHUNKER_MODEL_FILE_PARAM,
          Chunker.class.getResource("../models/chunk-model.claims-1.5.zip").toURI().getPath(),
          Chunker.CHUNKER_CREATOR_CLASS_PARAM,
          DefaultChunkCreator.class));
      // adjust NP in NP NP to span both
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          ChunkAdjuster.class,
          ChunkAdjuster.PARAM_CHUNK_PATTERN,
          new String[] { "NP", "NP" },
          ChunkAdjuster.PARAM_EXTEND_TO_INCLUDE_TOKEN,
          1));
      // adjust NP in NP PP NP to span all three
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          ChunkAdjuster.class,
          ChunkAdjuster.PARAM_CHUNK_PATTERN,
          new String[] { "NP", "PP", "NP" },
          ChunkAdjuster.PARAM_EXTEND_TO_INCLUDE_TOKEN,
          2));
      // add lookup windows for each NP
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(CopyNPChunksToLookupWindowAnnotations.class));
      // maximize lookup windows
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          OverlapAnnotator.class,
          "A_ObjectClass",
          LookupWindowAnnotation.class,
          "B_ObjectClass",
          LookupWindowAnnotation.class,
          "OverlapType",
          "A_ENV_B",
          "ActionType",
          "DELETE",
          "DeleteAction",
          new String[] { "selector=B" }));
      // add UMLS on top of lookup windows
      String umlsUser = System.getProperty("umls.user");
      String umlsPassword = System.getProperty("umls.password");
      if (umlsUser == null || umlsPassword == null) {
        throw new IllegalArgumentException(
            "The properties umls.user and umls.password must be set to use the "
                + "UmlsDictionaryLookupAnnotator. You can set them by provding java with the "
                + "arguments -Dumls.user=... and -Dumls.password=...");
      }
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
          UmlsDictionaryLookupAnnotator.class,
          "UMLSAddr",
          "https://uts-ws.nlm.nih.gov/restful/isValidUMLSUser",
          "UMLSVendor",
          "NLM-6515182895",
          "UMLSUser",
          umlsUser,
          "UMLSPW",
          umlsPassword,
          "LookupDescriptor",
          ExternalResourceFactory.createExternalResourceDescription(
              FileResourceImpl.class,
              getResourceAsFile(UmlsDictionaryLookupAnnotator.class, "../LookupDesc_Db.xml")),
          "DbConnection",
          ExternalResourceFactory.createExternalResourceDescription(
              JdbcConnectionResourceImpl.class,
              "",
              JdbcConnectionResourceImpl.PARAM_DRIVER_CLASS,
              "org.hsqldb.jdbcDriver",
              JdbcConnectionResourceImpl.PARAM_URL,
              "jdbc:hsqldb:res:/org/apache/ctakes/dictionary/lookup/umls2011ab/umls"),
          "RxnormIndexReader",
          ExternalResourceFactory.createExternalResourceDescription(
              LuceneIndexReaderResourceImpl.class,
              "",
              "UseMemoryIndex",
              true,
              "IndexDirectory",
              getResourceAsFile(UmlsDictionaryLookupAnnotator.class, "../rxnorm_index")),
          "OrangeBookIndexReader",
          ExternalResourceFactory.createExternalResourceDescription(
              LuceneIndexReaderResourceImpl.class,
              "",
              "UseMemoryIndex",
              true,
              "IndexDirectory",
              getResourceAsFile(UmlsDictionaryLookupAnnotator.class, "../OrangeBook"))));
    }

    // add lvg annotator
    if (this.annotatorFlags.contains(AnnotatorType.LEXICAL_VARIANTS)) {
      String[] XeroxTreebankMap = {
          "adj|JJ",
          "adv|RB",
          "aux|AUX",
          "compl|CS",
          "conj|CC",
          "det|DET",
          "modal|MD",
          "noun|NN",
          "prep|IN",
          "pron|PRP",
          "verb|VB" };
      String[] ExclusionSet = {
          "and",
          "And",
          "by",
          "By",
          "for",
          "For",
          "in",
          "In",
          "of",
          "Of",
          "on",
          "On",
          "the",
          "The",
          "to",
          "To",
          "with",
          "With" };
      AnalysisEngineDescription lvgAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
          LvgAnnotator.class,
          "UseSegments",
          false,
          "SegmentsToSkip",
          new String[0],
          "UseCmdCache",
          false,
          "CmdCacheFileLocation",
          "/org/apache/ctakes/lvg/2005_norm.voc",
          "CmdCacheFrequencyCutoff",
          20,
          "ExclusionSet",
          ExclusionSet,
          "XeroxTreebankMap",
          XeroxTreebankMap,
          "LemmaCacheFileLocation",
          "/org/apache/ctakes/lvg/2005_lemma.voc",
          "UseLemmaCache",
          false,
          "LemmaCacheFrequencyCutoff",
          20,
          "PostLemmas",
          true,
          "LvgCmdApi",
          ExternalResourceFactory.createExternalResourceDescription(
              LvgCmdApiResourceImpl.class,
              getResourceAsFile(LvgAnnotator.class, "../data/config/lvg.properties")));
      aggregateBuilder.add(lvgAnnotator);
    }

    // add dependency parser
    if (this.annotatorFlags.contains(AnnotatorType.DEPENDENCIES)) {
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ClearParserDependencyParserAE.class));
    }

    // add semantic role labeler
    if (this.annotatorFlags.contains(AnnotatorType.SEMANTIC_ROLES)) {
      aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ClearParserSemanticRoleLabelerAE.class));
    }
    return aggregateBuilder.createAggregateDescription();
  }

  /**
   * This is hack to deal with classes that don't handle resources correctly
   */
  private static File getResourceAsFile(Class<?> cls, String path) throws URISyntaxException {
    // this will fail if the resource is not a real File, but the UMLS code assumes that
    return new File(cls.getResource(path).toURI());
  }

  public static class CopyNPChunksToLookupWindowAnnotations extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (Chunk chunk : JCasUtil.select(jCas, Chunk.class)) {
        if (chunk.getChunkType().equals("NP")) {
          new LookupWindowAnnotation(jCas, chunk.getBegin(), chunk.getEnd()).addToIndexes();
        }
      }
    }
  }

  public static class EntityMentionRemover extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (EntityMention mention : Lists.newArrayList(JCasUtil.select(jCas, EntityMention.class))) {
        mention.removeFromIndexes();
      }
    }
  }
}
