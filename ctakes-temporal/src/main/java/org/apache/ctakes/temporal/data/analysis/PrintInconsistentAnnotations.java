package org.apache.ctakes.temporal.data.analysis;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.ctakes.temporal.eval.CommandLine;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.XMIReader;
import org.apache.ctakes.temporal.eval.THYMEData;
import org.apache.ctakes.typesystem.type.relation.TemporalTextRelation;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class PrintInconsistentAnnotations {
  static interface Options {
    @Option(longName = "xmi")
    public File getXMIDirectory();

    @Option(longName = "patients")
    public CommandLine.IntegerRanges getPatients();

    @Option(longName = "text")
    public File getRawTextDirectory();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    int windowSize = 50;
    
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

    int totalDocTimeRels = 0;
    int totalInconsistentDocTimeRels = 0;
    for (JCas jCas : new JCasIterable(reader, aggregateBuilder.createAggregate())) {
      String text = jCas.getDocumentText();
      JCas goldView = jCas.getView("GoldView");

      // group events by their narrative container
      Multimap<Annotation, EventMention> containers = HashMultimap.create();
      for (TemporalTextRelation relation : JCasUtil.select(goldView, TemporalTextRelation.class)) {
        if (relation.getCategory().equals("CONTAINS")) {
          Annotation arg1 = relation.getArg1().getArgument();
          Annotation arg2 = relation.getArg2().getArgument();
          if (arg2 instanceof EventMention) {
            EventMention event = (EventMention) arg2;
            containers.put(arg1, event);
          }
        }
      }

      // check each container for inconsistent DocTimeRels
      for (Annotation container : containers.keySet()) {
        Set<String> docTimeRels = Sets.newHashSet();
        for (EventMention event : containers.get(container)) {
          docTimeRels.add(event.getEvent().getProperties().getDocTimeRel());
        }
        totalDocTimeRels += docTimeRels.size();
        
        boolean inconsistentDocTimeRels;
        if (container instanceof EventMention) {
          EventMention mention = ((EventMention) container);
          String containerDocTimeRel = mention.getEvent().getProperties().getDocTimeRel();
          inconsistentDocTimeRels = false;
          for (String docTimeRel : docTimeRels) {
            if (docTimeRel.equals(containerDocTimeRel)) {
              continue;
            }
            if (containerDocTimeRel.equals("BEFORE/OVERLAP")
                && (docTimeRel.equals("BEFORE") || docTimeRel.equals("OVERLAP"))) {
              continue;
            }
            inconsistentDocTimeRels = true;
            break;
          }
        } else {
          if (docTimeRels.size() == 1) {
            inconsistentDocTimeRels = false;
          } else if (docTimeRels.contains("BEFORE/OVERLAP")) {
            inconsistentDocTimeRels =
                docTimeRels.size() == 1
                    && (docTimeRels.contains("BEFORE") || docTimeRels.contains("OVERLAP"));
          } else {
            inconsistentDocTimeRels = true;
          }
        }

        // if inconsistent: print events, DocTimeRels and surrounding context
        if (inconsistentDocTimeRels) {
          totalInconsistentDocTimeRels += docTimeRels.size();
          
          List<Integer> offsets = Lists.newArrayList();
          offsets.add(container.getBegin());
          offsets.add(container.getEnd());
          for (EventMention event : containers.get(container)) {
            offsets.add(event.getBegin());
            offsets.add(event.getEnd());
          }
          Collections.sort(offsets);
          int begin = Math.max(offsets.get(0) - windowSize, 0);
          int end = Math.min(offsets.get(offsets.size() - 1) + windowSize, text.length());
          System.err.printf(
              "Inconsistent DocTimeRels in %s, ...%s...\n",
              new File(ViewURIUtil.getURI(jCas)).getName(),
              text.substring(begin, end).replaceAll("([\r\n])[\r\n]+", "$1"));
          if (container instanceof EventMention) {
            System.err.printf(
                "Container: \"%s\" (docTimeRel=%s)\n",
                container.getCoveredText(),
                ((EventMention) container).getEvent().getProperties().getDocTimeRel());
          } else {
            System.err.printf("Container: \"%s\"\n", container.getCoveredText());
          }
          Ordering<EventMention> byBegin =
              Ordering.natural().onResultOf(new Function<EventMention, Integer>() {
                @Override
                public Integer apply(@Nullable EventMention event) {
                  return event.getBegin();
                }
              });
          for (EventMention event : byBegin.sortedCopy(containers.get(container))) {
            System.err.printf(
                "* \"%s\" (docTimeRel=%s)\n",
                event.getCoveredText(),
                event.getEvent().getProperties().getDocTimeRel());
          }
          System.err.println();
        }
      }
    }
    
    System.err.printf(
        "Inconsistent DocTimeRels: %.1f%% (%d/%d)\n",
        100.0 * totalInconsistentDocTimeRels / totalDocTimeRels,
        totalInconsistentDocTimeRels,
        totalDocTimeRels);
  }
}
