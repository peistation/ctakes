package edu.mayo.bmi.uima.pad.impl;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.context.ContextHitConsumer;
import edu.mayo.bmi.uima.context.NamedEntityContextHitConsumer;
import edu.mayo.bmi.uima.context.ContextHit;

/**
 * @author Mayo Clinic
 */
public class NegativeContextHitConsumerImpl extends NamedEntityContextHitConsumer implements
	ContextHitConsumer
{
	public void consumeHit(JCas jcas, Annotation focusAnnot, int scope,
			ContextHit ctxHit)
	{
		if (focusAnnot instanceof NamedEntity)
		{
			NamedEntity neAnnot = (NamedEntity) focusAnnot;
			if (neAnnot.getTypeID() != 7 /*&& neAnnot.getTypeID() != 2*/ )
				neAnnot.setCertainty(-1);
		}

		createContextAnnot(jcas, focusAnnot, scope, ctxHit).addToIndexes();
	}
}
