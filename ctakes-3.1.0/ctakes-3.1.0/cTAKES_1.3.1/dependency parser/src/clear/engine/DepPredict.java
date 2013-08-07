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


import java.io.PrintStream;

import org.w3c.dom.Element;

import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepParser;
import clear.dep.DepTree;
import clear.reader.AbstractReader;
import clear.reader.CoNLLReader;
import clear.reader.DepReader;
import clear.reader.PosReader;
import clear.util.IOUtil;

/**
 * Predicts dependency trees.
 * @author Jinho D. Choi
 * <b>Last update:</b> 6/29/2010
 */
public class DepPredict extends AbstractEngine
{
	protected final String TAG_PREDICT                = "predict";
	protected final String TAG_PREDICT_POS_MODEL_FILE = "pos_model_file";
	protected final String TAG_PREDICT_MORPH_DICT_DIR = "morph_dict_dir";
	
	/** Test file */
	private String s_testFile     = null;
	/** Output file */
	private String s_outputFile   = null;
	/** Configuration file */
	private String s_configFile   = null;
	/** Part-of-speech model file */
	private String s_posModelFile = null;
	/** Lemmatizer dictionary directory */
	private String s_morphDictDir = null;
	/** Flag to choose parsing algorithm */
	private byte   i_flag         = DepLib.FLAG_PREDICT;
	
	private int[]    n_size_total = new int[10];
	private double[] d_time       = new double[10];
	private double   d_time_total = 0;
	
	public DepPredict(String[] args)
	{
		if (!initArgs(args))					return;
		if (!initConfigElement(s_configFile))	return;
		if (!initCommonElements())				return;
		if (!initPredictElements())				return;
		printCommonConfig();	System.out.println();
		
		AbstractReader<DepNode, DepTree> reader = null;
		
		if      (s_format.equals(AbstractReader.FORMAT_POS))	reader = new PosReader  (s_testFile, s_language, s_morphDictDir);
		else if (s_format.equals(AbstractReader.FORMAT_DEP))	reader = new DepReader  (s_testFile, false);
		else 													reader = new CoNLLReader(s_testFile, false);
		
		System.out.println("Predict: "+s_outputFile);
		DepParser   parser = new DepParser(s_lexiconDir, s_modelFile, 	s_featureXml, i_flag);
		PrintStream fout   = IOUtil.createPrintFileStream(s_outputFile);
	//	PrintStream fplot  = JIO.createPrintFileOutputStream("plot.txt");
		DepTree     tree;
		
		long st, et;
		int  n = 0;

		while (true)
		{
			st   = System.currentTimeMillis();
			tree = reader.nextTree();
			if (tree == null)	break;
			parser.parse(tree);	n++;
			et   = System.currentTimeMillis();
			fout.println(tree+"\n");
			if (n%100 == 0)	System.out.print("\r- Parsing: "+n);
			
			int index = (tree.size() >= 101) ? 9 : (tree.size()-1) / 10;
			d_time [index]     += (et - st);
			d_time_total       += (et - st);
			n_size_total[index]++;
		//	fplot.println(tree.size()+"\t"+tree.n_trans);
		}	System.out.println("\r- Parsing: "+n);
		
		System.out.println("\nParsing time per sentence length:");
		for (int i=0; i<d_time.length; i++)
			System.out.printf("<= %3d: %4.2f (ms)\n", (i+1)*10, d_time[i]/n_size_total[i]);
		
		System.out.printf("\nAverage parsing time: %4.2f (ms)\n", d_time_total/n);
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
				if      (args[i].equals("-t"))	s_testFile   = args[i+1];
				else if (args[i].equals("-o"))	s_outputFile = args[i+1];
				else if (args[i].equals("-c"))	s_configFile = args[i+1];
				else if (args[i].equals("-f"))	i_flag       = Byte.parseByte(args[i+1]);
				else    { printUsage(); return false; }
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return false;
		}
		
		if (s_testFile == null)
		{
			System.err.println("Error: <test file> must be specified.");
			return false;
		}
		
		if (s_outputFile == null)
		{
			System.err.println("Error: <output file> must be specified.");
			return false;
		}
		
		if (s_configFile == null)
		{
			System.err.println("Error: <configure file> must be specified.");
			return false;
		}
		
		if (i_flag != DepLib.FLAG_PREDICT && i_flag != DepLib.FLAG_PREDICT_BEST)
		{
			System.err.println("Error: invalid <flag = "+i_flag+">.");
			return false;
		}
		
		return true;
	}
	
	/** Initializes <predict> element from the configuration file. */
	protected boolean initPredictElements()
	{
		Element ePredict, ePosModelFile, eMorphDictDir;
		
		// <predict>
		if ((ePredict = getElement(e_config, TAG_PREDICT)) == null)
		{
			if (s_format.equals(AbstractReader.FORMAT_RAW) || (s_language.equals(AbstractReader.LANG_EN) && s_format.equals(AbstractReader.FORMAT_POS)))
			{
				System.err.println("Error: <"+TAG_PREDICT+"> must be specified.");
				return false;
			}
			else
				return true;
		}
		
		// <pos_model_file>
		if ((ePosModelFile = getElement(ePredict, TAG_PREDICT_POS_MODEL_FILE)) != null)
			s_posModelFile = ePosModelFile.getTextContent().trim();
		else if (s_format.equals(AbstractReader.FORMAT_RAW))
		{
			System.err.println("Error: <"+TAG_PREDICT_POS_MODEL_FILE+"> must be specified for ["+s_format+"] format.");
			return false;
		}
		
		// <morph_dict_dir>
		if ((eMorphDictDir = getElement(ePredict, TAG_PREDICT_MORPH_DICT_DIR)) != null)
			s_morphDictDir = eMorphDictDir.getTextContent().trim();
		else if (s_language.equals(AbstractReader.LANG_EN) && (s_format.equals(AbstractReader.FORMAT_RAW) || s_format.equals(AbstractReader.FORMAT_POS)))
		{
			System.err.println("Error: <"+TAG_PREDICT_MORPH_DICT_DIR+"> must be specified for ["+s_format+"] format.");
			return false;
		}
	
		return true;
	}
	
	/** Prints usage */
	private void printUsage()
	{
		String usage = "Usage: java clear.engine.DepPredic -t <test file> -o <output file> -c <configuration file> [-f <flag = "+ i_flag+">]";
		System.out.println(usage);
		
		System.out.println("<flag> ::= " + DepLib.FLAG_PREDICT + ": greedy search");
		System.out.println("           " + DepLib.FLAG_PREDICT_BEST   + ": k-best search");
	}
	
	static public void main(String[] args)
	{
		new DepPredict(args);
	}
}
