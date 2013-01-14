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
package org.apache.ctakes.relationextractor.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.Level;
import org.apache.uima.util.XMLInputSource;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.libsvm.LIBSVMStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import org.apache.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.EntityMentionPairRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.Modifier;

public class RelationExtractorEvaluation extends Evaluation_ImplBase<File, AnnotationStatistics<String>> {

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--train-dir",
        usage = "specify the directory contraining the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
        required = true)
    public File trainDirectory;

    @Option(
        name = "--dev-dir",
        usage = "specify the directory contraining the XMI development files (for example, /NLP/Corpus/Relations/mipacq/xmi/dev)",
        required = false)
    public File devDirectory;
    
    @Option(
        name = "--test-dir",
        usage = "specify the directory contraining the XMI testing files (for example, /NLP/Corpus/Relations/mipacq/xmi/test)",
        required = false)
    public File testDirectory;
    
    @Option(name = "--grid-search", usage = "run a grid search to select the best parameters")
    public boolean gridSearch = false;

    @Option(
        name = "--relations",
        usage = "determines which relations to evaluate on (separately)",
        required = false)
    public List<String> relations = null;

    @Option(
        name = "--test-on-ctakes",
        usage = "evaluate test performance on ctakes entities, instead of gold standard entities")
    public boolean testOnCTakes = false;

    @Option(
        name = "--allow-smaller-system-arguments",
        usage = "for evaluation, allow system relation arguments to match gold relation arguments that enclose them")
    public boolean allowSmallerSystemArguments = false;

    @Option(
        name = "--ignore-impossible-gold-relations",
        usage = "for evaluation, ignore gold relations that would be impossible to find because there are no corresponding system mentions")
    public boolean ignoreImpossibleGoldRelations = false;

    @Option(
        name = "--print-errors",
        usage = "print relations that were incorrectly predicted")
    public boolean printErrors = false;

  }

  public static final String GOLD_VIEW_NAME = "GoldView";
  
  // parameter settings currently optimized for SHARP data
  private static final ParameterSettings BEST_DEGREE_OF_PARAMETERS = new ParameterSettings(false, 1.0f, "radial basis function", 10.0, 0.0010);
  private static final ParameterSettings BEST_NON_DEGREE_OF_PARAMETERS = new ParameterSettings(false, 1.0f, "radial basis function", 10.0, 0.01);
  
  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);
    if (options.relations == null) {
      options.relations = Arrays.asList("degree_of", "location_of");
    }

    // error on invalid option combinations
    if (options.testDirectory != null && options.gridSearch) {
      throw new IllegalArgumentException("grid search can only be run on the train or dev sets");
    }

    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());

    for (String relationCategory : options.relations) {

      // define the output directory for models
      File modelsDir = new File("target/models/" + relationCategory);

      // determine class for the classifier annotator
      boolean isDegreeOf = relationCategory.equals("degree_of");
      Class<? extends RelationExtractorAnnotator> annotatorClass = isDegreeOf
          ? DegreeOfRelationExtractorAnnotator.class
          : EntityMentionPairRelationExtractorAnnotator.class;

      // determine the type of classifier to be trained
      Class<? extends DataWriter<String>> dataWriterClass = LIBSVMStringOutcomeDataWriter.class;

      // define the set of possible training parameters
      List<ParameterSettings> possibleParams = Lists.newArrayList();
      if (options.gridSearch) {
        boolean[] classifyBothDirectionsOptions = isDegreeOf
            ? new boolean[] { false }
            : new boolean[] { false, true };
        for (boolean classifyBothDirections : classifyBothDirectionsOptions) {
          for (float probabilityOfKeepingANegativeExample : new float[] { 0.25f, 0.5f, 1.0f }) {
            // linear kernels
            for (double svmCost : new double[] { 0.05, 0.1, 0.5, 1 }) {
              possibleParams.add(new ParameterSettings(
                  classifyBothDirections,
                  probabilityOfKeepingANegativeExample,
                  "linear",
                  svmCost,
                  1.0));
            }
            // RBF kernels
            for (double svmCost : new double[] { 1, 10, 100 }) {
              for (double gamma : new double[] { 0.001, 0.01, 0.1 }) {
                possibleParams.add(new ParameterSettings(
                  classifyBothDirections,
                  probabilityOfKeepingANegativeExample,
                  "radial basis function",
                  svmCost,
                  gamma));
              }
            }
          }
        }
      } else if (isDegreeOf) {
        possibleParams.add(BEST_DEGREE_OF_PARAMETERS);
      } else {
        possibleParams.add(BEST_NON_DEGREE_OF_PARAMETERS);
      }

      // run an evaluation for each set of parameters
      Map<ParameterSettings, Double> scoredParams = new HashMap<ParameterSettings, Double>();
      for (ParameterSettings params : possibleParams) {
        System.err.println(relationCategory + ": " + params);
        System.err.println();

        // define additional configuration parameters for the annotator
        Object[] additionalParameters = new Object[] {
            RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
            params.probabilityOfKeepingANegativeExample,
            EntityMentionPairRelationExtractorAnnotator.PARAM_CLASSIFY_BOTH_DIRECTIONS,
            params.classifyBothDirections };

        // define arguments to be passed to the classifier
        String[] trainingArguments = new String[] {
            "-t",
            String.valueOf(params.svmKernelIndex),
            "-c",
            String.valueOf(params.svmCost),
            "-g",
            String.valueOf(params.svmGamma) };

        // create the evaluation
        RelationExtractorEvaluation evaluation = new RelationExtractorEvaluation(
            modelsDir,
            relationCategory,
            annotatorClass,
            dataWriterClass,
            additionalParameters,
            trainingArguments,
            options.testOnCTakes,
            options.allowSmallerSystemArguments,
            options.ignoreImpossibleGoldRelations,
            options.printErrors);

        if (options.devDirectory != null) {
          if (options.testDirectory != null) {
            // train on the training set + dev set and evaluate on the test set
            List<File> allTrainFiles = new ArrayList<File>();
            allTrainFiles.addAll(trainFiles);
            allTrainFiles.addAll(Arrays.asList(options.devDirectory.listFiles()));
            List<File> testFiles = Arrays.asList(options.testDirectory.listFiles());
            params.stats = evaluation.trainAndTest(allTrainFiles, testFiles);
          } else {
            // train on the training set and evaluate on the dev set
            List<File> devFiles = Arrays.asList(options.devDirectory.listFiles());
            params.stats = evaluation.trainAndTest(trainFiles, devFiles);
          }
        } else {
          if (options.testDirectory != null) {
            // train on the training set and evaluate on the test set
            List<File> testFiles = Arrays.asList(options.testDirectory.listFiles());
            params.stats = evaluation.trainAndTest(trainFiles, testFiles);
          } else {
            // run n-fold cross-validation on the training set
            List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(trainFiles, 2);
            params.stats = AnnotationStatistics.addAll(foldStats);
          }
        }
        scoredParams.put(params, params.stats.f1());
      }

      // print parameters sorted by F1
      List<ParameterSettings> list = new ArrayList<ParameterSettings>(scoredParams.keySet());
      Function<ParameterSettings, Double> getCount = Functions.forMap(scoredParams);
      Collections.sort(list, Ordering.natural().onResultOf(getCount));

      // print performance of each set of parameters
      if (list.size() > 1) {
        System.err.println(relationCategory + ": summary:");
        for (ParameterSettings params : list) {
          System.err.printf(
              "F1=%.3f P=%.3f R=%.3f %s\n",
              params.stats.f1(),
              params.stats.precision(),
              params.stats.recall(),
              params);
        }
        System.err.println();
      }

      // print overall best model
      if (!list.isEmpty()) {
        ParameterSettings lastParams = list.get(list.size() - 1);
        System.err.println(relationCategory + ": best model:");
        System.err.print(lastParams.stats);
        System.err.println(lastParams);
        System.err.println(lastParams.stats.confusions());
        System.err.println();
        System.err.println(lastParams.stats.confusions().toHTML());
      }
    }
  }

  /**
   * An evaluation of a relation extractor.
   * 
   * @param baseDirectory
   *          The directory where models, etc. should be written
   * @param classifierAnnotatorClass
   *          The CleartkAnnotator class that learns a relation extractor model
   * @param dataWriterClass
   *          The DataWriter defining what type of classifier to train
   * @param additionalParameters
   *          Additional parameters that should be supplied when creating the CleartkAnnotator
   * @param trainingArguments
   *          Arguments that should be passed to the classifier's train method
   * @param testOnCTakes
   *          During testing, use annotations from cTAKES, not from the gold standard
   * @param allowSmallerSystemArguments
   *          During testing, allow system annotations to match gold annotations that enclose them
   * @param ignoreImpossibleGoldRelations
   *          During testing, ignore gold relations that would be impossible to find because there
   *          are no corresponding system mentions
   */
  public RelationExtractorEvaluation(
      File baseDirectory,
      String relationCategory,
      Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass,
      Class<? extends DataWriter<String>> dataWriterClass,
      Object[] additionalParameters,
      String[] trainingArguments,
      boolean testOnCTakes,
      boolean allowSmallerSystemArguments,
      boolean ignoreImpossibleGoldRelations,
      boolean printErrors) {
    super(baseDirectory);
    this.relationCategory = relationCategory;
    this.classifierAnnotatorClass = classifierAnnotatorClass;
    this.dataWriterClass = dataWriterClass;
    this.additionalParameters = additionalParameters;
    this.trainingArguments = trainingArguments;
    this.testOnCTakes = testOnCTakes;
    this.allowSmallerSystemArguments = allowSmallerSystemArguments;
    this.ignoreImpossibleGoldRelations = ignoreImpossibleGoldRelations;
    this.printErrors = printErrors;
  }
  
  private String relationCategory;

  private Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass;

  private Class<? extends DataWriter<String>> dataWriterClass;

  private Object[] additionalParameters;

  private String[] trainingArguments;
  
  private boolean testOnCTakes;
  
  private boolean allowSmallerSystemArguments;
  
  private boolean ignoreImpossibleGoldRelations;
  
  private boolean printErrors;

  @Override
  public CollectionReader getCollectionReader(List<File> items) throws Exception {
    // convert the List<File> to a String[]
    String[] paths = new String[items.size()];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = items.get(i).getPath();
    }

    // return a reader that will load each of the XMI files
    return CollectionReaderFactory.createCollectionReader(
        XMIReader.class,
        TypeSystemDescriptionFactory.createTypeSystemDescription(),
        XMIReader.PARAM_FILES,
        paths);
  }

  @Override
  public void train(CollectionReader collectionReader, File directory) throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    // remove all but the relation of interest from the gold annotations
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        RemoveOtherRelations.class,
        RemoveOtherRelations.PARAM_RELATION_CATEGORY,
        this.relationCategory),
        CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);
    // replace cTAKES entity mentions and modifiers in the system view with the gold annotations
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(ReplaceCTakesEntityMentionsAndModifiersWithGold.class));
    // add the relation extractor, configured for training mode
    AnalysisEngineDescription classifierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
        this.classifierAnnotatorClass,
        this.additionalParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        RelationExtractorAnnotator.PARAM_GOLD_VIEW_NAME,
        RelationExtractorEvaluation.GOLD_VIEW_NAME,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        this.dataWriterClass,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        directory.getPath());
    builder.add(classifierAnnotator);

    // run the data-writing pipeline
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // train the classifier and package it into a .jar file
    HideOutput hider = new HideOutput();
    JarClassifierBuilder.trainAndPackage(directory, this.trainingArguments);
    hider.restoreOutput();
    hider.close(); // workaround for https://code.google.com/p/uimafit/issues/detail?id=129
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    // remove all but the relation of interest from the gold annotations
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        RemoveOtherRelations.class,
        RemoveOtherRelations.PARAM_RELATION_CATEGORY,
        this.relationCategory),
        CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);
    if (this.testOnCTakes) {
      // add the modifier extractor
      File file = new File("desc/analysis_engine/ModifierExtractorAnnotator.xml");
      XMLInputSource source = new XMLInputSource(file);
      builder.add(UIMAFramework.getXMLParser().parseAnalysisEngineDescription(source));
      // remove extraneous entity mentions
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveSmallerEntityMentions.class));
    } else {
      // replace cTAKES entity mentions and modifiers in the system view with the gold annotations
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(ReplaceCTakesEntityMentionsAndModifiersWithGold.class));
    }
    // add the relation extractor, configured for classification mode
    AnalysisEngineDescription classifierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
        this.classifierAnnotatorClass,
        this.additionalParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(directory, "model.jar").getPath());
    builder.add(classifierAnnotator);

    // statistics will be based on the "category" feature of the BinaryTextRelations
    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    Function<BinaryTextRelation, HashableArguments> getSpan = new Function<BinaryTextRelation, HashableArguments>() {
      @Override
      public HashableArguments apply(BinaryTextRelation relation) {
        return new HashableArguments(relation);
      }
    };
    Function<BinaryTextRelation, String> getOutcome = AnnotationStatistics.annotationToFeatureValue("category");

    // calculate statistics, iterating over the results of the classifier
    AnalysisEngine engine = builder.createAggregate();
    for (JCas jCas : new JCasIterable(collectionReader, engine)) {

      // get the gold view
      JCas goldView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // get the gold and system annotations
      Collection<BinaryTextRelation> goldBinaryTextRelations = JCasUtil.select(
          goldView,
          BinaryTextRelation.class);
      Collection<BinaryTextRelation> systemBinaryTextRelations = JCasUtil.select(
          jCas,
          BinaryTextRelation.class);
      
      if (this.ignoreImpossibleGoldRelations) {
        // collect only relations where both arguments have some possible system arguments
        List<BinaryTextRelation> relations = Lists.newArrayList();
        for (BinaryTextRelation relation : goldBinaryTextRelations) {
          boolean hasSystemArgs = true;
          for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
            IdentifiedAnnotation goldArg = (IdentifiedAnnotation) relArg.getArgument();
            Class<? extends IdentifiedAnnotation> goldClass = goldArg.getClass();
            boolean noSystemArg = JCasUtil.selectCovered(jCas, goldClass, goldArg).isEmpty();
            hasSystemArgs = hasSystemArgs && !noSystemArg;
          }
          if (hasSystemArgs) {
            relations.add(relation);
          } else {
            IdentifiedAnnotation arg1 = (IdentifiedAnnotation) relation.getArg1().getArgument();
            IdentifiedAnnotation arg2 = (IdentifiedAnnotation) relation.getArg2().getArgument();
            String messageFormat = "removing relation between %s and %s which is impossible to "
                + "find with system mentions";
            String message = String.format(messageFormat, format(arg1), format(arg2));
            UIMAFramework.getLogger(this.getClass()).log(Level.WARNING, message);
          }
        }
        goldBinaryTextRelations = relations;
      }
      
      if (this.allowSmallerSystemArguments) {

        // collect all the arguments of the manually annotated relations
        Set<IdentifiedAnnotation> goldArgs = Sets.newHashSet();
        for (BinaryTextRelation relation : goldBinaryTextRelations) {
          for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
            goldArgs.add((IdentifiedAnnotation) relArg.getArgument());
          }
        }

        // collect all the arguments of system-predicted relations that don't match some gold argument
        Set<IdentifiedAnnotation> unmatchedSystemArgs = Sets.newHashSet();
        for (BinaryTextRelation relation : systemBinaryTextRelations) {
          for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
            IdentifiedAnnotation systemArg = (IdentifiedAnnotation) relArg.getArgument();
            Class<? extends IdentifiedAnnotation> systemClass = systemArg.getClass();
            boolean matchesSomeGold = false;
            for (IdentifiedAnnotation goldArg : JCasUtil.selectCovered(goldView, systemClass, systemArg)) {
              if (goldArg.getBegin() == systemArg.getBegin() && goldArg.getEnd() == systemArg.getEnd()) {
                matchesSomeGold = true;
                break;
              }
            }
            if (!matchesSomeGold) {
              unmatchedSystemArgs.add(systemArg);
            }
          }
        }

        // map each unmatched system argument to the gold argument that encloses it
        Map<IdentifiedAnnotation, IdentifiedAnnotation> systemToGold = Maps.newHashMap();
        for (IdentifiedAnnotation goldArg : goldArgs) {
          Class<? extends IdentifiedAnnotation> goldClass = goldArg.getClass();
          for (IdentifiedAnnotation systemArg : JCasUtil.selectCovered(jCas, goldClass, goldArg)) {
            if (unmatchedSystemArgs.contains(systemArg)) {
              
              // if there's no mapping yet for this system arg, map it to the enclosing gold arg
              IdentifiedAnnotation oldGoldArg = systemToGold.get(systemArg);
              if (oldGoldArg == null) {
                systemToGold.put(systemArg, goldArg);
              }
              
              // if there's already a mapping for this system arg, only re-map it to match the type 
              else {
                IdentifiedAnnotation current, other;
                if (systemArg.getTypeID() == goldArg.getTypeID()) {
                  systemToGold.put(systemArg, goldArg);
                  current = goldArg;
                  other = oldGoldArg;
                } else {
                  current = oldGoldArg;
                  other = goldArg;
                }

                // issue a warning since this re-mapping procedure is imperfect
                UIMAFramework.getLogger(this.getClass()).log(Level.WARNING, String.format(
                    "system argument %s mapped to gold argument %s, but could also be mapped to %s",
                    format(systemArg),
                    format(current),
                    format(other)));
              }
            }
          }
        }
        
        // replace system arguments with gold arguments where necessary/possible
        for (BinaryTextRelation relation : systemBinaryTextRelations) {
          for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
            IdentifiedAnnotation systemArg = (IdentifiedAnnotation) relArg.getArgument();
            IdentifiedAnnotation matchingGoldArg = systemToGold.get(systemArg);
            if (matchingGoldArg != null) {
              String messageFormat = "replacing system argument %s with gold argument %s";
              String message = String.format(messageFormat, format(systemArg), format(matchingGoldArg));
              UIMAFramework.getLogger(this.getClass()).log(Level.WARNING, message);
              relArg.setArgument(matchingGoldArg);
            }
          }
        }
      }

      // update the statistics based on the argument spans of the relation
      stats.add(
          goldBinaryTextRelations,
          systemBinaryTextRelations,
          getSpan,
          getOutcome);
      
      // print errors if requested
      if (this.printErrors) {
        Map<HashableArguments, BinaryTextRelation> goldMap = Maps.newHashMap();
        for (BinaryTextRelation relation : goldBinaryTextRelations) {
          goldMap.put(new HashableArguments(relation), relation);
        }
        Map<HashableArguments, BinaryTextRelation> systemMap = Maps.newHashMap();
        for (BinaryTextRelation relation : systemBinaryTextRelations) {
          systemMap.put(new HashableArguments(relation), relation);
        }
        Set<HashableArguments> all = Sets.union(goldMap.keySet(), systemMap.keySet());
        List<HashableArguments> sorted = Lists.newArrayList(all);
        Collections.sort(sorted);
        for (HashableArguments key : sorted) {
          BinaryTextRelation goldRelation = goldMap.get(key);
          BinaryTextRelation systemRelation = systemMap.get(key);
          if (goldRelation == null) {
            System.out.println("System added: " + formatRelation(systemRelation));
          } else if (systemRelation == null) {
            System.out.println("System dropped: " + formatRelation(goldRelation));
          } else if (!systemRelation.getCategory().equals(goldRelation.getCategory())) {
            String label = systemRelation.getCategory();
            System.out.printf("System labeled %s for %s\n", label, formatRelation(systemRelation));
          }
        }
      }
    }

    System.err.printf("%s: %s:\n", this.relationCategory, directory.getName());
    System.err.print(stats);
    System.err.println(stats.confusions());
    System.err.println();
    return stats;
  }
  
  private static String formatRelation(BinaryTextRelation relation) {
    IdentifiedAnnotation arg1 = (IdentifiedAnnotation)relation.getArg1().getArgument();
    IdentifiedAnnotation arg2 = (IdentifiedAnnotation)relation.getArg2().getArgument();
    String text = arg1.getCAS().getDocumentText();
    int begin = Math.min(arg1.getBegin(), arg2.getBegin());
    int end = Math.max(arg1.getBegin(), arg2.getBegin());
    begin = Math.max(0, begin - 50);
    end = Math.min(text.length(), end + 50);
    return String.format(
        "%s(%s(type=%d), %s(type=%d)) in ...%s...",
        relation.getCategory(),
        arg1.getCoveredText(),
        arg1.getTypeID(),
        arg2.getCoveredText(),
        arg2.getTypeID(),
        text.substring(begin, end).replaceAll("[\r\n]", " "));
  }

  /**
   * Holds a set of parameters for a relation extraction model
   */
  public static class ParameterSettings {
    public boolean classifyBothDirections;

    public float probabilityOfKeepingANegativeExample;

    public String svmKernel;

    public int svmKernelIndex;

    public double svmCost;

    public double svmGamma;

    public AnnotationStatistics<String> stats;

    private static List<String> SVM_KERNELS = Arrays.asList(
        "linear",
        "polynomial",
        "radial basis function",
        "sigmoid");

    public ParameterSettings(
        boolean classifyBothDirections,
        float probabilityOfKeepingANegativeExample,
        String svmKernel,
        double svmCost,
        double svmGamma) {
      super();
      this.classifyBothDirections = classifyBothDirections;
      this.probabilityOfKeepingANegativeExample = probabilityOfKeepingANegativeExample;
      this.svmKernel = svmKernel;
      this.svmKernelIndex = SVM_KERNELS.indexOf(this.svmKernel);
      if (this.svmKernelIndex == -1) {
        throw new IllegalArgumentException("Unrecognized kernel: " + this.svmKernel);
      }
      this.svmCost = svmCost;
      this.svmGamma = svmGamma;
    }

    @Override
    public String toString() {
      ToStringHelper helper = Objects.toStringHelper(this);
      helper.add("classifyBothDirections", this.classifyBothDirections);
      helper.add("probabilityOfKeepingANegativeExample", this.probabilityOfKeepingANegativeExample);
      helper.add("svmKernel", this.svmKernel);
      helper.add("svmCost", this.svmCost);
      helper.add("svmGamma", this.svmGamma);
      return helper.toString();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(
          this.classifyBothDirections,
          this.probabilityOfKeepingANegativeExample,
          this.svmKernel,
          this.svmCost,
          this.svmGamma);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ParameterSettings)) {
        return false;
      }
      ParameterSettings that = (ParameterSettings) obj;
      return this.classifyBothDirections == that.classifyBothDirections
          && this.probabilityOfKeepingANegativeExample == that.probabilityOfKeepingANegativeExample
          && this.svmKernel == that.svmKernel && this.svmCost == that.svmCost
          && this.svmGamma == that.svmGamma;
    }

  }

  /**
   * Annotator that removes cTAKES EntityMentions and Modifiers from the system view, and copies
   * over the manually annotated EntityMentions and Modifiers from the gold view.
   * 
   */
  public static class ReplaceCTakesEntityMentionsAndModifiersWithGold extends
      JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove cTAKES EntityMentions and Modifiers from system view
      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
      for (IdentifiedAnnotation cTakesMention : cTakesMentions) {
        cTakesMention.removeFromIndexes();
      }

      // copy gold EntityMentions and Modifiers to the system view
      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
      goldMentions.addAll(JCasUtil.select(goldView, EntityMention.class));
      goldMentions.addAll(JCasUtil.select(goldView, Modifier.class));
      CasCopier copier = new CasCopier(goldView.getCas(), systemView.getCas());
      for (IdentifiedAnnotation goldMention : goldMentions) {
        Annotation copy = (Annotation) copier.copyFs(goldMention);
        Feature sofaFeature = copy.getType().getFeatureByBaseName("sofa");
        copy.setFeatureValue(sofaFeature, systemView.getSofa());
        copy.addToIndexes();
      }
    }
  }

  public static class ReplaceGoldEntityMentionsAndModifiersWithCTakes extends
      JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove manual EntityMentions and Modifiers from gold view
      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
      goldMentions.addAll(JCasUtil.select(goldView, EntityMention.class));
      goldMentions.addAll(JCasUtil.select(goldView, Modifier.class));
      for (IdentifiedAnnotation goldMention : goldMentions) {
        goldMention.removeFromIndexes();
      }

      // copy cTAKES EntityMentions and Modifiers to gold view
      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
      CasCopier copier = new CasCopier(systemView.getCas(), goldView.getCas());
      for (IdentifiedAnnotation cTakesMention : cTakesMentions) {
        Annotation copy = (Annotation) copier.copyFs(cTakesMention);
        Feature sofaFeature = copy.getType().getFeatureByBaseName("sofa");
        copy.setFeatureValue(sofaFeature, goldView.getSofa());
        copy.addToIndexes();
      }

      // replace gold EntityMentions and Modifiers in relations with cTAKES ones
      List<BinaryTextRelation> relations = new ArrayList<BinaryTextRelation>();
      relations.addAll(JCasUtil.select(goldView, BinaryTextRelation.class));
      for (BinaryTextRelation relation : relations) {

        // attempt to replace the gold RelationArguments with system ones
        for (RelationArgument relArg : Arrays.asList(relation.getArg1(), relation.getArg2())) {
          IdentifiedAnnotation goldArg = (IdentifiedAnnotation) relArg.getArgument();
          Class<? extends IdentifiedAnnotation> argClass = goldArg.getClass();

          // find all annotations covered by the gold argument and of the same class (these should
          // be the ones copied over from the cTAKES output earlier)
          List<? extends IdentifiedAnnotation> systemArgs = JCasUtil.selectCovered(
              goldView,
              argClass,
              goldArg);

          // find the largest covered annotation that has the same type
          IdentifiedAnnotation bestFitArg = null;
          int maxSize = 0;
          for (IdentifiedAnnotation systemArg : systemArgs) {
            int size = systemArg.getEnd() - systemArg.getBegin();
            if (size >= maxSize && goldArg.getTypeID() == systemArg.getTypeID()) {
              maxSize = size;
              bestFitArg = systemArg;
            }
          }
          if (bestFitArg != null) {
            relArg.setArgument(bestFitArg);
          }

          // log a message if we didn't find a perfect match
          if (maxSize != goldArg.getEnd() - goldArg.getBegin()) {
            List<String> choices = new ArrayList<String>();
            for (IdentifiedAnnotation systemArg : systemArgs) {
              choices.add(format(systemArg));
            }
            String actionFormat = bestFitArg == null ? "dropping" : "using %s instead of";
            String action = String.format(actionFormat, format(bestFitArg));
            String message = String.format("%s %s; choices: %s", action, format(goldArg), choices);
            this.getContext().getLogger().log(Level.WARNING, message);
          }
        }
      }
    }
  }
  
  static String format(IdentifiedAnnotation a) {
    return a == null ? null : String.format("\"%s\"(type=%d)", a.getCoveredText(), a.getTypeID());
  }
  
  public static class RemoveOtherRelations extends JCasAnnotator_ImplBase {
    
    public static final String PARAM_RELATION_CATEGORY = "RelationCategory";
    @ConfigurationParameter(name = PARAM_RELATION_CATEGORY)
    private String relationCategory;
    

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      List<BinaryTextRelation> relations = new ArrayList<BinaryTextRelation>();
      relations.addAll(JCasUtil.select(jCas, BinaryTextRelation.class));
      for (BinaryTextRelation relation : relations) {
        if (!relation.getCategory().equals(this.relationCategory)) {
          relation.removeFromIndexes();
        }
      }
    }
  }
  
  public static class RemoveSmallerEntityMentions extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      Collection<EntityMention> mentions = JCasUtil.select(jCas, EntityMention.class);
      for (EntityMention mention : Lists.newArrayList(mentions)) {
        int begin = mention.getBegin();
        int end = mention.getEnd();
        int typeID = mention.getTypeID();
        List<EntityMention> subMentions = JCasUtil.selectCovered(jCas, EntityMention.class, mention);
        for (EntityMention subMention : subMentions) {
          if (subMention.getBegin() > begin || subMention.getEnd() < end) {
            if (subMention.getTypeID() == typeID) {
              String message = String.format(
                  "removed %s inside %s",
                  format(subMention),
                  format(mention));
              this.getContext().getLogger().log(Level.WARNING, message);
              subMention.removeFromIndexes();
            }
          }
        }
      }
    }
  }
  
  /**
   * This class is useful for mapping the spans of relation arguments to the relation's category.
   */
  public static class HashableArguments implements Comparable<HashableArguments> {

    protected int arg1begin;

    protected int arg1end;

    protected int arg2begin;

    protected int arg2end;

    public HashableArguments(int arg1begin, int arg1end, int arg2begin, int arg2end) {
      this.arg1begin = arg1begin;
      this.arg1end = arg1end;
      this.arg2begin = arg2begin;
      this.arg2end = arg2end;
    }

    public HashableArguments(Annotation arg1, Annotation arg2) {
      this(arg1.getBegin(), arg1.getEnd(), arg2.getBegin(), arg2.getEnd());
    }

    public HashableArguments(BinaryTextRelation relation) {
      this(getArg1(relation), getArg2(relation));
    }

    // HACK: arg1 is not always arg1 because of bugs in the reader
    private static Annotation getArg1(BinaryTextRelation rel) {
      RelationArgument arg1 = rel.getArg1();
      return arg1.getRole().equals("Argument") ? arg1.getArgument() : rel.getArg2().getArgument();
    }

    // HACK: arg2 is not always arg2 because of bugs in the reader
    private static Annotation getArg2(BinaryTextRelation rel) {
      RelationArgument arg2 = rel.getArg2();
      return arg2.getRole().equals("Related_to")
          ? arg2.getArgument()
          : rel.getArg1().getArgument();
    }

    @Override
    public boolean equals(Object otherObject) {
      boolean result = false;
      if (otherObject instanceof HashableArguments) {
        HashableArguments other = (HashableArguments) otherObject;
        result = (this.getClass() == other.getClass() && this.arg1begin == other.arg1begin
            && this.arg1end == other.arg1end && this.arg2begin == other.arg2begin && this.arg2end == other.arg2end);
      }
      return result;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.arg1begin, this.arg1end, this.arg2begin, this.arg2end);
    }

    @Override
    public String toString() {
      return String.format(
          "%s(%s,%s,%s,%s)",
          this.getClass().getSimpleName(),
          this.arg1begin,
          this.arg1end,
          this.arg2begin,
          this.arg2end);
    }

    @Override
    public int compareTo(HashableArguments that) {
      int thisBegin = Math.min(this.arg1begin, this.arg2begin);
      int thatBegin = Math.min(that.arg1begin, that.arg2begin);
      if (thisBegin < thatBegin) {
        return -1;
      } else if (thisBegin > thatBegin) {
        return +1;
      } else if (this.equals(that)) {
        return 0;
      } else {
        return +1; // arbitrary choice for overlapping
      }
    }
  }
}
