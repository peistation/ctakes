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
package edu.mayo.bmi.nlp.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Mayo Clinic
 */
public class DocumentMetaData
{	
	private String iv_text;
	
	private List iv_scAnnotationList = new ArrayList();
	
	// key = unique key, value = document meta data
	private Map iv_docMetaDataMap = new HashMap();
	
	// key = segment ID, value = SegmentMetaData object
	private Map<String, SegmentMetaData> iv_segMetaDataHash = new HashMap<String, SegmentMetaData>();

	/**
	 * Adds a meta data entry for the document.
	 * @param key
	 * @param value
	 */
	public void addMetaData(Object key, Object value)
	{
		iv_docMetaDataMap.put(key, value);
	}

	/**
	 * Gets a map of meta data about the document.
	 * @return
	 */
	public Map getMetaData()
	{
		return iv_docMetaDataMap;
	}

	/**
	 * Adds a segment.
	 * @param smd
	 */
	public void addSegment(SegmentMetaData smd)
	{
		iv_segMetaDataHash.put(smd.id, smd);
	}

	/**
	 * Gets a set of segment identifiers.  Each identifier is a String object.
	 * @return Set of String objects, each String is a segment identifier.
	 */
	public Set<String> getSegmentIdentifiers()
	{
		return (Set<String>)iv_segMetaDataHash.keySet();
	}

	/**
	 * Gets meta data about the specified segment.
	 * @param segmentID Identifier of segment.
	 * @return SegmentMetaData object that contains meta data about the
	 * 	       specified segment.
	 */
	public SegmentMetaData getSegment(String segmentID)
	{
		return (SegmentMetaData) iv_segMetaDataHash.get(segmentID);
	}
	
	/**
	 * Adds a single annotation to the syntactic cue annotation list.
	 * @param a
	 */
	public void addAnnotation(Annotation a)
	{
		iv_scAnnotationList.add(a);
	}
	
	/**
	 * Adds a list of annotations to the syntactic cue annotation list.
	 * @param aList
	 */
	public void addAnnotations(List aList)
	{
		iv_scAnnotationList.addAll(aList);
	}

	/**
	 * Gets a list of Annotation objects that represent annotations based 
	 * off of document syntactic cues.
	 * @return List of Annotation objects.
	 */
	public List getAnnotations()
	{
		return iv_scAnnotationList;
	}

    /**
     * @return
     */
    public String getText()
    {
        return iv_text;
    }

    /**
     * @param string
     */
    public void setText(String string)
    {
        iv_text = string;
    }

}
