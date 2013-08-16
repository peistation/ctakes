package org.apache.ctakes.temporal.data.analysis;

import info.bethard.timenorm.Temporal;
import info.bethard.timenorm.TemporalExpressionParser;
import info.bethard.timenorm.TimeSpan;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ctakes.temporal.eval.CommandLine;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.XMIReader;
import org.apache.ctakes.temporal.eval.THYMEData;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import scala.util.Try;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class PrintFailedTimeNormalizations {
  static interface Options {
    @Option(longName = "xmi")
    public File getXMIDirectory();

    @Option(longName = "patients")
    public CommandLine.IntegerRanges getPatients();

    @Option(longName = "text")
    public File getRawTextDirectory();
  }

  private static Pattern DOC_TIME_PATTERN = Pattern.compile("rev_date=\"([^\"]+)\"");

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    List<File> files = THYMEData.getFilesFor(trainItems, options.getRawTextDirectory());

    CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(UriToDocumentTextAnnotator.getDescription());
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIReader.class,
        XMIReader.PARAM_XMI_DIRECTORY,
        options.getXMIDirectory()));

    String grammarPath = "/org/apache/ctakes/temporal/timenorm.en.grammar";
    URL grammarURL = PrintFailedTimeNormalizations.class.getResource(grammarPath);
    TemporalExpressionParser parser = new TemporalExpressionParser(grammarURL);
    for (JCas jCas : new JCasIterable(reader, aggregateBuilder.createAggregate())) {
      JCas goldView = jCas.getView("GoldView");

      Matcher matcher = DOC_TIME_PATTERN.matcher(goldView.getDocumentText());
      if (!matcher.find()) {
        System.err.println(goldView.getDocumentText());
      } else {
        Try<Temporal> anchorTry = parser.parse(matcher.group(1), TimeSpan.of(1, 1, 1));
        if (anchorTry.isSuccess() && anchorTry.get() instanceof TimeSpan) {
          TimeSpan anchor = (TimeSpan)anchorTry.get();
          for (Segment segment : JCasUtil.select(jCas, Segment.class)) {
            if (!THYMEData.SEGMENTS_TO_SKIP.contains(segment.getId())) {
              for (TimeMention time : JCasUtil.selectCovered(goldView, TimeMention.class, segment)) {
                String timeText = time.getCoveredText();
                Try<Temporal> parsedTime = parser.parse(timeText, anchor);
                if (parsedTime.isSuccess()) {
                  //System.err.printf("%s %s\n", timeText, parsedTime);
                } else {
                  System.err.println(timeText);
                  System.err.println(parsedTime);
                }
              }
            }
          }
        }
      }
    }
  }
}
