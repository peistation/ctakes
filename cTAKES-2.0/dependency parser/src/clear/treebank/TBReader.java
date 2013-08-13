/**
* Copyright (c) 2007, Regents of the University of Colorado
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
package clear.treebank;

import clear.util.JFileTokenizer;

/**
 * Treebank reader
 * @author Jinho D. Choi
 * <b>Last update:</b> 8/30/2010
 */
public class TBReader
{
	/** Left round bracket "(" */
	static final public String LRB = "(";
	/** Right round bracket "(" */
	static final public String RRB = ")";
	/** FileTokenizer to read the Treebank file */
	private JFileTokenizer f_tree;
	
	/**
	 * Initializes the Treebank reader.
	 * @param treeFile name of the Treebank file
	 */
	public TBReader(String treeFile)
	{
		String delim = LRB + RRB + JFileTokenizer.WHITE;
		f_tree = new JFileTokenizer(treeFile, delim, true);
	}
	
	/**
	 * Returns the next tree in the Treebank.
	 * If there is none, returns null.
	 */
	public TBTree nextTree()
	{
		String str;
		
		do		// find the first '('
		{
			str = nextToken();
			if (str == null){	f_tree.close();	return null;	}
		}
		while (!str.equals(LRB));
		
		int numBracket    = 1;
		int terminalIndex = 0;
		int tokenIndex    = 0;
		TBTree tree       = new TBTree();
		TBNode head       = new TBNode(null, "DUMMY");	// dummy-head
		TBNode curr       = head;						// pointer to the current node

		while (true)
		{		
			if ((str = nextToken()) == null)
				errorMsg("more token needed");
			
			if (numBracket == 1 && str.equals(TBEnLib.POS_TOP))
			{
				TBNode node = new TBNode(curr, str);	// add a child to 'curr'
				curr.addChild(node);
				curr = node;							// move to child
			}
			else if (str.equals(LRB))
			{
				numBracket++;
				if ((str = nextToken()) == null)		// str = pos-tag
					errorMsg("POS-tag is missing");
				
				TBNode node = new TBNode(curr, str);	// add a child to 'curr'
				curr.addChild(node);
				curr = node;							// move to child
			}
			else if (str.equals(RRB))
			{
				numBracket--;
				curr = curr.getParent();				// move to parent
				if (numBracket == 0)	break;
			}
			else
			{
				curr.setForm(str);						// str = word
				curr.terminalId = curr.headId = terminalIndex++;
				if (!curr.isEmptyCategory())	curr.tokenId = tokenIndex++;
				tree.addTerminal(curr);					// add 'curr' as a leaf
			}
		}
		
		TBNode root = head.getChildren().get(0);		// omit the dummy head
		root.setParent(null);
		tree.setRoot(root);
		
		return tree;
	}
	
	/**
	 * Skips all white-spaces and returns the next token.
	 * If there is no such token, returns null.
	 */
	private String nextToken()
	{
		while (f_tree.hasMoreTokens())
		{
			String str = f_tree.nextToken();
			
			if (JFileTokenizer.WHITE.indexOf(str) == -1)
				return str;
		}

		return null;
	}
	
	/** Prints an error-message and exits. */
	private void errorMsg(String msg)
	{
		System.err.println("error: "+msg+" (line: " + f_tree.getLineNumber()+")");
		System.exit(1);
	}
}
