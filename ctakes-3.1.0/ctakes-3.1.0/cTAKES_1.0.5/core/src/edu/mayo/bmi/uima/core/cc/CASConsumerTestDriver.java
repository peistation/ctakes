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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.collection.CasConsumer;
import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.util.XMLInputSource;


public class CASConsumerTestDriver 
{
	public static void main(String[] args) 
	{
		try
		{  
			String xCasLocation = args[0];
			String taeDescriptionLocation = args[1];
			String casConsumerDescriptorLocation = args[2];
			
			InputStream xCasStream = new FileInputStream(xCasLocation);
			
			AnalysisEngineDescription taeDescription = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(
					new XMLInputSource(new File(taeDescriptionLocation)));
			
			AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(taeDescription);
			System.out.println("analysis engine created.");
			
			CAS cas = ae.newCAS();
			
			XCASDeserializer.deserialize(xCasStream, cas);
			System.out.println("XCAS deserialized");
			
			CasConsumerDescription casConsumerDescription = UIMAFramework.getXMLParser().parseCasConsumerDescription(
				new XMLInputSource(new File(casConsumerDescriptorLocation)));
            CasConsumer casConsumer = UIMAFramework.produceCasConsumer(casConsumerDescription);
            System.out.println("CasConsumer initialized.  Calling processCas....");
            casConsumer.processCas(cas);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
