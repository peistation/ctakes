package edu.mayo.bmi.utils.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.uima.pear.tools.PackageCreator;
import org.apache.uima.pear.tools.PackageCreatorException;

/**
 * Build a PEAR file for this project<br>
 * Looks for BuildPear.properties file for  <code>classpath</code> and
 * <code>datapath</code> values to use for PEAR.<br>
 * Expects the properties file to be in the same directory
 * as this class file.
 * and expects this class file to be in a directory 'bin'
 */
public class BuildPear {
	
	/**
	 * @param args optional - project name (directory name) of project to edu.mayo.bmi.utils.build, 
	 * and in which to look for a BuildPear.properties file
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		
		// Assume project name is componentId, unless passed in args
		// So get the name and path of the current working directory
	    File cwd = new File(".");
	    String componentRootDir = ""; 
		String componentId = "";
		File cwdCanonical = null;
	    try {
	    	cwdCanonical = cwd.getCanonicalFile();
	    	componentId = cwdCanonical.getName(); // default if not passed as an arg
	    	
	    } catch (IOException ioe){
			System.err.println();
			System.err.println("Error accessing current directory.");
			ioe.printStackTrace(System.err);
	    }


	    // default if not passed is current project
		if (args.length == 1) {
			componentId = args[0];
		}
		
		String parent = cwdCanonical.getParent();	
		if (parent == null) {
			System.err.println("Error accessing current directory " + cwdCanonical);
		}

		
		System.out.println("Building PEAR for " + componentId + " using");
		
		// Set classpath and datapath for PEAR, and location of AE descriptor.
		// Look for properties file, else default
		FileInputStream fis;
		String propsFn = "/"+BuildPear.class.getPackage().getName().replace('.','/')+"/BuildPear."+componentId+".properties";
		try { propsFn = BuildPear.class.getResource(propsFn).toURI().getPath(); }
		catch (Exception e) { e.printStackTrace(); }
		
		// Look for BuildPear.properties file within the given project (componentId)
		File componentRoot = new File(parent, componentId);
		componentRootDir = parent + File.separator + componentId;
//		File p = new File(parent + File.separator + componentId + File.separator + "bin" + File.separator + packagePath + File.separator + propsFn);
		File p = new File(propsFn);
		System.out.println(p.getAbsolutePath());

		Properties props = new Properties();
		// set defaults in case file not found or properties file does not contain value
		// Nearly all project rely on core, so add it to the classpath
		final String classpathDefault = "$main_root/bin;$main_root/../core/bin;";
		final String datapathDefault = "$main_root/resources;";
		final String aeDescriptorDefault = "desc/AE.xml";
		// Set default values in case properties file not found
		String classpath = classpathDefault;
		String datapath = datapathDefault;
		String aeDescriptor = aeDescriptorDefault;

		try {
			fis = new FileInputStream(p);
			props.load(fis);
			classpath = props.getProperty("classpath", classpathDefault);
			datapath = props.getProperty("datapath", datapathDefault);
			aeDescriptor = props.getProperty("aeDescriptor", aeDescriptorDefault);
			componentId = props.getProperty("project");
			
		} catch (IOException ioe) {
			// Ignore. Use defaults
			System.out.println();
			System.out.println(quote(propsFn) + " not found, using defaults for " + componentId);
		}
		
		// Verify AE descriptor exists 
	    if (!(new File(componentRoot, aeDescriptor).exists())) {
	    	System.err.println();
	    	System.err.println("Unable to create or access " + aeDescriptor);
	    	System.exit(-1);
	    }


	    

	    // Create the PEAR file into %TEMP%/BuildPear/ or $TEMP/BuildPear/
	    // Assumes env var TEMP exists, and that we can create BuildPear into
	    // it if BuildPear does not exist.
	    String targetDir;
	    String tmpDir = System.getProperty("java.io.tmpdir");
	    targetDir = tmpDir.endsWith(File.separator) ? tmpDir+"BuildPear" : tmpDir+File.separator+"BuildPear";
//	    if (new File("/temp").exists()) {
//	    	targetDir = "/temp/BuildPear"; // Windows
//	    } else {
//	    	targetDir = "/tmp/BuildPear"; // *nix
//	    }
	    if (!createDirIfNotExist(targetDir)) {
	    	System.err.println();
	    	System.err.println("Unable to create or access " + targetDir);
	    	System.exit(-1);
	    }

	    
	    // We are ready to actually build the PEAR file
	    System.out.println("Building PEAR file for " + quote(componentId));
	    System.out.println("  from " + quote(componentRootDir));
	    System.out.println("  setting PEAR classpath =  " + quote(classpath));
	    System.out.println("  setting PEAR datapath =  " + quote(datapath));
	    System.out.println("  setting PEAR AE descriptor =  " + quote(aeDescriptor));

		try {
			PackageCreator.generatePearPackage(componentId, aeDescriptor, classpath, datapath, 
			   componentRootDir, targetDir, null);
		} catch (PackageCreatorException pce) {
			System.err.println();
			System.err.println("Error trying to package " + componentId);
			pce.printStackTrace(System.err);
		}
		
		System.out.println("BuildPear completed.");

	}

	/**
	 * Quote the string, using double quotes if the string contains
	 * any single quotes, otherwise using single quotes. 
	 */
	  public static String quote(String s) {
		  if (s==null) return "[null]";
		  char DBLQUOTES = '"';
		  String QUOTE = "'";
		  if (s.contains(QUOTE)) {
			  return DBLQUOTES + s + DBLQUOTES;
		  }
		  
		  return QUOTE + s + QUOTE;
	  }

	  /**
	   * @return true if the directory exists or if directory was 
	   * successfully created, false others
	   */
	  public static boolean createDirIfNotExist(String path) {
		  try {
			  File f = new File(path);
			  if (f.exists() && f.isDirectory() && f.canWrite()) {
				  return true;
			  }
			  if (f.mkdir()) {
				  return true;
			  }
			  System.err.println("Unable to create " + path);
			  return false;
		  } catch (SecurityException se) {
			  System.err.println("Security problem - unable to create " + path);
			  return false;
		  }
	  }
}
