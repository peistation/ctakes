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
package org.apache.ctakes.core.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility class that attempts to locate files.
 * 
 * @author Mayo Clinic
 */
public class FileLocator
{
    public static InputStream getAsStream(String location) throws FileNotFoundException
    {
        try
        {
        	//Get from classpath
        	return FileLocator.class.getClassLoader().getResourceAsStream(location);
        }
        catch (Exception e)
        {
        	//Try to get from filestream
        	File f = new File(location);
        	FileInputStream fs = new FileInputStream(f);
        	return fs;
        }
    }
	
    /**
     * @deprecated  As of release 3.1, replaced by {@link #getAsStream()}
     */    
    public static File locateFile(String location)
            throws FileNotFoundException
    {
        try
        {
        	//System.out.println("cwd=" + new File(".").getAbsolutePath());
            return locateOnClasspath(location);
        }
        catch (Exception e)
        {
            return locateExplicitly(location);
        }
    }

    private static File locateOnClasspath(String cpLocation)
            throws FileNotFoundException, URISyntaxException
    {
        ClassLoader cl = FileLocator.class.getClassLoader();
        URL indexUrl = cl.getResource(cpLocation);
        if (indexUrl == null)
        {
            throw new FileNotFoundException(cpLocation);
        }

        URI indexUri = new URI(indexUrl.toExternalForm());
        File f = new File(indexUri);
        return f;
    }

    private static File locateExplicitly(String explicitLocation)
            throws FileNotFoundException
    {
        File f = new File(explicitLocation);
        if (!f.exists())
        {
            throw new FileNotFoundException(explicitLocation);
        }
        return f;
    }
}