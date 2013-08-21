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
package org.apache.ctakes.temporal.pipelines;

import java.io.File;

import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.DefaultChunkCreator;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.constituency.parser.ae.ConstituencyParser;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.OverlapAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.resource.FileResourceImpl;
import org.apache.ctakes.core.resource.JdbcConnectionResourceImpl;
import org.apache.ctakes.core.resource.LuceneIndexReaderResourceImpl;
import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE;
import org.apache.ctakes.dependency.parser.ae.ClearNLPSemanticRoleLabelerAE;
import org.apache.ctakes.dictionary.lookup.ae.UmlsDictionaryLookupAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.lvg.resource.LvgCmdApiResourceImpl;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.temporal.ae.TimeAnnotator;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.CopyNPChunksToLookupWindowAnnotations;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textspan.LookupWindowAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ExternalResourceFactory;
import org.uimafit.factory.TypePrioritiesFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Given a temporal expression extraction model, run the time expression extractor 
 * on files in a directory of notes. Save the resulting annotations in XMI files. 
 * 
 * @author dmitriy dligach
 */
public class TimeExtractionPipeline {
  
  public static class Options extends Options_ImplBase {

    @Option(
        name = "--input-dir",
        usage = "specify the path to the directory containing the clinical notes to be processed",
        required = true)
    public String inputDirectory;
    
    @Option(
        name = "--output-dir",
        usage = "specify the path to the directory where the output xmi files are to be saved",
        required = true)
    public String outputDirectory;
        
    @Option(
        name = "--time-model-dir",
        usage = "specify the path to the directory where the temporal expression model is located",
        required = false)
    public String timeModelDirectory = "target/eval/time-spans/train_and_test/BackwardsTimeAnnotator/";
  }
  
	public static void main(String[] args) throws Exception {
		
		Options options = new Options();
		options.parseOptions(args);

		CollectionReader collectionReader = CollectionReaderFactory.createCollectionReaderFromPath(
				"../ctakes-core/desc/collection_reader/FilesInDirectoryCollectionReader.xml",
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR,
				options.inputDirectory);

		AggregateBuilder aggregateBuilder = getPreprocessorAggregateBuilder();
		aggregateBuilder.add(TimeAnnotator.createAnnotatorDescription(new File(options.timeModelDirectory)));
		
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
        XWriter.class,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        options.outputDirectory);
		
    SimplePipeline.runPipeline(
        collectionReader,
        aggregateBuilder.createAggregate(),
        xWriter);
	}
	
	/**
	 * Preprocessing needed for relation extraction.
	 */
  protected static AggregateBuilder getPreprocessorAggregateBuilder()
      throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    
    // identify segments; use simple segment annotator on non-mayo notes
    // aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(SegmentsFromBracketedSectionTagsAnnotator.class));
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(SimpleSegmentAnnotator.class));
    
    // identify sentences
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
            SentenceDetector.class,
            SentenceDetector.SD_MODEL_FILE_PARAM,
            "org/apache/ctakes/core/sentdetect/sd-med-model.zip"));
    // identify tokens
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(TokenizerAnnotatorPTB.class));
    // merge some tokens
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ContextDependentTokenizerAnnotator.class));

    // identify part-of-speech tags
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        POSTagger.class,
        TypeSystemDescriptionFactory.createTypeSystemDescription(),
        TypePrioritiesFactory.createTypePriorities(Segment.class, Sentence.class, BaseToken.class),
        POSTagger.POS_MODEL_FILE_PARAM,
        "org/apache/ctakes/postagger/models/mayo-pos.zip"));

    // identify chunks
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        Chunker.class,
        Chunker.CHUNKER_MODEL_FILE_PARAM,
        FileLocator.locateFile("org/apache/ctakes/chunker/models/chunk-model.claims-1.5.zip"),
        Chunker.CHUNKER_CREATOR_CLASS_PARAM,
        DefaultChunkCreator.class));

    // identify UMLS named entities

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
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        UmlsDictionaryLookupAnnotator.class,
        "ctakes.umlsaddr",
        "https://uts-ws.nlm.nih.gov/restful/isValidUMLSUser",
        "ctakes.umlsvendor",
        "NLM-6515182895",
        "LookupDescriptor",
        ExternalResourceFactory.createExternalResourceDescription(
            FileResourceImpl.class,
            FileLocator.locateFile("org/apache/ctakes/dictionary/lookup/LookupDesc_Db.xml")),
        "DbConnection",
        ExternalResourceFactory.createExternalResourceDescription(
            JdbcConnectionResourceImpl.class,
            "",
            JdbcConnectionResourceImpl.PARAM_DRIVER_CLASS,
            "org.hsqldb.jdbcDriver",
            JdbcConnectionResourceImpl.PARAM_URL,
            // Should be the following but it's WAY too slow
            // "jdbc:hsqldb:res:/org/apache/ctakes/dictionary/lookup/umls2011ab/umls"),
            "jdbc:hsqldb:file:target/unpacked/org/apache/ctakes/dictionary/lookup/umls2011ab/umls"),
        "RxnormIndexReader",
        ExternalResourceFactory.createExternalResourceDescription(
            LuceneIndexReaderResourceImpl.class,
            "",
            "UseMemoryIndex",
            true,
            "IndexDirectory",
            new File("target/unpacked/org/apache/ctakes/dictionary/lookup/rxnorm_index").getAbsoluteFile()),
        "OrangeBookIndexReader",
        ExternalResourceFactory.createExternalResourceDescription(
            LuceneIndexReaderResourceImpl.class,
            "",
            "UseMemoryIndex",
            true,
            "IndexDirectory",
            "org/apache/ctakes/dictionary/lookup/OrangeBook")));

    // add lvg annotator
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
            new File(LvgCmdApiResourceImpl.class.getResource(
                "/org/apache/ctakes/lvg/data/config/lvg.properties").toURI())));
    aggregateBuilder.add(lvgAnnotator);

    // add dependency parser
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ClearNLPDependencyParserAE.class));

    // add semantic role labeler
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ClearNLPSemanticRoleLabelerAE.class));

    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ConstituencyParser.class));
    
    return aggregateBuilder;
  }
}
