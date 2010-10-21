/**
* Copyright (c) 2009, Regents of the University of Colorado
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
package clear.engine;

import org.w3c.dom.Element;

import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepParser;
import clear.dep.DepTree;
import clear.reader.AbstractReader;
import clear.reader.CoNLLReader;
import clear.reader.DepReader;

/**
 * Trains dependency parser.
 * <b>Last update:</b> 6/29/2010
 * @author Jinho D. Choi
 */
public class DepTrain extends AbstractEngine
{
	final String TAG_LIBLINEAR         = "liblinear";
	final String TAG_LIBLINEAR_TRAINER = "trainer";
	final String TAG_LIBLINEAR_S       = "s";
	final String TAG_LIBLINEAR_C       = "c";
	final String TAG_LIBLINEAR_E       = "e";
	final String TAG_LIBLINEAR_B       = "B";
	
	/** Training file */
	private String s_trainFile   = null; 
	/** Configuration file */
	private String s_configFile  = null;
	/** Feature file */
	private String s_featureFile = null;
	/** Liblinear: train executable */
	protected String  s_trainer  = null;
	/** Liblinear: algorithm */
	protected int     i_s        = 3;
	/** Liblinear: cost */
	protected double  d_c        = 0.1;
	/** Liblinear: epsilon */
	protected double  d_e        = 0.1;
	/** Liblinear: bias */
	protected double  d_b        = -1;
	/** Flag for training method */
	private byte   i_flag        = DepLib.FLAG_PRINT_LEXICON;
	
	public DepTrain(String[] args)
	{
		if (!initArgs(args))					return;
		if (!initConfigElement(s_configFile))	return;
		if (!initCommonElements())				return;
		if (!initTrainElements())				return;
		
		s_featureFile = s_modelFile + ".ftr";
		
		if (i_flag == DepLib.FLAG_PRINT_LEXICON)
		{
			printCommonConfig();
			System.out.println("- train_file : "+s_trainFile);
			
			System.out.println("\nPrint lexicon files: ["+s_lexiconDir+"]");
			trainDepParser(DepLib.FLAG_PRINT_LEXICON);
			
			System.out.println("\nPrint training instances: " +s_featureFile);
			trainDepParser(DepLib.FLAG_PRINT_INSTANCE);
			
			System.out.println("\nTrain liblinear model: " +s_modelFile);
			trainLibLinear();
		}
		else
			trainDepParser(DepLib.FLAG_PRINT_TRANSITION);
	}
	
	/** Initializes arguments. */
	private boolean initArgs(String[] args)
	{
		if (args.length == 0 || args.length % 2 != 0)
		{
			printUsage();
			return false;
		}
		
		try
		{
			for (int i=0; i<args.length; i+=2)
			{
				if      (args[i].equals("-t"))	s_trainFile  = args[i+1];
				else if (args[i].equals("-c"))	s_configFile = args[i+1];
				else if (args[i].equals("-f"))	i_flag       = Byte.parseByte(args[i+1]);
				else    { printUsage(); return false; }
			}
		}
		catch (NumberFormatException e) {e.printStackTrace();return false;}
		
		if (s_trainFile == null)
		{
			System.err.println("Error: <training file> must be specified.");
			return false;
		}
		
		if (i_flag != DepLib.FLAG_PRINT_TRANSITION && s_configFile == null)
		{
			System.err.println("Error: <configuration file> must be specified.");
			return false;
		}

		if (i_flag != DepLib.FLAG_PRINT_LEXICON && i_flag != DepLib.FLAG_PRINT_TRANSITION)
		{
			System.err.println("Error: invalid <flag = "+i_flag+">.");
			return false;
		}
		
		return true;
	}
	
	/** Initializes <liblinear> element. */
	protected boolean initTrainElements()
	{
		// check format
		if (s_format.equals(AbstractReader.FORMAT_RAW) || s_format.equals(AbstractReader.FORMAT_POS))
		{
			System.err.println("Error: invalid <format = "+s_format+"> for training.");
			return false;
		}
		
		Element eLiblinear, eTrainExc;
		
		// <liblinear>
		if ((eLiblinear = getElement(e_config, TAG_LIBLINEAR)) == null)
		{
			System.err.println("Error: <"+TAG_LIBLINEAR+"> must be specified.");
			return false;
		}
		
		// <trainer>
		if ((eTrainExc = getElement(eLiblinear, TAG_LIBLINEAR_TRAINER)) == null)
		{
			System.err.println("Error: <"+TAG_LIBLINEAR+"."+TAG_LIBLINEAR_TRAINER+"> must be specified.");
			return false;
		}
		
		s_trainer = eTrainExc.getTextContent().trim();
		
		// liblinear parameters
		try
		{
			String tmp;
			
			if ((tmp = eLiblinear.getAttribute(TAG_LIBLINEAR_S).trim()).length() > 0)
				i_s = Integer.parseInt  (tmp);
			
			if ((tmp = eLiblinear.getAttribute(TAG_LIBLINEAR_C).trim()).length() > 0)
				d_c = Double.parseDouble(tmp);
			
			if ((tmp = eLiblinear.getAttribute(TAG_LIBLINEAR_E).trim()).length() > 0)
				d_e = Double.parseDouble(tmp);
			
			if ((tmp = eLiblinear.getAttribute(TAG_LIBLINEAR_B).trim()).length() > 0)
				d_b = Double.parseDouble(tmp);
		}
		catch (NumberFormatException e){e.printStackTrace(); return false;}
		
		return true;
	}
	
	/** Trains the dependency parser. */
	private void trainDepParser(byte flag)
	{
		AbstractReader<DepNode, DepTree> reader = null;
		
		if (s_format.equals(AbstractReader.FORMAT_DEP))	reader = new DepReader  (s_trainFile, true);
		else 											reader = new CoNLLReader(s_trainFile, true);

		DepParser parser = new DepParser(s_lexiconDir, s_featureFile, s_featureXml, flag);
		DepTree   tree;
		
		System.out.print("Parsing: ");	int n;
		
		for (n=0; (tree = reader.nextTree()) != null; n++)
		{
			parser.parse(tree);
			if (n % 1000 == 0)	System.out.printf("%s%dK", "\r- Parsing: ", n/1000);
		}	System.out.println("\r- Parsing: "+n);
		
		if (flag == DepLib.FLAG_PRINT_LEXICON)	parser.saveTags(s_lexiconDir);
	}
	
	/** Trains the LibLinear classifier. */
	private void trainLibLinear()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(s_trainer);
		build.append(" -s "+i_s);
		build.append(" -c "+d_c);
		build.append(" -e "+d_e);
		build.append(" -B "+d_b);
		build.append(" "+s_featureFile);
		build.append(" "+s_modelFile);
		
		String cmd = build.toString();
		System.out.println("- Command : "+cmd);
		
		try
		{
			Runtime rt = Runtime.getRuntime();
			long    st = System.currentTimeMillis();
			Process ps = rt.exec(cmd);	ps.waitFor();
			long  time = System.currentTimeMillis() - st;
			System.out.printf("- Duration: %d hours, %d minutes\n", time/(1000*60*60), time/(1000*60));
		}
		catch (Exception e) {e.printStackTrace();}
		
	/*	Train trainer = new Train();
		try
		{
			Problem   prob  = trainer.readProblem(new File(s_featureFile), d_b);
			Parameter param = new Parameter(SolverType.values()[i_s], d_c, d_e);
			Model     model = Linear.train(prob, param);
			Linear.saveModel(new File(s_modelFile), model);
		}
		catch (Exception e) {e.printStackTrace();}*/
	}
	
	/** Prints usage. */
	private void printUsage()
	{
		String usage = "Usage: java clear.engine.DepTrain -t <training file> -c <configuration file> [-f <flag = " + i_flag   +">]";
		System.err.println(usage);
		
		System.err.println("<flag> ::= " + DepLib.FLAG_PRINT_LEXICON    + ": train a model using LibLinear");
		System.err.println("           " + DepLib.FLAG_PRINT_TRANSITION + ": print transitions to the standard I/O");
	}
	
	static public void main(String[] args)
	{
		new DepTrain(args);
	}
}
