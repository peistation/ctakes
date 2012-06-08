package org.chboston.cnlp.ctakes.relationextractor.ae.wiki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.tartarus.martin.Stemmer;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

/**
 * A wrapper for a wikipedia lucene index.
 * 
 * @author dmitriy dligach
 *
 */
public class WikiIndex {

	public static int defaultMaxHits = 10;
	public static String defaultIndexPath = "/home/dima/mnt/pub/resources/wikipedia/index_nometa";
	public static String defaultSearchField = "text";
	
	private int maxHits;
	private String indexPath;
	private String searchField;
	
	private IndexReader indexReader;
  private IndexSearcher indexSearcher;
  private Analyzer standardAnalyzer;
  private QueryParser queryParser;
  private DefaultSimilarity defaultSimilarity;
  
  public WikiIndex(int maxHits, String indexPath, String searchField) {
  	this.maxHits = maxHits;
  	this.indexPath = indexPath;
  	this.searchField = searchField;
  }
  
  public WikiIndex() {
  	maxHits = defaultMaxHits;
  	indexPath = defaultIndexPath;
  	searchField = defaultSearchField;
  }
  
  public void initialize() throws CorruptIndexException, IOException {

  	indexReader = IndexReader.open(FSDirectory.open(new File(indexPath)));
  	indexSearcher = new IndexSearcher(indexReader);
  	standardAnalyzer = new StandardAnalyzer(Version.LUCENE_35);
  	queryParser = new QueryParser(Version.LUCENE_35, searchField, standardAnalyzer);
  	defaultSimilarity = new DefaultSimilarity();
  }
  
  /**
   * Search the index. Return a list of article titles and their scores.
   */
  public ArrayList<SearchResult> search(String queryText) throws ParseException, IOException {

  	ArrayList<SearchResult> articleTitles = new ArrayList<SearchResult>();
  	
  	String escaped = QueryParser.escape(queryText);
  	Query query = queryParser.parse(escaped);
  	
  	ScoreDoc[] scoreDocs = indexSearcher.search(query, null, maxHits).scoreDocs;
  	for(ScoreDoc scoreDoc : scoreDocs) {
  		ScoreDoc redirectScoreDoc = handlePossibleRedirect(scoreDoc);
  		Document doc = indexSearcher.doc(redirectScoreDoc.doc);
  		articleTitles.add(new SearchResult(doc.get("title"), redirectScoreDoc.score));
  	}
  	
  	return articleTitles;
  }
  
  /**
   * Send two queries to the index.
   * For each query, form a tfidf vector that represents N top matching documents.
   * Return cosine similarity between the two tfidf vectors.
   */
  public double getCosineSimilarity(String queryText1, String queryText2) throws ParseException, IOException {

  	String escaped1 = QueryParser.escape(queryText1);
  	Query query1 = queryParser.parse(escaped1);
  	ScoreDoc[] scoreDocs1 = indexSearcher.search(query1, null, maxHits).scoreDocs;
  	if(scoreDocs1.length == 0) {
  		return 0;
  	}

  	ArrayList<TermFreqVector> termFreqVectors1 = new ArrayList<TermFreqVector>();
  	for(ScoreDoc scoreDoc : scoreDocs1) {
  		ScoreDoc redirectScoreDoc = handlePossibleRedirect(scoreDoc);
  		TermFreqVector termFreqVector = indexReader.getTermFreqVector(redirectScoreDoc.doc, "text");
  		termFreqVectors1.add(termFreqVector);
  	}
  	HashMap<String, Double> vector1 = makeTfIdfVector(termFreqVectors1);
  	if(vector1.size() == 0) {
  		return 0; // e.g. redirects to a non-existent page
  	}
  	
  	String escaped2 = QueryParser.escape(queryText2);
  	Query query2 = queryParser.parse(escaped2);
  	ScoreDoc[] scoreDocs2 = indexSearcher.search(query2, null, maxHits).scoreDocs;
  	if(scoreDocs2.length == 0) {
  		return 0;
  	}
  	
  	ArrayList<TermFreqVector> termFreqVectors2 = new ArrayList<TermFreqVector>();
  	for(ScoreDoc scoreDoc : scoreDocs2) {
  		ScoreDoc redirectScoreDoc = handlePossibleRedirect(scoreDoc);
  		TermFreqVector termFreqVector = indexReader.getTermFreqVector(redirectScoreDoc.doc, "text");
  		termFreqVectors2.add(termFreqVector);
  	}
  	HashMap<String, Double> vector2 = makeTfIdfVector(termFreqVectors2);
  	if(vector2.size() == 0) {
  		return 0; // e.g. redirects to a non-existent page
  	}
  	
  	double dotProduct = computeDotProduct(vector1, vector2);
  	double norm1 = computeEuclideanNorm(vector1);
  	double norm2 = computeEuclideanNorm(vector2);
  	
  	return dotProduct / (norm1 * norm2);
  }

  /**
   * Form a tfidf vector for the set of pages matching each query. 
   * Return the terms that are common to the two sets.
   */
  public ArrayList<String> getCommmonTerms(String queryText1, String queryText2) throws ParseException, IOException {

  	String escaped1 = QueryParser.escape(queryText1);
  	Query query1 = queryParser.parse(escaped1);
  	ScoreDoc[] scoreDocs1 = indexSearcher.search(query1, null, maxHits).scoreDocs;
  	
  	ArrayList<TermFreqVector> termFreqVectors1 = new ArrayList<TermFreqVector>();
  	for(ScoreDoc scoreDoc : scoreDocs1) {
  		ScoreDoc redirectScoreDoc = handlePossibleRedirect(scoreDoc);
  		termFreqVectors1.add(indexReader.getTermFreqVector(redirectScoreDoc.doc, "text"));
  	}
  	HashMap<String, Double> vector1 = makeTfIdfVector(termFreqVectors1);
  	
  	String escaped2 = QueryParser.escape(queryText2);
  	Query query2 = queryParser.parse(escaped2);
  	ScoreDoc[] scoreDocs2 = indexSearcher.search(query2, null, maxHits).scoreDocs;
  	
  	ArrayList<TermFreqVector> termFreqVectors2 = new ArrayList<TermFreqVector>();
  	for(ScoreDoc scoreDoc : scoreDocs2) {
  		ScoreDoc redirectScoreDoc = handlePossibleRedirect(scoreDoc);
  		termFreqVectors2.add(indexReader.getTermFreqVector(redirectScoreDoc.doc, "text"));
  	}
  	HashMap<String, Double> vector2 = makeTfIdfVector(termFreqVectors2);
  	
  	
  	HashMap<String, Double> sum = addVectors(vector1, vector2);
  	
    Function<String, Double> getValue = Functions.forMap(sum);
    ArrayList<String> keys = new ArrayList<String>(sum.keySet());
  	Collections.sort(keys, Ordering.natural().reverse().onResultOf(getValue)); 
  	
  	return removeStringsFromList(queryText1, queryText2, keys);
  }

  /**
   * Take a list of strings and remove all occurences of two string arguments from it. Use stemming.
   */
  private static ArrayList<String> removeStringsFromList(String s1, String s2, ArrayList<String> list) {
  	
  	String stem1 = getStem(s1);
  	String stem2 = getStem(s2);
  	
  	ArrayList<String> result = new ArrayList<String>();
  	
  	for(String s : list) {
  		String stem = getStem(s);
  		if(stem.equals(stem1) || stem.equals(stem2)) {
  			continue;
  		}
  		result.add(s);
  	}
  	
  	return result;
  }
  
  /**
   * Stem a word using Porter stemmer
   */
  private static String getStem(String word) {
  	
		Stemmer stemmer = new Stemmer();
		stemmer.add(word.toCharArray(), word.length());
		stemmer.stem();
		
		return stemmer.toString();
  }
  
  
  /**
   * Return the document to which the input document redirects. 
   * Return the same document if there is no redirect for the input document.
   */
  private ScoreDoc handlePossibleRedirect(ScoreDoc scoreDoc) throws ParseException, CorruptIndexException, IOException  {
  
  	Document doc = indexSearcher.doc(scoreDoc.doc);
  	String redirectTitle = doc.get("redirect"); 
  	
  	// check if there is a redirect
  	if(redirectTitle == null) {
  		return scoreDoc; 
  	} else {
			QueryParser redirectQueryParser = new QueryParser(Version.LUCENE_35, "title", standardAnalyzer);
			
			String redirectTitleNoUnderscores = redirectTitle.replaceAll("_", " ");
			String redirectTitleQuoted = '"' + redirectTitleNoUnderscores + '"';
			String redirectTitleEscaped = QueryParser.escape(redirectTitleQuoted);
			Query redirectQuery  = redirectQueryParser.parse(redirectTitleEscaped);
			
			ScoreDoc[] redirectScoreDocs = indexSearcher.search(redirectQuery, null, 1).scoreDocs; 
			if(redirectScoreDocs.length < 1) {
				System.out.println("failed redirect: " + redirectQuery);
				return scoreDoc; // redirect query did not return any results
			}
			ScoreDoc redirectScoreDoc = redirectScoreDocs[0];
			
			return redirectScoreDoc;
  	}
  }
  
  /**
   * Return a hash table that maps terms to their tfidf values.
   * The input is a list of TermFreqVector objects. The return
   * value is formed by summing up individual tfidf vectors.
   */
  private HashMap<String, Double> makeTfIdfVector(ArrayList<TermFreqVector> termFreqVectors) throws IOException {

  	// map terms to their tfidf values
  	HashMap<String, Double> tfIdfVector = new HashMap<String, Double>(); 

  	for(TermFreqVector termFreqVector : termFreqVectors) {
  		if(termFreqVector == null) {
  			continue; // some documents are empty
  		}
  		
  		String[] terms = termFreqVector.getTerms();
  		int[] freqs = termFreqVector.getTermFrequencies();

  		for(int i = 0; i < terms.length; i++) {
  			double tf = defaultSimilarity.tf(freqs[i]);
  			double idf = defaultSimilarity.idf(indexReader.docFreq(new Term("text", terms[i])), indexReader.numDocs());
  			
  			if(tfIdfVector.containsKey(terms[i])) {
  				tfIdfVector.put(terms[i], tfIdfVector.get(terms[i]) + tf * idf);
  			}
  			else {
  				tfIdfVector.put(terms[i], tf * idf);
  			}
  		}
  	}
  	
  	return tfIdfVector;
  }
  
  private double computeEuclideanNorm(HashMap<String, Double> tfIdfVector) {
  	
  	double sumOfSquares = 0;
  	
  	for(double tfidf : tfIdfVector.values()) {
  		sumOfSquares = sumOfSquares + tfidf*tfidf;  //Math.pow(tfidf, 2);
  	}
  	
  	return Math.sqrt(sumOfSquares);
  }
  
  private double computeDotProduct(HashMap<String, Double> vector1, HashMap<String, Double> vector2) {
  	
  	double dotProduct = 0;
  	
  	for(String term : vector1.keySet()) {
  		if(vector2.containsKey(term)) {
  			dotProduct = dotProduct + vector1.get(term) * vector2.get(term);
  		}
  	}
  	
  	return dotProduct;
  }

  private HashMap<String, Double> addVectors(HashMap<String, Double> vector1, HashMap<String, Double> vector2) {
  	
  	HashMap<String, Double> sum = new HashMap<String, Double>();
  	
  	for(String term : vector1.keySet()) {
  		if(vector2.containsKey(term)) {
  			sum.put(term, vector1.get(term) + vector2.get(term));
  		}
  	}
  	
  	return sum;
  }
  
  public void close() throws IOException {
  	
  	indexReader.close();
  	indexSearcher.close();
  	standardAnalyzer.close();
  }
}

