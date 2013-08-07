package org.chboston.cnlp.ctakes.relationextractor.eval;

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