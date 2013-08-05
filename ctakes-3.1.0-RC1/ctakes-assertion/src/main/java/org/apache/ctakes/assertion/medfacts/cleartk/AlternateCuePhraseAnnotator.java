package org.apache.ctakes.assertion.medfacts.cleartk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.temporary.assertion.AssertionCuePhraseAnnotation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

public class AlternateCuePhraseAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_INPUT_DIR = "INPUT_DIR";
	
	@ConfigurationParameter(
			name = PARAM_INPUT_DIR,
			description = "Directory containing cue phrase files",
			mandatory = false
	) private String inputDirName = "org/apache/ctakes/assertion/cue_words";
	
	private HashMap<String,CuePhrase> cueWords = null;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		int[] lenCounts = new int[10];
		
		cueWords = new HashMap<String,CuePhrase>();
		File inputDir = null;
		try{
			inputDir = FileLocator.locateFile(inputDirName);
			File[] processFiles = inputDir.listFiles();
			for (int i = 0; i < processFiles.length; i++) {
				File nextFile = processFiles[i]; //new File(directoryOfDelimitedFiles + "/"
				//					+ processFiles[i]);
				System.out.println("Processing: " + processFiles[i].getName()
						+ "...");

				BufferedReader br = new BufferedReader(new FileReader(
						nextFile));
				String record = "";
				while ((record = br.readLine()) != null) {
					// System.out.println(" record so far out of " + record
					// );

					String splitRecord[] = record.split("\\|");
					if (splitRecord.length == 0)
					{ continue; }
					String cuePhrase = splitRecord[0];
					String cuePhraseCategory = "default_category";
					String cuePhraseFamily = "default_family";
					
					
					if (splitRecord.length >= 2)
					{
						cuePhraseCategory = splitRecord[1];
						cuePhraseFamily   = splitRecord[2];
						if (cuePhraseCategory == null || cuePhraseCategory.isEmpty())
						{
							cuePhraseCategory = "category__" + cuePhraseFamily;
						}
					}
					String[] cueTokens = cuePhrase.split("\\s+");
					if(cueTokens.length < 3){
						cueWords.put(cuePhrase, new CuePhrase(cuePhrase, cuePhraseCategory, cuePhraseFamily));
					}else{
						// TODO build tree for multi-word phrases.
					}
					if(cueTokens.length < lenCounts.length) lenCounts[cueTokens.length]++;
				}
			}
		}catch(IOException e){
			throw new ResourceInitializationException(e);
		}
//		System.out.println("Distribution of cue phrase token lengths:");
//		for(int i = 0; i < lenCounts.length; i++){
//			System.out.printf("%d => %d\n", i, lenCounts[i]);
//		}
	}
	
	@SuppressWarnings("null")
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		ArrayList<BaseToken> tokens = new ArrayList<BaseToken>(JCasUtil.select(jCas, BaseToken.class));
		String lastKey = null;
		BaseToken lastToken = null;
		for(int i = 0; i < tokens.size(); i++){
			BaseToken token = tokens.get(i);
			
			String key = token.getCoveredText().toLowerCase();
			if(cueWords.containsKey(key)){
				addCuePhrase(jCas, key, token.getBegin(), token.getEnd());
			}
			
			if(i > 0){
				String twoKey = lastKey + " " + key;
				if(cueWords.containsKey(twoKey)){
					addCuePhrase(jCas, twoKey, lastToken.getBegin(), token.getEnd());
				}
			}
			
			lastToken = token;
			lastKey = key;
		}
	}

	private void addCuePhrase(JCas jCas, String key, int begin, int end){
		CuePhrase cueWord = cueWords.get(key);
		AssertionCuePhraseAnnotation cuePhraseAnnotation = new AssertionCuePhraseAnnotation(jCas);
		cuePhraseAnnotation.setBegin(begin);
		cuePhraseAnnotation.setEnd(end);

		cuePhraseAnnotation.setCuePhrase(key);

		cuePhraseAnnotation.setCuePhraseCategory(cueWord.category);
		cuePhraseAnnotation.setCuePhraseAssertionFamily(cueWord.family);

		cuePhraseAnnotation.addToIndexes();
		
	}
}

class CuePhrase{
	String phrase=null;
	String category=null;
	String family=null;
	
	public CuePhrase(String phrase, String category, String family){
		this.phrase = phrase;
		this.category = category;
		this.family = family;
	}
	
	@Override
	public int hashCode() {
		return phrase == null? super.hashCode() : phrase.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return phrase == null ? super.equals(obj) : phrase.equals(obj);
	}
}