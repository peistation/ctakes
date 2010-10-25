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
package edu.mayo.bmi.dictionarytools;

/**
 * See http://www.onjava.com/pub/a/onjava/2003/01/15/lucene.html?page=1
 */
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
public class CreateLuceneIndexForExampleDrugs {

	/**
	 * Create a Lucene index containing some sample drug names
	 * The field names need to include the values of the fieldName attribute
	 * of the metaField elements and the lookupField element in the 
	 * LookupDescriptorFile for this dictionary.
	 * For example, if the LookupDescriptorFile being used is LookupDesc.xml
	 * and if it defines the following four elements for this dictionary:<br>
	 *   lookupField fieldName="first_word"<br>
	 *   metaField fieldName="code"<br>
	 * 	 metaField fieldName="preferred_designation"<br>
	 * 	 metaField fieldName="other_designation"<br>
	 * then the lucene index needs to include four fields with those names.
	 * 
	 * @param args unused/ignored
	 */
	public static void main(String args[]) throws Exception {

		// Name of the lucene index directory to be created 
		String indexDir = "C:/temp/lucene/" + "drug-index";
		Analyzer analyzer = new StandardAnalyzer();
		boolean createFlag = true;

		IndexWriter writer = new IndexWriter(indexDir, analyzer, createFlag);

		String [][] strings = {
				// rowID      code        (ignored)    first_word       preferred_designation
				{"5555555",  "C5555555",  "C5555555", "Acetaminophen", "Acetaminophen 80 mg chewable"}, 
				{"6666666",  "C6666666",  "C6666666", "Aspirin",       "Aspirin"}, 
				{"7777777",  "C7777777",  "C7777777", "Ibuprofen",     "Ibuprofen"}, 
				{"8888888",  "C8888888",  "C8888888", "Ibuprofen",     "Ibuprofen 200 mg"}, 
				{"99999999", "C99999999", "C99999999", "Ibuprofen",    "Ibuprofen 300 mg"}, 
				};
		
		int tcount = 0; 
		for (String [] t : strings) {
			int i=0;
			System.out.println("t = " + t);
			for (String s: t){
				System.out.println("s= " + s);			
			}
			Document document = new Document();
			document.add(Field.Keyword("UNIQUE_DOCUMENT_IDENTIFIER_FIELD", t[i])); i++;
			document.add(Field.Keyword("code", t[i])); i++;
			document.add(Field.Text("codeTokenized", t[i])); i++;
			document.add(Field.Text("first_word", t[i])); i++;
			document.add(Field.Text("preferred_designation", t[i])); i++;
			tcount++;
			writer.addDocument(document);
		}
		writer.close();
		System.out.println("Wrote lucene index: " + writer);
	}
	
}



