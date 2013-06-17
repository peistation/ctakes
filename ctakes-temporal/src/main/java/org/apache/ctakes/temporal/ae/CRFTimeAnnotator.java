package org.apache.ctakes.temporal.ae;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.temporal.ae.feature.ParseSpanFeatureExtractor;
import org.apache.ctakes.temporal.ae.feature.TimeWordTypeExtractor;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.syntax.TreebankNode;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instances;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor.PatternType;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class CRFTimeAnnotator extends TemporalSequenceAnnotator_ImplBase {
  public static AnalysisEngineDescription createDataWriterDescription(
      Class<? extends DataWriter<String>> dataWriterClass,
      File outputDirectory) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        CRFTimeAnnotator.class,
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
        CRFTimeAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(modelDirectory, "model.jar"));
  }
  
  protected List<SimpleFeatureExtractor> tokenFeatureExtractors;

  protected List<CleartkExtractor> contextFeatureExtractors;
  
//  protected List<SimpleFeatureExtractor> parseFeatureExtractors;
  protected ParseSpanFeatureExtractor parseExtractor;

  private BIOChunking<BaseToken, TimeMention> timeChunking;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // define chunking
    this.timeChunking = new BIOChunking<BaseToken, TimeMention>(BaseToken.class, TimeMention.class);
    CombinedExtractor allExtractors = new CombinedExtractor(
        new CoveredTextExtractor(),
//        new CharacterCategoryPatternExtractor(PatternType.REPEATS_MERGED),
//        new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
        new TypePathExtractor(BaseToken.class, "partOfSpeech"),
        new TimeWordTypeExtractor());

//    CombinedExtractor parseExtractors = new CombinedExtractor(
//        new ParseSpanFeatureExtractor()
//        );
    this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.tokenFeatureExtractors.add(allExtractors);

    this.contextFeatureExtractors = new ArrayList<CleartkExtractor>();
    this.contextFeatureExtractors.add(new CleartkExtractor(
        BaseToken.class,
        allExtractors,
        new Preceding(2),
        new Following(2)));
//    this.parseFeatureExtractors = new ArrayList<ParseSpanFeatureExtractor>();
//    this.parseFeatureExtractors.add(new ParseSpanFeatureExtractor());
    parseExtractor = new ParseSpanFeatureExtractor();
  }
  
  @Override
  public void process(JCas jCas, Segment segment)
      throws AnalysisEngineProcessException {
    // classify tokens within each sentence
    for (Sentence sentence : JCasUtil.selectCovered(jCas, Sentence.class, segment)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);

      // during training, the list of all outcomes for the tokens
      List<String> outcomes;
      if (this.isTraining()) {
        List<TimeMention> times = JCasUtil.selectCovered(jCas, TimeMention.class, sentence);
        outcomes = this.timeChunking.createOutcomes(jCas, tokens, times); 
      }
      // during prediction, the list of outcomes predicted so far
      else {
        outcomes = new ArrayList<String>();
      }

      // extract features for all tokens
      int tokenIndex = -1;
      List<List<Feature>> allFeatures = new ArrayList<List<Feature>>();
      for (BaseToken token : tokens) {
        ++tokenIndex;

        List<Feature> features = new ArrayList<Feature>();
        // features from token attributes
        for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
          features.addAll(extractor.extract(jCas, token));
        }
        // features from surrounding tokens
        for (CleartkExtractor extractor : this.contextFeatureExtractors) {
          features.addAll(extractor.extractWithin(jCas, token, sentence));
        }
        // features from previous classifications
        // not sure this makes sense in CRF context -- linear chain CRF uses this automatically?
        // TODO - fix or delete
//        int nPreviousClassifications = 2;
//        for (int i = nPreviousClassifications; i > 0; --i) {
//          int index = tokenIndex - i;
//          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
//          features.add(new Feature("PreviousOutcome_" + i, previousOutcome));
//        }
        
        // features from dominating parse tree
        // TODO-  think abouot how to incorporate this - fix or delete
//        BaseToken startToken = token;
//        for(int i = tokenIndex-1; i >= 0; --i){
//          String outcome = outcomes.get(i);
//          if(outcome.equals("O")){
//            break;
//          }
//          startToken = tokens.get(i);
//        }
        TreebankNode preTerm = AnnotationTreeUtils.annotationNode(jCas, token);
        features.addAll(parseExtractor.extract(jCas, token.getBegin(), token.getEnd()));
        //if(preTerm != null && preTerm.getParent() != null){
        //  features.addAll(parseExtractor.extract(jCas, preTerm.getParent().getBegin(), preTerm.getParent().getEnd()));
        //}
        
        // if training, write to data file
//        if (this.isTraining()) {
//          String outcome = outcomes.get(tokenIndex);
          allFeatures.add(features);
//        }

        // if predicting, add prediction to outcomes
//        else {
//        }
      }

      // during prediction, convert chunk labels to times and add them to the CAS
      if (this.isTraining()) {
        this.dataWriter.write(Instances.toInstances(outcomes, allFeatures));        
      }else{
//        outcomes.add(this.classifier.classify(features));
        outcomes = this.classifier.classify(allFeatures);
        this.timeChunking.createChunks(jCas, tokens, outcomes);
      }
    }
  }

}
