package edu.mayo.bmi.uima.termspotter.cc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.mayo.bmi.uima.core.cc.NonTerminalConsumer;
import edu.mayo.bmi.uima.pad.type.PADTerm;

public class RadialogyTermLookup
extends CasConsumer_ImplBase
implements NonTerminalConsumer
{

  public static final String PARAM_OUTPUTFILE = "outputFileName";
  
  private static final String[] RADIALOGY_TERMS = new String[]
  {
    "Cardiomegally", 
    "Pulmonary venous hypertension",
    "Pleural Effusion",
    "Edema",
    "Heart failure"
  };
  
  public RadialogyTermLookup()
  { super(); }

  public void initialize() throws ResourceInitializationException
  {
    try
    {
      super.initialize();
      //get file Name
      String outputFileName = (String)getConfigParameterValue(PARAM_OUTPUTFILE);
      
      iv_outputFile = new File(outputFileName);
      
      if(!iv_outputFile.exists())
        iv_outputFile.createNewFile();
      
      if(!iv_outputFile.canWrite() || !iv_outputFile.exists())
        throw new ResourceInitializationException(
            new Exception("Parameter setting 'OutputFileName' is invalid."));
      
      iv_fileWriter = new FileWriter(iv_outputFile);
      
    }
    catch(IOException ioe)
    { 
      throw new ResourceInitializationException(
        new Exception("Parameter setting 'OutputFileName' is invalid."));}
  }

  public void processCas(CAS cas) throws ResourceProcessException
  {
    StringBuffer data = new StringBuffer();
    int[] foundTerms = new int[]{0,0,0,0,0};
    //get a list of PADTerm annotations
    try
    {
      JCas jcas = cas.getJCas();
      JFSIndexRepository indexes = jcas.getJFSIndexRepository();
      
/**********************************************************************
 * NOTES: 1) Currently we are not using documentId for the webService.*
 *        2) process document Id - there must be only one of these.   *
 * ********************************************************************/
//      Iterator annotItr = indexes.getAnnotationIndex(DocumentId.type).iterator();
//      while(annotItr.hasNext())
//      {
//        DocumentId dia = (DocumentId)annotItr.next();
//        String sDocId = dia.getCoveredText();
//        data.append(sDocId);
//      }      
      
      //process all PADTerms
      Iterator annotItr = indexes.getAnnotationIndex(PADTerm.type).iterator();
      
      while(annotItr.hasNext())
      {
        PADTerm hft = (PADTerm)annotItr.next();
        
        String dictionaryTerm = hft.getHitDictionaryValue(); //.getCoveredText();
        storeTerm(dictionaryTerm, foundTerms);
      }

      converToCsvData(foundTerms, data);
      //for each document, build a Comma separated string representing
      //  the presence (with 1) or absence (with 0)
      //write the contents to a file

      iv_fileWriter.write(data.toString() +"\n");
    }
    catch(CASException ce)
    { throw new ResourceProcessException(ce); }
    catch(IOException ioe)
    { throw new ResourceProcessException(ioe);}
  }


  public void destroy()
  {
    super.destroy();
    try
    {
      iv_fileWriter.close();
    }
    catch(IOException ioe)
    { ioe.printStackTrace(); }
  }

  //-- private helper methods -------------
  private void converToCsvData(int[] foundTerms, StringBuffer data)
  {
    for(int i=0; i<foundTerms.length; i++)
    {
      data.append(",");
      data.append(foundTerms[i]);
    }
System.out.println("Radialogy term spotter"+data.toString());    
    webServiceOut = data.toString();
  }
  
  /**
   * cycle through RADIALOGY_TERMS, if the currentTerm matches 
   * (even part match)any element change the value in that element to 1
   * @param currentTerm
   * @param foundTerms
   */
  private void storeTerm(String currentTerm, int[] foundTerms)
  {
    for(int i=0; i<RADIALOGY_TERMS.length; i++)
    {
      //found the term
      if((RADIALOGY_TERMS[i].compareToIgnoreCase(currentTerm) == 0 || 
          RADIALOGY_TERMS[i].indexOf(currentTerm) >=0 || 
          currentTerm.indexOf(RADIALOGY_TERMS[i])>=0))
      {
        foundTerms[i] = 1;
        break;
      }
    }
  }
  
  /**
   * Used by the web service to get the result
   * @return
   */
  public String getOutputXml()
  {
System.out.println("RadialogyTermLookup.getOutputXml():["+webServiceOut+"]");    
    return webServiceOut; 
  }
  
  //-- private data members ---------------
  private File iv_outputFile;
  private FileWriter iv_fileWriter;
  private String webServiceOut;

}
