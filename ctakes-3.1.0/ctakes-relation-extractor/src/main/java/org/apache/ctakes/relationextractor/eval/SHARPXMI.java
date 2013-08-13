package org.apache.ctakes.relationextractor.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.ctakes.core.ae.SHARPKnowtatorXMLReader;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;
import org.xml.sax.ContentHandler;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.lexicalscope.jewel.cli.Option;

public class SHARPXMI {

  public static List<File> getTrainTextFiles(File batchesDirectory) {
    // seed_set1: batches 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 18, 19
    // seed_set2: batches 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 15, 16, 18, 19
    // seed_set3: batches 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15, 16, 18, 19
    // seed_set4: batches 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15, 16, 18, 19
    return getTextFilesFor(
        batchesDirectory,
        Pattern.compile("^(ss[1234]_batch0[2-9]|ss[1234]_batch1[56]"
            + "|ss[1234]_batch1[89]|ss[123]_batch01"
            + "|ss[12]_batch1[34]|ss[34]_batch1[12])$"));
  }

  public static List<File> getDevTextFiles(File batchesDirectory) {
    // seed_set1: batches 10, 17
    // seed_set2: batches 10, 17
    // seed_set3: batches 10, 17
    // seed_set4: batches 10, 17
    return getTextFilesFor(batchesDirectory, Pattern.compile("^(ss[1234]_batch1[07])$"));
  }

  public static List<File> getTestTextFiles(File batchesDirectory) {
    // seed_set1: batches 11, 12
    // seed_set2: batches 11, 12
    // seed_set3: batches 13, 14
    // seed_set4: batches 13, 14
    return getTextFilesFor(
        batchesDirectory,
        Pattern.compile("^(ss[12]_batch1[12]|ss[34]_batch1[34])$"));
  }

  public static List<File> getAllTextFiles(File batchesDirectory) {
    return getTextFilesFor(batchesDirectory, Pattern.compile(""));
  }

  private static List<File> getTextFilesFor(File batchesDirectory, Pattern pattern) {
    List<File> files = Lists.newArrayList();
    for (File batchDir : batchesDirectory.listFiles()) {
      if (batchDir.isDirectory() && !batchDir.isHidden()) {
        if (pattern.matcher(batchDir.getName()).find()) {
          File textDirectory = new File(batchDir, "Knowtator/text");
          for (File textFile : textDirectory.listFiles()) {
            if (textFile.isFile() && !textFile.isHidden()) {
              files.add(textFile);
            }
          }
        }
      }
    }
    return files;
  }

  public static List<File> toXMIFiles(Options options, List<File> textFiles) {
    List<File> xmiFiles = Lists.newArrayList();
    for (File textFile : textFiles) {
      xmiFiles.add(toXMIFile(options, textFile));
    }
    return xmiFiles;
  }

  private static File toXMIFile(Options options, File textFile) {
    return new File(options.getXMIDirectory(), textFile.getName() + ".xmi");
  }

  public static interface Options {
    @Option(
        longName = "batches-dir",
        description = "directory containing ssN_batchNN directories, each of which should contain "
            + "a Knowtator directory and a Knowtator_XML directory")
    public File getBatchesDirectory();

    @Option(
        longName = "xmi-dir",
        defaultValue = "target/xmi",
        description = "directory to store and load XMI serialization of annotations")
    public File getXMIDirectory();

    @Option(
        longName = "generate-xmi",
        description = "read in the gold annotations and serialize them as XMI")
    public boolean getGenerateXMI();
  }

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static void generateXMI(Options options) throws Exception {
    // if necessary, write the XMIs first
    if (options.getGenerateXMI()) {
      if (!options.getXMIDirectory().exists()) {
        options.getXMIDirectory().mkdirs();
      }

      // create a collection reader that loads URIs for all Knowtator text files
      List<File> files = Lists.newArrayList();
      files.addAll(getTrainTextFiles(options.getBatchesDirectory()));
      files.addAll(getDevTextFiles(options.getBatchesDirectory()));
      files.addAll(getTestTextFiles(options.getBatchesDirectory()));
      CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);

      // load the text from the URI, run the preprocessor, then run the
      // Knowtator XML reader
      AggregateBuilder builder = new AggregateBuilder();
      builder.add(UriToDocumentTextAnnotator.getDescription());
      File preprocessDescFile = new File("desc/analysis_engine/RelationExtractorPreprocessor.xml");
      XMLParser parser = UIMAFramework.getXMLParser();
      XMLInputSource source = new XMLInputSource(preprocessDescFile);
      builder.add(parser.parseAnalysisEngineDescription(source));
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(
          ViewCreatorAnnotator.class,
          ViewCreatorAnnotator.PARAM_VIEW_NAME,
          GOLD_VIEW_NAME));
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(CopyDocumentTextToGoldView.class));
      builder.add(
          AnalysisEngineFactory.createPrimitiveDescription(DocumentIDAnnotator.class),
          CAS.NAME_DEFAULT_SOFA,
          GOLD_VIEW_NAME);
      builder.add(
          AnalysisEngineFactory.createPrimitiveDescription(SHARPKnowtatorXMLReader.class),
          CAS.NAME_DEFAULT_SOFA,
          GOLD_VIEW_NAME);

      // write out an XMI for each file
      for (JCas jCas : new JCasIterable(reader, builder.createAggregate())) {
        JCas goldView = jCas.getView(GOLD_VIEW_NAME);
        String documentID = DocumentIDAnnotationUtil.getDocumentID(goldView);
        if (documentID == null) {
          throw new IllegalArgumentException("No documentID for CAS:\n" + jCas);
        }
        File outFile = toXMIFile(options, new File(documentID));
        FileOutputStream stream = new FileOutputStream(outFile);
        ContentHandler handler = new XMLSerializer(stream).getContentHandler();
        new XmiCasSerializer(jCas.getTypeSystem()).serialize(jCas.getCas(), handler);
        stream.close();
      }
    }
  }

  public enum EvaluateOn {
    TRAIN, DEV, TEST
  }

  public static interface EvaluationOptions extends Options {
    @Option(
        longName = "evalute-on",
        defaultValue = "DEV",
        description = "perform evaluation using the training (TRAIN), development (DEV) or test "
            + "(TEST) data.")
    public EvaluateOn getEvaluteOn();

    @Option(
        longName = "grid-search",
        description = "run a grid search to select the best parameters")
    public boolean getGridSearch();
  }
  
  public static abstract class Evaluation_ImplBase extends org.cleartk.eval.Evaluation_ImplBase<File, AnnotationStatistics<String>> {

    public Evaluation_ImplBase(File baseDirectory) {
      super(baseDirectory);
    }

    @Override
    public CollectionReader getCollectionReader(List<File> items) throws Exception {
      return CollectionReaderFactory.createCollectionReader(
          XMIReader.class,
          TypeSystemDescriptionFactory.createTypeSystemDescription(),
          XMIReader.PARAM_FILES,
          items);
    }

  }

  public static void validate(EvaluationOptions options) throws Exception {
    // error on invalid option combinations
    if (options.getEvaluteOn().equals(EvaluateOn.TEST) && options.getGridSearch()) {
      throw new IllegalArgumentException("grid search can only be run on the train or dev sets");
    }
  }

  public static <T extends Evaluation_ImplBase> void evaluate(
      EvaluationOptions options,
      ParameterSettings bestSettings,
      List<ParameterSettings> gridOfSettings,
      Function<ParameterSettings, T> getEvaluation) throws Exception {
    // define the set of possible training parameters
    List<ParameterSettings> possibleParams;
    if (options.getGridSearch()) {
      possibleParams = gridOfSettings;
    } else {
      possibleParams = Lists.newArrayList(bestSettings);
    }

    // run an evaluation for each set of parameters
    Map<ParameterSettings, Double> scoredParams = new HashMap<ParameterSettings, Double>();
    for (ParameterSettings params : possibleParams) {
      Evaluation_ImplBase evaluation = getEvaluation.apply(params);

      List<File> trainFiles, devFiles, testFiles;
      switch (options.getEvaluteOn()) {
      case TRAIN:
        // run n-fold cross-validation on the training set
        trainFiles = getTrainTextFiles(options.getBatchesDirectory());
        trainFiles = toXMIFiles(options, trainFiles);
        List<AnnotationStatistics<String>> foldStats = evaluation.crossValidation(trainFiles, 2);
        params.stats = AnnotationStatistics.addAll(foldStats);
        break;
      case DEV:
        // train on the training set and evaluate on the dev set
        trainFiles = getTrainTextFiles(options.getBatchesDirectory());
        trainFiles = toXMIFiles(options, trainFiles);
        devFiles = getDevTextFiles(options.getBatchesDirectory());
        devFiles = toXMIFiles(options, devFiles);
        params.stats = evaluation.trainAndTest(trainFiles, devFiles);
        break;
      case TEST:
        // train on the training set + dev set and evaluate on the test set
        List<File> allTrainFiles = new ArrayList<File>();
        allTrainFiles.addAll(getTrainTextFiles(options.getBatchesDirectory()));
        allTrainFiles.addAll(getDevTextFiles(options.getBatchesDirectory()));
        allTrainFiles = toXMIFiles(options, allTrainFiles);
        testFiles = getTestTextFiles(options.getBatchesDirectory());
        testFiles = toXMIFiles(options, testFiles);
        params.stats = evaluation.trainAndTest(allTrainFiles, testFiles);
        break;
      default:
        throw new IllegalArgumentException("Invalid EvaluateOn: " + options.getEvaluteOn());
      }
      scoredParams.put(params, params.stats.f1());
    }

    // print parameters sorted by F1
    List<ParameterSettings> list = new ArrayList<ParameterSettings>(scoredParams.keySet());
    Function<ParameterSettings, Double> getCount = Functions.forMap(scoredParams);
    Collections.sort(list, Ordering.natural().onResultOf(getCount));

    // print performance of each set of parameters
    if (list.size() > 1) {
      System.err.println("Summary");
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
      System.err.println("Best model:");
      System.err.print(lastParams.stats);
      System.err.println(lastParams);
      System.err.println(lastParams.stats.confusions());
      System.err.println();
    }
  }

  public static class DocumentIDAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String documentID = new File(ViewURIUtil.getURI(jCas)).getPath();
      DocumentID documentIDAnnotation = new DocumentID(jCas);
      documentIDAnnotation.setDocumentID(documentID);
      documentIDAnnotation.addToIndexes();
    }
  }

  public static class CopyDocumentTextToGoldView extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      try {
        JCas goldView = jCas.getView(GOLD_VIEW_NAME);
        goldView.setDocumentText(jCas.getDocumentText());
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }
}
