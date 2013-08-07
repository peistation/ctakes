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
package edu.mayo.bmi.uima.lookup.ae;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * UIMA annotator that identified entities based on lookup.
 * 
 * @author Mayo Clinic
 */
public class UmlsDictionaryLookupAnnotator extends DictionaryLookupAnnotator
{
	/* Special implementation to pre bundle the UMLS SnowmedCT/RxNorm dictionaries
	 * Performs a check for user's UMLS licence at init time via their RESTful API
	 * User's will need to configure their UMLS username/password in their config
	 */
	private final static String UMLSADDR_PARAM = "UMLSAddr";
	private final static String UMLSVENDOR_PARAM = "UMLSVendor";
	private final static String UMLSUSER_PARAM = "UMLSUser";
	private final static String UMLSPW_PARAM = "UMLSPW";
	
	private Logger iv_logger = Logger.getLogger(getClass().getName());
	private UimaContext iv_context;
	private Set iv_lookupSpecSet = new HashSet();
	private String UMLSAddr;
	private String UMLSVendor;
	private String UMLSUser;
	private String UMLSPW;

	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		super.initialize(aContext);
		iv_context = aContext;

		try {
			UMLSAddr = (String) iv_context.getConfigParameterValue(UMLSADDR_PARAM);
			UMLSVendor = (String) iv_context.getConfigParameterValue(UMLSVENDOR_PARAM);
			UMLSUser = (String) iv_context.getConfigParameterValue(UMLSUSER_PARAM);
			UMLSPW = (String) iv_context.getConfigParameterValue(UMLSPW_PARAM);
			
			iv_logger.info("Using " + UMLSADDR_PARAM + ":" + UMLSAddr + ":" + UMLSUser);
			if(!isValidUMLSUser(UMLSAddr, UMLSVendor, UMLSUser, UMLSPW))
			{
				iv_logger.error("Error: Invalid UMLS License.  A UMLS License is required to use the UMLS dictionary lookup. \n" +
						"Error: You may request one at: https://uts.nlm.nih.gov/license.html \n" +
						"Please verify your UMLS license settings in the DictionaryLookupAnnotatorUMLS.xml configuration.");
				throw new Exception("Failed to initilize.  Invalid UMLS License");
			}
			
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}	
	}

	public static boolean isValidUMLSUser(String umlsaddr, String vendor, String username, String password) throws Exception {
		String data = URLEncoder.encode("licenseCode", "UTF-8") + "="
				+ URLEncoder.encode(vendor, "UTF-8");
		data += "&" + URLEncoder.encode("user", "UTF-8") + "="
				+ URLEncoder.encode(username, "UTF-8");
		data += "&" + URLEncoder.encode("password", "UTF-8") + "="
				+ URLEncoder.encode(password, "UTF-8");
		URL url = new URL(umlsaddr);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		boolean result = false;
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			if(line!=null && line.trim().length()>0)
			{
			 result = line.trim().equalsIgnoreCase("<Result>true</Result>");
			}
		}
		wr.close();
		rd.close();
		return result;
	}
}