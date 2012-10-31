/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.uima.core.util;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;

import edu.mayo.bmi.uima.core.type.structured.DocumentID;

public class DocumentIDAnnotationUtil 
{
	public static String getDocumentID(JCas jcas)
	{
		try
		{
		 	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		 	FSIterator<TOP> documentIDIterator = indexes.getAllIndexedFS(DocumentID.type);
		 	DocumentID documentIDAnnotation = (DocumentID) documentIDIterator.next();
		 	String documentID = documentIDAnnotation.getDocumentID();
		 	return documentID;
		}
		catch(Exception e) { return null;}
	}

}
