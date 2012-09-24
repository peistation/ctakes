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
package org.apache.ctakes.core.cc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;

/**
 * For each CAS a local file with the offsets of the BaseToken annotations is written to a directory specifed by a parameter.
 * The format of the output files is
 * 0|13
 * 17|19
 * 19|20
 * ...
 *   
 * This CAS consumer does not make use of any annotation information in the 
 * cas except for the document id specified the CommonTypeSystem.xml 
 * descriptor and the BaseToken annotations.  The document id will be the 
 * name of the file written for each CAS.  
 * 
 * This CAS consumer was written so that token offsets could be written to 
 * a file.  The offsets were compared to similarly generated annotation offsets
 * from Knowtator annotations.  
 */

public class TokenOffsetsCasConsumer extends CasConsumer_ImplBase {

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
		
			List offsets = new ArrayList();
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();
			Iterator tokenItr = indexes.getAnnotationIndex(BaseToken.type).iterator();
	        while (tokenItr.hasNext())
	        {
	        	BaseToken token = (BaseToken) tokenItr.next();
	        	String offset = ""+token.getBegin()+"|"+token.getEnd();
	        	offsets.add(offset);
	        }	        	

	        String documentID = DocumentIDAnnotationUtil.getDocumentID(jcas);
			writeToFile(documentID, offsets);
			
		}
		catch(Exception e)
		{
			throw new ResourceProcessException(e);
		}
	}
	
	private void writeToFile(String documentID, List offsets) throws IOException
	{
		File outputFile = new File(iv_outputDirectory, documentID);
		outputFile.createNewFile();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		for(int i=0; i<offsets.size(); i++)
		{
			String offset = (String) offsets.get(i)+"\n";
			out.write(offset.getBytes());
		}
		out.flush();
		out.close();
	}
}
