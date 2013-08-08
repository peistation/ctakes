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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.ctakes.relationextractor.ae.DegreeOfRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.LocationOfRelationExtractorAnnotator;
import org.apache.ctakes.relationextractor.ae.RelationExtractorAnnotator;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.DegreeOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.LocationOfTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.Modifier;
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
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.liblinear.LIBLINEARStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class RelationExtractorEvaluation extends SHARPXMI.Evaluation_ImplBase {

  public static interface Options extends SHARPXMI.EvaluationOptions {

    @Option(
        longName = "relations",
        description = "determines which relations to evaluate on (separately)",
        defaultValue = { "degree_of", "location_of" })
    public List<String> getRelations();

    @Option(
        longName = "test-on-ctakes",
        description = "evaluate test performance on ctakes entities, instead of gold standard "
            + "entities")
    public boolean getTestOnCTakes();

    @Option(
        longName = "allow-smaller-system-arguments",
        description = "for evaluation, allow system relation arguments to match gold relation "
            + "arguments that enclose them")
    public boolean getAllowSmallerSystemArguments();

    @Option(
        longName = "ignore-impossible-gold-relations",
        description = "for evaluation, ignore gold relations that would be impossible to find "
            + "because there are no corresponding system mentions")
    public boolean getIgnoreImpossibleGoldRelations();

    @Option(
        longName = "--print-errors",
        description = "print relations that were incorrectly predicted")
    public boolean getPrintErrors();

  }

  public static final Map<String, Class<? extends BinaryTextRelation>> RELATION_CLASSES =
      Maps.newHashMap();
  public static final Map<Class<? extends BinaryTextRelation>, Class<? extends RelationExtractorAnnotator>> ANNOTATOR_CLASSES =
      Maps.newHashMap();
  public static final Map<Class<? extends BinaryTextRelation>, ParameterSettings> BEST_PARAMETERS =
      Maps.newHashMap();

  static {
    RELATION_CLASSES.put("degree_of", DegreeOfTextRelation.class);
    ANNOTATOR_CLASSES.put(DegreeOfTextRelation.class, DegreeOfRelationExtractorAnnotator.class);
    BEST_PARAMETERS.put(DegreeOfTextRelation.class, new ParameterSettings(
        LIBLINEARStringOutcomeDataWriter.class,
        new Object[] { RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
            1.0f },
        new String[] { "-s", "1", "-c", "10.0" }));

    RELATION_CLASSES.put("location_of", LocationOfTextRelation.class);
    ANNOTATOR_CLASSES.put(LocationOfTextRelation.class, LocationOfRelationExtractorAnnotator.class);
    BEST_PARAMETERS.put(LocationOfTextRelation.class, new ParameterSettings(
        LIBLINEARStringOutcomeDataWriter.class,
        new Object[] { RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
            1.0f },
        new String[] { "-s", "1", "-c", "0.05" }));
  }

  public static void main(String[] args) throws Exception {
    // parse the options, validate them, and generate XMI if necessary
    final Options options = CliFactory.parseArguments(Options.class, args);
    SHARPXMI.validate(options);
    SHARPXMI.generateXMI(options);

    // determine the grid of parameters to search through
    // for the full set of LIBLINEAR parameters, see:
    // https://github.com/bwaldvogel/liblinear-java/blob/master/src/main/java/de/bwaldvogel/liblinear/Train.java
    List<ParameterSettings> gridOfSettings = Lists.newArrayList();
    for (float probabilityOfKeepingANegativeExample : new float[] { 0.5f, 1.0f }) {
      for (int solver : new int[] { 0 /* logistic regression */, 1 /* SVM */}) {
        for (double svmCost : new double[] { 0.01, 0.05, 0.1, 0.5, 1, 5, 10, 50, 100 }) {
          gridOfSettings.add(new ParameterSettings(
              LIBLINEARStringOutcomeDataWriter.class,
              new Object[] {
                  RelationExtractorAnnotator.PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
                  probabilityOfKeepingANegativeExample },
              new String[] { "-s", String.valueOf(solver), "-c", String.valueOf(svmCost) }));
        }
      }
    }

    // run an evaluation for each selected relation
    for (final String relationCategory : options.getRelations()) {

      // get the best parameters for the relation
      final Class<? extends BinaryTextRelation> relationClass =
          RELATION_CLASSES.get(relationCategory);
      ParameterSettings bestSettings = BEST_PARAMETERS.get(relationClass);

      // run the evaluation
      SHARPXMI.evaluate(
          options,
          bestSettings,
          gridOfSettings,
          new Function<ParameterSettings, RelationExtractorEvaluation>() {
            @Override
            public RelationExtractorEvaluation apply(@Nullable ParameterSettings params) {
              return new RelationExtractorEvaluation(
                  new File("target/models/" + relationCategory),
                  relationClass,
                  ANNOTATOR_CLASSES.get(relationClass),
                  params,
                  options.getTestOnCTakes(),
                  options.getAllowSmallerSystemArguments(),
                  options.getIgnoreImpossibleGoldRelations(),
                  options.getPrintErrors());
            }
          });
    }
  }

  private Class<? extends BinaryTextRelation> relationClass;

  private Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass;

  private ParameterSettings parameterSettings;

  private boolean testOnCTakes;

  private boolean allowSmallerSystemArguments;

  private boolean ignoreImpossibleGoldRelations;

  private boolean printErrors;

  /**
   * An evaluation of a relation extractor.
   * 
   * @param baseDirectory
   *          The directory where models, etc. should be written
   * @param relationClass
   *          The class of the relation to be predicted
   * @param classifierAnnotatorClass
   *          The CleartkAnnotator class that learns a relation extractor model
   * @param parameterSettings
   *          The parameters defining how to train a classifier
   * @param testOnCTakes
   *          During testing, use annotations from cTAKES, not from the gold
   *          standard
   * @param allowSmallerSystemArguments
   *          During testing, allow system annotations to match gold annotations
   *          that enclose them
   * @param ignoreImpossibleGoldRelations
   *          During testing, ignore gold relations that would be impossible to
   *          find because there are no corresponding system mentions
   */
  public RelationExtractorEvaluation(
      File baseDirectory,
      Class<? extends BinaryTextRelation> relationClass,
      Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass,
      ParameterSettings parameterSettings,
      boolean testOnCTakes,
      boolean allowSmallerSystemArguments,
      boolean ignoreImpossibleGoldRelations,
      boolean printErrors) {
    super(baseDirectory);
    this.relationClass = relationClass;
    this.classifierAnnotatorClass = classifierAnnotatorClass;
    this.parameterSettings = parameterSettings;
    this.testOnCTakes = testOnCTakes;
    this.allowSmallerSystemArguments = allowSmallerSystemArguments;
    this.ignoreImpossibleGoldRelations = ignoreImpossibleGoldRelations;
    this.printErrors = printErrors;
  }

  public RelationExtractorEvaluation(
      File baseDirectory,
      Class<? extends BinaryTextRelation> relationClass,
      Class<? extends RelationExtractorAnnotator> classifierAnnotatorClass,
      ParameterSettings parameterSettings) {
    this(
        baseDirectory,
        relationClass,
        classifierAnnotatorClass,
        parameterSettings,
        false,
        false,
        false,
        false);
  }

  @Override
  public void train(CollectionReader collectionReader, File directory) throws Exception {
    System.err.printf(
        "%s: %s: %s:\n",
        this.getClass().getSimpleName(),
        this.relationClass.getSimpleName(),
        directory.getName());
    System.err.println(this.parameterSettings);

    AggregateBuilder builder = new AggregateBuilder();
    // remove cTAKES entity mentions and modifiers in the system view and copy
    // in the gold relations
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveCTakesMentionsAndCopyGoldRelations.class));
    // add the relation extractor, configured for training mode
    AnalysisEngineDescription classifierAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(
            this.classifierAnnotatorClass,
            this.parameterSettings.configurationParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        this.parameterSettings.dataWriterClass,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        directory.getPath());
    builder.add(classifierAnnotator);

    // run the data-writing pipeline
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // train the classifier and package it into a .jar file
    JarClassifierBuilder.trainAndPackage(directory, this.parameterSettings.trainingArguments);
  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File directory)
      throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    if (this.testOnCTakes) {
      // add the modifier extractor
      File file = new File("desc/analysis_engine/ModifierExtractorAnnotator.xml");
      XMLInputSource source = new XMLInputSource(file);
      builder.add(UIMAFramework.getXMLParser().parseAnalysisEngineDescription(source));
      // remove extraneous entity mentions
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(RemoveSmallerEventMentions.class));
    } else {
      // replace cTAKES entity mentions and modifiers in the system view with
      // the gold annotations
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(ReplaceCTakesMentionsWithGoldMentions.class));
    }
    // add the relation extractor, configured for classification mode
    AnalysisEngineDescription classifierAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(
            this.classifierAnnotatorClass,
            this.parameterSettings.configurationParameters);
    ConfigurationParameterFactory.addConfigurationParameters(
        classifierAnnotator,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        JarClassifierBuilder.getModelJarFile(directory));
    builder.add(classifierAnnotator);

    // statistics will be based on the "category" feature of the
    // BinaryTextRelations
    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    Function<BinaryTextRelation, HashableArguments> getSpan =
        new Function<BinaryTextRelation, HashableArguments>() {
          @Override
          public HashableArguments apply(BinaryTextRelation relation) {
            return new HashableArguments(relation);
          }
        };
    Function<BinaryTextRelation, String> getOutcome =
        AnnotationStatistics.annotationToFeatureValue("category");

    // calculate statistics, iterating over the results of the classifier
    AnalysisEngine engine = builder.createAggregate();
    for (JCas jCas : new JCasIterable(collectionReader, engine)) {

      // get the gold view
      JCas goldView;
      try {
        goldView = jCas.getView(SHARPXMI.GOLD_VIEW_NAME);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // get the gold and system annotations
      Collection<? extends BinaryTextRelation> goldBinaryTextRelations =
          JCasUtil.select(goldView, this.relationClass);
      Collection<? extends BinaryTextRelation> systemBinaryTextRelations =
          JCasUtil.select(jCas, this.relationClass);

      if (this.ignoreImpossibleGoldRelations) {
        // collect only relations where both arguments have some possible system
        // arguments
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
            String messageFormat =
                "removing relation between %s and %s which is impossible to "
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

        // collect all the arguments of system-predicted relations that don't
        // match some gold argument
        Set<IdentifiedAnnotation> unmatchedSystemArgs = Sets.newHashSet();
        for (BinaryTextRelation relation : systemBinaryTextRelations) {
          for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
            IdentifiedAnnotation systemArg = (IdentifiedAnnotation) relArg.getArgument();
            Class<? extends IdentifiedAnnotation> systemClass = systemArg.getClass();
            boolean matchesSomeGold = false;
            for (IdentifiedAnnotation goldArg : JCasUtil.selectCovered(
                goldView,
                systemClass,
                systemArg)) {
              if (goldArg.getBegin() == systemArg.getBegin()
                  && goldArg.getEnd() == systemArg.getEnd()) {
                matchesSomeGold = true;
                break;
              }
            }
            if (!matchesSomeGold) {
              unmatchedSystemArgs.add(systemArg);
            }
          }
        }

        // map each unmatched system argument to the gold argument that encloses
        // it
        Map<IdentifiedAnnotation, IdentifiedAnnotation> systemToGold = Maps.newHashMap();
        for (IdentifiedAnnotation goldArg : goldArgs) {
          Class<? extends IdentifiedAnnotation> goldClass = goldArg.getClass();
          for (IdentifiedAnnotation systemArg : JCasUtil.selectCovered(jCas, goldClass, goldArg)) {
            if (unmatchedSystemArgs.contains(systemArg)) {

              // if there's no mapping yet for this system arg, map it to the
              // enclosing gold arg
              IdentifiedAnnotation oldGoldArg = systemToGold.get(systemArg);
              if (oldGoldArg == null) {
                systemToGold.put(systemArg, goldArg);
              }

              // if there's already a mapping for this system arg, only re-map
              // it to match the type
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
                String message =
                    "system argument %s mapped to gold argument %s, but could also be mapped to %s";
                message = String.format(message, format(systemArg), format(current), format(other));
                UIMAFramework.getLogger(this.getClass()).log(Level.WARNING, message);
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
              String message =
                  String.format(messageFormat, format(systemArg), format(matchingGoldArg));
              UIMAFramework.getLogger(this.getClass()).log(Level.WARNING, message);
              relArg.setArgument(matchingGoldArg);
            }
          }
        }
      }

      // update the statistics based on the argument spans of the relation
      stats.add(goldBinaryTextRelations, systemBinaryTextRelations, getSpan, getOutcome);

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

    System.err.print(stats);
    System.err.println();
    return stats;
  }

  private static String formatRelation(BinaryTextRelation relation) {
    IdentifiedAnnotation arg1 = (IdentifiedAnnotation) relation.getArg1().getArgument();
    IdentifiedAnnotation arg2 = (IdentifiedAnnotation) relation.getArg2().getArgument();
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
   * Annotator that removes cTAKES mentions in the system view and copies
   * relations from the gold view to the system view
   */
  public static class RemoveCTakesMentionsAndCopyGoldRelations extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(SHARPXMI.GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove cTAKES Mentions and Modifiers from system view
      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
      cTakesMentions.addAll(JCasUtil.select(systemView, EventMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
      for (IdentifiedAnnotation cTakesMention : cTakesMentions) {
        cTakesMention.removeFromIndexes();
      }

      // copy gold Mentions and Modifiers to the system view
      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
      goldMentions.addAll(JCasUtil.select(goldView, EventMention.class));
      goldMentions.addAll(JCasUtil.select(goldView, EntityMention.class));
      goldMentions.addAll(JCasUtil.select(goldView, Modifier.class));
      CasCopier copier = new CasCopier(goldView.getCas(), systemView.getCas());
      Feature sofaFeature = jCas.getTypeSystem().getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
      for (IdentifiedAnnotation goldMention : goldMentions) {
        Annotation copy = (Annotation) copier.copyFs(goldMention);
        copy.setFeatureValue(sofaFeature, systemView.getSofa());
        copy.addToIndexes();
      }

      // copy gold relations to the system view
      for (BinaryTextRelation goldRelation : JCasUtil.select(goldView, BinaryTextRelation.class)) {
        BinaryTextRelation relation = (BinaryTextRelation) copier.copyFs(goldRelation);
        relation.addToIndexes(systemView);
        for (RelationArgument relArg : Lists.newArrayList(relation.getArg1(), relation.getArg2())) {
          relArg.addToIndexes(systemView);
          // relArg.getArgument() should have been added to indexes with
          // mentions above
        }
      }
    }
  }

  /**
   * Annotator that removes cTAKES Mentions and Modifiers from the system view,
   * and copies over the manually annotated Mentions and Modifiers from the gold
   * view.
   */
  public static class ReplaceCTakesMentionsWithGoldMentions extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas goldView, systemView;
      try {
        goldView = jCas.getView(SHARPXMI.GOLD_VIEW_NAME);
        systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }

      // remove cTAKES Mentions and Modifiers from system view
      List<IdentifiedAnnotation> cTakesMentions = new ArrayList<IdentifiedAnnotation>();
      cTakesMentions.addAll(JCasUtil.select(systemView, EventMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, EntityMention.class));
      cTakesMentions.addAll(JCasUtil.select(systemView, Modifier.class));
      for (IdentifiedAnnotation cTakesMention : cTakesMentions) {
        cTakesMention.removeFromIndexes();
      }

      // copy gold Mentions and Modifiers to the system view
      List<IdentifiedAnnotation> goldMentions = new ArrayList<IdentifiedAnnotation>();
      goldMentions.addAll(JCasUtil.select(goldView, EventMention.class));
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

  static String format(IdentifiedAnnotation a) {
    return a == null ? null : String.format("\"%s\"(type=%d)", a.getCoveredText(), a.getTypeID());
  }

  public static class RemoveSmallerEventMentions extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      Collection<EventMention> mentions = JCasUtil.select(jCas, EventMention.class);
      for (EventMention mention : Lists.newArrayList(mentions)) {
        int begin = mention.getBegin();
        int end = mention.getEnd();
        int typeID = mention.getTypeID();
        List<EventMention> subMentions = JCasUtil.selectCovered(jCas, EventMention.class, mention);
        for (EventMention subMention : subMentions) {
          if (subMention.getBegin() > begin || subMention.getEnd() < end) {
            if (subMention.getTypeID() == typeID) {
              String message =
                  String.format("removed %s inside %s", format(subMention), format(mention));
              this.getContext().getLogger().log(Level.WARNING, message);
              subMention.removeFromIndexes();
            }
          }
        }
      }
    }
  }

  /**
   * This class is useful for mapping the spans of relation arguments to the
   * relation's category.
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
      this(relation.getArg1().getArgument(), relation.getArg2().getArgument());
    }

    @Override
    public boolean equals(Object otherObject) {
      boolean result = false;
      if (otherObject instanceof HashableArguments) {
        HashableArguments other = (HashableArguments) otherObject;
        result =
            (this.getClass() == other.getClass()
                && this.arg1begin == other.arg1begin
                && this.arg1end == other.arg1end
                && this.arg2begin == other.arg2begin && this.arg2end == other.arg2end);
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
