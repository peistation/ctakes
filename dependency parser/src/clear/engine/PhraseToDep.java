/**
 * Copyright (c) 2010, Regents of the University of Colorado
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

import java.io.File;
import java.io.PrintStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import clear.dep.DepTree;
import clear.morph.MorphEnAnalyzer;
import clear.reader.AbstractReader;
import clear.treebank.TBEnConvert;
import clear.treebank.TBHeadRules;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import clear.util.IOUtil;

public class PhraseToDep
{
	@Option(name="-i", usage="name of a file containing phrase structure tree", required=true, metaVar="REQUIRED")
	String inputFile;
	@Option(name="-o", usage="name of a file containing dependency trees", required=true, metaVar="REQUIRED")
	String outputFile;
	@Option(name="-h", usage="name of a file containing head-percolation rules", required=true, metaVar="REQUIRED")
	String headruleFile;
	@Option(name="-m", usage="path of a directory containing dictionaries for morphological analyzer", metaVar="OPTIONAL")
	String dictDir = null;
	@Option(name="-l", usage="language ::= "+AbstractReader.LANG_CH+" | "+AbstractReader.LANG_EN+" (default)", metaVar="OPTIONAL")
	String language = AbstractReader.LANG_EN;
	@Option(name="-n", usage="minimum sentence length (inclusive; default = 0)", metaVar="OPTIONAL")
	int length = 0;

	static public void main(String[] args)
	{
		new PhraseToDep().convert(args);
	}

	public void convert(String[] args)
	{
		CmdLineParser cmd = new CmdLineParser(this);

		try
		{
			cmd.parseArgument(args);

			TBReader        reader    = new TBReader(inputFile);
			TBHeadRules     headrules = new TBHeadRules(headruleFile);
			MorphEnAnalyzer morph     = (dictDir != null) ? new MorphEnAnalyzer(dictDir) : null;
			PrintStream     fout      = IOUtil.createPrintFileStream(outputFile);
			TBTree          tree;
			TBEnConvert     converter = new TBEnConvert();

			String filename = inputFile.substring(inputFile.lastIndexOf(File.separator)+1);
			int i = 0;

			System.out.print("\r"+filename+": 0");
			while ((tree = reader.nextTree()) != null)
			{
				DepTree dTree = converter.toDepTree(tree, headrules, morph);
				if (dTree.size() > length){ fout.println(dTree+"\n");   i++; }
				if (i%1000 == 0)        System.out.print("\r"+filename+": "+i);
			}       System.out.println("\r"+filename+": "+i);
		}
		catch (CmdLineException e)
		{
			System.err.println(e.getMessage());
			cmd.printUsage(System.err);
		}
	}
}
