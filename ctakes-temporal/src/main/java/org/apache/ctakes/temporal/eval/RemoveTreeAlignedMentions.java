package org.apache.ctakes.temporal.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

public class RemoveTreeAlignedMentions extends JCasAnnotator_ImplBase {

  public static final String PARAM_GOLDVIEW_NAME = "GOLD_VIEW_NAME";
  public static Logger logger = Logger.getLogger(RemoveTreeAlignedMentions.class);
  
  @ConfigurationParameter(
      name = PARAM_GOLDVIEW_NAME,
      mandatory = true,
      description = "Name of the cas view of gold standard data")
  private String goldViewName;
  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    JCas goldView = null;
    try {
      goldView = jCas.getView(goldViewName);
    } catch (CASException e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException("Could not extract gold view from jcas!", new Object[]{e});
    }
    
    Collection<TimeMention> times = JCasUtil.select(jCas, TimeMention.class);
    logger.info("File contains: " + times.size() + " timex mentions from first pass.");
    
    List<Annotation> removeList = new ArrayList<Annotation>();
    for(TimeMention time : times){
      List<TreebankNode> nodes = JCasUtil.selectCovered(jCas, TreebankNode.class, time);
      boolean match = false;
      for(TreebankNode node : nodes){
        if(node.getBegin() == time.getBegin() && node.getEnd() == time.getEnd()){
          // we have a match
          match = true;
          break;
        }
      }
      if(match){
        // add the mention since it aligns with a tree.
        removeList.add(time);
//        time.removeFromIndexes();
      }     
    }
    for(Annotation mention : removeList){
      mention.removeFromIndexes();
    }
    for(TimeMention time : JCasUtil.select(jCas, TimeMention.class)){
      logger.info("Preserved time mention: " + time.getCoveredText());
    }
  }
}
