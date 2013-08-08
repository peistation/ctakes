package org.apache.ctakes.utils.wiki;

import org.apache.lucene.search.similarities.DefaultSimilarity;


public class ApproximateSimilarity extends DefaultSimilarity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	public final float idf(long docFreq, long numDocs){
		return (float)ApproximateMath.alog(numDocs / (docFreq+1.0))+1; 
	}
	
	@Override
	/* according to lucene javadocs, DefaultSimilarity.tf is simply a square root -- we can approximate with an
	 * array of pre-calculated square roots to save time
	 */
	public final float tf(int termFreq){
		return (float) ApproximateMath.asqrt(termFreq);
	}
}
