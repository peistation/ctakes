package org.chboston.cnlp.ctakes.coref.uima.cc;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.mayo.bmi.uima.core.type.relation.CoreferenceRelation;
import edu.mayo.bmi.uima.coref.type.Markable;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class InsertAnnotations
extends KnowtatorProject
{
	private Cls markable;
	private Cls pair;
	private Hashtable<Markable, SimpleInstance> seen;

	public InsertAnnotations(String protegeProjectFile, String textSourceDirectory, String annotID)
	throws TextSourceAccessException, IOException
	{
		super(protegeProjectFile, textSourceDirectory);

		markable = kb.getCls("markable");
		pair = kb.getCls("pair");
		seen = new Hashtable<Markable, SimpleInstance>();
		annotator = kb.getSimpleInstance(annotID);

	}


	public void createAnnotation(CoreferenceRelation ann, TextSource textSource, String textDoc)
	throws TextSourceAccessException
	{
		Markable m1 = (Markable) ann.getArg1().getArgument();
		Markable m2 = (Markable) ann.getArg2().getArgument();

		SimpleInstance si_antecedent;
		if (seen.containsKey(m1)) {
			si_antecedent = seen.get(m1);
		} else {
			si_antecedent = mentionUtil.createClassMention(markable);
			Span span = new Span(m1.getBegin(), m1.getEnd());
			List spans = new ArrayList();
			spans.add(span);
			annotationUtil.createAnnotation(si_antecedent, annotator, spans,
					textDoc.substring(m1.getBegin(), m1.getEnd()),
					tsu.getTextSourceInstance(textSource, true),
					null);
			seen.put(m1, si_antecedent);
		}

		SimpleInstance si_anaphor;
		if (seen.containsKey(m2)) {
			si_anaphor = seen.get(m2);
		} else {
			si_anaphor = mentionUtil.createClassMention(markable);
			Span span = new Span(m2.getBegin(), m2.getEnd());
			List spans = new ArrayList();
			spans.add(span);
			annotationUtil.createAnnotation(si_anaphor, annotator, spans,
					textDoc.substring(m2.getBegin(), m2.getEnd()),
					tsu.getTextSourceInstance(textSource, true),
					null);
			seen.put(m2, si_anaphor);
		}

		SimpleInstance si_pair = mentionUtil.createClassMention(pair);

		List spans = new ArrayList();

		Slot slot_antecedent = kb.getSlot("antecedent");
		SimpleInstance antecedentSlot = mentionUtil.createSlotMention(slot_antecedent);
		mentionUtil.addSlotMention(si_pair, antecedentSlot);
		mentionUtil.addValueToSlotMention(antecedentSlot, si_antecedent);
		Slot slot_anaphor = kb.getSlot("anaphor");
		SimpleInstance anaphorSlot = mentionUtil.createSlotMention(slot_anaphor);
		mentionUtil.addSlotMention(si_pair, anaphorSlot);
		mentionUtil.addValueToSlotMention(anaphorSlot, si_anaphor);
		annotationUtil.createAnnotation(si_pair, annotator, spans, "", tsu.getTextSourceInstance(textSource, true), null);

	}

	public void saveProject () {
		protegeProject.save(errors);
	}

}
