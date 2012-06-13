package org.chboston.cnlp.ctakes.relationextractor.ae.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;

import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * Features based on wikipedia
 */
public class WikiFeatureExtractor {
	
  public List<Feature> extract(JCas jCas, IdentifiedAnnotation arg1, IdentifiedAnnotation arg2, WikiIndex wikiIndex) {

  	List<Feature> features = new ArrayList<Feature>();
  	
  	try {
  		String text1 = arg1.getCoveredText();
  		String text2 = arg2.getCoveredText();
   		
  		double cosineSimilarity = wikiIndex.getCosineSimilarity(text1, text2);
	    features.add(new Feature("wikisim", cosineSimilarity));
    } catch (ParseException e) {
	    e.printStackTrace();
    } catch (IOException e) {
	    e.printStackTrace();
    }
  	
    return features;
  }

}
