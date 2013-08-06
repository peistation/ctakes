package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.Predicate;
import org.apache.ctakes.typesystem.type.textsem.SemanticArgument;
import org.apache.ctakes.typesystem.type.textsem.SemanticRoleRelation;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class SRLFeatureExtractor implements SimpleFeatureExtractor {

  @Override
  public List<Feature> extract(JCas jCas, Annotation entity)
      throws CleartkExtractorException {
    List<Feature> feats = new ArrayList<Feature>();
    List<Sentence> sents = JCasUtil.selectCovering(jCas, Sentence.class, entity.getBegin(), entity.getEnd());
    if(sents!= null && sents.size() > 0){
      List<String> srlFeats = getEntityFeats(jCas, (IdentifiedAnnotation) entity, sents.get(0));
      for(String feat : srlFeats){
        feats.add(new Feature(feat));
      }
    }
    return feats;
  }

  public static ArrayList<String> getEntityFeats(JCas jcas, IdentifiedAnnotation ent, Sentence sent){
    ArrayList<String> feats = new ArrayList<String>();

    List<BaseToken> words = JCasUtil.selectCovered(jcas, BaseToken.class, ent);
    if(words != null && words.size() > 0){
      BaseToken lastWord = words.get(words.size()-1);
      List<SemanticArgument> args = JCasUtil.selectCovered(jcas, SemanticArgument.class, lastWord);
      Predicate pred = null;

      if(args.size() > 0){
        SemanticArgument entArg = args.get(0);
        SemanticRoleRelation entRel = entArg.getRelation();
        pred = entRel.getPredicate();
        String feat = "pred:" + pred.getCoveredText().replace(' ', '_').toLowerCase();
        feats.add(feat);

        // get the relation this arg belongs to, then its predicate, then the other arguments (via relations)
        FSList rels = entArg.getRelation().getPredicate().getRelations();
        while(rels instanceof NonEmptyFSList){
          NonEmptyFSList node = (NonEmptyFSList) rels;
          SemanticRoleRelation curRel = (SemanticRoleRelation) node.getHead();
          SemanticArgument curArg =  curRel.getArgument();
          if(entArg.getCoveredText().equals(curArg.getCoveredText())){
            // show which arg is the current arg
            //          out.print("*");
          }else{
            feat = "SRL: " + curArg.getLabel() + ":" + curArg.getCoveredText().replace(' ', '_').toLowerCase();
            feats.add(feat);
          }

          rels = node.getTail();
        }
      }
      
      // now look for dependency relations:
      List<ConllDependencyNode> coveredNodes = JCasUtil.selectCovered(jcas, ConllDependencyNode.class, lastWord);
      if(coveredNodes.size() > 0){
        ConllDependencyNode curNode = coveredNodes.get(0);
        ConllDependencyNode predNode = null;
        if(pred != null){
          List<ConllDependencyNode> predNodes = JCasUtil.selectCovered(jcas, ConllDependencyNode.class, pred);
          if(predNodes.size() > 0){
            predNode = predNodes.get(0);
          }
        }
        List<ConllDependencyNode> allNodes = JCasUtil.selectCovered(jcas, ConllDependencyNode.class, sent);

        for(ConllDependencyNode node : allNodes){
          if(node.getHead() == curNode){
            String feat = "entpointer:" + node.getDeprel() + ":" + node.getCoveredText().replace(' ', '_').toLowerCase();
            feats.add(feat);
          }else if(predNode != null && node.getHead() == predNode){
            String feat = "predpointer:" + node.getDeprel() + ":" + node.getCoveredText().replace(' ', '_').toLowerCase();
            feats.add(feat);
          }
        }
      }
    }
    return feats;
  }
}
