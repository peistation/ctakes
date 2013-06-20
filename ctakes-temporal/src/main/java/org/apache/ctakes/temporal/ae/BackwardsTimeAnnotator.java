package org.apache.ctakes.temporal.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.temporal.ae.feature.ParseSpanFeatureExtractor;
import org.apache.ctakes.temporal.ae.feature.TimeWordTypeExtractor;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor;
import org.cleartk.classifier.feature.extractor.simple.CharacterCategoryPatternExtractor.PatternType;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;


public class BackwardsTimeAnnotator extends TemporalEntityAnnotator_ImplBase{

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
        new CharacterCategoryPatternExtractor(PatternType.REPEATS_MERGED),
        new CharacterCategoryPatternExtractor(PatternType.ONE_PER_CHAR),
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
        new Preceding(3),
        new Following(3)));
//    this.parseFeatureExtractors = new ArrayList<ParseSpanFeatureExtractor>();
//    this.parseFeatureExtractors.add(new ParseSpanFeatureExtractor());
    parseExtractor = new ParseSpanFeatureExtractor();
  }

  @Override
  public void process(JCas jCas, Segment segment) throws AnalysisEngineProcessException {
    // classify tokens within each sentence
    for (Sentence sentence : JCasUtil.selectCovered(jCas, Sentence.class, segment)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);
      
      // during training, the list of all outcomes for the tokens
      List<String> outcomes;
      if (this.isTraining()) {
        List<TimeMention> times = JCasUtil.selectCovered(jCas, TimeMention.class, sentence);
        outcomes = this.timeChunking.createOutcomes(jCas, tokens, times);
        outcomes = Lists.reverse(outcomes);
      }
      // during prediction, the list of outcomes predicted so far
      else {
        outcomes = new ArrayList<String>();
      }

      tokens = Lists.reverse(tokens);

      // extract features for all tokens
      int tokenIndex = -1;
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
        int nPreviousClassifications = 2;
        for (int i = nPreviousClassifications; i > 0; --i) {
          int index = tokenIndex - i;
          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
          features.add(new Feature("PreviousOutcome_" + i, previousOutcome));
        }
        // features from dominating parse tree
//        for(SimpleFeatureExtractor extractor : this.parseFeatureExtractors){
        BaseToken startToken = token;
        for(int i = tokenIndex-1; i >= 0; --i){
          String outcome = outcomes.get(i);
          if(outcome.equals("O")){
            break;
          }
          startToken = tokens.get(i);
        }
        features.addAll(parseExtractor.extract(jCas, startToken.getBegin(), token.getEnd()));
//        }
        // if training, write to data file
        if (this.isTraining()) {
          String outcome = outcomes.get(tokenIndex);
          this.dataWriter.write(new Instance<String>(outcome, features));
        }

        // if predicting, add prediction to outcomes
        else {
          outcomes.add(this.classifier.classify(features));
        }
      }

      // during prediction, convert chunk labels to times and add them to the CAS
      if (!this.isTraining()) {
        tokens = Lists.reverse(tokens);
        outcomes = Lists.reverse(outcomes);
        this.timeChunking.createChunks(jCas, tokens, outcomes);
      }
    }
  }

}
