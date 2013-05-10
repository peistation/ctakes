package org.apache.ctakes.temporal.ae;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import static org.cleartk.classifier.feature.extractor.CleartkExtractor.*;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor.PatternType;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class ConstituencyBasedTimeAnnotator extends
TemporalEntityAnnotator_ImplBase {

  private static final String NON_MENTION = "NON_TIME_MENTION";
  private static final String MENTION = "TIME_MENTION";


  public static AnalysisEngineDescription createDataWriterDescription(
      Class<? extends DataWriter<String>> dataWriterClass,
          File outputDirectory) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ConstituencyBasedTimeAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        dataWriterClass,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory);
  }

  public static AnalysisEngineDescription createAnnotatorDescription(File modelDirectory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ConstituencyBasedTimeAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(modelDirectory, "model.jar"));
  }

  protected List<SimpleFeatureExtractor> featureExtractors;

  @Override
  public void initialize(UimaContext context)
      throws ResourceInitializationException {
    super.initialize(context);

    CombinedExtractor allExtractors = new CombinedExtractor(
        new CoveredTextExtractor(),
        new CharacterCategoryPatternExtractor(PatternType.REPEATS_MERGED),
        new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
        new TypePathExtractor(BaseToken.class, "partOfSpeech"));

    featureExtractors = new ArrayList<SimpleFeatureExtractor>();
//    featureExtractors.add(new CleartkExtractor(BaseToken.class, new CoveredTextExtractor(), new Bag(new Covered())));
    featureExtractors.add(new CleartkExtractor(BaseToken.class, allExtractors, new Bag(new Covered())));
//    featureExtractors.add(new CleartkExtractor(BaseToken.class, new CoveredTextExtractor(), new Bag(new Preceding(1))));
 //   featureExtractors.add(new CleartkExtractor(BaseToken.class, new CoveredTextExtractor(), new Bag(new Following(1))));
    // bag of constituent descendent labels
//    featureExtractors.add(new CleartkExtractor(TreebankNode.class, new TypePathExtractor(TreebankNode.class, "nodeType"), new Bag(new Covered())));
    
  }
  
  @Override
  public void process(JCas jCas, Segment segment)
      throws AnalysisEngineProcessException {

    HashSet<TimeMention> mentions = new HashSet<TimeMention>(JCasUtil.selectCovered(TimeMention.class, segment));
	  
    for(TopTreebankNode root : JCasUtil.selectCovered(TopTreebankNode.class, segment)){
      recursivelyProcessNode(jCas, root.getChildren(0), NON_MENTION, mentions, 0.0);
    }
  }

  private void recursivelyProcessNode(JCas jCas, TreebankNode node, String parentCategory, Set<TimeMention> mentions, double parentScore) throws AnalysisEngineProcessException {
    // accumulate features:
	double score=0.0;
    ArrayList<Feature> features = new ArrayList<Feature>();
    String category = NON_MENTION;

    // node-based features
    if(node.getParent().getParent() == null) features.add(new Feature("IS_ROOT"));
    features.add(new Feature("NODE_LABEL", node.getNodeType()));
    features.add(new Feature("PARENT_LABEL", node.getParent().getNodeType()));
    features.add(new Feature("PARENT_CAT", parentCategory));
    
    if(node.getLeaf()){
      features.add(new Feature("IS_LEAF"));
    }else{
      StringBuilder buffer = new StringBuilder();
      for(int i = 0; i < node.getChildren().size(); i++){
        buffer.append(node.getChildren(i).getNodeType());
        buffer.append("_");
        features.add(new Feature("CHILD_BAG", node.getChildren(i).getNodeType()));
      }
//      features.add(new Feature("NUM_TOKENS", JCasUtil.selectCovered(BaseToken.class, node).size()));
      features.add(new Feature("PRODUCTION", buffer.toString()));
//      features.add(new Feature("LeftSibling", getSiblingCategory(node, -1)));
//      features.add(new Feature("RightSibling", getSiblingCategory(node, 1)));
    }
    
    // other feature types:
    for(SimpleFeatureExtractor extractor : featureExtractors){
      features.addAll(extractor.extract(jCas, node));
    }
      
    if(this.isTraining()){
      List<TimeMention> goldMentions = JCasUtil.selectCovered(TimeMention.class, node);
      for(TimeMention mention : goldMentions){
        if(mention.getBegin() == node.getBegin() && mention.getEnd() == node.getEnd()){
          category = MENTION;
          score=1.0;
          mentions.remove(mention);
        }
      }
      this.dataWriter.write(new Instance<String>(category, features));
    }else{
      score = this.classifier.score(features, 1).get(0).getScore();
      category = this.classifier.classify(features);
      if(category.equals(MENTION)){
        // add to cas
        TimeMention mention = new TimeMention(jCas, node.getBegin(), node.getEnd());
        mention.setConfidence((float)score);
        mention.addToIndexes();
      }
    }

    // now do children if not a leaf & not a mention
    if(node.getLeaf() || MENTION.equals(category)) return;

    double highestScoringChild = 0.0;
    
    if(!node.getLeaf()){
    	for(int i = 0; i < node.getChildren().size(); i++){
    		TreebankNode child = node.getChildren(i);
    		recursivelyProcessNode(jCas, child, category, mentions, score);
    	}
    }
    
    
//    if(MENTION.equals(category) && score > highestScoringChild && score > parentScore){
    	
//    }
  }
  
  private static String getSiblingCategory(TreebankNode node, int offset) throws AnalysisEngineProcessException{
	  String cat = null;
	  
	  TreebankNode parent = node.getParent();
	  int nodeIndex = -1;
	  for(int i = 0; i < parent.getChildren().size(); i++){
		  if(parent.getChildren(i) == node){
			  nodeIndex = i;
			  break;
		  }
	  }
	  
	  if(nodeIndex == -1){
		  throw new AnalysisEngineProcessException();
	  }else if(nodeIndex + offset < 0){
		  cat = "<";
	  }else if(nodeIndex + offset >= parent.getChildren().size()){
		  cat = ">";
	  }else{
		  cat = parent.getChildren(nodeIndex+offset).getNodeType();
	  }
	  
	  return cat;
  }
}
