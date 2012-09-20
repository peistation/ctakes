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
package edu.mayo.bmi.uima.core.cc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import org.apache.ctakes.typesystem.type.syntax.WordToken;
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

public class NormalizedFilesInDirectoryCasConsumer extends CasConsumer_ImplBase {

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
		
			StringBuffer normalizedText = new StringBuffer();
			
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
	        Iterator tokenItr = indexes.getAnnotationIndex(WordToken.type).iterator();
	        while (tokenItr.hasNext())
	        {
	        	WordToken token = (WordToken) tokenItr.next();
	        	String tokenNormText = token.getCanonicalForm();
	        	normalizedText.append(tokenNormText+" ");	        
	        }	        	
			String documentID = DocumentIDAnnotationUtil.getDocumentID(jcas);
			writeToFile(documentID, normalizedText.toString());
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
