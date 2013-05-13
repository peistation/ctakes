package org.apache.ctakes.temporal.eval;

import java.io.File;
import java.util.Collection;

import org.apache.ctakes.relationextractor.eval.RelationExtractorEvaluation.ParameterSettings;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.eval.AnnotationStatistics;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;
import com.lexicalscope.jewel.cli.Option;

public abstract class EvaluationOfTemporalRelations_ImplBase extends
		Evaluation_ImplBase<AnnotationStatistics<String>> {

	  static interface TempRelOptions extends Evaluation_ImplBase.Options{
		  @Option
		  public boolean getTest();
		  
		  @Option
		  public boolean getPrintFormattedRelations();
		  
		  @Option
		  public boolean getBaseline();
	    
	    @Option
	    public boolean getClosure();
	  }
	  
	  protected static boolean DEFAULT_BOTH_DIRECTIONS = false;
	  protected static float DEFAULT_DOWNSAMPLE = 1.0f;
	  protected static double DEFAULT_SVM_C = 1.0;
	  protected static double DEFAULT_SVM_G = 1.0;
	  protected static double DEFAULT_TK = 0.5;
	  protected static double DEFAULT_LAMBDA = 0.5;
	  
	  protected static ParameterSettings defaultParams = null; //new ParameterSettings(DEFAULT_BOTH_DIRECTIONS, DEFAULT_DOWNSAMPLE, "linear",
	  		  //DEFAULT_SVM_C, DEFAULT_SVM_G, "polynomial", ComboOperator.SUM, DEFAULT_TK, DEFAULT_LAMBDA);

	  
	  protected ParameterSettings params = null;
	  protected boolean printRelations = false;

	public EvaluationOfTemporalRelations_ImplBase(File baseDirectory,
			File rawTextDirectory, File knowtatorXMLDirectory,
			File xmiDirectory, File treebankDirectory, boolean printErrors, boolean printRelations, ParameterSettings params) {
		super(baseDirectory, rawTextDirectory, knowtatorXMLDirectory, xmiDirectory,
				treebankDirectory);
		this.params = params;
		this.printRelations = printRelations;
		this.printErrors =  printErrors;
	}

  public static class PreserveEventEventRelations extends JCasAnnotator_ImplBase {
    public static final String PARAM_GOLD_VIEW = "GoldView";

    @ConfigurationParameter(name = PARAM_GOLD_VIEW)
    private String goldViewName = CAS.NAME_DEFAULT_SOFA;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      for(BinaryTextRelation relation : Lists.newArrayList(JCasUtil.select(jCas, BinaryTextRelation.class))){
          RelationArgument arg1 = relation.getArg1();
          RelationArgument arg2 = relation.getArg2();
          if(arg1.getArgument() instanceof EventMention && arg2.getArgument() instanceof EventMention){
            // these are the kind we keep.
            continue;
          }
          arg1.removeFromIndexes();
          arg2.removeFromIndexes();
          relation.removeFromIndexes();
      }
    }   
  }

	  protected static void printRelationAnnotations(String fileName, Collection<BinaryTextRelation> relations) {

		  for(BinaryTextRelation binaryTextRelation : relations) {

			  Annotation arg1 = binaryTextRelation.getArg1().getArgument();
			  Annotation arg2 = binaryTextRelation.getArg2().getArgument();

			  String arg1Type = arg1.getClass().getSimpleName();
			  String arg2Type = arg2.getClass().getSimpleName();

			  int arg1Begin = arg1.getBegin();
			  int arg1End = arg1.getEnd();
			  int arg2Begin = arg2.getBegin();
			  int arg2End = arg2.getEnd();

			  String category = binaryTextRelation.getCategory();

			  System.out.format("%s\t%s\t%s\t%d\t%d\t%s\t%d\t%d\n", 
					  fileName, category, arg1Type, arg1Begin, arg1End, arg2Type, arg2Begin, arg2End);
		  }
	  }
	  
	  protected static String formatRelation(BinaryTextRelation relation) {
		  IdentifiedAnnotation arg1 = (IdentifiedAnnotation)relation.getArg1().getArgument();
		  IdentifiedAnnotation arg2 = (IdentifiedAnnotation)relation.getArg2().getArgument();
		  String text = arg1.getCAS().getDocumentText();
		  int begin = Math.min(arg1.getBegin(), arg2.getBegin());
		  int end = Math.max(arg1.getBegin(), arg2.getBegin());
		  begin = Math.max(0, begin - 50);
		  end = Math.min(text.length(), end + 50);
		  return String.format(
				  "%s(%s(type=%d), %s(type=%d)) in ...%s...",
				  relation.getCategory(),
				  arg1.getCoveredText(),
				  arg1.getTypeID(),
				  arg2.getCoveredText(),
				  arg2.getTypeID(),
				  text.substring(begin, end).replaceAll("[\r\n]", " "));
	  }

}
