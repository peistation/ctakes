package org.apache.ctakes.temporal.ae.feature;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.chunking.Chunking;

import com.google.common.collect.Lists;

public class ChunkingExtractor {

  private String name;

  private List<?> subChunkLabels;

  public <SUB_CHUNK_TYPE extends Annotation, CHUNK_TYPE extends Annotation> ChunkingExtractor(
      String name,
      Chunking<?, SUB_CHUNK_TYPE, CHUNK_TYPE> chunking,
      JCas jCas,
      List<SUB_CHUNK_TYPE> subChunks,
      List<CHUNK_TYPE> chunks) throws AnalysisEngineProcessException {
    this.name = name;
    this.subChunkLabels = chunking.createOutcomes(jCas, subChunks, chunks);
  }

  public List<Feature> extract(int tokenIndex, int nBefore, int nAfter) {
    List<Feature> features = Lists.newArrayList();
    int begin = Math.max(tokenIndex - nBefore, 0);
    int end = Math.min(tokenIndex + nAfter + 1, this.subChunkLabels.size());
    for (int i = begin; i < end; ++i) {
      String featureName = String.format("%s_%d", this.name, i - begin - nBefore);
      features.add(new Feature(featureName, this.subChunkLabels.get(i)));
    }
    return features;
  }

}
