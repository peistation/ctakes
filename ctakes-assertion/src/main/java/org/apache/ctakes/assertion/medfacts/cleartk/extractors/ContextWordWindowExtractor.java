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
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class ContextWordWindowExtractor implements SimpleFeatureExtractor {

	private HashMap<String,Double> termVals = null;
	private static final Pattern linePatt = Pattern.compile("^([^ ]+) : (.+)$");
	private static double[] weights = new double[50];
	static{
		weights[0] = 1.0;
		for(int i = 1; i < weights.length; i++){
			weights[i] = 1.0 / i;
		}
	}
	
	public ContextWordWindowExtractor(String resourceFilename) throws ResourceInitializationException {
		termVals = new HashMap<String,Double>();
		try{
			File termFile = FileLocator.locateFile(resourceFilename);
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
	public List<Feature> extract(JCas view, Annotation mention)
			throws CleartkExtractorException {
		ArrayList<Feature> feats = new ArrayList<Feature>();
		List<Sentence> sents = JCasUtil.selectCovering(view, Sentence.class, mention.getBegin(), mention.getEnd());
		if(sents.size() == 0) return feats;
		Sentence sent = sents.get(0);
		List<BaseToken> tokens = JCasUtil.selectCovered(BaseToken.class, sent);
		int startIndex = -1;
		int endIndex = -1;
		
		for(int i = 0; i < tokens.size(); i++){
			if(tokens.get(i).getBegin() == mention.getBegin()){
				startIndex = i;
			}
			if(tokens.get(i).getEnd() == mention.getEnd()){
				endIndex = i;
			}
		}
		
		double score = 0.0;
		double z = 0.0;
		String key = null;
		double weight;
		for(int i = 0; i < tokens.size(); i++){
			key = tokens.get(i).getCoveredText().toLowerCase();
			int dist = Math.min(Math.abs(startIndex - i), Math.abs(endIndex-i));
			weight = weightFunction(dist);
			z += weight;
			if(termVals.containsKey(key)){
				score += (weight * termVals.get(key));
			}
		}

		score /= z;  // weight by actual amount of context so we don't penalize begin/end of sentence.
		feats.add(new Feature("WORD_SCORE", score));
		return feats;
	}
	
	private static final double  weightFunction(int dist){
		if(dist >= weights.length) return 0.0;
		
		// quick decay
//		return 1.0 / dist;
		
		// linear decay
//		return 1.0 - dist * (1.0/50.0);
		
		// no decay:
		return 1.0;
	}
}
