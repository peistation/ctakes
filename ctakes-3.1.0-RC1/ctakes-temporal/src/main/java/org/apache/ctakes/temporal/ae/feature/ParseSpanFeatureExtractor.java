package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

public class ParseSpanFeatureExtractor  {

  public List<Feature> extract(JCas jcas, int begin, int end)
      {
    List<Feature> feats = new ArrayList<Feature>();
    
    TreebankNode domNode = AnnotationTreeUtils.annotationNode(jcas, begin, end);
    if(domNode != null){
      feats.add(new Feature("DominatingTreeCat", domNode.getNodeType()));
      if(domNode.getNodeTags() != null){
        for(int ind = 0; ind < domNode.getNodeTags().size(); ind++){
          String tag = domNode.getNodeTags(ind);
          if(tag.equals("TMP")){
            feats.add(new Feature("DominatingTmpTag", tag));
          }
        }
      }
      TreebankNode parent = domNode.getParent();
      if(parent != null){
        feats.add(new Feature("DominatingTreeParent", parent.getNodeType()));
        do{
          if(parent.getNodeTags() != null){
            for(int ind = 0; ind < parent.getNodeTags().size(); ind++){
              String tag = parent.getNodeTags(ind);
//              if(tag.equals("TMP")){
                feats.add(new Feature("DominatingAncestorTmpTag", tag));
//              }
            }
          }
          parent = parent.getParent();
        }while(parent != null);
      }
      
      if(domNode.getLeaf()){
        feats.add(new Feature("DominatingIsLeaf"));
      }else{
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < domNode.getChildren().size(); i++){
          buffer.append(domNode.getChildren(i).getNodeType());
          buffer.append("_");
          feats.add(new Feature("DominatingChildBag" + domNode.getChildren(i).getNodeType()));
        }
        feats.add(new Feature("DominatingProduction", buffer.toString()));
      }
      if(domNode.getBegin() == begin && domNode.getEnd() == end){
        feats.add(new Feature("DominatingExactMatch"));
      }
    }
    return feats;
  }

}
