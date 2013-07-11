package org.apache.ctakes.assertion.medfacts.cleartk.extractors;

import java.util.ArrayList;
import java.util.List;

import org.apache.ctakes.assertion.util.NegationManualDepContextAnalyzer;
import org.apache.ctakes.dependency.parser.util.DependencyUtility;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

public class NegationDependencyFeatureExtractor implements
		SimpleFeatureExtractor {

	NegationManualDepContextAnalyzer conAnal = null;

	public NegationDependencyFeatureExtractor(){
		conAnal = new NegationManualDepContextAnalyzer();
	}
	
	@Override
	public List<Feature> extract(JCas jcas, Annotation focusAnnotation)
			throws CleartkExtractorException {
		List<Feature> feats = new ArrayList<Feature>();
		Sentence sent = null;
		
		List<Sentence> sents = JCasUtil.selectCovering(jcas, Sentence.class, focusAnnotation.getBegin(), focusAnnotation.getEnd());
		if(sents != null && sents.size() > 0){
			sent = sents.get(0);
		}else{
			return feats;
		}
		
		List<ConllDependencyNode> nodes = DependencyUtility.getDependencyNodes(jcas, sent);
		ConllDependencyNode headNode = DependencyUtility.getNominalHeadNode(jcas, focusAnnotation);
		try {
			boolean[] regexFeats = conAnal.findNegationContext(nodes, headNode);
			for(int j = 0; j < regexFeats.length; j++){
				if(regexFeats[j]){
					feats.add(new Feature("DepPath_" + conAnal.getRegexName(j))); //"NEG_DEP_REGEX_"+j));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CleartkExtractorException(e);
		}
		return feats;
	}

}
