/**
* Copyright (c) 2010, Regents of the University of Colorado
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
package clear.dep.ftr;

import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jinho D. Choi
 * <b>Last update:</b> 7/7/2010
 */
public class DepFtrXml
{
	/** XML feature template tags */
	static public final String XML_TEMPLATE    = "feature_template";
	static public final String XML_UNIGRAM     = "unigram";
	static public final String XML_BIGRAM      = "bigram";
	static public final String XML_TRIGRAM     = "trigram";
	static public final String XML_FIELD       = "field";
	static public final String XML_TOKEN       = "token";
	static public final String XML_TOKEN_HD    = "hd";
	static public final String XML_TOKEN_LM    = "lm";
	static public final String XML_TOKEN_RM    = "rm";
	static public final String XML_VISIBLE     = "visible";
	static public final String XML_COMMENT     = "comment";
	static public final String XML_FORM        = "f";
	static public final String XML_LEMMA       = "m";
	static public final String XML_POS         = "p";
	static public final String XML_DEPREL      = "d";
	static public final String XML_POS_POS     = "pp";
	static public final String XML_POS_LEMMA   = "pm";
	static public final String XML_LEMMA_POS   = "mp";
	static public final String XML_LEMMA_LEMMA = "mm";
	static public final String XML_POS_POS_POS = "ppp";
	static public final char   XML_LAMBDA      = 'l';
	static public final char   XML_BETA        = 'b';
	static public final String XML_DELIM_REL   = "_";
	
	public ArrayList<DepFtrToken[]> form_1gram;
	public ArrayList<DepFtrToken[]> lemma_1gram;
	public ArrayList<DepFtrToken[]> pos_1gram;
	public ArrayList<DepFtrToken[]> deprel_1gram;
	public ArrayList<DepFtrToken[]> pos_lemma_1gram;
	public ArrayList<DepFtrToken[]> pos_pos_2gram;
	public ArrayList<DepFtrToken[]> pos_lemma_2gram;
	public ArrayList<DepFtrToken[]> lemma_pos_2gram;
	public ArrayList<DepFtrToken[]> lemma_lemma_2gram;
	public ArrayList<DepFtrToken[]> pos_pos_pos_3gram;
	
	public DepFtrXml(String featureXml)
	{
		init(featureXml);
	}
	
	public void init(String featureXml)
	{
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		form_1gram        = new ArrayList<DepFtrToken[]>();
		lemma_1gram       = new ArrayList<DepFtrToken[]>();
		pos_1gram         = new ArrayList<DepFtrToken[]>();
		deprel_1gram      = new ArrayList<DepFtrToken[]>();
		pos_lemma_1gram   = new ArrayList<DepFtrToken[]>();
		pos_pos_2gram     = new ArrayList<DepFtrToken[]>();
		pos_lemma_2gram   = new ArrayList<DepFtrToken[]>();
		lemma_pos_2gram   = new ArrayList<DepFtrToken[]>();
		lemma_lemma_2gram = new ArrayList<DepFtrToken[]>();
		pos_pos_pos_3gram = new ArrayList<DepFtrToken[]>();
		
		try
		{
			DocumentBuilder builder  = dFactory.newDocumentBuilder();
			Document        doc      = builder.parse(new FileInputStream(featureXml));
			NodeList        unigram  = doc.getElementsByTagName(XML_UNIGRAM);
			NodeList        bigram   = doc.getElementsByTagName(XML_BIGRAM);
			NodeList        trigram  = doc.getElementsByTagName(XML_TRIGRAM);
			
			initNgram(unigram, 1);
			initNgram(bigram , 2);
			initNgram(trigram, 3);
		}
		catch (Exception e){e.printStackTrace();System.exit(1);}
	}
	
	private void initNgram(NodeList lsNgram, int maxToken) throws Exception
	{
		for (int i=0; i<lsNgram.getLength(); i++)
		{
			Element eFeature = (Element)lsNgram.item(i);
			String  visible  = eFeature.getAttribute(XML_VISIBLE).trim();
			if (visible.equals("false"))	continue;
			
			DepFtrToken[] tokens = new DepFtrToken[maxToken];
			
			for (int n=0; n<maxToken; n++)
			{
				String token = eFeature.getAttribute(XML_TOKEN + n).trim();
				tokens[n] = initTokenTemplate(token);
			}
			
			String field = eFeature.getAttribute(XML_FIELD).trim();
			
			if      (maxToken == 1)		// unigram
			{
				if      (field.equals(XML_FORM))		form_1gram     .add(tokens);
				else if (field.equals(XML_LEMMA))		lemma_1gram    .add(tokens);
				else if (field.equals(XML_POS))			pos_1gram      .add(tokens);
				else if (field.equals(XML_DEPREL))		deprel_1gram   .add(tokens);
				else if (field.equals(XML_POS_LEMMA))	pos_lemma_1gram.add(tokens);
				else									xmlError(XML_FIELD, field);
			}
			else if (maxToken == 2)		// bigram
			{
				if      (field.equals(XML_POS_POS))		pos_pos_2gram    .add(tokens);
				else if (field.equals(XML_POS_LEMMA))	pos_lemma_2gram  .add(tokens);
				else if (field.equals(XML_LEMMA_POS))	lemma_pos_2gram  .add(tokens);
				else if (field.equals(XML_LEMMA_LEMMA))	lemma_lemma_2gram.add(tokens);
				else									xmlError(XML_FIELD, field);
			}
			else if (maxToken == 3)		// trigram
			{
				if      (field.equals(XML_POS_POS_POS))	pos_pos_pos_3gram.add(tokens);
				else									xmlError(XML_FIELD, field);
			}
		}
	}
		
	/** @param token l, l+1, l-1, l0_hd */
	private DepFtrToken initTokenTemplate(String token) throws Exception
	{
		String[] aToken = token.split(XML_DELIM_REL);
		
		char type = aToken[0].charAt(0);
		if (type != XML_LAMBDA && type != XML_BETA)		xmlError(XML_TOKEN, token);
		
		int offset = 0;
		if (aToken[0].length() >= 2)
		{
			if (aToken[0].charAt(1) == '+')	offset = Integer.parseInt(aToken[0].substring(2)); 
			else							offset = Integer.parseInt(aToken[0].substring(1));
		}
		
		if (aToken.length > 1)
		{
			if (!aToken[1].equals(XML_TOKEN_HD) && !aToken[1].equals(XML_TOKEN_LM) && !aToken[1].equals(XML_TOKEN_RM))
				xmlError(XML_TOKEN, token);

			return new DepFtrToken(type, offset, aToken[1]);
		}
		else
			return new DepFtrToken(type, offset, null);
	}

	/** Prints system error and exits. */
	public void xmlError(String attr, String error)
	{
		System.err.println("Invalid <"+attr+">: "+error);
		System.exit(1);
	}
}






