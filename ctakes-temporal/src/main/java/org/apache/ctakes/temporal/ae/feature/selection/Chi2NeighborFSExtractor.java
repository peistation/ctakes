package org.apache.ctakes.temporal.ae.feature.selection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ctakes.temporal.ae.feature.selection.Chi2NeighborFSExtractor.Chi2Evaluator.ComputeFeatureScore;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.BetweenAnnotationsFeatureExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bounds;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Context;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.TransformableFeature;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;

/**
 * 
 * Selects features via Chi-squared statistics between the features extracted from its
 * sub-extractor and the outcome values they are paired with in classification instances.
 * 
 * @author Chen Lin
 * 
 */
public class Chi2NeighborFSExtractor<OUTCOME_T> extends FeatureSelectionExtractor<OUTCOME_T>
		implements SimpleFeatureExtractor , BetweenAnnotationsFeatureExtractor{
	
			/**
			   * A Bounds implementation that puts no restrictions on the context.
			   */
			  private static class NoBounds implements Bounds {

			    public NoBounds() {
			    }

			    @Override
			    public boolean contains(Annotation annotation) {
			      return true;
			    }

			  }

			/**
			   * A Bounds implementation that restricts the context to annotations within a given span.
			   */
		private static class SpanBounds implements Bounds {

		private int begin;

		private int end;

		public SpanBounds(int begin, int end) {
		    this.begin = begin;
		    this.end = end;
		}

		@Override
		public boolean contains(Annotation annotation) {
		    return annotation.getBegin() >= this.begin && annotation.getEnd() <= this.end;
		}

	}

		/**
		   * Helper class for aggregating and computing mutual Chi2 statistics
		*/
		public static class Chi2Evaluator<OUTCOME_T> {
			 protected Multiset<OUTCOME_T> classCounts;

			 protected Table<String, OUTCOME_T, Integer> featValueClassCount;

			 public Chi2Evaluator() {
			      this.classCounts = HashMultiset.<OUTCOME_T> create();
			      this.featValueClassCount = HashBasedTable.<String, OUTCOME_T, Integer> create();
			 }

			 public void update(String featureName, OUTCOME_T outcome, int occurrences) {
			      Integer count = this.featValueClassCount.get(featureName, outcome);
			      if (count == null) {
			        count = 0;
			      }
			      this.featValueClassCount.put(featureName, outcome, count + occurrences);
			      this.classCounts.add(outcome, occurrences);
			 }

			 public double Chi2Cal(String featureName) {
			      // notation index of 0 means false, 1 mean true
				  //Contingency Table:
				  //    | class1 | class2 | class3 | sum
				  //posi| 		 |        |        | posiFeatCount
				  //nega|        |        |        | negaFeatCount
				  //    | outcnt1| outcnt2| outcnt3| n
				  
				  int numOfClass = this.classCounts.elementSet().size();
			      int[] posiOutcomeCounts = new int[numOfClass];
			      int[] outcomeCounts = new int[numOfClass];
			      int classId = 0;
			      int posiFeatCount = 0;
			      for ( OUTCOME_T clas: this.classCounts.elementSet()){
			    	  posiOutcomeCounts[classId] = this.featValueClassCount.contains(featureName, clas)? 
			    			  this.featValueClassCount.get(featureName, clas)
					          : 0;
			    	  posiFeatCount += posiOutcomeCounts[classId];
			    	  outcomeCounts[classId] = this.classCounts.count(clas);
			    	  classId ++;
			      }
			      
			      int n = this.classCounts.size();
			      int negaFeatCount = n - posiFeatCount;
			      
			      double chi2val = 0.0;
			      
			      if (posiFeatCount == 0 || posiFeatCount == n){ //all instances have same value on this feature, degree of freedom = 0
			    	  return chi2val;			    	  
			      }
			      
			      boolean yates = true;
			      for (int lbl =0; lbl < numOfClass; lbl++){
			    	  	//for positive part of feature:
				    	  double expected = (outcomeCounts[lbl]/(double)n)*(posiFeatCount);
				    	  if (expected > 0){
				    		  double diff = Math.abs(posiOutcomeCounts[lbl]-expected);
				    		  if (yates){ // apply Yate's correction
				    			  diff -= 0.5;
				    		  }
				    		  if (diff>0) chi2val += Math.pow(diff,2)/expected;
				    	  }
				    		  
				    	  //for negative part of feature:
				    	  expected = (outcomeCounts[lbl]/(double)n)*(negaFeatCount);
				    	  double observ = outcomeCounts[lbl]-posiOutcomeCounts[lbl];
				    	  if (expected > 0){
				    		  double diff = Math.abs(observ-expected);
				    		  if (yates){ // apply Yate's correction
				    			  diff -= 0.5;
				    		  }
				    		  if (diff>0) chi2val += Math.pow(diff,2)/expected;
				    	  }
			      }

			      return chi2val;
			    }

			    
			 public void save(URI outputURI) throws IOException {
			      File out = new File(outputURI);
			      BufferedWriter writer = null;
			      writer = new BufferedWriter(new FileWriter(out));

			      // Write out header
			      writer.append("Chi2 FS Neighbor Data\n");
			      writer.append("Feature\t");
			      writer.append(Joiner.on("\t").join(this.featValueClassCount.columnKeySet()));
			      writer.append("\n");

			      // Write out Chi2 values for all features
			      for (String featureName : this.featValueClassCount.rowKeySet()) {
			        writer.append(featureName);
			        writer.append("\t");
			        writer.append(String.format("%f", this.Chi2Cal(featureName)));
			        writer.append("\n");
			      }
			      writer.append("\n");
			      writer.append(this.featValueClassCount.toString());
			      writer.close();
			    }
			 
			 public ComputeFeatureScore<OUTCOME_T> getScoreFunction() {
			      return new ComputeFeatureScore<OUTCOME_T>(this);
			    }

			    public static class ComputeFeatureScore<OUTCOME_T> implements Function<String, Double> {

			      private Chi2Evaluator<OUTCOME_T> stats;

			      public ComputeFeatureScore(Chi2Evaluator<OUTCOME_T> stats) {
			        this.stats = stats;
			      }

			      @Override
			      public Double apply(String featureName) {
			        Double featureChi2 = stats.Chi2Cal(featureName);
			        return featureChi2;
			      }

			    }
	}
			
			
	protected boolean isTrained;
	private CombinedExtractor subExtractor;
	private List<String> selectedFeatures;
	private double chi2Threshold;
	private Chi2Evaluator<OUTCOME_T> chi2Evaluator;
	private Context[] contexts;
	private Class<? extends Annotation> annotationClass;

	public Chi2NeighborFSExtractor(String name, Class<? extends Annotation> annotationClass, CombinedExtractor featureExtractor, Context... contexts) {
		super(name);
		this.annotationClass = annotationClass;
		this.init(featureExtractor, 0.0);
		this.contexts = contexts;
	}
	
	public Chi2NeighborFSExtractor(String name, Class<? extends Annotation> annotationClass, CombinedExtractor featureExtractor, double thres, Context... contexts) {
		super(name);
		this.annotationClass = annotationClass;
		this.init(featureExtractor, thres);
		this.contexts = contexts;
	}

	public Chi2NeighborFSExtractor(String fsNeighborExtractorKey, Float thres) {
		super(fsNeighborExtractorKey);
		this.isTrained=false;
		this.chi2Threshold = thres;
	}

	private void init(CombinedExtractor featureExtractor, double thres) {
		this.subExtractor= featureExtractor;
		this.chi2Threshold = thres;
	}

	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws CleartkExtractorException {
		List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
	    List<Feature> result = new ArrayList<Feature>();
	    if (this.isTrained) {
	      // Filter out selected features
	      result.addAll(Collections2.filter(extracted, this));
	    } else {
	      // We haven't trained this extractor yet, so just mark the existing features
	      // for future modification, by creating one uber-container feature
	      result.add(new TransformableFeature(this.name, extracted));
	    }

	    return result;
	}
	
	public List<Feature> extract(JCas view, Annotation focusAnnotation, Bounds bounds)
		      throws CleartkExtractorException {
		    List<Feature> extracted = new ArrayList<Feature>();
		    for (Context context : this.contexts) {
			      extracted.addAll(context.extract(
			          view,
			          focusAnnotation,
			          bounds,
			          this.annotationClass,
			          this.subExtractor));
			    }
		    List<Feature> result = new ArrayList<Feature>();
		    if (this.isTrained){
		    	// Filter out selected features
			    result.addAll(Collections2.filter(extracted, this));
		    }else{
		    	// We haven't trained this extractor yet, so just mark the existing features
			    // for future modification, by creating one uber-container feature
			    result.add(new TransformableFeature(this.name, extracted));
		    }
		    
		    return result;
		  }

	/**
	  * Extract features from the annotations around the focus annotation and within the given bounds.
	   * 
	   * @param view
	   *          The JCas containing the focus annotation.
	   * @param focusAnnotation
	   *          The annotation whose context is to be searched.
	   * @param boundsAnnotation
	   *          The boundary within which context annotations may be identified.
	   * @return The features extracted in the context of the focus annotation.
	   */
	public List<Feature> extractWithin(
	      JCas view,
	      Annotation focusAnnotation,
	      Annotation boundsAnnotation) throws CleartkExtractorException {
	    Bounds bounds = new SpanBounds(boundsAnnotation.getBegin(), boundsAnnotation.getEnd());
	    return this.extract(view, focusAnnotation, bounds);
	}
	  
	@Override
	public boolean apply(Feature feature) {
		return this.selectedFeatures.contains(this.nameFeature(feature));
	}
	
	public String nameFeature(Feature feature) {
	    return (feature.getValue() instanceof Number) ? feature.getName() : feature.getName() + ":"
	        + feature.getValue();
	  }

	@Override
	public void train(Iterable<Instance<OUTCOME_T>> instances) {
		// aggregate statistics for all features
	    this.chi2Evaluator = new Chi2Evaluator<OUTCOME_T>();

	    for (Instance<OUTCOME_T> instance : instances) {
	      OUTCOME_T outcome = instance.getOutcome();
	      for (Feature feature : instance.getFeatures()) {
	        if (this.isTransformable(feature)) {
	          for (Feature untransformedFeature : ((TransformableFeature) feature).getFeatures()) {
	        	  chi2Evaluator.update(this.nameFeature(untransformedFeature), outcome, 1);
	          }
	        }else{
	        	chi2Evaluator.update(this.nameFeature(feature), outcome, 1);
	        }
	      }
	    }
	    // Compute mutual information score for each feature
	    Set<String> featureNames = chi2Evaluator.featValueClassCount.rowKeySet();

	
		//step3: remove small chi2 valued features
	    Iterator<String> iter = featureNames.iterator();
	    ComputeFeatureScore<OUTCOME_T> computeScore = this.chi2Evaluator.getScoreFunction();
	    while (iter.hasNext()){
	    	String feat = iter.next();
	    	Double chi2 = computeScore.apply(feat);
	    	if(chi2 <= this.chi2Threshold){
	    		iter.remove();
	    	}
	    }
	    
//	    this.selectedFeatures = new ArrayList<String>();
//	    for (String feature : featureNames){
//	    	this.selectedFeatures.add(feature);
//	    }
//	    
	    //step4:get selected features
	    this.selectedFeatures = Ordering.natural().onResultOf(
        this.chi2Evaluator.getScoreFunction()).reverse().immutableSortedCopy(
        featureNames);
	    
//	    Iterator<String> iter = featureNames.iterator();
//	    ComputeFeatureScore<OUTCOME_T> computeScore = this.chi2Evaluator.getScoreFunction();
//	    this.selectedFeatures = new ArrayList<String>();
//	    while (iter.hasNext()){
//	    	String feat = iter.next();
//	    	Double chi2 = computeScore.apply(feat);
//	    	if(chi2 > this.chi2Threshold){
//	    		this.selectedFeatures.add(feat);
//	    	}
//	    }
//		//order the list 
//	    this.selectedFeatures = Ordering.natural().onResultOf(
//	          this.chi2Evaluator.getScoreFunction()).reverse().immutableSortedCopy(
//	        		  this.selectedFeatures);
	    
		this.isTrained = true;
		
	}

	@Override
	public void save(URI uri) throws IOException {
		if (!this.isTrained) {
		      throw new IOException("Chi2FSExtractor: Cannot save before training.");
		}
		File out = new File(uri);
	    BufferedWriter writer = new BufferedWriter(new FileWriter(out));

	    ComputeFeatureScore<OUTCOME_T> computeScore = this.chi2Evaluator.getScoreFunction();
	    for (String feature : this.selectedFeatures) {
	      writer.append(String.format("%s\t%f\n", feature, computeScore.apply(feature)));
	    }

	    writer.close();
	}

	@Override
	public void load(URI uri) throws IOException {
		this.selectedFeatures = Lists.newArrayList();
	    File in = new File(uri);
	    BufferedReader reader = new BufferedReader(new FileReader(in));

	    // The rest of the lines are feature + selection scores
	    String line = null;
	    //int n = 0;
	    while ((line = reader.readLine()) != null ){//&& n < this.numFeatures) {
	      String[] featureValuePair = line.split("\\t");
	      this.selectedFeatures.add(featureValuePair[0]);
	      //n++;
	    }

	    reader.close();
	    this.isTrained = true;
		
	}

	@Override
	public List<Feature> extractBetween(JCas jCas, Annotation annotation1,
			Annotation annotation2) throws CleartkExtractorException {
		int begin = annotation1.getEnd();
	    int end = annotation2.getBegin();
	    // FIXME: creating a new annotation may leak memory - is there a better approach?
	    Annotation focusAnnotation = new Annotation(jCas, begin, end);
	    return this.extract(jCas, focusAnnotation, new NoBounds());
	}

	public Collection<? extends Feature> extract(int[] entityTypeIDs, Map<Integer, List<String>> entityTagsByType, int tokenIndex, int window) {
		List<Feature> extracted = new ArrayList<Feature>();
	    List<Feature> result = new ArrayList<Feature>();
	    for (int typeID : entityTypeIDs) {
            List<String> tokenEntityTags = entityTagsByType.get(typeID);
            int begin = Math.max(tokenIndex - window, 0);
            int end = Math.min(tokenIndex + window, tokenEntityTags.size());
            for (int i = begin; i < end; ++i) {
              String featureName = String.format("EntityTag_%d_%d", typeID, i - begin);
              extracted.add(new Feature(featureName, tokenEntityTags.get(i)));
            }
          }
		if (this.isTrained){
	    	// Filter out selected features
		    result.addAll(Collections2.filter(extracted, this));
	    }else{
	    	// We haven't trained this extractor yet, so just mark the existing features
		    // for future modification, by creating one uber-container feature
		    result.add(new TransformableFeature(this.name, extracted));
	    }
	    
	    return result;
	}

	public Collection<? extends Feature> extract(int nPreviousClassifications,
			int tokenIndex, List<String> outcomes) {
		List<Feature> extracted = new ArrayList<Feature>();
	    List<Feature> result = new ArrayList<Feature>();
		// features from previous classifications
        for (int i = nPreviousClassifications; i > 0; --i) {
          int index = tokenIndex - i;
          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
          extracted.add(new Feature("PreviousOutcome_" + i, previousOutcome));
        }
        
        if (this.isTrained){
	    	// Filter out selected features
		    result.addAll(Collections2.filter(extracted, this));
	    }else{
	    	// We haven't trained this extractor yet, so just mark the existing features
		    // for future modification, by creating one uber-container feature
		    result.add(new TransformableFeature(this.name, extracted));
	    }
	    
	    return result;
	}

}
