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
package edu.mayo.bmi.uima.core.resource;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import opennlp.model.MaxentModel;
import opennlp.maxent.io.SuffixSensitiveGISModelReader;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Loads a suffix-sensitive GISModel into memory as a shared UIMA resource.
 * 
 * @author Mayo Clinic
 */
public class SuffixMaxentModelResourceImpl
        implements MaxentModelResource, SharedResourceObject
{
    // LOG4J logger based on class name
    private Logger iv_logger = Logger.getLogger(getClass().getName());

    private MaxentModel iv_maxentModel;

    /**
     * Load model from a file.
     */
    public void load(DataResource dr) throws ResourceInitializationException
    {
        iv_logger.info("Loading resource: " + dr.getUrl());
        try
        {
            File modelFile = new File(dr.getUri());
            SuffixSensitiveGISModelReader reader = new SuffixSensitiveGISModelReader(modelFile);

            iv_maxentModel = reader.getModel();

            iv_logger.info("Loaded resource, bytes=" + modelFile.length());
        }
        catch (IOException ioe)
        {
            throw new ResourceInitializationException(ioe);
        }
    }

    public MaxentModel getModel()
    {
        return iv_maxentModel;
    }
}
