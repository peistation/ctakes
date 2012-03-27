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
package clear.morph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;

import clear.pos.PosEnLib;
import clear.util.IOUtil;
import clear.util.tuple.JObjectObjectTuple;

/**
 * English morphological analyzer.
 * @author Jinho D. Choi
 * <b>Last update:</b> 2/15/2010
 */
public class MorphEnAnalyzer
{
	final private String FIELD_DELIM = "_";
	
	/** Noun exceptions */
	HashMap<String, String> m_noun_exc;
	/** Verb exceptions */
	HashMap<String, String> m_verb_exc;
	/** Adjective exceptions */
	HashMap<String, String> m_adj_exc;
	/** Adverb exceptions */
	HashMap<String, String> m_adv_exc;
	
	/** Noun base-forms */
	HashSet<String> s_noun_base;
	/** Verb base-forms */
	HashSet<String> s_verb_base;
	/** Adjective base-forms */
	HashSet<String> s_adj_base;
	/** Ordinal forms */
	HashSet<String> s_ordinal;
	
	/** Noun detachment rules */
	ArrayList<JObjectObjectTuple<String,String>> a_noun_rule;
	/** Verb detachment rules */
	ArrayList<JObjectObjectTuple<String,String>> a_verb_rule;
	/** Adjective detachment rules */
	ArrayList<JObjectObjectTuple<String,String>> a_adj_rule;
	/** Abbreviation replacement rules */
	HashMap<String, String> m_abbr_rule;
	
	/**
	 * Initializes the morphological analyzer.
	 * @param dataDir path of the directory containing dictionary data
	 */
	public MorphEnAnalyzer(String dataDir)
	{
		m_noun_exc  = getExcecptionMap(dataDir + File.separator + "noun.exc");
		m_verb_exc  = getExcecptionMap(dataDir + File.separator + "verb.exc");
		m_adj_exc   = getExcecptionMap(dataDir + File.separator + "adj.exc");
		m_adv_exc   = getExcecptionMap(dataDir + File.separator + "adv.exc");
		
		s_noun_base = IOUtil.getHashSet(dataDir + File.separator + "noun.txt");
		s_verb_base = IOUtil.getHashSet(dataDir + File.separator + "verb.txt");
		s_adj_base  = IOUtil.getHashSet(dataDir + File.separator + "adj.txt");
		s_ordinal   = IOUtil.getHashSet(dataDir + File.separator + "ordinal.txt");

		a_noun_rule = getRuleList(dataDir + File.separator + "noun.rule");
		a_verb_rule = getRuleList(dataDir + File.separator + "verb.rule");
		a_adj_rule  = getRuleList(dataDir + File.separator + "adj.rule");
		
		m_abbr_rule = getAbbreviationMap(dataDir + File.separator + "abbr.rule");
	}
	
	/**
	 * Returns a hashmap taking exceptions as keys and their base-forms as values.
	 * @param filename name of the WordNet exception file (e.g., noun.exc)
	 */
	private HashMap<String,String> getExcecptionMap(String filename)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		Scanner scan = IOUtil.createFileScanner(filename);
		
		while (scan.hasNextLine())
		{
			StringTokenizer tok  = new StringTokenizer(scan.nextLine());
			String          exc  = (tok.hasMoreTokens()) ? tok.nextToken() : null;
			String          base = (tok.hasMoreTokens()) ? tok.nextToken() : null;
			
			if (exc != null && base != null)
			{
				map.put(exc, base);
				while (tok.hasMoreTokens())	map.put(tok.nextToken(), base);
			}
		}
		
		return map;
	}
	
	/**
	 * Returns a hashmap taking (abbreviation and pos-tag) as the key and its base-form as the value.
	 * @param filename name of the file containing abbreviation rules 
	 */
	private HashMap<String,String> getAbbreviationMap(String filename)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		Scanner scan = IOUtil.createFileScanner(filename);
		
		while (scan.hasNextLine())
		{
			StringTokenizer tok = new StringTokenizer(scan.nextLine());
			String abbr = tok.nextToken();
			String pos  = tok.nextToken();
			String key  = abbr + FIELD_DELIM + pos;
			String base = tok.nextToken();
			
			map.put(key, base);
		}
		
		return map;
	}
	
	/**
	 * Returns a list of rules.
	 * @param filename name of the file containing rules.
	 */
	private ArrayList<JObjectObjectTuple<String,String>> getRuleList(String filename)
	{
		ArrayList<JObjectObjectTuple<String,String>> list = new ArrayList<JObjectObjectTuple<String,String>>();
		Scanner scan = IOUtil.createFileScanner(filename);
		
		while (scan.hasNextLine())
		{
			StringTokenizer tok = new StringTokenizer(scan.nextLine());
			String str0 = tok.nextToken();
			String str1 = (tok.hasMoreTokens()) ? tok.nextToken() : "";
			
			list.add(new JObjectObjectTuple<String,String>(str0, str1));
		}
		
		return list;
	}

	/**
	 * Returns the lemma of the form using the pos-tag.
	 * @param form word-form
	 * @param pos pos-tag
	 */
	public String getLemma(String form, String pos)
	{
		form = form.toLowerCase();
		
		// exceptions
		String morphem = getException(form, pos);
		if (morphem != null)	return morphem;
		
		// base-forms
		morphem = getBase(form, pos);
		if (morphem != null)	return morphem;
		
		// abbreviations
		morphem = getAbbreviation(form, pos);
		if (morphem != null)	return morphem;
		
		// numbers
		morphem = getNumber(form, pos);
		if (morphem != null)	return morphem;
	
		return form;
	}
	
	/**
	 * Returns the base form of the form considered to be an exception.
	 * If the form is not an exception, returns null.
	 * @param form word-form
	 * @param pos pos-tag
	 */
	private String getException(String form, String pos)
	{
		if      (PosEnLib.isNoun     (pos))	return m_noun_exc.get(form);
		else if (PosEnLib.isVerb     (pos))	return m_verb_exc.get(form);
		else if (PosEnLib.isAdjective(pos))	return m_adj_exc .get(form);
		else if (PosEnLib.isAdverb   (pos))	return m_adv_exc .get(form);
		
		return null;
	}
	/**
	 * Returns the base-form of the form.
	 * If there is no base-form, returns null.
	 * @param form word-form
	 * @param pos pos-tag
	 */
	private String getBase(String form, String pos)
	{
		if (PosEnLib.isNoun(pos))		return getBaseAux(form, s_noun_base, a_noun_rule);
		if (PosEnLib.isVerb(pos))		return getBaseAux(form, s_verb_base, a_verb_rule);
		if (PosEnLib.isAdjective(pos))	return getBaseAux(form, s_adj_base , a_adj_rule);
		
		return null;
	}
	
	/**
	 * Returns the base-form of the form.
	 * If there is no base-form, returns null.
	 * @param form word-form
	 * @param set set containing base-forms
	 * @param rule list containing detachment rules
	 */
	private String getBaseAux(String form, HashSet<String> set, ArrayList<JObjectObjectTuple<String,String>> rule)
	{
		for (JObjectObjectTuple<String,String> tup : rule)
		{
			if (form.endsWith(tup.key))
			{
				int  offset = form.length() - tup.key.length();
				String base = form.substring(0, offset) + tup.value;
				
				if (set.contains(base))	return base;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the base form of the form considered to be an abbreviation.
	 * If the form is not an abbreviation, returns null.
	 * @param form word-form
	 * @param pos pos-tag
	 */
	private String getAbbreviation(String form, String pos)
	{
		String key = form + FIELD_DELIM + pos;

		return m_abbr_rule.get(key);
	}
	
	/**
	 * Returns a simplified form of numbers.
	 * @param form word-form
	 * @param pos pos-tag
	 */
	private String getNumber(String form, String pos)
	{
		if (s_ordinal.contains(form))	return "$#ORD#$";
		
		String prevStr = "", currStr = form;
		
		while (!prevStr.equals(currStr))
		{
			prevStr = currStr;
			
			currStr = currStr.replaceAll("\\d%", "0");
			currStr = currStr.replaceAll("\\$\\d", "0");
			currStr = currStr.replaceAll("\\.\\d", "0");
			currStr = currStr.replaceAll(",\\d", "0");
			currStr = currStr.replaceAll(":\\d", "0");
		//	currStr = currStr.replaceAll("-\\d", "0");
		//	currStr = currStr.replaceAll("\\\\/\\d", "0");
		}
		
		currStr = currStr.replaceAll("\\d+", "0");
		
		if (currStr.equals("0st") || currStr.equals("0nd") || currStr.equals("0rd") || currStr.equals("0th"))
			return "$#ORD#$";
		
		return (currStr.equals(form)) ? null : currStr;	
	}
}
