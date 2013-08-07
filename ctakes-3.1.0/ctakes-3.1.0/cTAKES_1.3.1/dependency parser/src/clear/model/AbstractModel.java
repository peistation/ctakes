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
package clear.model;

import java.util.ArrayList;

import clear.util.tuple.JIntDoubleTuple;


/**
 * Abstract mdoel.
 * @author Jinho D. Choi
 * <br><b>Last update:</b> 7/1/2010
 */
public abstract class AbstractModel
{
	/** Total number of labels. */
	protected int      n_labels;
	/** Total number of features. */
	protected int      n_features;
	/** List of labels */
	protected int[]    i_labels;
	/** Dense weight vectors for all labels */
	protected float[]  d_weights;
//	protected doulbe[] d_weights;
	
	/**
	 * Returns a tuple of predicated <label, score> for a feature vector <code>x</code>.
	 * The score is scaled between 0 and 1.
	 * @param x feature vector
	 */
	abstract public JIntDoubleTuple predict(ArrayList<Integer> x);
	
	/**
	 * Returns a sorted list of predicated <label, score> for a feature vector <code>x</code>.
	 * The list is sorted in descending order with respect to the scores.
	 * The score is scaled between 0 and 1.
	 * @param x feature vector
	 */
	abstract public ArrayList<JIntDoubleTuple> predictAll(ArrayList<Integer> x);
}
