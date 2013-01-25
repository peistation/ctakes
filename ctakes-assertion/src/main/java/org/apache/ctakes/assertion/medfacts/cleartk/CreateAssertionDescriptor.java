package org.apache.ctakes.assertion.medfacts.cleartk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier.ReferenceAnnotationsSystemAssertionClearer;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier.ReferenceIdentifiedAnnotationsSystemToGoldCopier;
import org.apache.ctakes.assertion.eval.AssertionEvalBasedOnModifier.ReferenceSupportingAnnotationsSystemToGoldCopier;
import org.apache.ctakes.core.ae.DocumentIdPrinterAnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.mitre.medfacts.uima.ZoneAnnotator;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.xml.sax.SAXException;

public class CreateAssertionDescriptor
{
  
  public static final Class<? extends DataWriterFactory<String>> dataWriterFactoryClass = DefaultMaxentDataWriterFactory.class;

  /**
   * @param args
   * @throws URISyntaxException 
   * @throws FileNotFoundException 
   * @throws ResourceInitializationException 
   */
  public static void main(String[] args) throws Exception
  {
    CreateAssertionDescriptor creator = new CreateAssertionDescriptor();
    
    creator.execute();

  }
  
  public void execute() throws Exception
  {
    createTrainDescriptor();
    createTestDescriptor();
  }
  
  public void createTrainDescriptor() throws Exception
  {
    File trainDirectory = new File("/tmp/assertion_data/train");
    File directory = trainDirectory;
    AggregateBuilder builder = new AggregateBuilder();

////
    AnalysisEngineDescription goldCopierIdentifiedAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierIdentifiedAnnotsAnnotator);
    
    AnalysisEngineDescription goldCopierSupportingAnnotsAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceSupportingAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierSupportingAnnotsAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    String generalSectionRegexFileUri =
        "org/mitre/medfacts/zoner/section_regex.xml";
    AnalysisEngineDescription zonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            generalSectionRegexFileUri
            );
    builder.add(zonerAnnotator);

    String mayoSectionRegexFileUri =
        "org/mitre/medfacts/uima/mayo_sections.xml";
    AnalysisEngineDescription mayoZonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            mayoSectionRegexFileUri
            );
    builder.add(mayoZonerAnnotator);
    
    
    AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        polarityAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "polarity").getPath()
        );
    builder.add(polarityAnnotator);

    AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        conditionalAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "conditional").getPath()
        );
    builder.add(conditionalAnnotator);

    AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        uncertaintyAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "uncertainty").getPath()
        );
    builder.add(uncertaintyAnnotator);

    AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        subjectAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "subject").getPath()
        );
    builder.add(subjectAnnotator);

    AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        genericAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        this.dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "generic").getPath()
        );
    builder.add(genericAnnotator);
    
////
    
    FileOutputStream outputStream = new FileOutputStream("desc/analysis_engine/assertion_train.xml");
    
    AnalysisEngineDescription description = builder.createAggregateDescription();
    
    description.toXML(outputStream);
  }

  public void createTestDescriptor() throws Exception
  {
    File testDirectory = new File("/tmp/assertion_data/test");
    File directory = testDirectory;
    File testOutputDirectory = new File("/tmp/assertion_data/test_output");
    AggregateBuilder builder = new AggregateBuilder();

////
    AnalysisEngineDescription goldCopierAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceIdentifiedAnnotationsSystemToGoldCopier.class);
    builder.add(goldCopierAnnotator);
    
    AnalysisEngineDescription assertionAttributeClearerAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ReferenceAnnotationsSystemAssertionClearer.class);
    builder.add(assertionAttributeClearerAnnotator);
    
    String generalSectionRegexFileUri =
      "org/mitre/medfacts/zoner/section_regex.xml";
    AnalysisEngineDescription zonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            generalSectionRegexFileUri
            );
    builder.add(zonerAnnotator);

    String mayoSectionRegexFileUri =
      "org/mitre/medfacts/uima/mayo_sections.xml";
    AnalysisEngineDescription mayoZonerAnnotator =
        AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
            ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
            mayoSectionRegexFileUri
            );
    builder.add(mayoZonerAnnotator);
    
    AnalysisEngineDescription polarityAnnotator = AnalysisEngineFactory.createPrimitiveDescription(PolarityCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        polarityAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(new File(directory, "polarity"), "model.jar").getPath()
        );
    builder.add(polarityAnnotator);

    AnalysisEngineDescription conditionalAnnotator = AnalysisEngineFactory.createPrimitiveDescription(ConditionalCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        conditionalAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(new File(directory, "conditional"), "model.jar").getPath()
        );
    builder.add(conditionalAnnotator);
  
    AnalysisEngineDescription uncertaintyAnnotator = AnalysisEngineFactory.createPrimitiveDescription(UncertaintyCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        uncertaintyAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(new File(directory, "uncertainty"), "model.jar").getPath()
        );
    builder.add(uncertaintyAnnotator);

    AnalysisEngineDescription subjectAnnotator = AnalysisEngineFactory.createPrimitiveDescription(SubjectCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        subjectAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(new File(directory, "subject"), "model.jar").getPath()
        );
    builder.add(subjectAnnotator);

    AnalysisEngineDescription genericAnnotator = AnalysisEngineFactory.createPrimitiveDescription(GenericCleartkAnalysisEngine.class); //,  this.additionalParamemters);
    ConfigurationParameterFactory.addConfigurationParameters(
        genericAnnotator,
        AssertionCleartkAnalysisEngine.PARAM_GOLD_VIEW_NAME,
        AssertionEvalBasedOnModifier.GOLD_VIEW_NAME,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(new File(directory, "generic"), "model.jar").getPath()
        );
    builder.add(genericAnnotator);

    AnalysisEngineDescription xwriter =
    AnalysisEngineFactory.createPrimitiveDescription(
          XWriter.class,
          AssertionComponents.CTAKES_CTS_TYPE_SYSTEM_DESCRIPTION,
          XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
          testOutputDirectory,
          XWriter.PARAM_XML_SCHEME_NAME,
          XWriter.XMI);
    builder.add(xwriter);
////
    
    FileOutputStream outputStream = new FileOutputStream("desc/analysis_engine/assertion_test.xml");
    
    AnalysisEngineDescription description = builder.createAggregateDescription();
    
    description.toXML(outputStream);
  }
  
  
}
