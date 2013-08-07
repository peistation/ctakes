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
package edu.mayo.bmi.uima.termspotter.cr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import edu.mayo.bmi.uima.core.type.DocumentID;

/**
 * 
 * The original code was copied from org.apache.uima.examples.cpe.FileSystemCollectionReader
 * and modified for Mayo use.
 *
 * This collection reader facilitates reading "documents" from a single file.  Each
 * line in the document will be considered an entity to be analyzed by the CPE.  That
 * is each line will be treated as a "document" and will have its own CAS.
 * 
 * Extremely large files will require large memory resources as each line is read into
 * memory upon initialization.  This was done to simplify implementation.  
 * 
 * @author Mayo Clinic
 *
 */

public class RadiologyRecordsCollectionReader extends CollectionReader_ImplBase {

	/**
	 * This parameter will be used the descriptor file to specify the location of the
	 * file that will be run through this collection reader.
	 */
	public static final String PARAM_INPUT_FILE_NAME = "InputFileName";
	/**
	 * Optional parameter specifies a comment string.  Any line that begins with the string
	 * will be ignored and not be added as a "document" to the CPE. 
	 */
	public static final String PARAM_COMMENT_STRING = "CommentString";
	/**
	 * Optional parameter determines whether a blank line will be processed as a document or
	 * will be ignored.  The default will be set to 'true'.  
	 */
	public static final String PARAM_IGNORE_BLANK_LINES = "IgnoreBlankLines";
	
  /**
   * Name of optional configuration parameter that contains the language of
   * the documents in the input directory.  If specified this information will
   * be added to the CAS.
   */
    public static final String PARAM_LANGUAGE = "Language";
    
    /**
     * Name of optional configuration parameter that specifies a character (or string) that delimits
     * the id of the document from the text of the document.  For example, if the parameter is 
     * set to '|' then the following line from a file:
     * <code>1234|this is some text</code>
     * would have an id of 1234 and text <code>this is some text</code>.  
     * If this parameter is not set, then
     * the id of a document will be its line number in the file.      
     */

    public static final String PARAM_ID_DELIMETER = "IdDelimeter";
    
    /**
     * Number of columns contained in the radiology record.  Typically, all the columns will be skipped for 
     * actual annotation except the final column which contains the details of the examination.
     */
    
    public static final String PARAM_TOTAL_COLUMNS = "ColumnCount";
    
    /**
     * Specifies the file which contains the valid types of exams which will be processed by the pipeline.
     */
    
    public static final String PARAM_FILTER_EXAMS = "FilterExamTypes";
    
    /**
     * The column number of the input file that contains the compare string to parse applicable exam types.
     */
    
    public static final String PARAM_EXAM_COLUMN = "FilterExamColumnNumber";
    
	List iv_linesFromFile;
	int iv_currentIndex = 0;
	String iv_language; 
	String iv_delimeter;
	Integer numberOfColumns;
	String filterExamTypes;
	Integer filterColunmNumber;
	
	private Logger iv_logger = Logger.getLogger(getClass().getName());

	public void initialize() throws ResourceInitializationException
	{
		BufferedReader fileReader = null;
		try
		{
			numberOfColumns = (Integer) getConfigParameterValue(PARAM_TOTAL_COLUMNS);
			String fileLocation = (String) getConfigParameterValue(PARAM_INPUT_FILE_NAME);
			String commentSeq = (String)getConfigParameterValue(PARAM_COMMENT_STRING);
			iv_language = (String)getConfigParameterValue(PARAM_LANGUAGE);
			Boolean paramValue = (Boolean)getConfigParameterValue(PARAM_IGNORE_BLANK_LINES);
			filterExamTypes = (String) getConfigParameterValue(PARAM_FILTER_EXAMS);
			filterColunmNumber = (Integer) getConfigParameterValue(PARAM_EXAM_COLUMN);
			boolean ignoreBlankLines = true;
			if(paramValue != null) 
			{
				ignoreBlankLines = paramValue.booleanValue();
			}
			iv_delimeter =  (String)getConfigParameterValue(PARAM_ID_DELIMETER);
				
			iv_linesFromFile = new ArrayList();
			fileReader = new BufferedReader(new FileReader(fileLocation));

			
			String line;
	
			while((line = fileReader.readLine()) != null)
			{
				if(commentSeq != null)
				{
					if(line.startsWith(commentSeq)) continue;
				}
				if(ignoreBlankLines && line.trim().length() == 0) continue;

				iv_linesFromFile.add(line);
			}
		}
		catch(IOException fnfe)
		{
			throw new ResourceInitializationException(fnfe);
		}
		finally
		{
			if(fileReader != null)
			try { fileReader.close(); } catch(IOException ioe) {}
		}
	}
	
	public void getNext(CAS cas) throws IOException, CollectionException 
	{
	  	JCas jcas;
	  	try
	    {
	  		jcas = cas.getJCas();
	  	    boolean foundLine = false;
		  	String line = (String) iv_linesFromFile.get(iv_currentIndex);
		  	int lineNumber = iv_currentIndex + 1;
		  	String id;
		  	String text, filterLine;
			BufferedReader filterExamTypesFile = null;
			if (filterExamTypes != null && filterExamTypes != "")
				filterExamTypesFile = new BufferedReader(new FileReader(filterExamTypes));
		  	if(iv_delimeter != null)
			{
		  		String [] delimitedColumns = line.split(iv_delimeter);
				if (filterExamTypesFile != null) {
					filterExamTypesFile.readLine(); //skip first line, since a header column is expected.
					while ((filterLine = filterExamTypesFile.readLine()) != null  && !foundLine) {
						String examToCheck = filterLine.substring(0, filterLine.indexOf(','));
						if (delimitedColumns[filterColunmNumber].equals(examToCheck))
							foundLine=true;
					}
				}
				
				int delimeterLoc = line.indexOf(iv_delimeter);
				if(delimeterLoc <= 0)
					throw new CollectionException(new Exception("Line in file number "+lineNumber+" is not well formatted.  " +
							"\nIt should have the format:" +
							"\n<doc_id>"+iv_delimeter+"<doc_text>"));
				id = line.substring(0,delimeterLoc);
				if (foundLine) {
					if (numberOfColumns.intValue() > 0 && iv_delimeter != null
							&& iv_delimeter != "") {

						text = delimitedColumns[numberOfColumns.intValue() - 1];
					} else
						text = line.substring(delimeterLoc
								+ iv_delimeter.length());
				} else {
			  		id = line.substring(0,delimeterLoc); 
			  		text = "";
				}
			}
		  	else
		  	{
		  		id = Integer.toString(lineNumber); //id will one more than its index into iv_linesFromFile (iv_currentIndex has already been incremented)
		  		text = line;
		  	}
	  		

		  	iv_logger.debug("id="+id);
		  	iv_logger.debug("text="+text);
		  	
			//if there's a CAS Initializer, call it	
			if (getCasInitializer() != null)
			{
				Reader reader = new StringReader(text);
				getCasInitializer().initializeCas(reader, cas);
				reader.close();
			}
			else  //No CAS Initiliazer, so read file and set document text ourselves
			{				
				jcas.setDocumentText(text);
			}
		   
		    //set language if it was explicitly specified as a configuration parameter
		    if (iv_language != null)
		    {
		      ((DocumentAnnotation)jcas.getDocumentAnnotationFs()).setLanguage(iv_language);
		    }
		    
		    
		    DocumentID documentIDAnnotation = new DocumentID(jcas);
		    documentIDAnnotation.setDocumentID(id);
		    documentIDAnnotation.addToIndexes();

	    } 
	  	catch (ArrayIndexOutOfBoundsException aioobe) {
	  		iv_logger.log(Level.ERROR, "Radiology input file corrupt or some record row contents are not valid.");
	  		throw new CollectionException(aioobe);
	  	}
	    catch (CASException e)
	    {
	      throw new CollectionException(e);
	    }
	    finally
	    {
	    	iv_currentIndex++;
	    }
	    
	}

	public boolean hasNext() throws IOException, CollectionException 
	{
		return iv_currentIndex < iv_linesFromFile.size();
	}

	public Progress[] getProgress() {
	    return new Progress[]{
	    	       new ProgressImpl(iv_currentIndex, iv_linesFromFile.size(),Progress.ENTITIES)};
	}

	 /**
	   * Gets the total number of documents that will be returned by this
	   * collection reader.  
	   * @return the number of documents in the collection
	   */
	  public int getNumberOfDocuments()
	  {
	    return iv_linesFromFile.size();
	  }
	  
	public void close() throws IOException {}
}
