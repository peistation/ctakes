package edu.mayo.bmi.nlp.parser.util;

import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.textsem.Predicate;
import edu.mayo.bmi.uima.core.type.textsem.SemanticRoleRelation;

public class SRLUtility {
	public static String dumpSRLOutput(Annotation annotation) {
		StringBuilder builder = new StringBuilder();
		for (Predicate predicate : JCasUtil.selectCovered(Predicate.class, annotation)) {
			builder.append(predicate.getCoveredText() + predicate.getFrameSet() + "(");
			for (SemanticRoleRelation relation : JCasUtil.select(predicate.getRelations(), SemanticRoleRelation.class)) {
				builder.append(String.format("%s=%s ", relation.getArgument().getLabel(), relation.getArgument().getCoveredText()));
			}
			builder.append(")\n");
		}
		return builder.toString();
	}
}
