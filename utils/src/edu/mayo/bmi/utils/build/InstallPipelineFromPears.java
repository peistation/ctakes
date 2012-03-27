package edu.mayo.bmi.utils.build;

import java.io.File;
//package org.apache.uima.tools.pear.install;


/**
 * This class is used to install a pear file locally in a directory chosen by the 
 * user and then run the installed AE in CVD.
 * 
 */
public class InstallPipelineFromPears  {

    
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
		  "clinical documents pipeline", // install last as depends on others
   };
  		
  
   /**
   * 
   * @param args
   * 			<br>directory where the PEAR files are located
   *          	<br>directory where projects are to be installed
   */
   public static void main(String[] args) {

	   String pearFileLocation = getString(args, 0);
	   String installDir = getString(args, 1);
	   
	   if (pearFileLocation==null) {
		   System.err.println("Need to give pear file location.");
		   System.exit(-1);
	   }

	   if (installDir==null) {
		   System.err.println("Need to give pear file location AND the target directory.");
		   System.exit(-1);
	   }

	   System.out.println("Installing " + projects.length + " PEAR files to " + quote(installDir));
	   for (String proj : projects) {
		   
		   String pearFn = pearFileLocation + File.separator + proj + ".pear";
		   //		   System.out.println("Pear file name = " + quote(pearFn));
		   //		   System.out.println("  Install dir =    " + quote(installDir));

		   String [] installArgs = new String[2];
		   installArgs[0] = pearFn;
		   installArgs[1] = installDir;
		   InstallPearSilently.main(installArgs);

	   }
	   System.out.println("Done installing " + projects.length + " PEAR files to " + quote(installDir));
	  

  }

}
