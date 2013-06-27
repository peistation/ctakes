package org.apache.ctakes.temporal.ae;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.temporal.eval.THYMEData;
import org.apache.ctakes.typesystem.type.syntax.BaseToken;
import org.apache.ctakes.typesystem.type.textsem.TimeMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.chunking.BIOChunking;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class MetaTimeAnnotator extends TemporalEntityAnnotator_ImplBase {

  private BIOChunking<BaseToken, TimeMention> timeChunking;

  static Class[] components = new Class[]{ BackwardsTimeAnnotator.class, TimeAnnotator.class, ConstituencyBasedTimeAnnotator.class, CRFTimeAnnotator.class };
  
  public static AnalysisEngineDescription getDataWriterDescription(
      Class<? extends DataWriter<String>> dataWriterClass,
      File directory) throws ResourceInitializationException {
    AggregateBuilder builder = new AggregateBuilder();
    
    for(Class component : components){
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(ViewCreatorAnnotator.class, ViewCreatorAnnotator.PARAM_VIEW_NAME, component.getSimpleName()));
    }
    
    builder.add(TimeAnnotator.createAnnotatorDescription(
                      new File(directory, TimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, TimeAnnotator.class.getSimpleName());
    builder.add(BackwardsTimeAnnotator.createAnnotatorDescription(
                      new File(directory, BackwardsTimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, BackwardsTimeAnnotator.class.getSimpleName());
    builder.add(ConstituencyBasedTimeAnnotator.createAnnotatorDescription(
                      new File(directory, ConstituencyBasedTimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, ConstituencyBasedTimeAnnotator.class.getSimpleName());
    builder.add(CRFTimeAnnotator.createAnnotatorDescription(
                      new File(directory, CRFTimeAnnotator.class.getSimpleName())), 
                      TimeAnnotator.TIMEX_VIEW, CRFTimeAnnotator.class.getSimpleName());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(MetaTimeAnnotator.class, 
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        dataWriterClass,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        new File(directory, MetaTimeAnnotator.class.getSimpleName())));   
    return builder.createAggregateDescription();
  }

  public static AnalysisEngineDescription getAnnotatorDescription(File directory) throws ResourceInitializationException{
    AggregateBuilder builder = new AggregateBuilder();
    
    for(Class component : components){
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(ViewCreatorAnnotator.class, ViewCreatorAnnotator.PARAM_VIEW_NAME, component.getSimpleName()));
    }
    builder.add(TimeAnnotator.createAnnotatorDescription(
                      new File(directory, TimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, TimeAnnotator.class.getSimpleName());
    builder.add(BackwardsTimeAnnotator.createAnnotatorDescription(
                      new File(directory, BackwardsTimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, BackwardsTimeAnnotator.class.getSimpleName());
    builder.add(ConstituencyBasedTimeAnnotator.createAnnotatorDescription(
                      new File(directory, ConstituencyBasedTimeAnnotator.class.getSimpleName())),
                      TimeAnnotator.TIMEX_VIEW, ConstituencyBasedTimeAnnotator.class.getSimpleName());
    builder.add(CRFTimeAnnotator.createAnnotatorDescription(
                      new File(directory, CRFTimeAnnotator.class.getSimpleName())), 
                      TimeAnnotator.TIMEX_VIEW, CRFTimeAnnotator.class.getSimpleName());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        MetaTimeAnnotator.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        new File(directory, MetaTimeAnnotator.class.getSimpleName() + File.separator + "model.jar")));
    return builder.createAggregateDescription();
  }
  
  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // define chunking
    this.timeChunking = new BIOChunking<BaseToken, TimeMention>(BaseToken.class, TimeMention.class);
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
      
      List<List<String>> componentOutcomes = new ArrayList<List<String>>();
      for(Class component : components){
        JCas componentView;
        try {
          componentView = jCas.getView(component.getSimpleName());
          CasCopier casCopy = new CasCopier(jCas.getCas(), componentView.getCas());
          org.apache.uima.cas.Feature sofaFeature = jCas.getTypeSystem().getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
          for(BaseToken token: tokens){
            BaseToken fs = (BaseToken) casCopy.copyFs(token);
            fs.setFeatureValue(sofaFeature, componentView.getSofa());
            fs.addToIndexes(componentView);
          }
          List<BaseToken> viewTokens = JCasUtil.selectCovered(componentView, BaseToken.class, sentence.getBegin(), sentence.getEnd());
          List<TimeMention> times = JCasUtil.selectCovered(componentView, TimeMention.class, sentence);
          componentOutcomes.add(this.timeChunking.createOutcomes(componentView, viewTokens, times));
        } catch (CASException e) {
          e.printStackTrace();
          throw new AnalysisEngineProcessException(e);
        }
      }
      
      for(int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++){
        List<Feature> features = new ArrayList<Feature>();
        
        for(int componentNum = 0; componentNum < componentOutcomes.size(); componentNum++){
          String outcome = componentOutcomes.get(componentNum).get(tokenIndex);
          if(tokenIndex > 0){
            features.add(new Feature("PreviousOutcome", outcomes.get(tokenIndex-1)));
            features.add(new Feature(String.format("Component%d_PreviousLabel", componentNum), componentOutcomes.get(componentNum).get(tokenIndex-1)));
          }
          features.add(new Feature(String.format("Component%d_Label", componentNum), outcome));
          if(tokenIndex < tokens.size() -1){
            features.add(new Feature(String.format("Component%d_NextLabel", componentNum), componentOutcomes.get(componentNum).get(tokenIndex+1)));
          }
          if(!outcome.equals("O")){
            features.add(new Feature(String.format("Component%d_IsTime", componentNum)));
          }
        }
        
        if (this.isTraining()) {
          String outcome = outcomes.get(tokenIndex);
          this.dataWriter.write(new Instance<String>(outcome, features));
        }
        // if predicting, add prediction to outcomes
        else {
          outcomes.add(this.classifier.classify(features));
        }
      }
      
      if (!this.isTraining()) {
        this.timeChunking.createChunks(jCas, tokens, outcomes);
      }

    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Segment segment : JCasUtil.select(jCas, Segment.class)) {
      if (!THYMEData.SEGMENTS_TO_SKIP.contains(segment.getId())) {
        this.process(jCas, segment);
      }
    }    
  }


}
