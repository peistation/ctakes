/**
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
package org.apache.ctakes.utils.build;

/**
 * This class is used to edu.mayo.bmi.utils.build the PEAR files for the pipeline.
 * 
 */
public class BuildPipelinePears  {

    
  // handles if a is null or if any element is null or if <code>i</code> is out of range
  // by returning null in any of those cases
  public static String getString(String [] a, int i) {
	  try {
		return a[i];
	  }
	  catch (Exception e)
	  {
		System.err.println();
		System.err.println("Error accessing argument " + i);
		System.err.println("Number of arguments received = " + ( a==null ? -1 : a.length));
		e.printStackTrace(System.err);
		return null;
	  }
  }
  
  public static String quote(String s) {
	  if (s==null) return "[null]";
	  char DBLQUOTES = '"';
	  String QUOTE = "'";
	  if (s.contains(QUOTE)) {
		  return DBLQUOTES + s + DBLQUOTES;
	  }
	  
	  return QUOTE + s + QUOTE;
  }
  
   private static String projects [] = {
		  "core", // install core first as other depend on it
		  "document preprocessor",
		  "POS tagger",
		  "chunker",
		  "context dependent tokenizer",
		  "dictionary lookup",
		  "LVG",
		  "NE contexts",
		  "ctakes-clinical-pipeline", // install after all above ones, as depends on others
		  "Drug NER",
		  "PAD term spotter",
		  "SideEffect",
		  "smoking status",
		  "dependency parser",
		  "Constituency Parser",
		  "coref-resolver",
   };
  		
  
   /**
   * 
   * @param args
   * 			<br>directory where the PEAR files are located
   *          	<br>directory where projects are to be installed
   */
   public static void main(String[] args) {

		//	   String pearFileLocation = getString(args, 0);
		//	   String installDir = getString(args, 1);
		//	   
		//	   if (pearFileLocation==null) {
		//		   System.err.println("Need to give pear file location.");
		//		   System.exit(-1);
		//	   }
		//
		//	   if (installDir==null) {
		//		   System.err.println("Need to give pear file location AND the target directory.");
		//		   System.exit(-1);
		//	   }

	   for (String proj : projects) {

		   String [] buildArgs = new String[1];
		   buildArgs[0] = proj;
		   System.out.println("Processing " + proj);
		   BuildPear.main(buildArgs);

	   }
	   System.out.println("Done building " + projects.length + " PEAR files.");

  }

}
