package org.apache.ctakes.temporal.data.analysis;

import java.io.File;
import java.util.Collections;
import java.util.List;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
        String containerDocTimeRel =
            container instanceof EventMention
                ? ((EventMention) container).getEvent().getProperties().getDocTimeRel()
                : null;
        boolean inconsistentDocTimeRels = false;
        String groupDocTimeRel = null;
        for (EventMention event : containers.get(container)) {
          String docTimeRel = event.getEvent().getProperties().getDocTimeRel();
          if (groupDocTimeRel == null) {
            groupDocTimeRel = docTimeRel;
          } else if (!docTimeRel.equals(groupDocTimeRel)) {
            inconsistentDocTimeRels = true;
            break;
          } else if (containerDocTimeRel != null && !docTimeRel.equals(containerDocTimeRel)) {
            inconsistentDocTimeRels = true;
            break;
          }
        }

        // if inconsistent: print events, DocTimeRels and surrounding context
        if (inconsistentDocTimeRels) {
          List<Integer> offsets = Lists.newArrayList();
          offsets.add(container.getBegin());
          offsets.add(container.getEnd());
          for (EventMention event : containers.get(container)) {
            offsets.add(event.getBegin());
            offsets.add(event.getEnd());
          }
          Collections.sort(offsets);
          int begin = Math.max(offsets.get(0), 0);
          int end = Math.min(offsets.get(offsets.size() - 1), text.length());
          System.err.printf(
              "Inconsistent DocTimeRels in %s, ...%s...\n",
              new File(ViewURIUtil.getURI(jCas)).getName(),
              text.substring(begin, end));
          if (container instanceof EventMention) {
            System.err.printf(
                "Container: \"%s\" (docTimeRel=%s)\n",
                container.getCoveredText(),
                ((EventMention) container).getEvent().getProperties().getDocTimeRel());
          } else {
            System.err.printf("Container: \"%s\"\n", container.getCoveredText());
          }
          for (EventMention event : containers.get(container)) {
            System.err.printf(
                "* \"%s\" (docTimeRel=%s)\n",
                event.getCoveredText(),
                event.getEvent().getProperties().getDocTimeRel());
          }
          System.err.println();
        }
      }
    }
  }
}
