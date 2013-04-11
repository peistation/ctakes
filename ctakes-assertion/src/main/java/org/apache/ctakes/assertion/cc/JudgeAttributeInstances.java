package org.apache.ctakes.assertion.cc;

/*
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.component.xwriter.XWriterFileNamer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;
import org.xml.sax.SAXException;

/**
 * 
 * A simple CAS consumer that generates XCAS (XML representation of the CAS) files in the
 * filesystem.
 * 
 * @author Philip Ogren
 */

public class JudgeAttributeInstances extends JCasConsumer_ImplBase {

	/**
	 * The parameter name for the configuration parameter that specifies the output directory
	 */
	public static final String PARAM_OUTPUT_DIRECTORY_NAME = ConfigurationParameterFactory
	.createConfigurationParameterName(JudgeAttributeInstances.class, "outputDirectoryName");
	@ConfigurationParameter(mandatory = true, description = "takes a path to directory into which output files will be written.")
	private String outputDirectoryName;

	/**
	 * The parameter name for the configuration parameter that provides the name of the XML scheme
	 * to use.
	 */
	public static final String PARAM_XML_SCHEME_NAME = ConfigurationParameterFactory
	.createConfigurationParameterName(JudgeAttributeInstances.class, "xmlSchemeName");
	@ConfigurationParameter(mandatory = true, defaultValue = "XMI", description = "specifies the UIMA XML serialization scheme that should be used. "
		+ "Valid values for this parameter are 'XMI' (default) and 'XCAS'.")
		private String xmlSchemeName;

	/**
	 * The parameter name for the configuration parameter that specifies the name of the class that
	 * implements the file namer
	 */
	public static final String PARAM_FILE_NAMER_CLASS_NAME = ConfigurationParameterFactory
	.createConfigurationParameterName(JudgeAttributeInstances.class, "fileNamerClassName");
	@ConfigurationParameter(mandatory = true, description = "the class name of the XWriterFileNamer implementation to use", defaultValue = "org.uimafit.component.xwriter.IntegerFileNamer")
	protected String fileNamerClassName;

	/**
	 * The name of the XMI XML scheme. This is a valid value for the parameter
	 * {@value #PARAM_XML_SCHEME_NAME}
	 */
	public static final String XMI = "XMI";

	/**
	 * The name of the XCAS XML scheme. This is a valid value for the parameter
	 * {@value #PARAM_XML_SCHEME_NAME}
	 */
	public static final String XCAS = "XCAS";

	protected static enum Selector {
		CONDITIONAL, GENERIC, HISTORYOF, POLARITY, SUBJECT, UNCERTAINTY;
	}
	
	private static final HashMap<Selector,String> msg = new HashMap<Selector,String>();
	static {
		msg.put(Selector.CONDITIONAL,"conditional");
		msg.put(Selector.GENERIC,"generic");
		msg.put(Selector.HISTORYOF,"historyOf");
		msg.put(Selector.POLARITY,"polarity");
		msg.put(Selector.SUBJECT,"subject");
		msg.put(Selector.UNCERTAINTY,"uncertainty");
	}
		

	private HashSet<EventMention> deletableMentions = new HashSet<EventMention>();
	
	private File outputDirectory;

	private boolean useXMI = true;

	private XWriterFileNamer fileNamer;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		outputDirectory = new File(outputDirectoryName);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		if (xmlSchemeName.equals(XMI)) {
			useXMI = true;
		}
		else if (xmlSchemeName.equals(XCAS)) {
			useXMI = false;
		}
		else {
			throw new ResourceInitializationException(String.format(
					"parameter '%1$s' must be either '%2$s' or '%3$s'.", PARAM_XML_SCHEME_NAME,
					XMI, XCAS), null);
		}

		fileNamer = InitializableFactory
		.create(context, fileNamerClassName, XWriterFileNamer.class);
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String fileName = fileNamer.nameFile(jcas);
		System.out.println("==================\nFile: "+fileName);
		judgeAttributes(jcas);

		removeExtraneousMentions(jcas);
		
		try {
			if (useXMI) {
				writeXmi(jcas.getCas(), fileName);
			}
			else {
				writeXCas(jcas.getCas(), fileName);
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (SAXException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void writeXCas(CAS aCas, String fileName) throws IOException, SAXException {
		File outFile = new File(outputDirectory, fileName + ".xcas");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			XCASSerializer ser = new XCASSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void writeXmi(CAS aCas, String id) throws IOException, SAXException {
		File outFile = new File(outputDirectory, id );
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(outFile);
			XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private void judgeAttributes(JCas jCas) {

		// TODO: expand this beyond just EventMentions
		Collection<EventMention> mentions = JCasUtil.select(jCas, EventMention.class);
		for (EventMention mention : mentions) {

			// only consider attributes for entities and events
			//		  if (mention.getClass()==EntityMention.class || mention.getClass()==EventMention.class) {
			boolean flag = false;
			String text = jCas.getDocumentText();

			boolean conditional = mention.getConditional();
			boolean generic = mention.getGeneric();
			int historyOf = mention.getHistoryOf();
			int polarity = mention.getPolarity();
			String subject = mention.getSubject();
			int uncertainty = mention.getUncertainty();

			if (conditional==true) {
				interact(jCas,mention,Selector.CONDITIONAL); // uses the attribute in mention
				flag = true;
			}
			if (generic==true) {
				interact(jCas,mention,Selector.GENERIC); // uses the attribute in mention
				flag = true;
			}
			if (historyOf==CONST.NE_HISTORY_OF_PRESENT) {
				interact(jCas,mention,Selector.HISTORYOF); // uses the attribute in mention
				flag = true;
			}
			if (polarity==CONST.NE_POLARITY_NEGATION_PRESENT) {
				System.out.println("Polarity="+polarity+": "+mention.getCoveredText());
				flag = true;
			}
			if (!CONST.ATTR_SUBJECT_PATIENT.equals(subject) && subject!=null) {
				interact(jCas,mention,Selector.SUBJECT); // uses the attribute in mention
				flag = true;
			}
			if (uncertainty==CONST.NE_UNCERTAINTY_PRESENT) {
				interact(jCas,mention,Selector.UNCERTAINTY); // uses the attribute in mention
				flag = true;
			}

			// optionally delete non-useful attributes 
			if (!flag) {
				if (mention!=null)
					deletableMentions.add(mention);
			}

		}
	}

	private void printContext(String text, EventMention mention, int radius) {
		int mentionBegin = mention.getBegin();
		int mentionEnd   = mention.getEnd();
		StringBuilder sb = new StringBuilder();
		
		int snipBegin;
		int snipEnd;
		snipBegin = (mentionBegin-radius<0)?             0               : mentionBegin-radius;
		snipEnd   = (mentionEnd+radius>text.length()-1)? text.length()-1 : mentionEnd+radius;

		String[] tmp = mention.getClass().getName().split("\\.");
		String semGroup = tmp[tmp.length-1];

		sb.append(text.substring(snipBegin, mentionBegin));
		sb.append("[[["+mention.getCoveredText()+"]]]");
		sb.append(text.substring(mentionEnd, snipEnd));
//		sb.toString().replaceAll("\\n", "\\n| ");
		System.out.println("| "+ sb.toString().replaceAll("\\n", "\n| "));
		
		System.out.println(": "+ semGroup + 
				" : c=" + mention.getConditional()  + " : g=" + mention.getGeneric() +
				" : h=" + mention.getHistoryOf() + " : p="  + mention.getPolarity() + 
				" : s=" + mention.getSubject() + " : u="  + mention.getUncertainty());
	}
	
	private void printContext(String text, EventMention mention) {
		printContext(text,mention,80);
	}
	
	static public String prompt (String attr) {

		//  prompt the user to enter their name
		System.out.print(attr+"? ");

		//  open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String response = "";

		//  read the username from the command-line; need to use try/catch with the
		//  readLine() method
		try {
			response = br.readLine();
			System.out.println();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your response!");
			System.exit(1);
		}

		//        System.out.println("Thanks for the response, " + response);
		return response;
	}


	private void interact(JCas jCas, EventMention mention,
			Selector attr) {
		
		printContext(jCas.getDocumentText(),mention);
		String response = prompt( "> "+msg.get(attr) + "=" + getAttrValueString(mention,attr));
		
		while (true) {
			if ("".equals(response) || response==null) {
				response = prompt( "umm... is this " + msg.get(attr) + "=" + getAttrValueString(mention,attr));
			} 
			else if (response.toLowerCase().startsWith("y")) {
				// yes response -- do nothing or put into alternate view?
				break;
			}
			else if (response.toLowerCase().startsWith("n") || response.toLowerCase().startsWith("s")) {
				// no response
				deletableMentions.add(mention);
				break;
			}
			else if (response.toLowerCase().startsWith("m")) {
				// more context response
				printContext(jCas.getDocumentText(),mention,160);
				response = prompt( msg.get(attr) + "=" + getAttrValueString(mention,attr));
			}
			else {
				response = prompt( "not sure what you meant. y=yes, n=no, m=more_context, s=skip.\\n"+
						msg.get(attr) + "=" + getAttrValueString(mention,attr));
			}
		}		
	}

	private String getAttrValueString(EventMention mention, Selector s) {
		switch (s) {
		case CONDITIONAL:
			return String.valueOf(mention.getConditional());
		case GENERIC:
			return String.valueOf(mention.getGeneric());
		case HISTORYOF:
			return String.valueOf(mention.getHistoryOf());
		case POLARITY:
			return String.valueOf(mention.getPolarity());
		case SUBJECT:
			return String.valueOf(mention.getSubject());
		case UNCERTAINTY:
			return String.valueOf(mention.getUncertainty());
		default:
			return "?";
		}
	}
	
	private void removeExtraneousMentions(JCas jcas) {
		// TODO: not operational yet
//		for (EventMention mention : deletableMentions) {
//			if (mention!=null) {
//				mention.removeFromIndexes(jcas);
//			}
//		}
	}
	
}