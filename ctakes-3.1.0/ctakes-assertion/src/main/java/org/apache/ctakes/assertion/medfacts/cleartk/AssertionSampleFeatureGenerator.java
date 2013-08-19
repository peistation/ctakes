package org.apache.ctakes.assertion.medfacts.cleartk;

import java.util.Arrays;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.util.cr.XReader;
import org.uimafit.factory.CollectionReaderFactory;

public class AssertionSampleFeatureGenerator
{
	public AssertionSampleFeatureGenerator()
	{
	}
	
	public void main(String args[]) throws ResourceInitializationException
	{
		AssertionSampleFeatureGenerator generator =
			new AssertionSampleFeatureGenerator();
		
		generator.execute();
	}

	public void execute() throws ResourceInitializationException
	{
		String filename = "/work/medfacts/sharp/data/2012-10-16_full_data_set_updated/Seed_Corpus/clean_dirs/splits/official/train";
		
	    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
		        XReader.class,
		        XReader.PARAM_ROOT_FILE,
		        filename,
		        XReader.PARAM_XML_SCHEME,
		        XReader.XMI);
	    
		    
		
//	    entityFeatureExtractors = Arrays.asList(
//	            new CoveredTextExtractor(),
//	            //new TypePathExtractor(IdentifiedAnnotation.class, "stem"),
//	            new ProliferatingExtractor(
//	                new SpannedTextExtractor(),
//	                new LowerCaseProliferator(),    
//	                new CapitalTypeProliferator(),
//	                new NumericTypeProliferator(),
//	                new CharacterNGramProliferator(fromRight, 0, 2),
//	                new CharacterNGramProliferator(fromRight, 0, 3)));
		
	}

}
