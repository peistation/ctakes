package org.chboston.cnlp.ctakes.relationextractor.cpe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.uimafit.component.CasConsumer_ImplBase;

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

public class CasConsumer extends CasConsumer_ImplBase
{
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    // FIXME: this should become a configuration parameter
    private String iv_outputDir = "/home/dima/temp/out";
    
    // iv_procCount is used to name the output files sequentially if there 
    // is a problem with naming based on source names
    private int iv_procCount = 0; 

    
    /**
     * Read in configuration parameters
     */
    public void initialize() throws ResourceInitializationException {
        //iv_outputDir = (String) getConfigParameterValue("outputDir");
    }


    /**
     * Write a formatted xml file containing data from the view.
     * The file name will come from the DocumentID annotation,
     * which is associated with a view.
     * We append .xml to the DocumentID/filename 
     */
    private void processView(JCas view) throws Exception {

        String docName = DocumentIDAnnotationUtil.getDocumentID(view);

        File outputFile;
        if (docName==null) {
        	docName = "doc" + iv_procCount + ".xml";
        }
        else {
        	docName = docName + ".xml";        		
        }
        
        OutputStream out=null;
        try {
        	File outputDir = new File(iv_outputDir);
        	outputDir.mkdirs();
            outputFile = new File(iv_outputDir + File.separatorChar + docName);
            out = new FileOutputStream(outputFile);
            XCASSerializer.serialize(view.getCas(), out, true); // true -> formats the output
            System.out.println(out);
        } 
        finally {
	        iv_procCount++;
	        if (out != null) {
	        	out.close();
	        }
        }

    }

    /**
     * Create an xml file from the data in the cas.
     */
    public void processCas(CAS cas) throws ResourceProcessException {

    	iv_logger.info("Started");
    	
        try { 

        	JCas currentView = cas.getCurrentView().getJCas();
            processView(currentView);
            
        } catch (Exception e) {
        	throw new ResourceProcessException(e);
        }
    }

	@Override
	public void process(CAS cas) throws AnalysisEngineProcessException {
		
		try {
	    processCas(cas);
    } catch (ResourceProcessException e) {
	    e.printStackTrace();
    }
	}

}
