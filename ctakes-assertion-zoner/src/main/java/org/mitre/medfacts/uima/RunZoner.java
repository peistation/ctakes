package org.mitre.medfacts.uima;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

import org.apache.ctakes.core.util.CtakesFileNamer;
import org.apache.ctakes.core.ae.DocumentIdPrinterAnalysisEngine;
import org.apache.ctakes.core.cr.XMIReader;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ExternalResourceFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.pipeline.SimplePipeline;

public class RunZoner
{
  private static Logger logger = Logger.getLogger(RunZoner.class.getName());
  
  File inputDirectory;
  List<File> inputFiles;

  File outputDirectory;
  
  public static void main(String args[]) throws UIMAException, IOException, URISyntaxException
  {
    if (args.length != 2)
    {
      System.err.format("Syntax: %s input_directory output_directory%n", RunZoner.class.getName());
    }
    
    File inputDirectory = new File(args[0]);
    File outputDirectory = new File(args[1]);
    
    List<File> inputFiles = listContents(inputDirectory);
    
    RunZoner runner = new RunZoner();
    runner.setInputDirectory(inputDirectory);
    runner.setInputFiles(inputFiles);
    runner.setOutputDirectory(outputDirectory);
    
    runner.execute();
  }
  
  public static List<File> listContents(File inputDirectory)
  {
    File fileArray[] = inputDirectory.listFiles(new FilenameFilter()
    {
      
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xmi");
      }
    });
    
    List<File> fileList = Arrays.asList(fileArray);
    return fileList;
  }

  public void execute() throws UIMAException, IOException, URISyntaxException
  {
    AggregateBuilder builder = new AggregateBuilder();
    
    TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath();
    
    CollectionReader reader = 
        CollectionReaderFactory.createCollectionReader(
          XMIReader.class,
          typeSystemDescription,
          XMIReader.PARAM_FILES,
          inputFiles);
    
      AnalysisEngineDescription documentIdPrinter =
          AnalysisEngineFactory.createPrimitiveDescription(DocumentIdPrinterAnalysisEngine.class);
      builder.add(documentIdPrinter);
    
      String generalSectionRegexFileUri =
        "org/mitre/medfacts/zoner/section_regex.xml";
      //URI generalSectionRegexFileUri =
      //  this.getClass().getClassLoader().getResource("org/mitre/medfacts/zoner/section_regex.xml").toURI();
//      ExternalResourceDescription generalSectionRegexDescription = ExternalResourceFactory.createExternalResourceDescription(
//          SectionRegexConfigurationResource.class, new File(generalSectionRegexFileUri));
      AnalysisEngineDescription zonerAnnotator =
          AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
              ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
              generalSectionRegexFileUri
              );
      builder.add(zonerAnnotator);

      String mayoSectionRegexFileUri =
          "org/mitre/medfacts/zoner/mayo_sections.xml";
//      URI mayoSectionRegexFileUri =
//          this.getClass().getClassLoader().getResource("org/mitre/medfacts/zoner/mayo_sections.xml").toURI();
//        ExternalResourceDescription mayoSectionRegexDescription = ExternalResourceFactory.createExternalResourceDescription(
//            SectionRegexConfigurationResource.class, new File(mayoSectionRegexFileUri));
      AnalysisEngineDescription mayoZonerAnnotator =
          AnalysisEngineFactory.createPrimitiveDescription(ZoneAnnotator.class,
              ZoneAnnotator.PARAM_SECTION_REGEX_FILE_URI,
              mayoSectionRegexFileUri
              );
      builder.add(mayoZonerAnnotator);

      AnalysisEngineDescription xWriter = AnalysisEngineFactory.createPrimitiveDescription(
          XWriter.class,
          typeSystemDescription,
          XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
          outputDirectory.toString(),
          XWriter.PARAM_FILE_NAMER_CLASS_NAME,
          CtakesFileNamer.class.getName()
          );
      
      builder.add(xWriter);

    logger.info("BEFORE RUNNING PIPELINE...");
    SimplePipeline.runPipeline(reader,  builder.createAggregateDescription());
    logger.info("AFTER RUNNING PIPELINE...COMPLETED");
  }

  public File getInputDirectory()
  {
    return inputDirectory;
  }

  public void setInputDirectory(File inputDirectory)
  {
    this.inputDirectory = inputDirectory;
  }

  public List<File> getInputFiles()
  {
    return inputFiles;
  }

  public void setInputFiles(List<File> inputFiles)
  {
    this.inputFiles = inputFiles;
  }

  public File getOutputDirectory()
  {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory)
  {
    this.outputDirectory = outputDirectory;
  }
  
}
