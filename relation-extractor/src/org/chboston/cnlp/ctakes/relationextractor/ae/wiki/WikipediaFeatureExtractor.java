package org.chboston.cnlp.ctakes.relationextractor.ae.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Features that capture punctuation marks between the two arguments.
 */
public class WikipediaFeatureExtractor {

  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2, WikipediaIndex wikipediaIndex) {

  	List<Feature> features = new ArrayList<Feature>();
  	
  	try {
	    features.add(new Feature("wikisim", wikipediaIndex.getCosineSimilarityNHits(arg1.getCoveredText(), arg2.getCoveredText())));
    } catch (ParseException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    }
  	
    return features;
  }

}
