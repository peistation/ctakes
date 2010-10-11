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

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;

/**
 * Oct 2010 - convert to lucene 3.0.2
 * @author Mayo Clinic
 */
public class LuceneIndexReaderResourceImpl
        implements LuceneIndexReaderResource, SharedResourceObject {
	
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    private IndexReader iv_indexReader;

    /**
     * Loads a Lucene index for reading.
     */
    public void load(DataResource dr) throws ResourceInitializationException {

        ConfigurationParameterSettings cps = dr.getMetaData().getConfigurationParameterSettings();
        Boolean useMemoryIndex = (Boolean) cps.getParameterValue("UseMemoryIndex");

        String indexDirStr = (String) cps.getParameterValue("IndexDirectory");
        try {

            File indexDir = FileLocator.locateFile(indexDirStr);

            if(!indexDir.exists())
            	iv_logger.info("indexDir="+indexDirStr+"  does not exist!");
            else
            	iv_logger.info("indexDir="+indexDirStr+"  exists.");
            
            if (useMemoryIndex.booleanValue()) {

                iv_logger.info("Loading Lucene Index into memory: " + indexDir);
                FSDirectory fsd = FSDirectory.open(indexDir);
                Directory d = new RAMDirectory(fsd);
                iv_indexReader = IndexReader.open(d);
            }
            else {
                iv_logger.info("Loading Lucene Index: " + indexDir);
                FSDirectory fsd = FSDirectory.open(indexDir);
                iv_indexReader = IndexReader.open(fsd);
            }
            iv_logger.info("Loaded Lucene Index, # docs=" + iv_indexReader.numDocs());
        }
        catch (Exception e) {
        	throw new ResourceInitializationException(e);
        }
    }

    public IndexReader getIndexReader() {
        return iv_indexReader;
    }
}