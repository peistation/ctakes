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

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * @author Mayo Clinic
 */
public class FileResourceImpl implements FileResource, SharedResourceObject
{
    private File iv_file;
    private Logger iv_logger = Logger.getLogger(getClass().getName());
    
    public void load(DataResource dr) throws ResourceInitializationException
    {
    	URI uri = dr.getUri();
    	if(uri != null)
    	{
        	iv_file = new File(dr.getUri());
    	}
    	else
    	{
    		iv_logger.info("URI for data resource is null - using path from URL");
    		URL url = dr.getUrl();
    		if(url != null)
    		{
    	        String path = dr.getUrl().getPath();
	        	iv_file = new File(path);
    		}
    	}
    }

    public File getFile()
    {
        return iv_file;
    }    
}
