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
/*
 * This class was based on parts of 
 * @see <code>org.apache.uima.tools.pear.install.InstallPear</code>
 */
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

package org.apache.ctakes.utils.build;
//package org.apache.uima.tools.pear.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.jar.JarFile;


import org.apache.uima.pear.tools.InstallationController;
import org.apache.uima.pear.tools.InstallationDescriptor;
import org.apache.uima.pear.tools.InstallationDescriptorHandler;
import org.apache.uima.pear.tools.PackageBrowser;
import org.apache.uima.pear.util.MessageRouter;
import org.apache.uima.tools.cvd.CVD;
import org.apache.uima.tools.cvd.MainFrame;

/**
 * This class is used to install a pear file locally in a directory chosen by the 
 * user and then run the installed AE in CVD.
 * 
 * This class was based on parts of <code>org.apache.uima.tools.pear.install.InstallPear</code>
 * @see <code>org.apache.uima.tools.pear.install.InstallPear</code>
 */
public class InstallPearSilently {

  /**
   * The <code>RunInstallation</code> class implements a thread that is used to run the
   * installation.
   */
  protected static class RunInstallation implements Runnable {
    private File pearFile;

    private File installationDir = null;

    /**
     * Constructor that sets a given input PEAR file and a given installation directory.
     * 
     * @param pearFile
     *          The given PEAR file.
     * @param installationDir
     *          The given installation directory.
     */
    public RunInstallation(File pearFile, File installationDir) {
      this.pearFile = pearFile;
      this.installationDir = installationDir;
    }

    /**
     * Runs the PEAR installation process. Notifies waiting threads upon completion.
     */
    public void run() {
      installPear(pearFile, installationDir);
      synchronized(this) {
    	  this.notifyAll();
      }
    }

  }


  private static File localTearFile = null;

  private static File installationDir = null;

  private static String mainComponentId;

  private static InstallationDescriptor insdObject;

  private static String mainComponentRootPath;

  private static String message = null;

  private static boolean errorFlag = false;


  private static final String SET_ENV_FILE = "metadata/setenv.txt";

 

  /**
   * Method that installs the given PEAR file to the given installation directory.
   * 
   * @param localPearFile
   *          The given PEAR file path.
   * @param installationDir
   *          The given installation directory.
   */
  private static void installPear(File localPearFile, File installationDir) {
	//System.out.println("installPear(\"" + localPearFile + "\", \"" + installationDir + "\")");
    InstallationController.setLocalMode(true);
    InstallationDescriptorHandler installationDescriptorHandler = new InstallationDescriptorHandler();
    printInConsole(false, "");
    // check input parameters
    if (localPearFile != null && !localPearFile.exists()) {
      errorFlag = true;
      message = localPearFile.getAbsolutePath() + "file not found \n";
      printInConsole(errorFlag, message);
    } else {
      if(localPearFile != null) {
    	  printInConsole(false, "PEAR file to install is => " + localPearFile.getAbsolutePath() + "\n");
      }
    }
    /* setting current working directory by default */
    if (installationDir == null) {
      installationDir = new File("./");
    }
    printInConsole(false,"Installation directory is => " + installationDir.getAbsolutePath() + "\n");

    try {
      JarFile jarFile = new JarFile(localPearFile);
      installationDescriptorHandler.parseInstallationDescriptor(jarFile);
      insdObject = installationDescriptorHandler.getInstallationDescriptor();

      if (insdObject != null)
        mainComponentId = insdObject.getMainComponentId();

      else {
        throw new FileNotFoundException("installation descriptor not found \n");
      }
      // this version does not support separate delegate components
      if (insdObject.getDelegateComponents().size() > 0) {
        throw new RuntimeException("separate delegate components not supported \n");
      }
    } catch (Exception err) {
      errorFlag = true;
      message = " terminated \n" + err.toString();
      printInConsole(errorFlag, message);
      return;
    }
    InstallationController installationController = new InstallationController(mainComponentId,
            localPearFile, installationDir);
    // adding installation controller message listener
    installationController.addMsgListener(new MessageRouter.StdChannelListener() {
      public void errMsgPosted(String errMsg) {
        printInConsole(true, errMsg);
      }

      public void outMsgPosted(String outMsg) {
        printInConsole(false, outMsg);
      }
    });
    
    insdObject = installationController.installComponent();
    if (insdObject == null) {
      /* installation failed */
      errorFlag = true;
      message = " \nInstallation of " + mainComponentId + " failed => \n "
              + installationController.getInstallationMsg();
      printInConsole(errorFlag, message);

    } else {
      try {

        /* save modified installation descriptor file */
        installationController.saveInstallationDescriptorFile();
        mainComponentRootPath = insdObject.getMainComponentRoot();
        errorFlag = false;
        message = " \nInstallation of " + mainComponentId + " completed \n";
        printInConsole(errorFlag, message);
        message = "The " + mainComponentRootPath + "/" + SET_ENV_FILE
                + " \n    file contains required " + "environment variables for this component\n";
        printInConsole(errorFlag, message);
        /* 2nd step: verification of main component installation */
        if (installationController.verifyComponent()) {
          errorFlag = false;
          message = "Verification of " + mainComponentId + " completed \n";
          printInConsole(errorFlag, message);
        } else {
          errorFlag = true;
          message = "Verification of " + mainComponentId + " failed => \n "
                  + installationController.getVerificationMsg();
          printInConsole(errorFlag, message);
        }
      } catch (Exception exc) {
        errorFlag = true;
        message = "Error in InstallationController.main(): " + exc.toString();
        printInConsole(errorFlag, message);
      } finally {
        installationController.terminate();
      }
    }
  }

  /**
   * This method runs the installed AE in CVD (Gladis).
   * 
   * @throws IOException
   *           If any I/O exception occurred.
   */
  private static void runCVD() {
    try {

      // create PackageBrowser object
      PackageBrowser pkgBrowser = new PackageBrowser(new File(mainComponentRootPath));

      // get pear descriptor
      String pearDesc = pkgBrowser.getComponentPearDescPath();

      // start CVD
      MainFrame frame = CVD.createMainFrame();
      
      // Prevent CVD from shutting down JVM after exit
      frame.setExitOnClose(false);

      // load pear descriptor
      frame.loadAEDescriptor(new File(pearDesc));

      // run CVD
      frame.runAE(true);

    } catch (Throwable e) {
    	printInConsole(true, " Error in runCVD() " + e.toString());
		StringWriter strWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(strWriter, true);
		e.printStackTrace(printWriter);
		printWriter.flush();
		strWriter.flush();
		printInConsole(true, strWriter.toString());
    }
  }



  /**
   * Prints messages and set foreground color in the console according to a given errorFlag.
   * 
   * @param errorFlag
   *          The given error flag.
   * @param message
   *          The given message to print.
   */
  private static void printInConsole(boolean errorFlag, String message) {
    if (errorFlag) {
    	System.err.println(message);
    } else {
    	System.out.println(message);
    }
  }


  
  // handles if a is null or if any element is null or if <code>i</code> is out of range
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
  
  /**
   * Starts the application.
   * 
   * @param args
   *          optional runCVD boolean, default = false
   */
  public static void main(String[] args) {
	  
	  	//System.out.println(quote("The Doctor's car")); // unit test 
	  	//System.out.println(quote("The Doctor said, \"hi!\"")); // unit test
	  	
	  	
		String pearFn = getString(args, 0);
  		String installDir = getString(args, 1);
		//  System.out.println(" Pear file name = " + quote(pearFn));
		//  System.out.println("   Install dir =    " + quote(installDir));
  		
  		if (pearFn== null || installDir == null) {
  			System.err.println("Usage: java InstallPearSilently  filename.pear  directory/to/install/into <false>");
  			System.exit(-1);
  		}

  		
	  	// determine whether requested to invoke runCVD() after the install
	  	String arg2 = "false";
	  	try {
	  		arg2 = args[2];
	  	} catch (Exception e) {
	  		// ignore if argument was not passed 
	  	}
	  	boolean runCvd = Boolean.parseBoolean(arg2);	  	

	  	
        localTearFile = new File(pearFn);
        installationDir = new File(installDir);
        RunInstallation runner = new RunInstallation(localTearFile, installationDir);
        Thread thread = new Thread(runner);
        thread.start();
          
    	synchronized(runner) {
			try { // wait for the installation to finish, or if interrupted, output message that was interrupted.
				runner.wait(500000);
				//System.out.println("Wait completed");
			} catch (InterruptedException ex) {
				errorFlag = true;
				message = "InterruptedException " + ex;
				printInConsole(errorFlag, message);
			}
    	}
    	
        if (runCvd) runCVD();

  }



}
