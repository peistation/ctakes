package org.apache.ctakes.temporal.ae.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;

public class SurfaceFormFeatureExtractor implements SimpleFeatureExtractor {

	private final String SYMBOL = "Symbol";
	private final String SYMBOL_REG = "\\W+";
	private final String ALL_CAPITAL = "AllCapital";
	private final String ALL_CAPITAL_REG = "[A-Z][A-Z]+";
	private final String FIRST_CAPITAL = "FirstCapital";
	private final String FIRST_CAPITAL_REG = "^[A-Z][a-z]+";
	private final String SINGLE_CAPITAL = "SingelCapital";
	private final String SINGLE_CAPITAL_REG = "^[A-Z]{1}$";
	private final String SINGLE_LETTER ="SingleLetter";
	private final String SINGLE_LETTER_REG = "^[a-z]{1}$";
	private final String ALL_LOWER = "AllLower";
	private final String ALL_LOWER_REG = "[a-z][a-z]+";
	private final String NUMBER = "Number";
	private final String NUMBER_REG ="[\\d]*\\.?[\\d]+";
	private final String WORDNUMMIX ="WordNumberMix";
	private final String WORDNUMMIX_REG ="[\\w][\\w]+";
	private final String FEATURE_SURF = "Surface";
	private final String FEATURE_LENGTH = "Length";
	
	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws CleartkExtractorException {
		ArrayList<Feature> features = new ArrayList<Feature>();
		String jCasText = view.getDocumentText();
	    int begin = focusAnnotation.getBegin();
	    int end = focusAnnotation.getEnd();
	    String text = jCasText == null ? null : jCasText.substring(begin, end);
	    features.add(new Feature(this.FEATURE_SURF, getStrType(text)));
	    int length = text == null ? 0 : text.length();
	    if (length <=1) features.add(new Feature(this.FEATURE_LENGTH, "single"));
	    else features.add(new Feature(this.FEATURE_LENGTH, "multiple"));

	    // create a single feature from the text
	    return features;
	}
	
	public static void main(String[] args) throws Exception {
		SurfaceFormFeatureExtractor se = new SurfaceFormFeatureExtractor();
		String test = "a";
		System.out.println("String type is :" + se.getStrType(test));
	}

	private String getStrType(String test) {
		if ( test.matches(this.ALL_CAPITAL_REG)) return this.ALL_CAPITAL;
		else if ( test.matches(ALL_LOWER_REG)) return this.ALL_LOWER;
		else if ( test.matches(FIRST_CAPITAL_REG)) return this.FIRST_CAPITAL;
		else if ( test.matches(NUMBER_REG)) return this.NUMBER;
		else if ( test.matches(SINGLE_CAPITAL_REG)) return this.SINGLE_CAPITAL;
		else if ( test.matches(SINGLE_LETTER_REG)) return this.SINGLE_LETTER;
		else if ( test.matches(SYMBOL_REG)) return this.SYMBOL;
		else if ( test.matches(WORDNUMMIX_REG)) return this.WORDNUMMIX;
		else return "Nomatch";
	}

}
