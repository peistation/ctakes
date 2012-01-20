package edu.mayo.bmi.uima.lookup.ae;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.SmokingStatus.type.UnknownSmokerNamedEntityAnnotation;
import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;

/**
 * copied from edu.may.bmi.uima.lookup.ae.NamedEntityLookupConsumerImpl in the "Dictionary Lookup" project
 */
public class UnknownSmokerNamedEntityLookupConsumerImpl extends BaseLookupConsumerImpl
		implements LookupConsumer
{

	private final String CODE_MF_PRP_KEY = "codeMetaField";

	private final String CODING_SCHEME_PRP_KEY = "codingScheme";

	private Properties iv_props;

	public UnknownSmokerNamedEntityLookupConsumerImpl(UimaContext aCtx, Properties props)
	{
		// TODO property validation
		iv_props = props;
	}

	public void consumeHits(JCas jcas, Iterator lhItr)
			throws AnalysisEngineProcessException
	{
		Iterator hitsByOffsetItr = organizeByOffset(lhItr);
		while (hitsByOffsetItr.hasNext())
		{
			Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();

			FSArray ocArr = new FSArray(jcas, hitsAtOffsetCol.size());
			int ocArrIdx = 0;

			// iterate over the LookupHit objects and create
			// a corresponding JCas OntologyConcept object that will
			// be placed in a FSArray
			Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
			int neBegin = -1;
			int neEnd = -1;
			while (lhAtOffsetItr.hasNext())
			{
				LookupHit lh = (LookupHit) lhAtOffsetItr.next();
				neBegin = lh.getStartOffset();
				neEnd = lh.getEndOffset();
				//MetaDataHit mdh = lh.getGazMetaDataHit();
				MetaDataHit mdh = lh.getDictMetaDataHit();

				OntologyConcept oc = new OntologyConcept(jcas);
				oc.setCode(mdh.getMetaFieldValue(iv_props.getProperty(CODE_MF_PRP_KEY)));
				oc.setCodingScheme(iv_props.getProperty(CODING_SCHEME_PRP_KEY));

				ocArr.set(ocArrIdx, oc);
				ocArrIdx++;
			}

			UnknownSmokerNamedEntityAnnotation neAnnot = new UnknownSmokerNamedEntityAnnotation(jcas); //modification
			neAnnot.setBegin(neBegin);
			neAnnot.setEnd(neEnd);
			neAnnot.setDiscoveryTechnique(TypeSystemConst.NE_DISCOVERY_TECH_DICT_LOOKUP);
			neAnnot.setOntologyConceptArr(ocArr);
			neAnnot.addToIndexes();
		}
	}
}
