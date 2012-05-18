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
package edu.mayo.bmi.uima.core.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Reads in an array of Strings from a file - each line in the file is a string.  
 */


public class LinesFromFileStringsResourceImpl implements StringsResource, SharedResourceObject 
{
    public static String COMMENT_STRING = "%%";
	private String[] lines;
    
    public void load(DataResource dr) throws ResourceInitializationException
    {
    	try
    	{
	    	File file = new File(dr.getUri());
	        BufferedReader fileReader = new BufferedReader(new FileReader(file));
			ArrayList lines = new ArrayList();
			
			String line;
			while((line = fileReader.readLine()) != null)
			{
				if(!line.startsWith(COMMENT_STRING) &&
					line.trim().length() > 0)
					lines.add(line);
			}
	
	    	this.lines = (String[])lines.toArray(new String[lines.size()]);
    	}
    	catch(IOException ioe)
    	{
    		throw new ResourceInitializationException(ioe);
    	}
    }

    public String[] getStrings()
    {
    	return lines;
    }

}
