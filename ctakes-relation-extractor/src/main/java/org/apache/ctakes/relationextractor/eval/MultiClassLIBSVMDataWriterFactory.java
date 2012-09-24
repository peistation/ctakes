/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.relationextractor.eval;

import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.StringToIntegerOutcomeEncoder;
import org.cleartk.classifier.jar.DataWriterFactory_ImplBase;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.classifier.libsvm.MultiClassLIBSVMDataWriter;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * This is a replacement for {@link DefaultMultiClassLIBSVMDataWriterFactory} that doesn't do
 * row-normalization.
 */
public class MultiClassLIBSVMDataWriterFactory extends
    DataWriterFactory_ImplBase<FeatureVector, String, Integer> {

  public DataWriter<String> createDataWriter() throws IOException {
    MultiClassLIBSVMDataWriter dataWriter = new MultiClassLIBSVMDataWriter(this.outputDirectory);
    FeatureVectorFeaturesEncoder fe = new FeatureVectorFeaturesEncoder(0);
    fe.addEncoder(new NumberEncoder());
    fe.addEncoder(new BooleanEncoder());
    fe.addEncoder(new StringEncoder());
    dataWriter.setFeaturesEncoder(fe);
    dataWriter.setOutcomeEncoder(new StringToIntegerOutcomeEncoder());
    return dataWriter;
  }
}