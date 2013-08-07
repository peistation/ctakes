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
package clear.dep;

import java.io.PrintStream;

import clear.dep.ftr.DepFtrXml;
import clear.ftr.FtrMap;
import clear.model.AbstractModel;
import clear.model.LiblinearModel;
import clear.util.IOUtil;

/**
 * Abstract parser.
 * @author Jinho D. Choi
 * <b>Last update:</b> 7/5/2010
 */
abstract public class AbstractParser
{
	/** Dependency tree to parse */
	protected DepTree       d_tree;
	/** {@link DepLib#FLAG_PRINT_LEXICON}, {@link DepLib#FLAG_PRINT_INSTANCE}}, {@link DepLib#FLAG_PRINT_TRANSITION}, {@link DepLib#FLAG_PREDICT}, {@link DepLib#FLAG_PREDICT_BEST} */
	protected byte          i_flag;
	/** Feature templates */
	protected DepFtrXml     t_xml;
	/** Feature mappings */
	protected FtrMap        t_map;
	/** Prints training instances */
	protected PrintStream   f_out;
	/** Decoders to predict dependency labels */
	protected AbstractModel c_decode;

	/**
	 * Initializes the parser for training.
	 * @param lexiconDir name of the directory containing lexicon files
	 * @param inputFile  name of the feature/model file for training/decoding
	 * @param featureXml name of the feature XML file 
	 * @param flag       {@link AbstractParser#i_flag}
	 */
	public AbstractParser(String lexiconDir, String inputFile, String featureXml, byte flag)
	{
		i_flag = flag;
		t_xml  = new DepFtrXml(featureXml);
		
		if (flag == DepLib.FLAG_PRINT_LEXICON)
		{
			t_map = new FtrMap();
			t_map.addForm  (DepLib.ROOT_TAG);
			t_map.addLemma (DepLib.ROOT_TAG);
			t_map.addPos   (DepLib.ROOT_TAG);
			t_map.addDeprel(DepLib.ROOT_TAG);
		}
		else if (flag == DepLib.FLAG_PRINT_INSTANCE)
		{
			System.out.print("- Loading lexicon files: ");
			t_map = new FtrMap(lexiconDir);
			System.out.println();
			f_out = IOUtil.createPrintFileStream(inputFile);
		}
		else if (flag == DepLib.FLAG_PRINT_TRANSITION)
		{
			f_out = IOUtil.createPrintFileStream(inputFile);
		}
		else if (flag == DepLib.FLAG_PREDICT || flag == DepLib.FLAG_PREDICT_BEST)
		{
			System.out.print("- Loading lexicon files  : ");
			t_map    = new FtrMap(lexiconDir);
			System.out.print("\n- Loading liblinear model: ");
			c_decode = new LiblinearModel(inputFile);
			System.out.println();
		}
	}
	
	/** Parses <code>tree</code>. */
	abstract public void parse(DepTree tree);
	
	/** @see DepFtrMap#save(String) */
	public void saveTags(String lexiconDir)
	{
		System.out.print("- Saving lexicon files: ");
		t_map.save(lexiconDir);
		System.out.println();
	}
}
