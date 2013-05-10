package org.apache.ctakes.temporal.data.analysis;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.TreeUtils;
import org.apache.ctakes.temporal.eval.CommandLine;
import org.apache.ctakes.temporal.eval.Evaluation_ImplBase.XMIReader;
import org.apache.ctakes.temporal.eval.THYMEData;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

public class TimexTreeAlignmentStatistics {
  static interface Options{
    @Option(longName = "xmi")
    public File getXMIDirectory();

    @Option(longName = "patients")
    public CommandLine.IntegerRanges getPatients();

    @Option(longName = "text")
    public File getRawTextDirectory();
  }
  
  /**
   * @param args
   * @throws IOException 
   * @throws UIMAException 
   */
  public static void main(String[] args) throws UIMAException, IOException {
    Options options = CliFactory.parseArguments(Options.class, args);
    List<Integer> patientSets = options.getPatients().getList();
    List<Integer> trainItems = THYMEData.getTrainPatientSets(patientSets);
    //List<Integer> devItems = THYMEData.getDevPatientSets(patientSets);
    //List<Integer> testItems = THYMEData.getTestPatientSets(patientSets);

    CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(getFilesFor(trainItems, options.getRawTextDirectory()));
    AggregateBuilder aggregateBuilder = new AggregateBuilder();
    aggregateBuilder.add(UriToDocumentTextAnnotator.getDescription());
    aggregateBuilder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIReader.class,
        XMIReader.PARAM_XMI_DIRECTORY,
        options.getXMIDirectory()));
    AnalysisEngine ae = aggregateBuilder.createAggregate();
    int numMentions=0;
    int numMatches=0;
    
    for(JCas jCas : new JCasIterable(reader, ae)){
      //      String docId = DocumentIDAnnotationUtil.getDocumentID(jCas);
      //      String docId = jCas.
      //      System.out.println("Document: " + docId);
      for(Segment segment : JCasUtil.select(jCas, Segment.class)){
    	  if(THYMEData.SEGMENTS_TO_SKIP.contains(segment.getId())) continue;
        Collection<TimeMention> mentions = JCasUtil.selectCovered(jCas.getView("GoldView"), TimeMention.class, segment);
        for(TimeMention mention : mentions){
          numMentions++;
          boolean match = false;
          List<TreebankNode> nodes = JCasUtil.selectCovered(jCas, TreebankNode.class, mention);
          for(TreebankNode node : nodes){
            if(node.getBegin() == mention.getBegin() && node.getEnd() == mention.getEnd()){
              numMatches++;
              match = true;
              break;
            }
          }
          if(!match){
            List<TreebankNode> coveringNodes = JCasUtil.selectCovering(jCas, TreebankNode.class, mention.getBegin(), mention.getEnd());
            TreebankNode smallestCoveringNode = null;
            int smallestLen = Integer.MAX_VALUE;
            for(TreebankNode node : coveringNodes){
              int len = node.getEnd() - node.getBegin();
              if(len <  smallestLen){
                smallestLen = len;
                smallestCoveringNode = node;
              }
            }
            System.out.println("No alignment for: " + mention.getCoveredText());
            System.out.println("  Smallest covering treebank node is: " + (smallestCoveringNode == null ? "null" : smallestCoveringNode.getCoveredText()));
            System.out.println("  " + (smallestCoveringNode == null ? "no tree" : TreeUtils.tree2str(smallestCoveringNode)));
          }
        }
      }
    }
    System.out.printf("Found %d mentions, %d match with node spans\n", numMentions, numMatches);
  }

  private static List<File> getFilesFor(List<Integer> patientSets, File rawTextDirectory) {
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
}
