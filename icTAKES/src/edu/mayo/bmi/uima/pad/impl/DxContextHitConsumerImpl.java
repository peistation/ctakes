package edu.mayo.bmi.uima.pad.impl;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.context.ContextHit;
import edu.mayo.bmi.uima.context.ContextHitConsumer;
import edu.mayo.bmi.uima.context.NamedEntityContextHitConsumer;
import edu.mayo.bmi.uima.core.type.refsem.Element;
import edu.mayo.bmi.uima.core.type.textsem.IdentifiedAnnotation;

/**
 * @author Mayo Clinic
 */
public class DxContextHitConsumerImpl extends NamedEntityContextHitConsumer
		implements ContextHitConsumer {
	public void consumeHit(JCas jcas, Annotation focusAnnot, int scope,
			ContextHit ctxHit) {
		Integer status = (Integer) ctxHit
				.getMetaData(DxContextAnalyzerImpl.CTX_HIT_KEY_ILLNESS_TYPE);
		if (focusAnnot instanceof IdentifiedAnnotation) {
			IdentifiedAnnotation neAnnot = (IdentifiedAnnotation) focusAnnot;
			if (neAnnot.getTypeID() == 6 || neAnnot.getTypeID() == 7
					|| neAnnot.getTypeID() == 0)
				// TODO: Status Family History, History Of, etc. are constant
				// strings?
				neAnnot.setUncertainty(status);
		}

		createContextAnnot(jcas, focusAnnot, scope, ctxHit).addToIndexes();
	}
}