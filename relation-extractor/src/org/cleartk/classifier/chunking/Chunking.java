/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
package org.cleartk.classifier.chunking;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Interface for classes that assemble individual classifier outcomes on smaller annotations
 * ("sub-chunks") to form larger annotations ("chunks").
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public interface Chunking<OUTCOME_TYPE, SUB_CHUNK_TYPE extends Annotation, CHUNK_TYPE extends Annotation> {
  public List<OUTCOME_TYPE> toOutcomes(
      JCas jCas,
      List<SUB_CHUNK_TYPE> subChunks,
      List<CHUNK_TYPE> chunks) throws AnalysisEngineProcessException;

  public List<CHUNK_TYPE> toChunks(
      JCas jCas,
      List<SUB_CHUNK_TYPE> subChunks,
      List<OUTCOME_TYPE> outcomes) throws AnalysisEngineProcessException;

}
