package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.chunking.Chunking;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textsem.Modifier;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public class ModifierExtractorAnnotator extends CleartkAnnotator<String> {

  public static AnalysisEngineDescription getDescription(Object... additionalConfiguration)
      throws ResourceInitializationException {
    AnalysisEngineDescription desc = AnalysisEngineFactory.createPrimitiveDescription(ModifierExtractorAnnotator.class);
    if (additionalConfiguration.length > 0) {
      ConfigurationParameterFactory.addConfigurationParameters(desc, additionalConfiguration);
    }
    return desc;
  }

  private Chunking<String, BaseToken, Modifier> chunking;

  private int nPreviousClassifications = 2;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.chunking = new BIOChunking<BaseToken, Modifier>(BaseToken.class, Modifier.class);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<BaseToken> tokens = JCasUtil.selectCovered(jCas, BaseToken.class, sentence);

      // during training, the list of all outcomes for the tokens
      List<String> outcomes;
      if (this.isTraining()) {
        List<Modifier> modifiers = JCasUtil.selectCovered(jCas, Modifier.class, sentence);
        outcomes = this.chunking.toOutcomes(jCas, tokens, modifiers);
      }

      // during prediction, the list of outcomes predicted so far
      else {
        outcomes = new ArrayList<String>();
      }

      // one classification instance per token
      int outcomeIndex = -1;
      for (BaseToken token : tokens) {
        ++outcomeIndex;

        // extract token features
        List<Feature> features = new ArrayList<Feature>();
        features.add(new Feature(token.getCoveredText()));
        features.add(new Feature("PartOfSpeech", token.getPartOfSpeech()));

        // extract previous classification features
        for (int i = this.nPreviousClassifications; i > 0; --i) {
          int index = outcomeIndex - i;
          String previousOutcome = index < 0 ? "O" : outcomes.get(index);
          features.add(new Feature("PreviousOutcome_" + i, previousOutcome));
        }

        // extract length of Modifier that is currently being created (if any)
        // int length = 0;
        // for (int i = outcomeIndex - 1; i > 0 && !"O".equals(outcomes.get(i)); --i) {
        //   ++length;
        // }
        // features.add(new Feature("CurrentModifierLength", length));

        // if training, write to data file
        if (this.isTraining()) {
          String outcome = outcomes.get(outcomeIndex);
          this.dataWriter.write(new Instance<String>(outcome, features));
        }

        // if predicting, add prediction to outcomes
        else {
          outcomes.add(this.classifier.classify(features));
        }
      }

      // convert classifications to Modifiers
      if (!this.isTraining()) {
        this.chunking.toChunks(jCas, tokens, outcomes);
      }
    }

  }

}
