package org.apache.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ctakes.typesystem.type.syntax.TerminalTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class IdentifiedAnnotationExpander extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    
    for(EventMention eventMention : JCasUtil.select(jCas, EventMention.class)) {
      List<Integer> oldSpan = expandToNP(jCas, eventMention);
    }
  }

  public static List<Integer> expandToNP(JCas jCas, IdentifiedAnnotation identifiedAnnotation) {

    // preserve the original begin and end of the annotation
    List<Integer> originalSpan = Lists.newArrayList(identifiedAnnotation.getBegin(), identifiedAnnotation.getEnd());

    // map each covering treebank node to its character length
    Map<TreebankNode, Integer> treebankNodeSizes = new HashMap<TreebankNode, Integer>();
    for(TreebankNode treebankNode : JCasUtil.selectCovering(
        jCas, 
        TreebankNode.class, 
        identifiedAnnotation.getBegin(), 
        identifiedAnnotation.getEnd())) {

      // only expand nouns (and not verbs or adjectives)
      if(treebankNode instanceof TerminalTreebankNode) {
        if(! treebankNode.getNodeType().startsWith("N")) {
          return originalSpan;
        }
      }

      // because only nouns are expanded, look for covering NPs
      if(treebankNode.getNodeType().equals("NP")) {
        treebankNodeSizes.put(treebankNode, treebankNode.getCoveredText().length());
      }
    }

    // find the shortest covering treebank node
    List<TreebankNode> sortedTreebankNodes = new ArrayList<TreebankNode>(treebankNodeSizes.keySet());
    Function<TreebankNode, Integer> getValue = Functions.forMap(treebankNodeSizes);
    Collections.sort(sortedTreebankNodes, Ordering.natural().onResultOf(getValue));

    if(sortedTreebankNodes.size() > 0) {
      identifiedAnnotation.setBegin(sortedTreebankNodes.get(0).getBegin());
      identifiedAnnotation.setEnd(sortedTreebankNodes.get(0).getEnd());
    }

    return originalSpan;
  }
  
  public static String getEnclosingNP(JCas jCas, IdentifiedAnnotation identifiedAnnotation) {

    // map each covering treebank node to its character length
    Map<TreebankNode, Integer> treebankNodeSizes = new HashMap<TreebankNode, Integer>();
    for(TreebankNode treebankNode : JCasUtil.selectCovering(
        jCas, 
        TreebankNode.class, 
        identifiedAnnotation.getBegin(), 
        identifiedAnnotation.getEnd())) {

      // only expand nouns (and not verbs or adjectives)
      if(treebankNode instanceof TerminalTreebankNode) {
        if(! treebankNode.getNodeType().startsWith("N")) {
          return identifiedAnnotation.getCoveredText();
        }
      }

      // because only nouns are expanded, look for covering NPs
      if(treebankNode.getNodeType().equals("NP")) {
        treebankNodeSizes.put(treebankNode, treebankNode.getCoveredText().length());
      }
    }

    // find the shortest covering treebank node
    List<TreebankNode> sortedTreebankNodes = new ArrayList<TreebankNode>(treebankNodeSizes.keySet());
    Function<TreebankNode, Integer> getValue = Functions.forMap(treebankNodeSizes);
    Collections.sort(sortedTreebankNodes, Ordering.natural().onResultOf(getValue));

    if(sortedTreebankNodes.size() > 0) {
      return sortedTreebankNodes.get(0).getCoveredText();
    } 

    return identifiedAnnotation.getCoveredText();
  }
}
