package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class UncertaintyWordWindowExtractor implements SimpleFeatureExtractor {

	private HashMap<String,Double> termVals = null;
	private static final Pattern linePatt = Pattern.compile("^([^ ]+) : (.+)$");
	private static final int WINDOW_SIZE = 5;
	
	public UncertaintyWordWindowExtractor() throws ResourceInitializationException {
		termVals = new HashMap<String,Double>();
		try{
			File termFile = FileLocator.locateFile("org/apache/ctakes/assertion/models/uncertainty.txt");
			Scanner scanner = new Scanner(termFile);
			Matcher m = null;
			double max = 0.0;
			double maxNeg = 0.0;
			while(scanner.hasNextLine()){
				String line = scanner.nextLine().trim();
				m = linePatt.matcher(line);
				if(m.matches()){
					double val = Double.parseDouble(m.group(2));
					termVals.put(m.group(1), val);
					if(Math.abs(val) > max){
						max = Math.abs(val);
					}
					if(val < maxNeg){
						maxNeg = val;
					}
				}
			}
			max = max - maxNeg;
			for(String key : termVals.keySet()){
				termVals.put(key, (termVals.get(key)-maxNeg) / max);
			}
		}catch(IOException e){
			throw new ResourceInitializationException();
		}
	}
	
	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws CleartkExtractorException {
		ArrayList<Feature> feats = new ArrayList<Feature>();
		List<BaseToken> precedingTokens = JCasUtil.selectPreceding(view, BaseToken.class, focusAnnotation, WINDOW_SIZE);
		List<BaseToken> followingTokens = JCasUtil.selectFollowing(view, BaseToken.class, focusAnnotation, WINDOW_SIZE);
		
		double score = 0.0;
		String key = null;
		int ctxSize = 0;
		for(int i = 0; i < precedingTokens.size(); i++){
			key = precedingTokens.get(i).getCoveredText().toLowerCase();
			if(termVals.containsKey(key)){
				score += termVals.get(key);
			}
			ctxSize++;
		}
		for(int i = 0; i < followingTokens.size(); i++){
			key = followingTokens.get(i).getCoveredText().toLowerCase();
			if(termVals.containsKey(key)){
				score += termVals.get(key);
			}
			ctxSize++;
		}
		score /= ctxSize;  // weight by actual amount of context so we don't penalize begin/end of sentence.
		feats.add(new Feature("WORD_SCORE", score));
		return feats;
	}
}
