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

import java.util.ArrayList;

import clear.dep.ftr.DepFtrToken;
import clear.dep.ftr.DepFtrXml;
import clear.ftr.FtrLib;
import clear.util.DSUtil;
import clear.util.tuple.JIntDoubleTuple;
import clear.util.tuple.JIntIntTuple;
import clear.util.tuple.JObjectDoubleTuple;

/**
 * Dependency parser.
 * @author Jinho D. Choi
 * <b>Last update:</b> 7/5/2010
 */
public class DepParser extends AbstractParser
{
	/** Label for Shift transition */
	static public final String SHIFT     = "SH";
	/** Label for No-Arc transition */
	static public final String NO_ARC    = "NA";
	/** Label for Left-Arc transition */
	static public final String LEFT_ARC  = "LA";
	/** Label for Right-Arc transition */
	static public final String RIGHT_ARC = "RA";
	/** Delimiter between transition and dependency label */
	static public final String LB_DELIM  = "-";
	
	/** Index of lambda_1 */
	private int i_lambda;
	/** Index of beta */
	private int i_beta;

	/**
	 * Initializes the parser for training.
	 * @param lexiconDir name of the directory containing lexicon files
	 * @param inputFile  name of the feature/model file for training/decoding
	 * @param featureXml name of the feature XML file
	 * @param flag       {@link AbstractParser#i_flag}
	 */
	public DepParser(String lexiconDir, String inputFile, String featureXml, byte flag)
	{
		super(lexiconDir, inputFile, featureXml, flag);
	}
	
	/**
	 * Initializes lambda1, lambda2, and beta with <code>tree</code>. 
	 * @param tree dependency tree to initialize
	 */
	private void init(DepTree tree)
	{
		d_tree   = tree;
		i_lambda = 0;
		i_beta   = 1;
		
		if (i_flag == DepLib.FLAG_PRINT_TRANSITION)	printTransition("", "");
	}
	
	/* (non-Javadoc)
	 * @see harvest.dep.AbstractDepParser#parse(harvest.dep.DepTree)
	 */
	public void parse(DepTree tree)
	{
		init(tree);
		
		while (i_beta < tree.size())	// beta is not empty
		{
			d_tree.n_trans++;
			
			if (i_lambda == -1)			// lambda_1 is empty: deterministic shift
				shift(true);	
			else if (i_flag == DepLib.FLAG_PREDICT)
				predict();
			else
				train();
		}
		
		if      (i_flag == DepLib.FLAG_PRINT_TRANSITION)	f_out.println();
		else if (i_flag == DepLib.FLAG_PREDICT)				postProcess();
	}
	
	/** Trains the dependency tree ({@link DepParser#d_tree}). */
	private void train()
	{
		DepNode lambda = d_tree.get(i_lambda);
		DepNode beta   = d_tree.get(i_beta);
		
		if      (lambda.headId == beta.id)	leftArc (lambda, beta, lambda.deprel, 1d);
		else if (lambda.id == beta.headId)	rightArc(lambda, beta, beta  .deprel, 1d);
		else if (isShift())					shift(false);
		else								noArc();
	}
	
	/**
	 * Returns true if 'shift' operation should be performed.
	 * In other words, returns true if there is no relation between lambda_1[*] and beta[0].
	 * This method is called from {@link DepParser#train()}.
	 */
	private boolean isShift()
	{
		DepNode beta = d_tree.get(i_beta);
		
		for (int i=i_lambda; i>=0; i--)
		{
			DepNode lambda = d_tree.get(i);
			
			if (lambda.headId == beta.id || lambda.id == beta.headId)
				return false;
		}

		return true;
	}
	
	/** Predicts dependency relations using a greedy algorithm. */
	private void predict()
	{
		JIntDoubleTuple res = c_decode.predict(getFeatureArray());
		String  label  = (res.i < 0) ? NO_ARC : t_map.indexToLabel(res.i);
		int     index  = label.indexOf(LB_DELIM);
		String  trans  = (index > 0) ? label.substring(0,index) : label;
		String  deprel = (index > 0) ? label.substring(index+1) : "";
		DepNode lambda = d_tree.get(i_lambda);
		DepNode beta   = d_tree.get(i_beta);
		
		if      (trans.equals( LEFT_ARC) && !d_tree.isAncestor(lambda, beta) && lambda.id != DepLib.ROOT_ID)
			leftArc (lambda, beta, deprel, res.d);
		else if (trans.equals(RIGHT_ARC) && !d_tree.isAncestor(beta, lambda))
			rightArc(lambda, beta, deprel, res.d);
		else if (trans.equals(SHIFT))
			shift(false);
		else
			noArc();
	}
	
	/** Predicts dependency relations for tokens that have not found their heads. */
	private void postProcess()
	{
		for (int currId=1; currId<d_tree.size(); currId++)
		{
			DepNode curr = d_tree.get(currId);
			if (curr.hasHead)	continue;
			
			JObjectDoubleTuple<String> max = new JObjectDoubleTuple<String>(null, -1000);
			int maxId = -1;
			
			for (int leftId=currId-1; leftId>=0; leftId--)
			{
				DepNode left = d_tree.get(leftId);
				if (d_tree.isAncestor(curr, left))	continue;
				maxId = getMaxHeadId(curr, left, maxId, max, RIGHT_ARC);
			}
			
			for (int rightId=currId+1; rightId<d_tree.size(); rightId++)
			{
				DepNode right = d_tree.get(rightId);
				if (d_tree.isAncestor(curr, right))	continue;
				maxId = getMaxHeadId(curr, right, maxId, max, LEFT_ARC);
			}
		
			if (maxId != -1)	curr.setHead(maxId, max.object, max.value);
		}
	}
	
	/** This method is called from {@link DepParser#postProcess()}. */
	private int getMaxHeadId(DepNode curr, DepNode head, int maxId, JObjectDoubleTuple<String> max, String sTrans)
	{
		if (curr.id < head.id)
		{
			i_lambda = curr.id;
			i_beta   = head.id;
		}
		else
		{
			i_lambda = head.id;
			i_beta   = curr.id;
		}
		
		ArrayList<JIntDoubleTuple> aRes = c_decode.predictAll(getFeatureArray());
		
		for (int i=0; i<aRes.size(); i++)
		{
			JIntDoubleTuple res = aRes.get(i);
			
			String label = t_map.indexToLabel(res.i);
			int    index = label.indexOf(LB_DELIM);
			if (index == -1)	continue;
			String trans = label.substring(0, index);
			
			if (trans.equals(sTrans))
			{
				if (max.value < res.d)
				{
					String deprel = label.substring(index+1);
					max.set(deprel, res.d);
					maxId = head.id;
				}	break;
			}
		}
		
		return maxId;
	}
	
	/**
	 * Performs a shift transition.
	 * @param isDeterministic true if this is called for a deterministic-shift.
	 */
	private void shift(boolean isDeterministic)
	{
		if (!isDeterministic)
		{
			if      (i_flag == DepLib.FLAG_PRINT_LEXICON  )	addTag       (SHIFT);
			else if (i_flag == DepLib.FLAG_PRINT_INSTANCE)	printInstance(SHIFT);
		}
			
		i_lambda = i_beta++;
		
		if (i_flag == DepLib.FLAG_PRINT_TRANSITION)	printTransition("SHIFT", "");
	}
	
	/** Performs a no-arc transition. */
	private void noArc()
	{
		if      (i_flag == DepLib.FLAG_PRINT_LEXICON  )	addTag       (NO_ARC);
		else if (i_flag == DepLib.FLAG_PRINT_INSTANCE)	printInstance(NO_ARC);
		
		i_lambda--;
		
		if (i_flag == DepLib.FLAG_PRINT_TRANSITION)	printTransition("NO-ARC", "");
	}
	
	/**
	 * Performs a left-arc transition.
	 * @param lambda lambda_1[0]
	 * @param beta   beta[0]
	 * @param deprel dependency label between <code>lambda</code> and <code>beta</code>
	 * @param score  dependency score between <code>lambda</code> and <code>beta</code>
	 */
	private void leftArc(DepNode lambda, DepNode beta, String deprel, double score)
	{
		String  label = LEFT_ARC + LB_DELIM + deprel;
		
	    if      (i_flag == DepLib.FLAG_PRINT_LEXICON)  	addTag(label);
		else if (i_flag == DepLib.FLAG_PRINT_INSTANCE)	printInstance(label);

		lambda.setHead(beta.id, deprel, score);
		if (lambda.id < beta.leftDepId)	beta.leftDepId = lambda.id;
		i_lambda--;
		
		if (i_flag == DepLib.FLAG_PRINT_TRANSITION)
			printTransition("LEFT-ARC", lambda.id+" <-"+beta.deprel+"- "+beta.id);
	}
	
	/**
	 * Performs a right-arc transition.
	 * @param lambda lambda_1[0]
	 * @param beta   beta[0]
	 * @param deprel dependency label between lambda_1[0] and beta[0]
	 * @param score  dependency score between lambda_1[0] and beta[0]
	 */
	private void rightArc(DepNode lambda, DepNode beta, String deprel, double score)
	{
		String  label = RIGHT_ARC + LB_DELIM + deprel;
		
		if      (i_flag == DepLib.FLAG_PRINT_LEXICON)	addTag(label);
		else if (i_flag == DepLib.FLAG_PRINT_INSTANCE)	printInstance(label);

		beta.setHead(lambda.id, deprel, score);
		if (lambda.rightDepId < beta.id)	lambda.rightDepId = beta.id;
		i_lambda--;
		
		if (i_flag == DepLib.FLAG_PRINT_TRANSITION)
			printTransition("RIGHT-ARC", lambda.id+" -"+beta.deprel+"-> "+beta.id);
	}
	
	/**
	 * Adds tags to {@link DepParser#t_tag}.
	 * @param label <trainsition>[-<dependency label>]
	 */
	private void addTag(String label)
	{
		t_map.addLabel(label);
		
		if      (label.startsWith(LEFT_ARC))	t_map.addPosPosDepRule(d_tree.get(i_lambda), d_tree.get(i_beta),  1);
		else if (label.startsWith(RIGHT_ARC))	t_map.addPosPosDepRule(d_tree.get(i_lambda), d_tree.get(i_beta), -1);
		
		getFeatureArray();
	}
	
	/**
	 * Prints the current training instance.
	 * @param label <trainsition>[-<dependency label>]
	 */
	private void printInstance(String label)
	{
		f_out.println(getInstance(label));
	}
	
	/**
	 * Prints the current transition.
	 * @param trans transition
	 * @param arc   lambda_1[0] <- deprel -> beta[0]
	 */
	private void printTransition(String trans, String arc)
	{
		// operation
		String str = trans + "\t";
		
		// lambda_1
		str += "[";
		if (i_lambda >= 0)	str += "" +0;
		if (i_lambda >= 1)	str += ":"+i_lambda;
		str += "]\t";
		
		// lambda_2
		str += "[";
		if (getLambda2Count() > 0)	str += "" +(i_lambda+1);
		if (getLambda2Count() > 1)	str += ":"+(i_beta  -1);
		str += "]\t";
		
		// beta
		str += "[";
		if (i_beta < d_tree.size())		str += "" +i_beta;
		if (i_beta <= d_tree.size())	str += ":"+(d_tree.size()-1);
		str += "]\t";
		
		// transition
		str += arc;
		f_out.println(str);
	}
	
	/** @return number of nodes in lambda_2 (list #2) */
	private int getLambda2Count()
	{
		return i_beta - (i_lambda+1);
	}
	
	// ---------------------------- getFtr*() ----------------------------
	
	/**
	 * Returns an instance consists of feature set represented as an integer array and <code>label</code>.
	 * @param label <trainsition>[-<dependency label>]
	 */
	private String getInstance(String label)
	{
		return t_map.labelToIndex(label) + " " + DSUtil.toString(getFeatureArray(),":1 ");
	//	return t_map.labelToIndex(label) + AbstractTrainer.COL_DELIM + JArrays.toString(getFeatureArray()," ");
	}
	
	private ArrayList<Integer> getFeatureArray()
	{
		DepNode l0 = d_tree.get(i_lambda);
		DepNode b0 = d_tree.get(i_beta);
		
		ArrayList<String> s_form_1gram        = new ArrayList<String>();
		ArrayList<String> s_lemma_1gram       = new ArrayList<String>();
		ArrayList<String> s_pos_1gram         = new ArrayList<String>();
		ArrayList<String> s_deprel_1gram      = new ArrayList<String>();
		ArrayList<String> s_pos_lemma_1gram   = new ArrayList<String>();
		ArrayList<String> s_pos_pos_2gram     = new ArrayList<String>();
		ArrayList<String> s_pos_lemma_2gram   = new ArrayList<String>();
		ArrayList<String> s_lemma_pos_2gram   = new ArrayList<String>();
		ArrayList<String> s_lemma_lemma_2gram = new ArrayList<String>();
		ArrayList<String> s_pos_pos_pos_3gram = new ArrayList<String>();
		
		for (DepFtrToken[] tokens : t_xml.form_1gram)	s_form_1gram  .add(getNode(tokens[0]).form);
		for (DepFtrToken[] tokens : t_xml.lemma_1gram)	s_lemma_1gram .add(getNode(tokens[0]).lemma);
		for (DepFtrToken[] tokens : t_xml.pos_1gram)	s_pos_1gram   .add(getNode(tokens[0]).pos);
		for (DepFtrToken[] tokens : t_xml.deprel_1gram)	s_deprel_1gram.add(getNode(tokens[0]).getDeprel());
		
		for (DepFtrToken[] tokens : t_xml.pos_lemma_1gram)
		{
			DepNode node = getNode(tokens[0]);
			if (node.isNull())	s_pos_lemma_1gram.add(FtrLib.TAG_NULL);
			else				s_pos_lemma_1gram.add(node.pos + FtrLib.TAG_DELIM + node.lemma);
		}
		
		for (DepFtrToken[] tokens : t_xml.pos_pos_2gram)
		{
			DepNode node0 = getNode(tokens[0]);
			DepNode node1 = getNode(tokens[1]);
			if (node0.isNull() || node1.isNull())	s_pos_pos_2gram.add(FtrLib.TAG_NULL);
			else									s_pos_pos_2gram.add(node0.pos + FtrLib.TAG_DELIM + node1.pos);
		}
		
		for (DepFtrToken[] tokens : t_xml.pos_lemma_2gram)
		{
			DepNode node0 = getNode(tokens[0]);
			DepNode node1 = getNode(tokens[1]);
			if (node0.isNull() || node1.isNull())	s_pos_lemma_2gram.add(FtrLib.TAG_NULL);
			else									s_pos_lemma_2gram.add(node0.pos + FtrLib.TAG_DELIM + node1.lemma);
		}
		
		for (DepFtrToken[] tokens : t_xml.lemma_pos_2gram)
		{
			DepNode node0 = getNode(tokens[0]);
			DepNode node1 = getNode(tokens[1]);
			if (node0.isNull() || node1.isNull())	s_lemma_pos_2gram.add(FtrLib.TAG_NULL);
			else									s_lemma_pos_2gram.add(node0.lemma + FtrLib.TAG_DELIM + node1.pos);
		}
		
		for (DepFtrToken[] tokens : t_xml.lemma_lemma_2gram)
		{
			DepNode node0 = getNode(tokens[0]);
			DepNode node1 = getNode(tokens[1]);
			if (node0.isNull() || node1.isNull())	s_lemma_lemma_2gram.add(FtrLib.TAG_NULL);
			else									s_lemma_lemma_2gram.add(node0.lemma + FtrLib.TAG_DELIM + node1.lemma);
		}
		
		for (DepFtrToken[] tokens : t_xml.pos_pos_pos_3gram)
		{
			DepNode node0 = getNode(tokens[0]);
			DepNode node1 = getNode(tokens[1]);
			DepNode node2 = getNode(tokens[2]);
			if (node0.isNull() || node1.isNull() || node2.isNull())	s_pos_pos_pos_3gram.add(FtrLib.TAG_NULL);
			else													s_pos_pos_pos_3gram.add(node0.pos + FtrLib.TAG_DELIM + node1.pos + FtrLib.TAG_DELIM + node2.pos);
		}
		
		if (i_flag == DepLib.FLAG_PRINT_LEXICON)	// store features for configuration files
		{
			t_map.addForm  (b0.form);
			t_map.addLemma (b0.lemma);
			t_map.addPos   (b0.pos);
			t_map.addDeprel(b0.deprel);
			
			for (String pos_lemma_1gram : s_pos_lemma_1gram)
				if (!pos_lemma_1gram.equals(FtrLib.TAG_NULL))		t_map.addPosLemma1gram(pos_lemma_1gram);
			
			for (String pos_pos_2gram : s_pos_pos_2gram)
				if (!pos_pos_2gram.equals(FtrLib.TAG_NULL)) 		t_map.addPosPos2gram(pos_pos_2gram);
			
			for (String pos_lemma_2gram : s_pos_lemma_2gram)
				if (!pos_lemma_2gram.equals(FtrLib.TAG_NULL))		t_map.addPosLemma2gram(pos_lemma_2gram);
			
			for (String lemma_pos_2gram : s_lemma_pos_2gram)
				if (!lemma_pos_2gram.equals(FtrLib.TAG_NULL))		t_map.addLemmaPos2gram(lemma_pos_2gram);
			
			for (String lemma_lemma_2gram : s_lemma_lemma_2gram)
				if (!lemma_lemma_2gram.equals(FtrLib.TAG_NULL))		t_map.addLemmaLemma2gram(lemma_lemma_2gram);

			for (String pos_pos_pos_3gram : s_pos_pos_pos_3gram)
				if (!pos_pos_pos_3gram.equals(FtrLib.TAG_NULL))		t_map.addPosPosPos3gram(pos_pos_pos_3gram);
			
			if (b0.isDeprel(DepLib.DEPREL_P))						t_map.addPunctuation(b0.form);
			
			if (l0.chunkBE != null)  t_map.addChunkPos(l0.chunkPos);
			if (b0.chunkBE != null)  t_map.addChunkPos(b0.chunkPos);
			
			return null;
		}
		
		// add features
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int idx[] = {1};
		
		for (String form       : s_form_1gram)			addNgramFeature(arr, form      , idx,  0);
		for (String lemma      : s_lemma_1gram)			addNgramFeature(arr, lemma     , idx,  1);
		for (String pos        : s_pos_1gram)			addNgramFeature(arr, pos       , idx,  2);
		for (String deprel     : s_deprel_1gram)		addNgramFeature(arr, deprel    , idx,  3);
		for (String posLemma   : s_pos_lemma_1gram)		addNgramFeature(arr, posLemma  , idx,  4);
		for (String posPos     : s_pos_pos_2gram)		addNgramFeature(arr, posPos    , idx,  5);
		for (String posLemma   : s_pos_lemma_2gram)		addNgramFeature(arr, posLemma  , idx,  6);
		for (String lemmaPos   : s_lemma_pos_2gram)		addNgramFeature(arr, lemmaPos  , idx,  7);
		for (String lemmaLemma : s_lemma_lemma_2gram)	addNgramFeature(arr, lemmaLemma, idx,  8);
		for (String posPosPos  : s_pos_pos_pos_3gram)	addNgramFeature(arr, posPosPos , idx,  9);
		
		addPunctuationFeatures  (arr, l0.id, b0.id, idx);
		addUnlabeledRuleFeatures(arr, l0   , b0   , idx);
	//	addChunkBoundaryFeatures(arr, l0   , b0   , idx);
		
		return arr;
	}
	
	/**
	 * Adds an n-gram feature.
	 * This method is called from {@link DepParser#getFeatureArray()}. 
	 */
	private void addNgramFeature(ArrayList<Integer> arr, String feature, int[] beginIndex, int flag)
	{
		int index = -1, begin = beginIndex[0];
		
		switch (flag)
		{
		case  0: index = t_map.formToIndex(feature);
				 beginIndex[0] += t_map.n_form;					break;
		case  1: index = t_map.lemmaToIndex(feature);
				 beginIndex[0] += t_map.n_lemma;				break;
		case  2: index = t_map.posToIndex(feature);
        		 beginIndex[0] += t_map.n_pos;					break;
		case  3: index = t_map.deprelToIndex(feature);
				 beginIndex[0] += t_map.n_deprel;				break;
		case  4: index = t_map.posLemma1gramToIndex(feature);
				 beginIndex[0] += t_map.n_pos_lemma_1gram;		break;
		case  5: index = t_map.posPos2gramToIndex(feature);
				 beginIndex[0] += t_map.n_pos_pos_2gram;		break;
		case  6: index = t_map.posLemma2gramToIndex(feature);
				 beginIndex[0] += t_map.n_pos_lemma_2gram;		break;
		case  7: index = t_map.lemmaPos2gramToIndex(feature);
				 beginIndex[0] += t_map.n_lemma_pos_2gram;		break;
		case  8: index = t_map.lemmaLemma2gramToIndex(feature);
				 beginIndex[0] += t_map.n_lemma_lemma_2gram;	break;
		case  9: index = t_map.posPosPos3gramToIndex(feature);
		         beginIndex[0] += t_map.n_pos_pos_pos_3gram;	break;
		}

		if (!feature.equals(FtrLib.TAG_NULL) && index > 0)	arr.add(begin+index);
	}
	
	/**
	 * Adds punctuation features.
	 * This method is called from {@link DepParser#getFeatureArray()}.
	 */
	private void addPunctuationFeatures(ArrayList<Integer> arr, int leftId, int rightId, int[] beginIndex)
	{
		int index;
		
		index = d_tree.getRightNearestPunctuation(leftId, rightId-1, t_map);
		if (index != -1)	arr.add(beginIndex[0] + index);
		beginIndex[0] += t_map.n_punctuation;
		
		index = d_tree.getLeftNearestPunctuation(rightId, leftId+1, t_map);
		if (index != -1)	arr.add(beginIndex[0] + index);
		beginIndex[0] += t_map.n_punctuation;

		index = d_tree.getRightNearestPunctuation(rightId, d_tree.size()-1, t_map);
		if (index != -1)	arr.add(beginIndex[0] + index);
		beginIndex[0] += t_map.n_punctuation;	// 88.54 -> 88.62
		
	/*	index = d_tree.getLeftNearestPunctuation(leftId, 1, t_map);
		if (index != -1)	arr.add(beginIndex[0] + index);
		beginIndex[0] += t_map.n_punctuation;	// 88.62 -> 87.56(-)*/	
	}
	
	private void addUnlabeledRuleFeatures(ArrayList<Integer> arr, DepNode lambda, DepNode beta, int[] beginIndex)
	{
		String rule  = lambda.pos + FtrLib.TAG_DELIM + beta.pos;
		int    value = t_map.getPosPosDepRule(rule);
		
		if      (value < 0)	arr.add(beginIndex[0]);
		else if (value > 0)	arr.add(beginIndex[0]+1);
		
		beginIndex[0] += 2;
	}
	
	private void addChunkBoundaryFeatures(ArrayList<Integer> arr, DepNode lambda, DepNode beta, int[] beginIndex)
	{
	    JIntIntTuple lTup = lambda.chunkBE;
	    JIntIntTuple bTup = beta  .chunkBE; 
	    
	    if (lTup != null && bTup != null && lTup.int1 == bTup.int1 && lTup.int2 == bTup.int2)
	    {
	        int index = t_map.chunkPosToIndex(lambda.chunkPos);
	        arr.add(beginIndex[0]+index);
	    }
	    else
	        arr.add(beginIndex[0]+t_map.n_chunk_pos);
	    
	    beginIndex[0] += t_map.n_chunk_pos + 1;
	}
		
	/** @return DepNode retrieved from <code>attr</code> */
	protected DepNode getNode(DepFtrToken token)
	{
		int index = (token.token == DepFtrXml.XML_LAMBDA) ? i_lambda : i_beta;
		index    += token.offset;
		
		if (!d_tree.isRange(index) || (token.token == DepFtrXml.XML_LAMBDA && index == i_beta) || (token.token == DepFtrXml.XML_BETA && index == i_lambda))
			return new DepNode();
	
		if      (token.relation == null)						return d_tree.get(index);
		else if (token.relation.equals(DepFtrXml.XML_TOKEN_HD))	return d_tree.getHead(index);
		else if (token.relation.equals(DepFtrXml.XML_TOKEN_LM))	return d_tree.getLeftMostDependent(index);
		else if (token.relation.equals(DepFtrXml.XML_TOKEN_RM))	return d_tree.getRightMostDependent(index);
		
		return null;
	}
}