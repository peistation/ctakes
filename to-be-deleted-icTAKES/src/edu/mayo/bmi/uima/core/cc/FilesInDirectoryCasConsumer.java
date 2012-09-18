/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.core.cc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * For each CAS a local file with the document text is written to a directory specifed by a parameter.  
 * This CAS consumer does not make use of any annotation information in the cas except for the document 
 * id specified the CommonTypeSystem.xml descriptor.  The document id will be the name of the file written 
 * for each CAS.  
 * 
 * This CAS consumer may be useful if you want to write the results of a collection reader and/or CAS 
 * initializer to the local file system.  For example, a JDBC Collection Reader may read XML documents 
 * from a database and a specialized cas initializer may convert the XML to plain text.  The 
 * FilesInDirectoryCasConsumer can now be used to write the plain text to local plain text files.
 */

public class FilesInDirectoryCasConsumer extends CasConsumer_ImplBase {

	public static final String PARAM_OUTPUTDIR = "OutputDirectory";

	File iv_outputDirectory;
	
	public void initialize() throws ResourceInitializationException 
	{
	    String outputDirectoryName = (String)getConfigParameterValue(PARAM_OUTPUTDIR);
	    iv_outputDirectory = new File(outputDirectoryName);
	    if(!iv_outputDirectory.exists() || !iv_outputDirectory.isDirectory())
	    	throw new ResourceInitializationException(
	    			new Exception("Parameter setting 'OutputDirectory' does not point to an existing directory."));
	}
	
	public void processCas(CAS cas) throws ResourceProcessException 
	{
		try 
		{
			JCas jcas;
			jcas = cas.getJCas();
			//	jcas = cas.getJCas().getView("_InitialView");
			//	jcas = cas.getJCas().getView("plaintext");
		
			String documentID = DocumentIDAnnotationUtil.getDocumentID(jcas);
			String documentText = jcas.getDocumentText();

			if (documentID==null) {

				jcas = cas.getJCas().getView("_InitialView");
				documentID = DocumentIDAnnotationUtil.getDocumentID(jcas);

				if (documentID==null) {
				
					jcas = cas.getJCas().getView("plaintext");
					documentID = DocumentIDAnnotationUtil.getDocumentID(jcas);
					
					if (documentID==null) {
						documentID = "doc_"+new java.util.Date().getTime()+".xml"; // use timestamp in name: doc_TIMESTAMP.xml 
						System.err.println("Unable to find DocumentIDAnnotation, using " + documentID);
					}
				}
				
			}

			writeToFile(documentID, documentText);
			
		}
		catch(Exception e)
		{
			throw new ResourceProcessException(e);
		}
	}
	
	private void writeToFile(String documentID, String documentText) throws IOException
	{
		File outputFile = new File(iv_outputDirectory, documentID);
		outputFile.createNewFile();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		out.write(documentText.getBytes());
		out.flush();
		out.close();
	}
}
