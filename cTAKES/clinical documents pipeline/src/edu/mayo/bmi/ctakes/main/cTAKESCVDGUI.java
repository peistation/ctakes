package edu.mayo.bmi.ctakes.main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.uima.internal.util.CommandLineParser;
import org.apache.uima.resource.RelativePathResolver;
import org.apache.uima.tools.cvd.MainFrame;
import org.apache.uima.tools.images.Images;

/**
 * Adapted from the main class of the CAS Visual Debugger.
 * ToDo: to get the preferred display for cTAKES
 * 
 */
public class cTAKESCVDGUI  {

  public static final String MAN_PATH_PROPERTY = "uima.tools.cvd.manpath";

  private static final String TEXT_FILE_PARAM = "-text";

  private static final String DESC_FILE_PARAM = "-desc";

  private static final String EXECUTE_SWITCH = "-exec";

  private static final String DATA_PATH_PARAM = "-datapath";

  private static final String INI_FILE_PARAM = "-ini";

  private static final String LOOK_AND_FEEL_PARAM = "-lookandfeel";
  
  private static final String XMI_FILE_PARAM = "-xmi"; 

  

  public static MainFrame createMainFrame() {
     final MainFrame frame = new MainFrame(null);// "cTAKES CAS VISUAL DEBUGGER");
    // Set icon -- ToDo check cTAKES logo
    ImageIcon icon = Images.getImageIcon(Images.MICROSCOPE);
    if (icon != null) {
      frame.setIconImage(icon.getImage());
    }
    try {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          frame.pack();
          frame.setVisible(true);
        }
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
       e.printStackTrace();
    }
    return frame;
  }

  private static final CommandLineParser createCmdLineParser() {
    CommandLineParser parser = new CommandLineParser();
    parser.addParameter(TEXT_FILE_PARAM, true);
    parser.addParameter(DESC_FILE_PARAM, true);
    parser.addParameter(DATA_PATH_PARAM, true);
    parser.addParameter(LOOK_AND_FEEL_PARAM, true);
    parser.addParameter(EXECUTE_SWITCH);
    parser.addParameter(XMI_FILE_PARAM, true); 
    parser.addParameter(INI_FILE_PARAM, true);
    return parser;
  }

  private static final void printUsage() {
    System.out
        .println("Usage: java edu.mayo.bmi.ctakes.main.cTAKESCVDGUI [-text <TextFile>] [-desc <XmlDescriptor>] [-datapath <DataPath>] [-exec]");
    System.out.println("Additional optional parameters:");
    System.out.println("  -lookandfeel <LookAndFeelClassName>");
  }

  private static final boolean checkCmdLineSyntax(CommandLineParser clp) {
    if (clp.getRestArgs().length > 0) {
      System.err.println("Error parsing CVD command line: unknown argument(s):");
      String[] args = clp.getRestArgs();
      for (int i = 0; i < args.length; i++) {
        System.err.print(" ");
        System.err.print(args[i]);
      }
      System.err.println();
      return false;
    }
    if (clp.isInArgsList(EXECUTE_SWITCH) && !clp.isInArgsList(DESC_FILE_PARAM)) {
      System.err.println("Error parsing cTAKESCVDGUI command line: -exec switch requires -desc parameter.");
      return false;
    }
    
    return true;
  }

  public static void main(String[] args) {
    try {
      CommandLineParser clp = createCmdLineParser();
      clp.parseCmdLine(args);
      if (!checkCmdLineSyntax(clp)) {
        printUsage();
        System.exit(2);
      }
      String lookAndFeel = null;
      if (clp.isInArgsList(LOOK_AND_FEEL_PARAM)) {
        lookAndFeel = clp.getParamArgument(LOOK_AND_FEEL_PARAM);
        try {
          UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
          System.err.println(e.getMessage());
        }
      }
      MainFrame frame = createMainFrame();
      if (clp.isInArgsList(TEXT_FILE_PARAM)) {
        frame.loadTextFile(new File(clp.getParamArgument(TEXT_FILE_PARAM)));
      }
      if (clp.isInArgsList(DATA_PATH_PARAM)) {
        frame.setDataPath(clp.getParamArgument(DATA_PATH_PARAM));
      } else {
        String dataProp = System.getProperty(RelativePathResolver.UIMA_DATAPATH_PROP);
        if (dataProp != null) {
          frame.setDataPath(dataProp);
        }
      }
      if (clp.isInArgsList(DESC_FILE_PARAM)) {
        frame.loadAEDescriptor(new File(clp.getParamArgument(DESC_FILE_PARAM)));
      }
      if (clp.isInArgsList(TEXT_FILE_PARAM)) {
	frame.loadTextFile(new File(clp.getParamArgument(TEXT_FILE_PARAM)));
      } 
      if (clp.isInArgsList(EXECUTE_SWITCH)) {
        frame.runAE(true);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}