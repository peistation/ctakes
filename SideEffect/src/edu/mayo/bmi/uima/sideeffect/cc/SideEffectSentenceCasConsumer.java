package edu.mayo.bmi.uima.sideeffect.cc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.sideeffect.types.SESentence;

public class SideEffectSentenceCasConsumer extends CasConsumer_ImplBase {
	public static final String PARAM_OUTPUT_FILE = "OutputFile";
	public static final String PARAM_DELIMITER = "Delimiter";  
	private BufferedWriter iv_bw = null;
	private String iv_delimiter;

	public void initialize() throws ResourceInitializationException {
		File outFile;

		try
		{
			String filename = (String) getConfigParameterValue(PARAM_OUTPUT_FILE);
			outFile = new File(filename);
			if (!outFile.exists())
				outFile.createNewFile();
			iv_bw = new BufferedWriter(new FileWriter(outFile));

			iv_delimiter = (String) getConfigParameterValue(PARAM_DELIMITER);

		} catch (Exception ioe)
		{
			throw new ResourceInitializationException(ioe);
		}
	}
	 
	public void processCas(CAS cas) throws ResourceProcessException {
		try {
			JCas jcas;
			jcas = cas.getJCas();
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();			
			
	        String docName = DocumentIDAnnotationUtil.getDocumentID(jcas);        

	        Iterator ssIter = indexes.getAnnotationIndex(SESentence.type).iterator();
	        while(ssIter.hasNext()) {
	        	SESentence ss = (SESentence) ssIter.next();
	        	
	        	String ssText = ss.getCoveredText().trim();
	        	
	        	String ssSpan = Integer.toString(ss.getBegin()) + ":" 
	        					+ Integer.toString(ss.getEnd());
	        		   
	        	String output = docName + iv_delimiter + ssText + iv_delimiter 
	        					+ ssSpan;
	        	
	        	iv_bw.write(output+"\n");
	        }

		} catch (Exception e) {
			throw new ResourceProcessException(e);
		}
	}
	
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException, IOException
	{
		super.collectionProcessComplete(arg0);

		try
		{
			iv_bw.flush();
			iv_bw.close();
		}
		catch(Exception e)
		{ throw new ResourceProcessException(e); }
	}
}
