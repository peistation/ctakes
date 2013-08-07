/**
* Copyright (c) 2009, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.engine;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import clear.reader.AbstractReader;

/**
 * <b>Last update:</b> 6/29/2010
 * @author Jinho D. Choi
 */
public class AbstractEngine
{
	protected final String TAG_COMMON             = "common";
	protected final String TAG_COMMON_LANGUAGE    = "language";
	protected final String TAG_COMMON_FORMAT      = "format";
	protected final String TAG_COMMON_LEXICON_DIR = "lexicon_dir";
	protected final String TAG_COMMON_MODEL_FILE  = "model_file";
	protected final String TAG_COMMON_FEATURE_XML = "feature_xml";
	
	/** Language */
	protected String  s_language   = null;
	/** Format */
	protected String  s_format     = null;
	/** Lexicon directory */
	protected String  s_lexiconDir = null;
	/** Model file */
	protected String  s_modelFile  = null;
	/** Feature template file */
	protected String  s_featureXml = null;
	/** Configure element */
	protected Element e_config;
	
	/** Initializes <configuration> element. */
	public boolean initConfigElement(String configXml)
	{
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		
		try
		{
			DocumentBuilder builder = dFactory.newDocumentBuilder();
			Document        doc     = builder.parse(new File(configXml));
			e_config                = doc.getDocumentElement();
		}
		catch (Exception e) {e.printStackTrace();return false;}
		
		return true;
	}
	
	/** Initializes <common> element. */	
	protected boolean initCommonElements()
	{
		Element eCommon, eLanguage, eFormat, eConfigDir, eModelFile, eFeatureXml;
		
		// <common>
		if ((eCommon = getElement(e_config, TAG_COMMON)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"> must be specified.");
			return false;
		}
		
		// <language>
		if ((eLanguage = getElement(eCommon, TAG_COMMON_LANGUAGE)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"."+TAG_COMMON_LANGUAGE+"> must be specified.");
			return false;
		}
		
		s_language = eLanguage.getTextContent().trim();
	
		if (!s_language.equals(AbstractReader.LANG_CH) && !s_language.equals(AbstractReader.LANG_EN) && !s_language.equals(AbstractReader.LANG_HI))
		{
			System.err.println("Error: invalid <"+TAG_COMMON+"."+TAG_COMMON_LANGUAGE+" = "+s_language+">.");
			return false;
		}
		
		// <format>
		if ((eFormat = getElement(eCommon, TAG_COMMON_FORMAT)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"."+TAG_COMMON_FORMAT+"> must be specified.");
			return false;
		}
		
		s_format = eFormat.getTextContent().trim();
		
		if (!s_format.equals(AbstractReader.FORMAT_RAW) && !s_format.equals(AbstractReader.FORMAT_POS) && !s_format.equals(AbstractReader.FORMAT_DEP) &&
			!s_format.equals(AbstractReader.FORMAT_SRL) && !s_format.equals(AbstractReader.FORMAT_CONLL))
		{
			System.err.println("Error: invalid <"+TAG_COMMON+"."+TAG_COMMON_FORMAT+" = "+s_format+">.");
			return false;
		}
		
		// <lexicon_dir>
		if ((eConfigDir = getElement(eCommon, TAG_COMMON_LEXICON_DIR)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"."+TAG_COMMON_LEXICON_DIR+"> must be specified.");
			return false;
		}
		
		s_lexiconDir = eConfigDir.getTextContent().trim();
		
		// <model_file>
		if ((eModelFile = getElement(eCommon, TAG_COMMON_MODEL_FILE)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"."+TAG_COMMON_MODEL_FILE+"> must be specified.");
			return false;
		}
		
		s_modelFile = eModelFile.getTextContent().trim();
		
		// <feature_xml>
		if ((eFeatureXml = getElement(eCommon, TAG_COMMON_FEATURE_XML)) == null)
		{
			System.err.println("Error: <"+TAG_COMMON+"."+TAG_COMMON_FEATURE_XML+"> must be specified.");
			return false;
		}
		
		s_featureXml = eFeatureXml.getTextContent().trim();
		
		return true;
	}
	
	/** Prints <common> configuration. */
	protected void printCommonConfig()
	{
		System.out.println("Configurations");
		System.out.println("- language   : "+s_language);
		System.out.println("- format     : "+s_format);
		System.out.println("- lexicon_dir: "+s_lexiconDir);
		System.out.println("- model_file : "+s_modelFile);
		System.out.println("- feature_xml: "+s_featureXml);
	}
	
	protected Element getElement(Element parent, String name)
	{
		NodeList list = parent.getElementsByTagName(name);
		return (list.getLength() > 0) ? (Element)list.item(0) : null;
	}
}
