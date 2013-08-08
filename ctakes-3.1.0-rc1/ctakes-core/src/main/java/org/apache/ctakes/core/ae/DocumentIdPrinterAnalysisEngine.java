package org.apache.ctakes.core.ae;


import java.util.logging.Logger;

import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;

public class DocumentIdPrinterAnalysisEngine extends JCasAnnotator_ImplBase
{
  protected final Logger logger = Logger.getLogger(DocumentIdPrinterAnalysisEngine.class.getName());

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException
  {
    String documentId = DocumentIDAnnotationUtil.getDocumentID(jcas);
    String logMessage = String.format("##### current file document id: \"%s\"", documentId);
    logger.info(logMessage);
    System.out.println(logMessage);
  }

}
