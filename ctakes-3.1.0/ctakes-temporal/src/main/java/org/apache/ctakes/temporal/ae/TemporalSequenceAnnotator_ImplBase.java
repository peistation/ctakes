package org.apache.ctakes.temporal.ae;

import org.apache.ctakes.temporal.eval.THYMEData;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.uimafit.util.JCasUtil;

public abstract class TemporalSequenceAnnotator_ImplBase extends
    CleartkSequenceAnnotator<String> {
  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Segment segment : JCasUtil.select(jCas, Segment.class)) {
      if (!THYMEData.SEGMENTS_TO_SKIP.contains(segment.getId())) {
        this.process(jCas, segment);
      }
    }
  }

  public abstract void process(JCas jCas, Segment segment) throws AnalysisEngineProcessException;

}
