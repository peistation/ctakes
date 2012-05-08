package org.chboston.cnlp.ctakes.relationextractor.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.textsem.Modifier;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

/**
 * Identifies Degree_Of relation between entities and modifiers
 *
 */
public class DegreeOfRelationExtractorAnnotator extends RelationExtractorAnnotator {

	@Override
	public List<IdentifiedAnnotationPair> getCandidateRelationArgumentPairs(
			JCas identifiedAnnotationView, Sentence sentence) {
		
		List<EntityMention> entities = JCasUtil.selectCovered(identifiedAnnotationView, EntityMention.class, sentence);
		List<Modifier> modifiers = JCasUtil.selectCovered(identifiedAnnotationView, Modifier.class, sentence);
		
		List<IdentifiedAnnotationPair> pairs = new ArrayList<IdentifiedAnnotationPair>();
		for (EntityMention entity : entities) {
			for (Modifier modifier : modifiers) {
				pairs.add(new IdentifiedAnnotationPair(entity, modifier));
			}
		}
		return pairs;
	}

	@Override
	protected String getRelationCategory(
			Map<List<Annotation>, BinaryTextRelation> relationLookup,
			IdentifiedAnnotation arg1, IdentifiedAnnotation arg2) {
		BinaryTextRelation relation = relationLookup.get(Arrays.asList(arg1, arg2));
		return (relation != null) ? relation.getCategory() : NO_RELATION_CATEGORY;
	}

}
