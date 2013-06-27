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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.DefaultChunkCreator;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.constituency.parser.ae.ConstituencyParser;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.OverlapAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
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
import org.apache.ctakes.temporal.ae.THYMEKnowtatorXMLReader;
import org.apache.ctakes.temporal.ae.THYMETreebankReader;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.Chunk;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.LookupWindowAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.component.ViewTextCopierAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ExternalResourceFactory;
import org.uimafit.factory.TypePrioritiesFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.lexicalscope.jewel.cli.Option;

public abstract class Evaluation_ImplBase<STATISTICS_TYPE> extends
    org.cleartk.eval.Evaluation_ImplBase<Integer, STATISTICS_TYPE> {

  public static final String GOLD_VIEW_NAME = "GoldView";

  static interface Options {

    @Option(longName = "text")
    public File getRawTextDirectory();

    @Option(longName = "xml")
    public File getKnowtatorXMLDirectory();

    @Option(longName = "xmi")
    public File getXMIDirectory();

    @Option(longName = "patients")
    public CommandLine.IntegerRanges getPatients();
    
    @Option(longName = "treebank", defaultToNull=true)
    public File getTreebankDirectory();
    
    @Option
    public boolean getGrid();
    
    @Option
    public boolean getPrintErrors();
    
    @Option
    public boolean getPrintOverlappingSpans();
    
    @Option(longName = "kernelParams", defaultToNull=true)
    public String getKernelParams();
  }

  protected File rawTextDirectory;

  protected File knowtatorXMLDirectory;

  protected File xmiDirectory;

  private boolean xmiExists;

  protected File treebankDirectory;
  
  protected boolean printErrors = false;
  
  protected boolean printOverlapping = false;
  
  protected String[] kernelParams;
  
  public Evaluation_ImplBase(
      File baseDirectory,
      File rawTextDirectory,
      File knowtatorXMLDirectory,
      File xmiDirectory,
      File treebankDirectory) {
    super(baseDirectory);
    this.rawTextDirectory = rawTextDirectory;
    this.knowtatorXMLDirectory = knowtatorXMLDirectory;
    this.xmiDirectory = xmiDirectory;
    this.xmiExists = this.xmiDirectory.exists() && this.xmiDirectory.listFiles().length > 0;
    this.treebankDirectory = treebankDirectory;
  }

  public void prepareXMIsFor(List<Integer> patientSets) throws Exception {
    boolean needsXMIs = false;
    for (File textFile : this.getFilesFor(patientSets)) {
      if (!getXMIFile(this.xmiDirectory, textFile).exists()) {
        needsXMIs = true;
        break;
      }
    }
    if (needsXMIs) {
      CollectionReader reader = this.getCollectionReader(patientSets);
      AnalysisEngine engine = this.getXMIWritingPreprocessorAggregateBuilder().createAggregate();
      SimplePipeline.runPipeline(reader, engine);
    }
  }
  
  private List<File> getFilesFor(List<Integer> patientSets) {
	  if ( !rawTextDirectory.exists() ) {
		  return Collections.emptyList();
	  }
	  List<File> files = new ArrayList<File>();
	  for (Integer set : patientSets) {
		  final int setNum = set;
		  for (File file : rawTextDirectory.listFiles(new FilenameFilter(){
			  @Override
			  public boolean accept(File dir, String name) {
				  return name.contains(String.format("ID%03d", setNum));
			  }})) {
			  // skip hidden files like .svn
			  if (!file.isHidden()) {
				  files.add(file);
			  } 
		  }
	  }
	  return files;
  }

  @Override
  protected CollectionReader getCollectionReader(List<Integer> patientSets) throws Exception {
    return UriCollectionReader.getCollectionReaderFromFiles(this.getFilesFor(patientSets));
  }

  protected AggregateBuilder getPreprocessorAggregateBuilder() throws Exception {
    return this.xmiExists
        ? this.getXMIReadingPreprocessorAggregateBuilder()
        : this.getXMIWritingPreprocessorAggregateBuilder();
  }

  protected AggregateBuilder getXMIReadingPreprocessorAggregateBuilder() throws UIMAException {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(UriToDocumentTextAnnotator.getDescription());
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIReader.class,
        XMIReader.PARAM_XMI_DIRECTORY,
        this.xmiDirectory));
    return aggregateBuilder;
  }

  protected AggregateBuilder getXMIWritingPreprocessorAggregateBuilder()
      throws Exception {
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(UriToDocumentTextAnnotator.getDescription());

    // read manual annotations into gold view
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

    // identify segments
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(SegmentsFromBracketedSectionTagsAnnotator.class));
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
        "org/apache/ctakes/postagger/models/mayo-pos.zip",
        POSTagger.TAG_DICTIONARY_PARAM,
        "org/apache/ctakes/postagger/models/tag.dictionary.txt",
        POSTagger.CASE_SENSITIVE_PARAM,
        true));

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
            new File("target/unpacked/org/apache/ctakes/dictionary/lookup/LookupDesc_Db.xml").getAbsoluteFile()),
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
            new File("target/unpacked/org/apache/ctakes/dictionary/lookup/OrangeBook").getAbsoluteFile())));

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

    // add constituency parser (or gold standard treebank if we have it)
    if(this.treebankDirectory != null){
    	aggregateBuilder.add(THYMETreebankReader.getDescription(this.treebankDirectory));
    	aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(TimexAnnotationCorrector.class));
    }else{
    	aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(ConstituencyParser.class));
    }

    // write out the CAS after all the above annotations
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIWriter.class,
        XMIWriter.PARAM_XMI_DIRECTORY,
        this.xmiDirectory));

    return aggregateBuilder;
  }

  public static <T extends Annotation> List<T> selectExact(JCas jCas, Class<T> annotationClass, Segment segment) {
    List<T> annotations = Lists.newArrayList();
    for (T annotation : JCasUtil.selectCovered(jCas, annotationClass, segment)) {
      if (annotation.getClass().equals(annotationClass)) {
        annotations.add(annotation);
      }
    }
    return annotations;
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

  public static class EventMentionRemover extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for (EventMention mention : Lists.newArrayList(JCasUtil.select(jCas, EventMention.class))) {
        mention.removeFromIndexes();
      }
    }
  }
  
  // replace this with SimpleSegmentWithTagsAnnotator if that code ever gets fixed
  public static class SegmentsFromBracketedSectionTagsAnnotator extends JCasAnnotator_ImplBase {
    private static Pattern SECTION_PATTERN = Pattern.compile(
        "(\\[start section id=\"?(.*?)\"?\\]).*?(\\[end section id=\"?(.*?)\"?\\])",
        Pattern.DOTALL);

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      Matcher matcher = SECTION_PATTERN.matcher(jCas.getDocumentText());
      while (matcher.find()) {
        Segment segment = new Segment(jCas);
        segment.setBegin(matcher.start() + matcher.group(1).length());
        segment.setEnd(matcher.end() - matcher.group(3).length());
        segment.setId(matcher.group(2));
        segment.addToIndexes();
      }
    }
  }

  static File getXMIFile(File xmiDirectory, File textFile) {
    return new File(xmiDirectory, textFile.getName() + ".xmi");
  }

  static File getXMIFile(File xmiDirectory, JCas jCas) throws AnalysisEngineProcessException {
    return getXMIFile(xmiDirectory, new File(ViewURIUtil.getURI(jCas).getPath()));
  }

  public static class XMIWriter extends JCasAnnotator_ImplBase {

    public static final String PARAM_XMI_DIRECTORY = "XMIDirectory";

    @ConfigurationParameter(name = PARAM_XMI_DIRECTORY, mandatory = true)
    private File xmiDirectory;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      if (!this.xmiDirectory.exists()) {
        this.xmiDirectory.mkdirs();
      }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      File xmiFile = getXMIFile(this.xmiDirectory, jCas);
      try {
        FileOutputStream outputStream = new FileOutputStream(xmiFile);
        try {
          XmiCasSerializer serializer = new XmiCasSerializer(jCas.getTypeSystem());
          ContentHandler handler = new XMLSerializer(outputStream, false).getContentHandler();
          serializer.serialize(jCas.getCas(), handler);
        } finally {
          outputStream.close();
        }
      } catch (SAXException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }

  public static class XMIReader extends JCasAnnotator_ImplBase {

    public static final String PARAM_XMI_DIRECTORY = "XMIDirectory";

    @ConfigurationParameter(name = PARAM_XMI_DIRECTORY, mandatory = true)
    private File xmiDirectory;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      File xmiFile = getXMIFile(this.xmiDirectory, jCas);
      try {
        FileInputStream inputStream = new FileInputStream(xmiFile);
        try {
          XmiCasDeserializer.deserialize(inputStream, jCas.getCas());
        } finally {
          inputStream.close();
        }
      } catch (SAXException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }
  
  public static class TimexAnnotationCorrector extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        e.printStackTrace();
        throw new AnalysisEngineProcessException();
      }
      for(TimeMention mention : JCasUtil.select(goldView, TimeMention.class)){
        // for each time expression, get the treebank node with the same span.
        List<TreebankNode> nodes = JCasUtil.selectCovered(systemView, TreebankNode.class, mention);
        TreebankNode sameSpanNode = null;
        for(TreebankNode node : nodes){
          if(node.getBegin() == mention.getBegin() && node.getEnd() == mention.getEnd()){
            sameSpanNode = node;
            break;
          }
        }
        if(sameSpanNode != null){
          // look at node at the position of the timex3.
          if(sameSpanNode.getNodeType().equals("PP")){
            // if it is a PP it should be moved down to the NP
            int numChildren = sameSpanNode.getChildren().size();
            if(numChildren == 2 && sameSpanNode.getChildren(0).getNodeType().equals("IN") && sameSpanNode.getChildren(1).getNodeType().equals("NP")){
              // move the time span to this node:
              TreebankNode mentionNode = sameSpanNode.getChildren(numChildren-1);
              mention.setBegin(mentionNode.getBegin());
              mention.setEnd(mentionNode.getEnd());
            }
          }
        }
      }
    }
  }


  public static class CopyFromGold extends JCasAnnotator_ImplBase {

    public static AnalysisEngineDescription getDescription(Class<?>... classes)
        throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(
          CopyFromGold.class,
          CopyFromGold.PARAM_ANNOTATION_CLASSES,
          classes);
    }

    public static final String PARAM_ANNOTATION_CLASSES = "AnnotationClasses";

    @ConfigurationParameter(name = PARAM_ANNOTATION_CLASSES, mandatory = true)
    private Class<? extends TOP>[] annotationClasses;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      for (Class<? extends TOP> annotationClass : this.annotationClasses) {
        for (TOP annotation : Lists.newArrayList(JCasUtil.select(systemView, annotationClass))) {
          if (annotation.getClass().equals(annotationClass)) {
            annotation.removeFromIndexes();
          }
        }
      }
      CasCopier copier = new CasCopier(goldView.getCas(), systemView.getCas());
      Feature sofaFeature = jCas.getTypeSystem().getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
      for (Class<? extends TOP> annotationClass : this.annotationClasses) {
        for (TOP annotation : JCasUtil.select(goldView, annotationClass)) {
          TOP copy = (TOP) copier.copyFs(annotation);
          if (copy instanceof Annotation) {
            copy.setFeatureValue(sofaFeature, systemView.getSofa());
          }
          copy.addToIndexes(systemView);
        }
      }
    }
  }
}
