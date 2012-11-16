/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ctakes.relationextractor.ae;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Objects;
import com.google.common.io.Files;

import org.apache.ctakes.relationextractor.ae.features.DependencyPathFeaturesExtractor;
import org.apache.ctakes.relationextractor.ae.features.DependencyTreeFeaturesExtractor;
import org.apache.ctakes.relationextractor.ae.features.NamedEntityFeaturesExtractor;
import org.apache.ctakes.relationextractor.ae.features.PartOfSpeechFeaturesExtractor;
import org.apache.ctakes.relationextractor.ae.features.PhraseChunkingExtractor;
import org.apache.ctakes.relationextractor.ae.features.RelationFeaturesExtractor;
import org.apache.ctakes.relationextractor.ae.features.TokenFeaturesExtractor;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

public abstract class RelationExtractorAnnotator extends CleartkAnnotator<String> {

  public static final String NO_RELATION_CATEGORY = "-NONE-";

  public static final String PARAM_GOLD_VIEW_NAME = "GoldViewName";
  
  public static int relationId; // counter for error logging

  @ConfigurationParameter(
      name = PARAM_GOLD_VIEW_NAME,
      mandatory = false,
      description = "view containing the manual relation annotations; needed for training")
  protected String goldViewName;

  public static final String PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE = "ProbabilityOfKeepingANegativeExample";

  @ConfigurationParameter(
      name = PARAM_PROBABILITY_OF_KEEPING_A_NEGATIVE_EXAMPLE,
      mandatory = false,
      description = "probability that a negative example should be retained for training")
  protected double probabilityOfKeepingANegativeExample = 1.0;
  
  public static final String PARAM_PRINT_ERRORS = "PrintErrors";
  
  @ConfigurationParameter(
		 name = PARAM_PRINT_ERRORS,
		 mandatory = false,
		 description = "Print errors true/false",
		 defaultValue = "false")
  boolean printErrors;
  
  public static final String PARAM_ERROR_FILE = "ErrorOutputStream";
  //private static final String DEFAULT_ERROR_OUT = "System.out";
  
  @ConfigurationParameter(
		 name = PARAM_ERROR_FILE,
		 mandatory = false,
		 description = "If PARAM_PRINT_ERRORS is true, this indicates where to write files.  If unspecified, it will output to STDOUT.")
		 //defaultValue = DEFAULT_ERROR_OUT)
  protected File errorFile = null;
  protected PrintStream errorOutStream;
  
  protected Random coin = new Random(0);

  /**
   * The list of feature extractors used by the classifier.
   */
  protected List<RelationFeaturesExtractor> featureExtractors = Arrays.<RelationFeaturesExtractor> asList(
      new TokenFeaturesExtractor(),
      new PartOfSpeechFeaturesExtractor(),
      new PhraseChunkingExtractor(),
      new NamedEntityFeaturesExtractor(),
      new DependencyTreeFeaturesExtractor(),
      new DependencyPathFeaturesExtractor()
      );

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (this.isTraining() && this.goldViewName == null) {
      throw new IllegalArgumentException(PARAM_GOLD_VIEW_NAME + " must be defined during training");
    }
    relationId = 0;
    
    try {
    	if (errorFile == null) {
    		this.errorOutStream = System.out;
    	} else {
    		this.errorOutStream = new PrintStream(Files.newOutputStreamSupplier(errorFile).getOutput());
    	}
	} catch (IOException e) {
		throw new ResourceInitializationException(e);
	}
    
  }
  
  
 
  /**
   * Selects the relevant mentions/annotations within a sentence for relation identification/extraction
   */
  public abstract List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(JCas identifiedAnnotationView, Sentence sentence);

  /*
   * Implement the standard UIMA process method.
   */
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // during training, pull entity and relation annotations from the manual annotation view
    
  	// map argument spans to the category of the relation between them
  	HashMap<HashableArguments, String> categoryLookup = new HashMap<HashableArguments, String>();
  	
  	// get gold standard relation instances during testing for error analysis
  	if (! this.isTraining() && printErrors) {
  		JCas goldView;
  		try {
  			goldView = jCas.getView("GoldView");
  		} catch(CASException e) {
  			throw new AnalysisEngineProcessException(e);
  		}
  		
  		categoryLookup = createCategoryLookup(goldView); 
  	}
  	
  	JCas identifiedAnnotationView, relationView;
    if (this.isTraining()) {
      try {
        identifiedAnnotationView = relationView = jCas.getView(this.goldViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    } else {
      identifiedAnnotationView = relationView = jCas;
    }

    // lookup from pair of annotations to binary text relation
    // note: assumes that there will be at most one relation per pair
    Map<List<Annotation>, BinaryTextRelation> relationLookup;
    relationLookup = new HashMap<List<Annotation>, BinaryTextRelation>();
    if (this.isTraining()) {
    	relationLookup = createRelationLookup(relationView);
    }

    // walk through each sentence in the text
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

    	// collect all relevant relation arguments from the sentence
    	List<IdentifiedAnnotationPair> candidatePairs = this.getCandidateRelationArgumentPairs(identifiedAnnotationView, sentence);

    	// walk through the pairs of annotations
    	for (IdentifiedAnnotationPair pair : candidatePairs) {
    		IdentifiedAnnotation arg1 = pair.getArg1();
    		IdentifiedAnnotation arg2 = pair.getArg2();
    		// apply all the feature extractors to extract the list of features
    		List<Feature> features = new ArrayList<Feature>();
    		for (RelationFeaturesExtractor extractor : this.featureExtractors) {
    			features.addAll(extractor.extract(jCas, arg1, arg2));
    		}

    		// sanity check on feature values
    		for (Feature feature : features) {
    			if (feature.getValue() == null) {
    				String message = "Null value found in %s from %s";
    				throw new IllegalArgumentException(String.format(message, feature, features));
    			}
    		}

    		// during training, feed the features to the data writer
    		if (this.isTraining()) {
    			String category = this.getRelationCategory(relationLookup, arg1, arg2);
    			if (category == null) { continue; }
    			
    			// create a classification instance and write it to the training data
    			this.dataWriter.write(new Instance<String>(category, features));
    		}

    		// during classification feed the features to the classifier and create annotations
    		else {
    			String predictedCategory = this.classifier.classify(features);

    			if(printErrors) {
    				String goldCategory; // gold standard relation category
    				if (categoryLookup.containsKey(new HashableArguments(arg1, arg2))) {
    					goldCategory = categoryLookup.get(new HashableArguments(arg1, arg2));
    				} else {
    					goldCategory = NO_RELATION_CATEGORY;
    				}

    				logResults(sentence, arg1, arg2, features, predictedCategory, goldCategory);
    			}
    			
    			// add a relation annotation if a true relation was predicted
    			if (!predictedCategory.equals(NO_RELATION_CATEGORY)) {

    				// if we predict an inverted relation, reverse the order of the arguments
    				if (predictedCategory.endsWith("-1")) {
    					predictedCategory = predictedCategory.substring(0, predictedCategory.length() - 2);
    					IdentifiedAnnotation temp = arg1;
    					arg1 = arg2;
    					arg2 = temp;
    				}

    				// add the relation to the CAS
    				RelationArgument relArg1 = new RelationArgument(relationView);
    				relArg1.setArgument(arg1);
    				relArg1.setRole("Argument");
    				relArg1.addToIndexes();
    				RelationArgument relArg2 = new RelationArgument(relationView);
    				relArg2.setArgument(arg2);
    				relArg2.setRole("Related_to");
    				relArg2.addToIndexes();
    				BinaryTextRelation relation = new BinaryTextRelation(relationView);
    				relation.setArg1(relArg1);
    				relation.setArg2(relArg2);
    				relation.setCategory(predictedCategory);
    				relation.addToIndexes();
    			}
    		}
    	} // end pair in pairs
    } // end for(Sentence)
  }
  
  
  /**
   * Looks up the arguments in the specified lookup table and converts the relation
   * into a label for classification
   * 
   * @return If this category should not be processed for training return <i>null</i>
   *         otherwise it returns the label sent to the datawriter
   */
  protected abstract String getRelationCategory(Map<List<Annotation>, BinaryTextRelation> relationLookup,
		  IdentifiedAnnotation arg1, IdentifiedAnnotation arg2);

  private void logResults(Sentence sentence, IdentifiedAnnotation arg1,
		  IdentifiedAnnotation arg2, List<Feature> features, String predictedCategory,
		  String goldCategory) {
	  if (printErrors && !predictedCategory.equals(goldCategory)) {
		  errorOutStream.format("%-15s%d\n", "instance:", relationId++);
		  errorOutStream.format("%-15s%s\n", "prediction:", predictedCategory);
		  errorOutStream.format("%-15s%s\n", "gold label:", goldCategory);
		  errorOutStream.format("%-15s%s\n", "arg1:", arg1.getCoveredText());
		  errorOutStream.format("%-15s%s\n", "arg2:", arg2.getCoveredText());
		  errorOutStream.format("%-15s%s\n", "sentence:", sentence.getCoveredText());
		  errorOutStream.format("\n%s\n\n", features);
		  errorOutStream.println();
	  }
  }

  /**
   * Creates a lookup map between lists of arguments and their relation
   * This map does not key in simply on a HashableArgument because 
   * @param relationView
   * @return
   */
  private static Map<List<Annotation>, BinaryTextRelation> createRelationLookup(
		  JCas relationView) {
	  Map<List<Annotation>, BinaryTextRelation> relationLookup;
	  relationLookup = new HashMap<List<Annotation>, BinaryTextRelation>();
	  for (BinaryTextRelation relation : JCasUtil.select(relationView, BinaryTextRelation.class)) {
		  Annotation arg1, arg2;
		  if (relation.getArg1().getRole().equals("Argument")) {
			  arg1 = relation.getArg1().getArgument();
			  arg2 = relation.getArg2().getArgument();
		  } else {
			  arg2 = relation.getArg1().getArgument();
			  arg1 = relation.getArg2().getArgument();
		  }
		  // The key is a list of args so we can do bi-directional lookup
		  relationLookup.put(Arrays.asList(arg1, arg2), relation);
	  }
	  return relationLookup;
  }

  /**
   * Creates a lookup map between relations and their categories
   * This is used for error analysis
   * @param goldView
   * @return
   */
  private static HashMap<HashableArguments, String> createCategoryLookup(JCas goldView) {
	  // save gold relations for lookup during error analysis; normalize order of arguments
	  HashMap<HashableArguments, String> categoryLookup = new HashMap<HashableArguments, String>();
	  for (BinaryTextRelation relation : JCasUtil.select(goldView, BinaryTextRelation.class)) {
		  // arguments must be in the correct order to be found during lookup
		  categoryLookup.put(new HashableArguments(relation), relation.getCategory());
	  }
	  return categoryLookup;
  }
  
  
  public static class IdentifiedAnnotationPair {
	  
	 private final IdentifiedAnnotation arg1;
	 private final IdentifiedAnnotation arg2;
	 public IdentifiedAnnotationPair(IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {
		 this.arg1 = arg1;
		 this.arg2 = arg2;
	 }
	 
	 public final IdentifiedAnnotation getArg1() { return arg1; }
		 
	 public final IdentifiedAnnotation getArg2() { return arg2; }
  }
	  
  
  /**
   * This class is useful for mapping the spans of relation arguments to the relation's category.
   */
  public static class HashableArguments {

    protected int arg1begin;
    protected int arg1end;
    protected int arg2begin;
    protected int arg2end;

    public HashableArguments(int arg1begin, int arg1end, int arg2begin, int arg2end) {
      this.arg1begin = arg1begin;
      this.arg1end = arg1end;
      this.arg2begin = arg2begin;
      this.arg2end = arg2end;
    }

    public HashableArguments(Annotation arg1, Annotation arg2) {
      this(arg1.getBegin(), arg1.getEnd(), arg2.getBegin(), arg2.getEnd());
    }

    public HashableArguments(BinaryTextRelation relation) {
      this(getArg1(relation), getArg2(relation));
    }

    // HACK: arg1 is not always arg1 because of bugs in the reader
    private static Annotation getArg1(BinaryTextRelation rel) {
      RelationArgument arg1 = rel.getArg1();
      return arg1.getRole().equals("Argument") ? arg1.getArgument() : rel.getArg2().getArgument();
    }

    // HACK: arg2 is not always arg2 because of bugs in the reader
    private static Annotation getArg2(BinaryTextRelation rel) {
      RelationArgument arg2 = rel.getArg2();
      return arg2.getRole().equals("Related_to") ? arg2.getArgument() : rel.getArg1().getArgument();
    }

	@Override
	public boolean equals(Object otherObject) {
	  boolean result = false;
		if (otherObject instanceof HashableArguments) {
			HashableArguments other = (HashableArguments) otherObject;
			result = (this.getClass() == other.getClass() && 
					this.arg1begin == other.arg1begin && 
					this.arg1end == other.arg1end && 
					this.arg2begin == other.arg2begin && 
					this.arg2end == other.arg2end);
		}
		return result;
	}
	
    @Override
    public int hashCode() {
      return Objects.hashCode(
          this.arg1begin,
          this.arg1end,
          this.arg2begin,
          this.arg2end);
    }
    
	@Override
	public String toString() {
		return String.format(
				"%s(%s,%s,%s,%s)",
				this.getClass().getSimpleName(),
				this.arg1begin,
				this.arg1end,
				this.arg2begin,
				this.arg2end);
	}
  }
}
