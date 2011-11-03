package edu.mayo.bmi.uima.drugner.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.fsm.drugner.machines.elements.DecimalStrengthFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.DosagesFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.DrugChangeStatusFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.DurationFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.FormFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.FractionStrengthFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.FrequencyFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.FrequencyUnitFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.RangeStrengthFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.RouteFSM;
import edu.mayo.bmi.fsm.drugner.machines.elements.StrengthFSM;
import edu.mayo.bmi.fsm.drugner.machines.util.SubSectionIndicatorFSM;
import edu.mayo.bmi.fsm.drugner.machines.util.SuffixStrengthFSM;
import edu.mayo.bmi.fsm.drugner.output.elements.BaseTokenImpl;
import edu.mayo.bmi.fsm.drugner.output.elements.DosageToken;
import edu.mayo.bmi.fsm.drugner.output.elements.DrugChangeStatusToken;
import edu.mayo.bmi.fsm.drugner.output.elements.DurationToken;
import edu.mayo.bmi.fsm.drugner.output.elements.FormToken;
import edu.mayo.bmi.fsm.drugner.output.elements.FrequencyToken;
import edu.mayo.bmi.fsm.drugner.output.elements.FrequencyUnitToken;
import edu.mayo.bmi.fsm.drugner.output.elements.RouteToken;
import edu.mayo.bmi.fsm.drugner.output.elements.StrengthToken;
import edu.mayo.bmi.fsm.drugner.output.util.SubSectionIndicator;
import edu.mayo.bmi.fsm.drugner.output.util.SuffixStrengthToken;
import edu.mayo.bmi.lookup.algorithms.FirstTokenPermutationImpl;
import edu.mayo.bmi.lookup.vo.LookupToken;
import edu.mayo.bmi.uima.cdt.type.DateAnnotation;
import edu.mayo.bmi.uima.core.ae.TokenizerAnnotator;
import edu.mayo.bmi.uima.core.fsm.adapters.ContractionTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.DecimalTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.IntegerTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.NewlineTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.PunctuationTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.SymbolTokenAdapter;
import edu.mayo.bmi.uima.core.fsm.adapters.WordTokenAdapter;
import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.ContractionToken;
import edu.mayo.bmi.uima.core.type.IdentifiedAnnotation;
import edu.mayo.bmi.uima.core.type.NamedEntity;
import edu.mayo.bmi.uima.core.type.NewlineToken;
import edu.mayo.bmi.uima.core.type.NumToken;
import edu.mayo.bmi.uima.core.type.OntologyConcept;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.Segment;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.SymbolToken;
import edu.mayo.bmi.uima.core.type.WordToken;
import edu.mayo.bmi.uima.core.util.FSUtil;
import edu.mayo.bmi.uima.core.util.ParamUtil;
import edu.mayo.bmi.uima.core.util.TypeSystemConst;
import edu.mayo.bmi.uima.drugner.type.ChunkAnnotation;
import edu.mayo.bmi.uima.drugner.type.DecimalStrengthAnnotation;
import edu.mayo.bmi.uima.drugner.type.DosagesAnnotation;
import edu.mayo.bmi.uima.drugner.type.DrugChangeStatusAnnotation;
import edu.mayo.bmi.uima.drugner.type.DrugMentionAnnotation;
import edu.mayo.bmi.uima.drugner.type.DurationAnnotation;
import edu.mayo.bmi.uima.drugner.type.FormAnnotation;
import edu.mayo.bmi.uima.drugner.type.FractionStrengthAnnotation;
import edu.mayo.bmi.uima.drugner.type.FrequencyAnnotation;
import edu.mayo.bmi.uima.drugner.type.FrequencyUnitAnnotation;
import edu.mayo.bmi.uima.drugner.type.RangeStrengthAnnotation;
import edu.mayo.bmi.uima.drugner.type.RouteAnnotation;
import edu.mayo.bmi.uima.drugner.type.StrengthAnnotation;
import edu.mayo.bmi.uima.drugner.type.SubSectionAnnotation;
import edu.mayo.bmi.uima.drugner.type.SuffixStrengthAnnotation;
import edu.mayo.bmi.uima.lookup.ae.LookupAnnotationToJCasAdapter;
import edu.mayo.bmi.uima.drugner.DrugMention;
import edu.mayo.bmi.uima.drugner.elements.DrugChangeStatusElement;

/**
 * Finds tokens based on context. There are two major groupings or ranges that
 * will be used to create the additional annotations needed to handle the drug
 * mentions used to represent the status changes.
 * 
 * @author Mayo Clinic
 */
public class DrugMentionAnnotator extends JTextAnnotator_ImplBase 
{
  // LOG4J logger based on class name
  public static Logger iv_logger = Logger.getLogger(DrugMentionAnnotator.class);

  /**
   * This identifies the section ids that will be considered in generating DrugMentionAnnotaitons
   */
  public static final String PARAM_SEGMENTS_MEDICATION_RELATED = "medicationRelatedSection";

  private FractionStrengthFSM iv_fractionFSM;
  private RangeStrengthFSM iv_rangeFSM;
  private SubSectionIndicatorFSM iv_subMedSectionFSM;
  private DosagesFSM iv_dosagesFSM;
  private SuffixStrengthFSM iv_suffixFSM;
  private DurationFSM iv_durationFSM;
  private RouteFSM iv_routeFSM;
  private FrequencyFSM iv_frequencyFSM;
  private DrugChangeStatusFSM iv_statusFSM;
  private DecimalStrengthFSM iv_decimalFSM;
  private StrengthFSM iv_strengthFSM;
  private FrequencyUnitFSM iv_frequencyUnitFSM;
  private FormFSM iv_formFSM;
  private static final int NERTypeIdentifier = 1;
  private static boolean handledRanges;
  private final String CANONICAL_VARIANT_ATTR = "canonicalATTR";
  private Set<String> iv_exclusionTagSet = null;
  private Set<String> iv_medicationRelatedSections = new HashSet<String>();

  public void initialize(AnnotatorContext annotCtx)
      throws AnnotatorInitializationException, AnnotatorConfigurationException
  {
    super.initialize(annotCtx);
    try
    {
      iv_medicationRelatedSections = ParamUtil.getStringParameterValuesSet(
          PARAM_SEGMENTS_MEDICATION_RELATED, annotCtx);
    } catch (AnnotatorContextException e)
    {
      throw new AnnotatorInitializationException(e);
    }

    iv_fractionFSM = new FractionStrengthFSM();
    iv_suffixFSM = new SuffixStrengthFSM();
    iv_durationFSM = new DurationFSM();
    iv_routeFSM = new RouteFSM();
    iv_frequencyFSM = new FrequencyFSM();
    iv_dosagesFSM = new DosagesFSM();
    iv_rangeFSM = new RangeStrengthFSM();
    iv_statusFSM = new DrugChangeStatusFSM();
    iv_decimalFSM = new DecimalStrengthFSM();
    iv_strengthFSM = new StrengthFSM();
    iv_frequencyUnitFSM = new FrequencyUnitFSM();
    iv_formFSM = new FormFSM();
    iv_subMedSectionFSM = new SubSectionIndicatorFSM();
    iv_logger.info("Finite state machines loaded.");
  }

  public void process(JCas jcas, ResultSpecification rs)
      throws AnnotatorProcessException
  {

    try
    {
      JFSIndexRepository indexes = jcas.getJFSIndexRepository();
      FSIterator segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();
      FSIterator baseTokenItr = indexes.getAnnotationIndex(BaseToken.type).iterator();

      List<edu.mayo.bmi.fsm.token.BaseToken> baseTokenList = new ArrayList<edu.mayo.bmi.fsm.token.BaseToken>();
      while (baseTokenItr.hasNext())
      {
        BaseToken bta = (BaseToken) baseTokenItr.next();
        baseTokenList.add(adaptToFSMBaseToken(bta));
      }
      
      prepareSubSection(jcas, indexes, 
          iv_subMedSectionFSM.execute(baseTokenList));

      while (segmentItr.hasNext())
      {
        Segment seg = (Segment) segmentItr.next();
        if (iv_medicationRelatedSections.contains(seg.getId()))
          generateDrugMentions(jcas, seg);
      }

      generateUidValues(jcas);
    } 
    catch (Exception e)
    {
      throw new AnnotatorProcessException(e);
    }
  }

  /**
   * Generates UID values for all IdentifiedAnnotation objects.
   */
  private void generateUidValues(JCas jcas)
  {
    int uid = 0;
    FSIterator itr = jcas.getJFSIndexRepository().getAnnotationIndex(
        IdentifiedAnnotation.type).iterator();
    while (itr.hasNext())
    {
      IdentifiedAnnotation idAnnot = (IdentifiedAnnotation) itr.next();
      idAnnot.setUid(uid);
      uid++;
    }
  }

  private List sortSubSectionInd(Object[] holdOutSet)
  {
    List holdList = new ArrayList();
    // SubSectionIndicator tempSsi = null;
    for (int i = 0; i < holdOutSet.length - 1; i++)
    {
      SubSectionIndicator hos1 = (SubSectionIndicator) holdOutSet[i];
      SubSectionIndicator hos2 = (SubSectionIndicator) holdOutSet[i + 1];

      if (hos1.getStartOffset() > hos2.getStartOffset())
      {
        // tempSsi = hos2;
        holdOutSet[i + 1] = hos1;
        holdOutSet[i] = hos2;
        sortSubSectionInd(holdOutSet);
      }
    }

    holdList.addAll(Arrays.asList(holdOutSet));

    return holdList;

  }

    /**
     * Sort annotations by begin offset
     * @param holdOutSet
     * @return
     */
    private List sortAnnotations(Object[] holdOutSet) 
    {
	List holdList = new ArrayList();

	for (int i = 0; i < holdOutSet.length - 1; i++) 
	{
	    Annotation hos1 = (Annotation) holdOutSet[i];
	    Annotation hos2 = (Annotation) holdOutSet[i + 1];
	    
	    if (hos1.getBegin() > hos2.getBegin()) 
	    {
		holdOutSet[i + 1] = hos1;
		holdOutSet[i] = hos2;
		sortAnnotations(holdOutSet);
	    }
	}
	holdList.addAll(Arrays.asList(holdOutSet));

	return holdList;
    }    
    
  /**
   * finds unique annotations by their begin offsets
   * 
   * @param holdOutSet
   * @return
   */

  private List findUniqueMentions(Object[] holdOutSet)
  {
    boolean isDuplicate = false;
    List list = new ArrayList();

    for (int i = 0; i < holdOutSet.length; i++, isDuplicate = false)
    {
      for (int j = 0; j < holdOutSet.length; j++)
      {
    	  iv_logger.debug("Comparing ["
            + ((Annotation) holdOutSet[i]).getCoveredText() + "] ==? ["
            + ((Annotation) holdOutSet[j]).getCoveredText() + "]");
        isDuplicate = (isDuplicate(holdOutSet, i, j) || isDuplicate);
      }
      if (!isDuplicate)
      {
        iv_logger.debug("Adding NE: "
            + ((Annotation) holdOutSet[i]).getCoveredText());
        list.add(holdOutSet[i]);
      } else
      {
    	  iv_logger.debug("NOT Adding NE: "
            + ((Annotation) holdOutSet[i]).getCoveredText());
      }
    }
    return list;
  }

  private boolean isDuplicate(Object[] neArray, int curIdx, int checkIdx)
  {
    if (curIdx == checkIdx || checkIdx > neArray.length)
    {
      iv_logger.debug("Are indices equal?:" + curIdx + "==" + checkIdx);
      return false;
    }

    Annotation ann1 = (Annotation) neArray[curIdx];
    Annotation ann2 = (Annotation) neArray[checkIdx];

    if (ann1.getBegin() == ann2.getBegin() && ann1.getEnd() < ann2.getEnd())
      return true;

    return false;
  }

  /**
   * Given the set of subSectionInds to parse (via SubSectionIndicatorFSM)
   * create SubSectionAnnotation This method created SubsectionAnnotation and
   * sets the header begin, end as well as body begin and end
   * 
   * @param jcas
   * @param indexes
   * @param subSectionIndSet
   * @throws Exception
   */
  private void prepareSubSection(JCas jcas, JFSIndexRepository indexes,
      Set subSectionIndSet) throws Exception
  {
    List sortedSubSecInds = sortSubSectionInd(subSectionIndSet.toArray());

    for (int i = 0, endBodyOffset = 0; i < sortedSubSecInds.size(); i++, endBodyOffset = 0)
    {
      SubSectionIndicator ssi = (SubSectionIndicator) sortedSubSecInds.get(i);
      Segment segment = getSegmentContainingOffsets(jcas, ssi.getStartOffset(),
          ssi.getEndOffset());
      endBodyOffset = segment.getEnd(); // backup

      SubSectionAnnotation ssa = new SubSectionAnnotation(jcas);

      ssa.setBegin(ssi.getStartOffset());
      // header is marked by the indicator
      ssa.setSubSectionHeaderBegin(ssi.getStartOffset());
      ssa.setSubSectionHeaderEnd(ssi.getEndOffset());

      // body begins where SubSectionIndicator ends
      ssa.setSubSectionBodyBegin(ssi.getEndOffset() + 1);

      if (i + 1 < sortedSubSecInds.size()) // i is not the last element
      {
        SubSectionIndicator nextSsi = (SubSectionIndicator) sortedSubSecInds
            .get(i + 1);
        endBodyOffset = getSubSectionAnnotationBodyEnd(segment, nextSsi);

      } else
      // this was the last SubSectionIndicator
      {
        endBodyOffset = getSubSectionAnnotationBodyEnd(segment, null);
      }
      ssa.setSubSectionBodyEnd(endBodyOffset);
      ssa.setEnd(endBodyOffset);
      ssa.addToIndexes();
    }
  }

  /**
   * Decides where the SubSectionAnnotation should end. This is based on the
   * following:
   * 
   * If the next SubSectionIndicator starts before the end of current segment
   * then, use the next SubSectionIndicator.begin - 1 else if the next
   * SubSectionIndicator starts after the end of the current segment then, use
   * the end of the current Segment.end
   * 
   */
  private int getSubSectionAnnotationBodyEnd(Segment currSeg,
      SubSectionIndicator nextSsi)
  {
    // next SubSectionIndicator is not present, use the segment's end
    if (nextSsi == null)
      return currSeg.getEnd();
    // decide between segment's end and next subsection's end
    if (nextSsi.getStartOffset() > currSeg.getEnd())
      return currSeg.getEnd();
    else if (nextSsi.getStartOffset() < currSeg.getEnd()) // might check
                                                          // nextSsi.getEndOffset()
                                                          // if confident about
                                                          // the end
      return nextSsi.getStartOffset() - 1;
    return -1;// screams error!
  }

  private Segment getSegmentContainingOffsets(JCas jcas, int start, int end)
  {
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator segmentItr = indexes.getAnnotationIndex(Segment.type).iterator();

    while (segmentItr.hasNext())
    {
      Segment seg = (Segment) segmentItr.next();

      if (seg.getBegin() <= start && seg.getEnd() >= end)
        return seg;
    }

    return null; // did not find a segment - cannot happen - we always have
                 // segments
  }
  
  /**
   * used by executeFSMs to add annotations for features for DrugMention
   * @param jcas
   * @param annotations
   * @param type
   */
  private void addAnnotations(JCas jcas, Set annotations, int type)
  {
    Iterator annItr = annotations.iterator();
    while(annItr.hasNext())
    {
      Annotation ann = null;
      BaseTokenImpl oldAnn = (BaseTokenImpl)annItr.next();
      
      if(FractionStrengthAnnotation.type == type)
        ann = new FractionStrengthAnnotation(jcas, oldAnn.getStartOffset(), oldAnn.getEndOffset());
      else if (DecimalStrengthAnnotation.type == type)
        ann = new DecimalStrengthAnnotation(jcas, oldAnn.getStartOffset(), oldAnn.getEndOffset());
      else if (DrugChangeStatusAnnotation.type == type)
      {
        ann = new DrugChangeStatusAnnotation(jcas, oldAnn.getStartOffset(), oldAnn.getEndOffset());
        ((DrugChangeStatusAnnotation)ann).setChangeStatus(((DrugChangeStatusToken)oldAnn).getDrugChangeStatus());
      }
      else if (RangeStrengthAnnotation.type == type)
        ann = new RangeStrengthAnnotation(jcas, oldAnn.getStartOffset(), oldAnn.getEndOffset());

      if(ann != null)
        ann.addToIndexes();
    }
  }
  
  //TODO: review the executeFSMs method
  /**
   * The namedE consists of a list of the Named Entities (NE) found in the prior
   * method. wordTokenList is included in case there are tokens representing
   * drug names not yet discovered. If no NE is found in a provided window size
   * and the provided confidence level has been met then the word token is
   * marked as a NE and added to a cache to hold undiscovered drug names.
   * 
   * @param jcas
   * @param baseTokenList
   * @param namedE
   * @param wordTokenList
   * @throws AnnotatorProcessException
   */
  private void executeFSMs(JCas jcas, List baseTokenList, List namedE,
      List wordTokenList) throws AnnotatorProcessException
  {
    try
    {
      Set fractionTokenSet = iv_fractionFSM.execute(baseTokenList);
      addAnnotations(jcas, fractionTokenSet, FractionStrengthAnnotation.type);

      Set decimalTokenSet = iv_decimalFSM.execute(baseTokenList);

      addAnnotations(jcas, decimalTokenSet, FractionStrengthAnnotation.type);

      Set statusTokenSet = iv_statusFSM.execute(baseTokenList);

      addAnnotations(jcas, statusTokenSet, DrugChangeStatusAnnotation.type);    
      //statusConfidence = true;

      Set rangeTokenSet = iv_rangeFSM.execute(baseTokenList);

      addAnnotations(jcas, decimalTokenSet, RangeStrengthAnnotation.type);

      Set strengthTokenSet = iv_strengthFSM.execute(baseTokenList, rangeTokenSet);
      
      Iterator measurementTokenItr = strengthTokenSet.iterator();
      int begin = 0, previous = 0;
      while (measurementTokenItr.hasNext())
      {
        Iterator chkNE = namedE.iterator();
        Iterator newNE = wordTokenList.iterator();
        boolean neFound = false;
        NamedEntity ne = null;
        WordToken we = null;

        StrengthToken mt = (StrengthToken) measurementTokenItr.next();
        int begSeg = mt.getStartOffset(), endSeg = mt.getEndOffset();
        StrengthAnnotation ma = new StrengthAnnotation(jcas, begSeg, endSeg);
        ma.addToIndexes();
        //TODO: Does this need to be commented out? Created Strength        
      }
      
      Set decTokenSet = null;
      {
        decTokenSet = iv_strengthFSM.execute(baseTokenList, decimalTokenSet,
            fractionTokenSet);
        Iterator decTokenItr = decTokenSet.iterator();

        while (decTokenItr.hasNext())
        {
          StrengthToken mt = (StrengthToken) decTokenItr.next();
          StrengthAnnotation ma = new StrengthAnnotation(jcas, mt
              .getStartOffset(), mt.getEndOffset());
          ma.addToIndexes();
          // loadandAppend("./strengthTable.csv", ma.getCoveredText(),
          // true);

        }
      }
      
      Set formTokenSet = iv_formFSM.execute(baseTokenList,
      /* decimalTokenSet */new HashSet());
      Iterator formTokenItr = formTokenSet.iterator();

      while (formTokenItr.hasNext())
      {
        FormToken mt = (FormToken) formTokenItr.next();
        FormAnnotation ma = new FormAnnotation(jcas, mt.getStartOffset(), mt
            .getEndOffset());

        ma.addToIndexes();
        // if (defaultNE != null && defaultNE.getCoveredText() != null)
        // defaultNE.setForm(ma.getCoveredText());

      }

      // last run checks for numbers (text or actual) in front of dosage
      // info and see how we align w/ the ne if they exist to determine
      // confidence factoring
//      if (!doseConfidence && decTokenSet != null)
      {
        Set preTokenSet = iv_strengthFSM.execute(baseTokenList, decTokenSet);
        Iterator preTokenItr = preTokenSet.iterator();

        while (preTokenItr.hasNext())
        {
          StrengthToken mt = (StrengthToken) preTokenItr.next();
          int begSeg = mt.getStartOffset();
          int endSeg = mt.getEndOffset();
          StrengthAnnotation ma = new StrengthAnnotation(jcas, begSeg, endSeg);
          ma.addToIndexes();
          // loadandAppend("./strengthTable.csv", ma.getCoveredText(),
          // true);

        }
      }
      Set doseTokenSet = iv_dosagesFSM.execute(baseTokenList, formTokenSet,
          strengthTokenSet);
      Iterator dosTokenItr = doseTokenSet.iterator();
      Iterator formCheckItr = formTokenSet.iterator();
      Iterator strengthCheckItr = strengthTokenSet.iterator();
      boolean foundDosage = false;
      int begSegDose = 0, endSegDose = 0;

      while (dosTokenItr.hasNext())
      {
        DosageToken mt = (DosageToken) dosTokenItr.next();
        begSegDose = mt.getStartOffset();
        endSegDose = mt.getEndOffset();
        DosagesAnnotation ma = new DosagesAnnotation(jcas, begSegDose,
            endSegDose);

        ma.addToIndexes();

      }
      Set suffixTokenSet = iv_suffixFSM
          .execute(baseTokenList, strengthTokenSet);

      Iterator suffixTokenItr = suffixTokenSet.iterator();
      while (suffixTokenItr.hasNext())
      {
        SuffixStrengthToken mt = (SuffixStrengthToken) suffixTokenItr.next();
        SuffixStrengthAnnotation ma = new SuffixStrengthAnnotation(jcas, mt
            .getStartOffset(), mt.getEndOffset());
        ma.addToIndexes();
      }
      // This needs to be handled differently. But since I'm not sure if
      // this feature will be utilized
      // I going to leave 'as is' for now.

      Set routeTokenSet = iv_routeFSM.execute(baseTokenList);
      boolean foundRoute = false;
      Iterator routeTokenItr = routeTokenSet.iterator();
      int begSegRT = 0, endSegRT = 0;
      while (routeTokenItr.hasNext())
      {
        RouteToken mt = (RouteToken) routeTokenItr.next();
        begSegRT = mt.getStartOffset();
        endSegRT = mt.getEndOffset();
        RouteAnnotation ma = new RouteAnnotation(jcas, begSegRT, endSegRT);
        ma.setIntakeMethod(new Integer(mt.getFormMethod()).toString());
        ma.addToIndexes();

      }

      Set frequencyUnitTokenSet = iv_frequencyUnitFSM.execute(baseTokenList);
      boolean foundFrequencyUnit = false;
      Iterator frequencyUnitTokenItr = frequencyUnitTokenSet.iterator();
      int begSegFUT = 0, endSegFUT = 0;
      while (frequencyUnitTokenItr.hasNext())
      {

        FrequencyUnitToken fut = (FrequencyUnitToken) frequencyUnitTokenItr
            .next();

        begSegFUT = fut.getStartOffset();
        endSegFUT = fut.getEndOffset();
        FrequencyUnitAnnotation ma = new FrequencyUnitAnnotation(jcas,
            begSegFUT, endSegFUT);
        ma.setPeriod(new Float(fut.getFrequencyUnitQuantity()).floatValue());
        ma.addToIndexes();

      }
      // The frequencyFSM can take advantage of the frequencyUnit to
      // establish conditions via the override
      Set frequencyTokenSet = iv_frequencyFSM.execute(baseTokenList,
          frequencyUnitTokenSet, rangeTokenSet);
      boolean foundFrequency = false;
      Iterator frequencyTokenItr = frequencyTokenSet.iterator();
      int begSegFT = 0, endSegFT = 0;
      while (frequencyTokenItr.hasNext())
      {

        FrequencyToken ft = (FrequencyToken) frequencyTokenItr.next();
        begSegFT = ft.getStartOffset();
        endSegFT = ft.getEndOffset();
        FrequencyAnnotation ma = new FrequencyAnnotation(jcas, begSegFT,
            endSegFT);
        ma.addToIndexes();

      }

      /* Check again if confidence was found during frequency check */

      Set durationTokenSet = iv_durationFSM.execute(baseTokenList,
          rangeTokenSet);
      Iterator durationTokenItr = durationTokenSet.iterator();

      int begSegDU = 0, endSegDU = 0;
      while (durationTokenItr.hasNext())
      {
        DurationToken du = (DurationToken) durationTokenItr.next();
        begSegDU = du.getStartOffset();
        endSegDU = du.getEndOffset();
        DurationAnnotation ma = new DurationAnnotation(jcas, begSegDU, endSegDU);
        // dm.setDurationElement(jcas, begSegDU, endSegDU);
        ma.addToIndexes();
        // loadandAppend("./frequencyTable.csv", ma.getCoveredText(),
        // true);
      }

    } catch (Exception e)
    {

      throw new AnnotatorProcessException(e);
    }
  }

  /*
   * The first primary range begins with a drug mention and ends with one of the
   * following (whichever comes first): 1) Another drug mention that is not
   * based on the first mention (e.g. Tylenol and Tylenol 200mg would be
   * considered as related and any related terms will be ignored) 2) A
   * DrugChangeStatus annotation that specifies a stop status change (an
   * exception would be when the SubSection annotation is being used, since it
   * would apply to the entire group) The end of a SubSection or Section
   */

  private void generateDrugMentions(JCas jcas, Segment seg) throws Exception
  {
    int begin = seg.getBegin(), end = seg.getEnd() + 1;
    NamedEntity nextNER = null;
    int nextNERPosition = 0;
    List uniqueNEs;
    List allNEs;

    int[] validNeTypes =
    { TypeSystemConst.NE_TYPE_ID_DRUG, TypeSystemConst.NE_TYPE_ID_UNKNOWN };
    
    allNEs = 
      FSUtil.getAnnotationsInSpan(jcas, NamedEntity.type, begin, end, validNeTypes);
    
    uniqueNEs = findUniqueMentions(allNEs.toArray());

    int lastNL = seg.getEnd();
    boolean lastOne = false;
    Iterator newLineItr = 
      FSUtil.getAnnotationsIteratorInSpan(jcas, NewlineToken.type, begin, end);
    
    for (int i = 0; i < uniqueNEs.size(); i++)
    {
      NamedEntity thisNER = (NamedEntity) uniqueNEs.get(i);
      boolean hasNext = false;
      if (uniqueNEs.size() > i + 1)
      {
        nextNER = (NamedEntity) uniqueNEs.get(i + 1);
        nextNERPosition = nextNER.getBegin();
        if (nextNER != null)
          hasNext = true;
      } else if (!uniqueNEs.isEmpty())
      {
        nextNER = (NamedEntity) uniqueNEs.get(i);
        nextNERPosition = nextNER.getBegin();
        lastOne = true;
      }
      boolean foundLeftParen = false;
      boolean foundRightParen = false;

      foundRightParen = findCoveredTextInSpan(jcas, PunctuationToken.type, thisNER.getEnd(), thisNER.getEnd()+3, (new String[]{")","/"}));
      
      
      if (hasNext && !lastOne)
        end = nextNERPosition;
      else
        end = seg.getEnd();

      boolean hasNLEnd = true;
      boolean wrapItUp = false;

      while (hasNLEnd && !wrapItUp && end <= seg.getEnd()
          && ((begin < end) || (!hasNext && begin <= end) || foundLeftParen))
      {

        if (begin == end) {
          foundLeftParen = false;
          end = end+1;
        }
        NewlineToken nl = null;
        if (hasNLEnd && newLineItr.hasNext())
        {
          nl = (NewlineToken) newLineItr.next();
          hasNLEnd = true;
        }
        if ((!hasNext && begin <= end) || (nextNERPosition < end))
          wrapItUp = true;
        boolean findNextNL = false;

        if (lastNL <= thisNER.getBegin())
        {
          begin = thisNER.getBegin();
        }

        if ((nl != null) && (thisNER.getBegin() >= nl.getEnd()))
        {
          findNextNL = true;

        } else if (nl != null)
        {
          lastNL = nl.getEnd();
        }

        if (!hasNext)
        {
          findNextNL = false;
          end = seg.getEnd();
        }
        if (!findNextNL)
        {
          if ((nextNER != null)
              && (((nextNER.getCoveredText().compareToIgnoreCase(
                  thisNER.getCoveredText()) == 0) || ((foundRightParen) || nextNER
                  .getBegin() == thisNER.getEnd() + 2))))
          {
            if (nl == null)
            {
              if (!hasNext)
                end = seg.getEnd();
            } else if (nextNER.getBegin() >= nl.getEnd() && hasNext)
            {
              end = nextNERPosition;
            } 
          } else if (hasNLEnd && hasNext)
          {

            foundLeftParen = findCoveredTextInSpan(jcas, PunctuationToken.type, nextNER.getBegin()-1, nextNER.getBegin()+1, (new String[]{"(","/"}));
            
           /* if (nl == null && foundLeftParen && !hasNext)
            {
              end = seg.getEnd();
            } else */if (nl != null && nl.getEnd() > nextNER.getBegin()
                && !foundLeftParen)
            {
              end = nextNERPosition;
            } /*else if (foundLeftParen && nl != null)
            {
              end = nl.getEnd();
            } */else
            {
              end = nextNER.getBegin();
            }
          } else if (hasNext)
          {
            end = nextNERPosition;
          } else
            end = seg.getEnd();

          if (begin <  end)
          {
            findDrugAttributesInRange(jcas, begin, end);
//TODO: need to fix - use the list above - uniqueNEs and subset that list instead of getting new list of annotations
            List neTokenUpdatedList = getAnnotationsInSpan(jcas,
                NamedEntity.type, begin, end + 1);
//TODO: 10/28/2010 -- exception
// it seems that this can still happen triggered by either from FSM or a case where the array exceeds the length            
            if (!neTokenUpdatedList.isEmpty())
            {
              List globalDrugNERList = new ArrayList();
              try
              {
                generateDrugMentionsAndAnnotations(jcas, neTokenUpdatedList,
                    begin, end, null, null, 0, globalDrugNERList);
              } catch (NumberFormatException nfe)
              {
            	  iv_logger.info(nfe.getMessage());
              } catch (Exception e)
              {
            	  iv_logger.info(e.getMessage());
              }

              globalDrugNERList.clear();
            }
          }
          begin = end;
        }
      }
    }
  }

  private boolean findCoveredTextInSpan(JCas jcas, int annotationType, int beginOffset, int endOffset, String[] searchStrs)
  {
    boolean foundCoveredText = false;
    
    Iterator coveredTextIter = FSUtil.getAnnotationsIteratorInSpan(jcas, annotationType, beginOffset, endOffset);
    while (coveredTextIter.hasNext() && !foundCoveredText)
    {
      Annotation ann= (Annotation) coveredTextIter.next();
      for(int i=0; i<searchStrs.length && !foundCoveredText; i++)
        foundCoveredText = searchStrs[i].equals(ann.getCoveredText());
    }
    
    return foundCoveredText;
  }
  
  private List getAnnotationsInSpanWithAdaptToBaseTokenFSM(JCas jcas, int type,
      int begin, int end) throws Exception
  {
    List list = getAnnotationsInSpan(jcas, type, begin, end);
    for (int i = 0; i < list.size(); i++)
    {
      list.add(i, adaptToFSMBaseToken((BaseToken) list.get(i)));
      list.remove(i + 1);
    }
    return list;
  }

  private List<Annotation> getAnnotationsInSpan(JCas jcas, int type, int begin, int end)
  {
    List<Annotation> list = new ArrayList<Annotation>();
    FSIterator annItr = FSUtil.getAnnotationsIteratorInSpan(jcas, type, begin,
        end);
    while (annItr.hasNext())
    {
      Annotation ann = (Annotation) annItr.next();
      list.add(ann);
    }
    return list;
  }

  /**
   * finds drug attributes using the given range, this method uses FSM
   * 
   * @param jcas
   * @param begin
   * @param end
   * @throws Exception
   */

  private void findDrugAttributesInRange(JCas jcas, int begin, int end)
      throws Exception
  {
    List baseTokenList = getAnnotationsInSpanWithAdaptToBaseTokenFSM(jcas, BaseToken.type, begin, end + 1);
    List<Annotation> neTokenList = getAnnotationsInSpan(jcas, NamedEntity.type, begin, end + 1);
    List<Annotation> weTokenList = getAnnotationsInSpan(jcas, WordToken.type, begin, end + 1);

    // execute FSM logic
    executeFSMs(jcas, baseTokenList, neTokenList, weTokenList);
  }

  private void generateDrugMentionsAndAnnotations(JCas jcas, List<NamedEntity> nerTokenList,
      int begin, int end, DrugMentionAnnotation recurseNER,
      String [] relatedStatus, int countNER, List<DrugMentionAnnotation> globalDrugNER) throws Exception
  {

    Iterator<NamedEntity> uniqueNER = nerTokenList.iterator();
    DrugMentionAnnotation drugTokenAnt = null;
    NamedEntity tokenAnt = null;

    List<DrugMentionAnnotation> holdDrugNERArr = new ArrayList<DrugMentionAnnotation>();

    while (uniqueNER.hasNext())
    {

      tokenAnt = (NamedEntity) uniqueNER.next();
      boolean isDrugNER = false;
      FSArray ocArr = tokenAnt.getOntologyConceptArr();
      if (ocArr != null)
      {
        for (int i = 0; i < ocArr.size() && !isDrugNER; i++)
        {
          OntologyConcept oc = (OntologyConcept) ocArr.get(i);

          String scheme = oc.getCodingScheme();
          if (scheme.compareTo("RXNORM") == 0)
          {
            isDrugNER = true;
          }

        }
      }
      if (tokenAnt != null && (isDrugNER || relatedStatus != null))
      {
    	boolean keepNoChangeStatus = false;

        boolean maxExists = false;
        int maxOffsetEnd = 0;
        int holdRightEnd = end + 1;
        int holdLeftStart = begin;
        drugTokenAnt = new DrugMentionAnnotation(jcas, tokenAnt.getBegin(),
            tokenAnt.getEnd());
        tokenAnt.setTypeID(NERTypeIdentifier);
        holdDrugNERArr.add(drugTokenAnt);

        Iterator statusChangeItr = FSUtil.getAnnotationsIteratorInSpan(jcas,
            DrugChangeStatusAnnotation.type, holdLeftStart, holdRightEnd);
        List holdStatusChanges = new ArrayList();
        // Separate the subsection from the change status elements

        while (statusChangeItr.hasNext())
        {
          Iterator findSubSection = FSUtil.getAnnotationsIteratorInSpan(jcas,
              SubSectionAnnotation.type, holdLeftStart, holdRightEnd);
          // if there aren't subsection in the span add to the hold
          // status changes list (unless Maximum special case)
          boolean isolate = false;
          if (!findSubSection.hasNext())
          {

            DrugChangeStatusAnnotation dsa = (DrugChangeStatusAnnotation) statusChangeItr
                .next();
            // Maximum case means the drug mention elements should
            // be overridden by this value
            if ((dsa.getChangeStatus().compareTo(
                DrugChangeStatusElement.MAXIMUM_STATUS) != 0)
                && dsa.getEnd() < holdRightEnd)
            {
            	holdStatusChanges.add(dsa);
            } else if (dsa.getChangeStatus().compareTo(
            		DrugChangeStatusElement.MAXIMUM_STATUS) == 0
            		&& dsa.getEnd() < holdRightEnd)
            {
            	maxExists = true;
            	maxOffsetEnd = dsa.getEnd();
            }

          } else
          {
        	  boolean noWeirdError = true;
        	  boolean pullOut = false;
        	  while (!pullOut & !isolate && findSubSection.hasNext()
        			  && noWeirdError)
        	  {
        		  try
        		  {
        			  // each status change is checked against all
        			  // available sub-spans in that range
        			  SubSectionAnnotation sub = (SubSectionAnnotation) findSubSection
        			  .next();
        			  Iterator findStartLF = FSUtil.getAnnotationsIteratorInSpan(
        					  jcas, NewlineToken.type, holdLeftStart, sub.getBegin() + 1);
        			  Iterator findEndLF = FSUtil.getAnnotationsIteratorInSpan(jcas,
        					  NewlineToken.type, sub.getEnd(), holdRightEnd);

        			  if (findStartLF.hasNext() && findEndLF.hasNext())
        			  {

        				  while (findStartLF.hasNext())
        				  {
        					  // int countSymbols = 0;
        					  NewlineToken nta = (NewlineToken) findStartLF.next();

        					  // Iterator findSymbols =
        					  // FSUtil.getAnnotationsInSpanIterator(jcas,
        					  // SymbolToken.type,
        					  // nta.getEnd(), sub.getBegin());
        					  //					
        					  // while (findSymbols.hasNext())
        					  // {
        					  // findSymbols.next();
        					  // countSymbols++;
        					  // }

        					  int countSymbols = FSUtil.countAnnotationsInSpan(jcas,
        							  SymbolToken.type, nta.getEnd(), sub.getBegin());

        					  if ((nta.getEnd() + countSymbols + 1) >= sub.getBegin())
        					  {
        						  isolate = true;
        						  holdRightEnd = sub.getBegin();
        						  end = sub.getBegin();
        					  }
        				  }

        				  if (!isolate)
        				  {
        					  DrugChangeStatusAnnotation dsa = (DrugChangeStatusAnnotation) statusChangeItr
        					  .next();
        					  holdStatusChanges.add(dsa);
        					  pullOut = true;
        					  sub.removeFromIndexes();
        				  }
        			  } else if (findEndLF.hasNext())
        			  {
        				  // subsection is on a prior separate line than the rest
        				  // of the content
        				  holdLeftStart = sub.getEnd();
        				  // sub.removeFromIndexes();

        			  } else if (sub.getBegin() > tokenAnt.getEnd())
        			  {
        				  end = sub.getBegin();
        				  holdRightEnd = sub.getBegin();
        				  sub.removeFromIndexes();
        			  } else
        			  {
        				  holdLeftStart = sub.getEnd();
        				  holdRightEnd = tokenAnt.getBegin();
        			  }
        		  } catch (NoSuchElementException nsee)
        		  {
        			  noWeirdError = false;
        			  iv_logger.info(nsee.getLocalizedMessage());
        		  }
        	  }
          }
        }

        // handles cases like "then discontinue" so the two change status mentions are merged and the last 
        // value is used for the change status i.e. 'discontinue'

        List modifiedOrderDrugStatusChanges = new ArrayList();
        Iterator sortStatusChanges = sortAnnotations(
        		holdStatusChanges.toArray()).iterator();
        Iterator sortNextStatusChanges = sortAnnotations(
        		holdStatusChanges.toArray()).iterator();
        // increment sortNextStatusChanges
        if (sortNextStatusChanges.hasNext()) sortNextStatusChanges.next();
        boolean skipNext = false;
        int checkSkippedOffsetBegin = 0, checkSkippedOffsetEnd = 0;
        while (sortStatusChanges.hasNext()) {
        	DrugChangeStatusAnnotation hos1 = (DrugChangeStatusAnnotation) sortStatusChanges.next();
        	if (sortNextStatusChanges.hasNext()) {

        		DrugChangeStatusAnnotation hos2 = (DrugChangeStatusAnnotation) sortNextStatusChanges.next();
        		if (hos1.getBegin() == hos2.getBegin()) {
        			if (hos1.getEnd() >= hos2.getEnd()) {
        				skipNext = true;
        				checkSkippedOffsetBegin = hos2.getBegin();
        				checkSkippedOffsetEnd = hos2.getEnd();
        				hos2.removeFromIndexes();
        				modifiedOrderDrugStatusChanges.add(hos1);

        			} else {
        				iv_logger.info("found reverse case . . need to handle");
        			}

        		} else if (!skipNext) {
        			modifiedOrderDrugStatusChanges.add(hos1);
        		} else 
        			skipNext = false;
        	}
        	else if (checkSkippedOffsetBegin == 0 || (checkSkippedOffsetBegin != hos1.getBegin() && checkSkippedOffsetEnd != hos1.getEnd())){
        		modifiedOrderDrugStatusChanges.add(hos1);
        	}
        }        

        Iterator orderedStatusChanges = sortAnnotations(
        		holdStatusChanges.toArray()).iterator();
        Iterator orderedDrugStatusChanges = sortAnnotations(
        		holdStatusChanges.toArray()).iterator();

        if (modifiedOrderDrugStatusChanges.size() > 0 ) {
        	int [] newSpan = {begin, end};
        	newSpan = statusChangePhraseGenerator ( jcas,  begin,  end,  maxExists,  uniqueNER, 
        			orderedStatusChanges,  modifiedOrderDrugStatusChanges,  relatedStatus,  drugTokenAnt,  
        			globalDrugNER,  countNER );
        	begin = newSpan[0];
        	end = newSpan[1];
        	if ((drugTokenAnt.getDrugChangeStatus() != null && drugTokenAnt.getDrugChangeStatus().equals(DrugChangeStatusToken.NOCHANGE)) ||
        			(drugTokenAnt.getDrugChangeStatus() != null && drugTokenAnt.getDrugChangeStatus().equals(DrugChangeStatusToken.OTHER)))  {
        		keepNoChangeStatus = true;
        		if (drugTokenAnt.getDrugChangeStatus().equals(DrugChangeStatusToken.OTHER))
        			drugTokenAnt.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
        	}            
        	// No change is default state since the change state has been handled
        }
        DrugMention dm = new DrugMention(jcas, begin, end);	
        boolean overrideStatus = false;
        boolean statusFound = false;
        if (!keepNoChangeStatus) {
        // All entries may not be appropriate, so some
        // filtering
        // may need to be implemented here
        JFSIndexRepository indexes = jcas.getJFSIndexRepository();
        Iterator subSectionItr = indexes.getAnnotationIndex(
        		SubSectionAnnotation.type).iterator();
    
        String statusKey = null;
        while (subSectionItr.hasNext() && !statusFound)
        {

        	SubSectionAnnotation ssid = (SubSectionAnnotation) subSectionItr
        	.next();

        	if (ssid.getSubSectionBodyBegin() <= tokenAnt.getBegin()
        			&& ssid.getSubSectionBodyEnd() >= tokenAnt.getEnd())
        	{

        		// Look for special case where date comes before the
        		// drug mention
        		// A better means to locate the beginning of the chunk
        		// is lacking here mainly due
        		// to the fact that the sentence annotator cannot be
        		// trusted to find the beginning
        		// accurately.
        		boolean overrideDate = false;
        		Iterator statusSpecialDateItr = FSUtil
        		.getAnnotationsIteratorInSpan(jcas, DateAnnotation.type, ssid
        				.getEnd(), drugTokenAnt.getBegin());
        		while (statusSpecialDateItr.hasNext() && !overrideDate)
        		{
        			DateAnnotation specialDate = (DateAnnotation) statusSpecialDateItr
        			.next();
        			Iterator findLF = FSUtil.getAnnotationsIteratorInSpan(jcas,
        					NewlineToken.type, ssid.getEnd(), specialDate.getBegin());
        			if (!findLF.hasNext())
        			{
        				// if (specialDate.getEnd() <=
        				// drugTokenAnt.getBegin() ){
        				drugTokenAnt.setStartDate(specialDate.getCoveredText());
        				overrideDate = true;
        			}
        		}

        		DrugChangeStatusAnnotation dsa = null;
        		if (orderedDrugStatusChanges.hasNext())
        		{
        			dsa = (DrugChangeStatusAnnotation) orderedDrugStatusChanges
        			.next();
        		}
        		if (dsa != null
        				&& (dsa.getChangeStatus().compareTo(
        						DrugChangeStatusElement.START_STATUS) == 0 || dsa
        						.getChangeStatus().compareTo(
        								DrugChangeStatusElement.STOP_STATUS) == 0))
        		{
        			// Should we override here? Let's get only the first
        			// one as an override

        			drugTokenAnt.setDrugChangeStatus(dsa.getChangeStatus());
        		} else
        		{
        			statusKey = dm.convertToChangeStatus(ssid.getCoveredText());
        			if (ssid.getStatus() == 1)
        			{

        				// drugTokenAnt.setCertainty(-1);
        				statusKey = DrugChangeStatusToken.STOP;
        			}
        			if (statusKey.compareTo(DrugChangeStatusToken.NOCHANGE) == 0)
        			{
        				Iterator oneDrugChangeStatus = FSUtil
        				.getAnnotationsIteratorInSpan(jcas,
        						DrugChangeStatusAnnotation.type, ssid.getBegin(), ssid
        						.getEnd() + 1);
        				if (oneDrugChangeStatus.hasNext())
        				{
        					dsa = (DrugChangeStatusAnnotation) oneDrugChangeStatus.next();
        					drugTokenAnt.setDrugChangeStatus(dsa.getChangeStatus());
        					statusKey = dsa.getChangeStatus();
        				}
        			}
        			drugTokenAnt.setStatus(ssid.getStatus());
        			dm.setDrugChangeStatusElement(statusKey, begin, end);

        			statusFound = true;
        		}
        	}
        }

        // Look for special case where status comes before the drug
        // mention
        // A better means to locate the beginning of the chunk is
        // lacking here mainly due
        // to the fact that the sentence annotator cannot be trusted to
        // find the beginning
        // accurately.

        Iterator statusSpecialChangeItr = FSUtil.getAnnotationsIteratorInSpan(
        		jcas, DrugChangeStatusAnnotation.type, begin - 20, drugTokenAnt
        		.getBegin() + 1);
        while (statusSpecialChangeItr.hasNext())
        {
        	DrugChangeStatusAnnotation specialDsa = (DrugChangeStatusAnnotation) statusSpecialChangeItr
        	.next();
        	if (specialDsa.getEnd() + 1 == drugTokenAnt.getBegin()
        			&& relatedStatus == null)
        	{
        		drugTokenAnt.setDrugChangeStatus(specialDsa.getChangeStatus());
        		drugTokenAnt.setChangeStatusBegin(specialDsa.getBegin());
        		drugTokenAnt.setChangeStatusEnd(specialDsa.getEnd());
        		overrideStatus = true;
        	}
        }
        }
        // If a strength token is discovered before the next
        // distinguished
        // drug mentions then the remaining sentence is scanned for
        // DrugChangeStatus.
        // Iterator strengthAllItr = FSUtil.getAnnotationsIteratorInSpan(
        // jcas, StrengthAnnotation.type, begin, end + 1);
        //
        // List holdStrength = new ArrayList();
        // while (strengthAllItr.hasNext()) {
        // StrengthAnnotation sa = (StrengthAnnotation) strengthAllItr
        // .next();
        // holdStrength.add(sa);
        // }
        String strengthText = null;
        boolean onlyNeedOneStrength = false;
        if (!keepNoChangeStatus || (drugTokenAnt.getStrength() == null)) {
        List holdStrength = getAnnotationsInSpan(jcas, StrengthAnnotation.type,
        		begin, end + 1);

        Iterator strengthItr = findUniqueMentions(holdStrength.toArray())
        .iterator();

        double strengthValue = 0;


        int holdStrengthBeginOffset = 0, holdStrengthEndOffset = 0;


        	while (strengthItr.hasNext() && !onlyNeedOneStrength)
        	{
        		StrengthAnnotation sa = (StrengthAnnotation) strengthItr.next();

        		if (holdStrengthBeginOffset != sa.getBegin()
        				&& holdStrengthEndOffset != sa.getEnd()
        				&& (relatedStatus != null))
        		{

        			double curStrengthValue = 0;

        			int hyphenLocation = sa.getCoveredText().indexOf("-");
        			String holdStrengthValue = sa.getCoveredText();

        			if (hyphenLocation > 0)
        			{
        				holdStrengthValue = holdStrengthValue
        				.substring(0, hyphenLocation);
        			}

        			int spaceLocation = holdStrengthValue.indexOf(" ");

        			if (spaceLocation > 0)
        			{
        				holdStrengthValue = holdStrengthValue.substring(0, spaceLocation);
        			}

        			if (holdStrengthValue != null
        					&& holdStrengthValue.compareTo("") != 0)
        				curStrengthValue = new Double(dm
        						.parseDoubleValue(holdStrengthValue)).doubleValue();
        			boolean findLowValue = true;

        			if (relatedStatus[0].compareTo(DrugChangeStatusToken.INCREASE) == 0)
        			{
        				if (curStrengthValue > strengthValue)
        				{
        					strengthValue = curStrengthValue;
        					strengthText = dm.getStrengthElement();
        				}
        			} else if (relatedStatus[0].compareTo(DrugChangeStatusToken.DECREASE) == 0)
        			{
        				if (findLowValue)
        					strengthValue = curStrengthValue;
        				if (curStrengthValue <= strengthValue)
        				{
        					strengthValue = curStrengthValue;
        					strengthText = dm.getStrengthElement();
        				}
        				findLowValue = false;

        			} else if (relatedStatus[0].compareTo(DrugChangeStatusToken.SUM) == 0)
        			{

        				strengthValue = curStrengthValue;
        				strengthText = dm.getStrengthElement();
        				// get first value found
        			}
        		} else
        		{
        			strengthText = dm.getStrengthElement();
        			if (!maxExists)
        				onlyNeedOneStrength = true;
        			else if (maxOffsetEnd + 1 == sa.getBegin())
        			{
        				onlyNeedOneStrength = true;
        				strengthText = sa.getCoveredText();
        			}
        		}

        		holdStrengthBeginOffset = sa.getBegin();
        		holdStrengthEndOffset = sa.getEnd();

        	}
      }
        String doseText = null;
        if (!keepNoChangeStatus || (drugTokenAnt.getDosage() == null)) {
        Iterator dosageItr = FSUtil.getAnnotationsIteratorInSpan(jcas,
        		DosagesAnnotation.type, begin, end + 1);
        List holdDosages = new ArrayList();
        double doseValue = 0;

        int holdDoseBeginOffset = 0, holdDoseEndOffset = 0;
        boolean onlyNeedOneDose = false;

        	while (dosageItr.hasNext() && !onlyNeedOneDose)
        	{
        		DosagesAnnotation da = (DosagesAnnotation) dosageItr.next();
        		if (holdDoseBeginOffset != da.getBegin()
        				&& holdDoseEndOffset != da.getEnd() && relatedStatus != null)
        		{
        			int removeComma = da.getCoveredText().indexOf(',');
        			String doseTextCheck = da.getCoveredText();
        			if (removeComma > 0)
        			{
        				doseTextCheck = doseTextCheck.substring(0, removeComma);
        			}
        			double curDoseValue = new Double(dm
        					.convertFromTextToNum(doseTextCheck)).doubleValue();
        			boolean findLowValue = true;
        			if (relatedStatus[0].compareTo(DrugChangeStatusToken.INCREASE) == 0)
        			{
        				if (curDoseValue > doseValue)
        				{
        					doseValue = curDoseValue;
        					doseText = dm.getDosageElement();
        				} else if (relatedStatus[0].compareTo(DrugChangeStatusToken.SUM) == 0)
        				{

        					doseValue = curDoseValue;
        					doseText = dm.getDosageElement();

        				}
        			} else if (relatedStatus[0].compareTo(DrugChangeStatusToken.DECREASE) == 0)
        			{
        				if (findLowValue)
        					doseValue = curDoseValue;
        				if (curDoseValue <= doseValue)
        				{
        					doseValue = curDoseValue;
        					doseText = dm.getDosageElement();
        				}
        				findLowValue = false;
        			}
        			holdDosages.add(da);
        			holdDoseBeginOffset = da.getBegin();
        			holdDoseEndOffset = da.getEnd();
        		} else
        		{
        			doseText = dm.getDosageElement();

        			if (!maxExists)
        				onlyNeedOneDose = true;
        		}
        	}
        }
        String frequencyText = null;
        if (!keepNoChangeStatus || (drugTokenAnt.getFrequency() == null)) {
        Iterator freqItr = FSUtil.getAnnotationsIteratorInSpan(jcas,
        		FrequencyAnnotation.type, begin, end + 1);

        List holdFreqItr = new ArrayList();
        while (freqItr.hasNext())
        {
        	holdFreqItr.add(freqItr.next());
        }
        Iterator frequencyItr = sortAnnotations(holdFreqItr.toArray())
        .iterator();

        List holdFrequency = new ArrayList();
        double frequencyValue = 0;

        int holdFrequencyBeginOffset = 0, holdFrequencyEndOffset = 0;
        boolean onlyNeedOneFrequency = false;

        	while (frequencyItr.hasNext() && !onlyNeedOneFrequency)
        	{
        		FrequencyAnnotation fa = (FrequencyAnnotation) frequencyItr.next();

        		if (dm.frequency != null
        				&& dm.frequency.getFrequencyMention() == null)
        		{
        			double curFrequencyValue = new Double(dm.convertFromTextToNum(fa
        					.getCoveredText())).doubleValue();
        			String curFreqValueText = new Double(curFrequencyValue).toString();
        			dm
        			.setFrequencyElement(curFreqValueText, fa.getBegin(), fa
        					.getEnd());
        			frequencyText = curFreqValueText;
        		}
        		onlyNeedOneFrequency = true;

        		holdFrequency.add(fa);
        		holdFrequencyBeginOffset = fa.getBegin();
        		holdFrequencyEndOffset = fa.getEnd();
        	}
        }
        boolean foundPRN = false;
        String frequencyUnitText = null;
        if (!keepNoChangeStatus || (drugTokenAnt.getFrequencyUnit() == null)) {
        Iterator frequencyUnitItr = FSUtil.getAnnotationsIteratorInSpan(jcas,
        		FrequencyUnitAnnotation.type, begin, end + 1);
        List holdFrequencyUnit = new ArrayList();
        double frequencyUnitValue = 0;

        int holdFrequencyUnitBeginOffset = 0, holdFrequencyUnitEndOffset = 0;
        boolean onlyNeedOneFrequencyUnit = false;
 
       
        	while (frequencyUnitItr.hasNext() && !onlyNeedOneFrequencyUnit)
        	{
        		FrequencyUnitAnnotation fua = (FrequencyUnitAnnotation) frequencyUnitItr
        		.next();
        		if (holdFrequencyUnitBeginOffset != fua.getBegin()
        				&& holdFrequencyUnitEndOffset != fua.getEnd()
        				&& relatedStatus != null)
        		{
        			double curFrequencyUnitValue = new Float(fua.getPeriod())
        			.doubleValue();

        			boolean findLowValue = true;
        			if (relatedStatus[0].compareTo(DrugChangeStatusToken.INCREASE) == 0)
        			{
        				if (curFrequencyUnitValue > frequencyUnitValue)
        				{
        					frequencyUnitValue = curFrequencyUnitValue;
        					frequencyUnitText = dm.getFrequencyUnitElement();
        				}
        			} else if (relatedStatus[0] == null
        					|| relatedStatus[0].compareTo(DrugChangeStatusToken.DECREASE) == 0)
        			{
        				if (findLowValue)
        					frequencyUnitValue = curFrequencyUnitValue;
        				if (curFrequencyUnitValue <= frequencyUnitValue)
        				{
        					frequencyUnitValue = curFrequencyUnitValue;
        					frequencyUnitText = dm.getFrequencyUnitElement();
        				}
        				findLowValue = false;
        			}
        		} else
        		{
        			if (fua.getPeriod() == FrequencyUnitToken.QUANTITY_PRN)
        				foundPRN = true;
        			else
        			{
        				frequencyUnitText = dm.getFrequencyUnitElement();

        				if (!maxExists)
        				{
        					onlyNeedOneStrength = true;
        				}

        			}
        		}

        		holdFrequencyUnit.add(fua);
        		holdFrequencyUnitBeginOffset = fua.getBegin();
        		holdFrequencyUnitEndOffset = fua.getEnd();
        	}

        }
        if (recurseNER != null && recurseNER.getDrugChangeStatus() != null
        		&& relatedStatus[0] != null  && dm.changeStatus == null)
        	drugTokenAnt.setDrugChangeStatus(relatedStatus[0]);
        else if (keepNoChangeStatus || (dm.changeStatus != null && 
        		(dm.changeStatus.getDrugChangeStatus().equals(DrugChangeStatusToken.INCREASEFROM) 
        				|| dm.changeStatus.getDrugChangeStatus().equals(DrugChangeStatusToken.DECREASEFROM)))) {
        	drugTokenAnt.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
        }
        else if (dm.getDrugChangeStatusElement() != null
        		&& dm.getDrugChangeStatusElement().compareTo("") != 0
        		&& dm.getDrugChangeStatusElement().compareTo(
        				DrugChangeStatusToken.NOCHANGE) != 0
        				/*
        				 * && drugTokenAnt.getDrugChangeStatus() != null && drugTokenAnt
        				 * .getDrugChangeStatus().compareTo(DrugChangeStatusToken .NOCHANGE)
        				 * == 0
        				 */
        				&& !overrideStatus)
        {
        	// Don't want subsections here

        	Iterator negateStatusChanges = FSUtil.getAnnotationsIteratorInSpan(
        			jcas, SubSectionAnnotation.type,
        			dm.changeStatus.getBeginOffset(),
        			dm.changeStatus.getEndOffset() + 2);
        	if ((!negateStatusChanges.hasNext() || statusFound) && !keepNoChangeStatus) {
        		drugTokenAnt.setDrugChangeStatus(dm.getDrugChangeStatusElement());
        		drugTokenAnt.setChangeStatusBegin(dm.getChangeStatusBegin());
        		drugTokenAnt.setChangeStatusEnd(dm.getChangeStatusEnd());
        	}
        	else
        		drugTokenAnt.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
        } else if (relatedStatus != null && relatedStatus[0] != null) {
        	drugTokenAnt.setDrugChangeStatus(relatedStatus[0]);
        	drugTokenAnt.setChangeStatusBegin(new Integer (relatedStatus[1]).intValue());
        	drugTokenAnt.setChangeStatusEnd(new Integer (relatedStatus[2]).intValue());
        } else if (drugTokenAnt.getDrugChangeStatus() == null
        		|| drugTokenAnt.getDrugChangeStatus().compareTo("") == 0)
        	drugTokenAnt.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
        if (!keepNoChangeStatus) {
        	String relatedStatusString = null;
        	if (relatedStatus != null && relatedStatus[0] != null)
        		relatedStatusString = relatedStatus[0];
        	float confidenceScore = alignDrugMentionAttributes( strengthText,  dm ,  drugTokenAnt,  recurseNER,  relatedStatusString,  statusFound,  overrideStatus,
        		maxExists,  doseText,  frequencyText,  frequencyUnitText);
        	drugTokenAnt.setConfidence(confidenceScore);
        }

        if (foundPRN)
        	drugTokenAnt.setDrugChangeStatus(drugTokenAnt.getDrugChangeStatus());

        ChunkAnnotation ca = new ChunkAnnotation(jcas, begin, end);
        ca.addToIndexes();
        ca.setSentenceID(tokenAnt.getSentenceID());

        drugTokenAnt.addToIndexes();
        globalDrugNER.add(drugTokenAnt);

      }

      if (isDrugNER)
      {
        countNER = globalDrugNER.size();
        FSArray drugNERArr = new FSArray(jcas, countNER);
        for (int i = 0; i < countNER; i++)
        {
          drugNERArr.set(i, (FeatureStructure) globalDrugNER.get(i));
        }
        tokenAnt.setDrugMentionArr(drugNERArr);
      }
    }
  }

  private float alignDrugMentionAttributes(String strengthText, DrugMention dm , DrugMentionAnnotation drugTokenAnt, DrugMentionAnnotation recurseNER, String relatedStatus, boolean statusFound, boolean overrideStatus,
			boolean maxExists, String doseText, String frequencyText, String frequencyUnitText)
  {
      
      float keepScoreOfConfidence = (float) 0.05;
      if (dm.getStrengthElement() != null && dm.getStrengthElement() != ""
          && dm.getStrengthElement().compareTo("null") != 0)
        keepScoreOfConfidence = (float) 0.15;
      if (strengthText != null /* && holdStatusChanges.isEmpty() */)
      {
        drugTokenAnt.setStrength(strengthText);
        drugTokenAnt.setStrengthBegin(dm.getStrengthBegin());
        drugTokenAnt.setStrengthEnd(dm.getStrengthEnd());
      } else if (recurseNER != null && recurseNER.getStrength() != null)
      {
        drugTokenAnt.setStrength(recurseNER.getStrength());
        drugTokenAnt.setStrengthBegin(recurseNER.getStrengthBegin());
        drugTokenAnt.setStrengthEnd(recurseNER.getStrengthEnd());
      } else if (dm.getStrengthElement() != null && dm.strength != null)
      {
        drugTokenAnt.setStrength(dm.getStrengthElement());
        drugTokenAnt.setStrengthBegin(dm.getStrengthBegin());
        drugTokenAnt.setStrengthEnd(dm.getStrengthEnd());
      }
      if ((dm.getDosageElement() != null && dm.getDosageElement().compareTo(
          "null") != 0)
          && doseText != null)
        keepScoreOfConfidence = keepScoreOfConfidence + (float) 0.05;
      if (doseText != null /* && holdStatusChanges.isEmpty() */)
      {

        if (maxExists)
        {
          drugTokenAnt.setDosage("1.0");
        } else
        {
          drugTokenAnt.setDosage(doseText);
          drugTokenAnt.setDosageBegin(dm.getDosageBegin());
          drugTokenAnt.setDosageEnd(dm.getDosageEnd());
        }
      } else if ((recurseNER != null) && (recurseNER.getDosage() != null))
      {
        drugTokenAnt.setDosage(recurseNER.getDosage());
        drugTokenAnt.setDosageBegin(recurseNER.getDosageBegin());
        drugTokenAnt.setDosageEnd(recurseNER.getDosageEnd());
      } else if (dm.getDosageElement() != null && dm.dosage != null)
      {

        drugTokenAnt.setDosage(dm.getDosageElement());
        drugTokenAnt.setDosageBegin(dm.getDosageBegin());
        drugTokenAnt.setDosageEnd(dm.getDosageEnd());
      }

      // Only want the updated element in this case
      if (dm.getDurationElement() != null
          && dm.getDurationElement().compareTo("") != 0
          && dm.getDurationElement().compareTo("null") != 0)
      {
        keepScoreOfConfidence = keepScoreOfConfidence * (float) 1.3;
        drugTokenAnt.setDuration(dm.getDurationElement());
        drugTokenAnt.setDurationBegin(dm.getDuratationBegin());
        drugTokenAnt.setDurationEnd(dm.getDuratationEnd());
      }
      if (dm.getStartDateElement() != null
          && dm.getStartDateElement().compareTo("") != 0)
        drugTokenAnt.setStartDate(dm.getStartDateElement());

      if ((dm.getFormElement() != null)
          && (dm.getFormElement().compareTo("") != 0)
          && dm.getFormElement().compareTo("null") != 0)
      {

        keepScoreOfConfidence = keepScoreOfConfidence * (float) 1.3;
        drugTokenAnt.setForm(dm.getFormElement());
        drugTokenAnt.setFormBegin(dm.getFormBegin());
        drugTokenAnt.setFormEnd(dm.getFormEnd());
      } else if (recurseNER != null && recurseNER.getForm() != null)
      {
        drugTokenAnt.setForm(recurseNER.getForm());
        drugTokenAnt.setFormBegin(recurseNER.getFormBegin());
        drugTokenAnt.setFormEnd(recurseNER.getFormEnd());
      }
      if (dm.getFrequencyElement() != null
          && dm.getFrequencyElement().compareTo("") != 0
          && dm.frequency != null
          && dm.getFrequencyElement().compareTo("null") != 0)
        keepScoreOfConfidence = keepScoreOfConfidence * (float) 1.5;
      if (frequencyText != null /* && holdStatusChanges.isEmpty() */)
      {

        if (maxExists)
        {
          drugTokenAnt.setFrequency("1.0");
        } else
          drugTokenAnt.setFrequency(frequencyText);
        drugTokenAnt.setFrequencyBegin(dm.getFrequencyBegin());
        drugTokenAnt.setFrequencyEnd(dm.getFrequencyEnd());
      } else if (recurseNER != null && recurseNER.getFrequencyUnit() != null)
      {
        drugTokenAnt.setFrequency(recurseNER.getFrequency());
        drugTokenAnt.setFrequencyBegin(recurseNER.getFrequencyBegin());
        drugTokenAnt.setFrequencyEnd(recurseNER.getFrequencyEnd());
      } else if (dm.getFrequencyElement() != null && dm.frequency != null)
      {
        drugTokenAnt.setFrequency(dm.getFrequencyElement());
        drugTokenAnt.setFrequencyBegin(dm.getFrequencyBegin());
        drugTokenAnt.setFrequencyEnd(dm.getFrequencyEnd());
      }
      if (dm.getFrequencyUnitElement() != null
          && dm.getFrequencyUnitElement().compareTo("") != 0
          && dm.frequencyUnit != null)
        keepScoreOfConfidence = keepScoreOfConfidence * (float) 1.5;
      if (frequencyUnitText != null /* && holdStatusChanges.isEmpty() */)
      {

        drugTokenAnt.setFrequencyUnit(frequencyUnitText);
        drugTokenAnt.setFuBegin(dm.getFUBegin());
        drugTokenAnt.setFuEnd(dm.getFUENd());
      } else if (recurseNER != null && recurseNER.getFrequencyUnit() != null)
      {
        drugTokenAnt.setFrequencyUnit(recurseNER.getFrequencyUnit());
        drugTokenAnt.setFuBegin(recurseNER.getFuBegin());
        drugTokenAnt.setFuEnd(recurseNER.getFuEnd());
      } else if (dm.getFrequencyElement() != null
          && dm.getFrequencyElement().compareTo("null") != 0
          && dm.frequency != null)
      {

        drugTokenAnt.setFrequency(dm.getFrequencyElement());
        drugTokenAnt.setFrequencyBegin(dm.getFrequencyBegin());
        drugTokenAnt.setFrequencyEnd(dm.getFrequencyEnd());
      }
      if (dm.getRouteElement() != null
          && dm.getRouteElement().compareTo("") != 0
          && dm.getRouteElement().compareTo("null") != 0)
      {
        keepScoreOfConfidence = keepScoreOfConfidence * (float) 1.3;
        drugTokenAnt.setRoute(dm.getRouteElement());
        drugTokenAnt.setRouteBegin(dm.getRouteBegin());
        drugTokenAnt.setRouteEnd(dm.getRouteEnd());
      } else if (recurseNER != null && recurseNER.getRoute() != null)
      {
        drugTokenAnt.setRoute(recurseNER.getRoute());
        drugTokenAnt.setRouteBegin(recurseNER.getRouteBegin());
        drugTokenAnt.setRouteEnd(recurseNER.getRouteEnd());
      }
	  return keepScoreOfConfidence;
  }
  
  private int [] statusChangePhraseGenerator (JCas jcas, int begin, int end, boolean maxExists, Iterator uniqueNER, 
			Iterator orderedStatusChanges, List holdStatusChanges, String [] relatedStatus, 
			DrugMentionAnnotation drugTokenAnt, List globalDrugNER, int countNER ) throws Exception 
  {
		int [] checkSpan = {begin, end};
		
      handledRanges = false;
      boolean deferRight = false;
      if (orderedStatusChanges.hasNext() && !handledRanges)
      {
        // Iterator nextStatusChanges =
        // sortStatusMentionsItr(holdStatusChanges.toArray()).iterator();
        Iterator nextStatusChanges = sortAnnotations(
            holdStatusChanges.toArray()).iterator();

        // prime for next status change in chunk
        DrugChangeStatusAnnotation nextDrugStatus = null;
        if (nextStatusChanges.hasNext())
        {
          nextDrugStatus = (DrugChangeStatusAnnotation) nextStatusChanges
              .next();
        }
        DrugChangeStatusAnnotation drugStatus = (DrugChangeStatusAnnotation) orderedStatusChanges
            .next();

        if (nextStatusChanges.hasNext()
            && drugStatus.getChangeStatus().compareTo(
                DrugChangeStatusToken.STOP) != 0)
        {
          nextDrugStatus = (DrugChangeStatusAnnotation) nextStatusChanges
              .next();
          if (drugStatus.getBegin() == nextDrugStatus.getBegin())
          {
            if (drugStatus.getEnd() < nextDrugStatus.getEnd())
              drugStatus = nextDrugStatus;
            else
              nextDrugStatus = drugStatus;
          }
          if (!uniqueNER.hasNext())
          {
            if ((nextDrugStatus.getChangeStatus().compareTo(
                DrugChangeStatusToken.INCREASE) == 0
                || nextDrugStatus.getChangeStatus().compareTo(
                    DrugChangeStatusToken.DECREASE) == 0 || nextDrugStatus
                .getChangeStatus().compareTo(DrugChangeStatusToken.OTHER) == 0)
                && (drugStatus.getChangeStatus().compareTo(
                    DrugChangeStatusToken.START) == 0 || (drugStatus
                    .getChangeStatus().compareTo(DrugChangeStatusToken.STOP) == 0))
                || (drugTokenAnt.getEnd() + 1 == drugStatus.getBegin()))
            {
              drugStatus = nextDrugStatus;
              deferRight = true;
            }

          }
          // +2 takes the cases of adjacent drug mentions with or
          // without a punctuation token
          else if (nextDrugStatus.getBegin() <= drugStatus.getEnd() + 2)
          {
            if (orderedStatusChanges.hasNext()
                && nextDrugStatus.getBegin() != drugStatus.getBegin())
            {
              orderedStatusChanges.next();
            }
            // Decrease or Increase should trump stop, start and
            // change
            else
            {
              if ((nextDrugStatus.getChangeStatus().compareTo(
                  DrugChangeStatusToken.INCREASE) == 0
                  || nextDrugStatus.getChangeStatus().compareTo(
                      DrugChangeStatusToken.DECREASE) == 0 || nextDrugStatus
                  .getChangeStatus().compareTo(DrugChangeStatusToken.OTHER) == 0)
                  && (drugStatus.getChangeStatus().compareTo(
                      DrugChangeStatusToken.START) == 0 || (drugStatus
                      .getChangeStatus()
                      .compareTo(DrugChangeStatusToken.STOP) == 0)))
                drugStatus = nextDrugStatus;
            }
          }
          if (relatedStatus != null)
            end = nextDrugStatus.getBegin();
        }
        if (drugStatus.getEnd() < end
            && !maxExists
            && (drugStatus.getEnd() != nextDrugStatus.getEnd()
                || drugStatus.getChangeStatus().compareTo(
                    DrugChangeStatusToken.DECREASE) == 0
                || drugStatus.getChangeStatus().compareTo(
                    DrugChangeStatusToken.DECREASEFROM) == 0
                || drugStatus.getChangeStatus().compareTo(
                    DrugChangeStatusToken.INCREASE) == 0 || drugStatus
                .getChangeStatus().compareTo(
                    DrugChangeStatusToken.INCREASEFROM) == 0)
            || drugStatus.getChangeStatus().compareTo(
                DrugChangeStatusToken.OTHER) == 0)
        {

        	checkSpan = generateAdditionalNER(jcas, drugTokenAnt, drugStatus, begin, end,
                countNER, globalDrugNER);

          }
      }
      return checkSpan;
  }
  
  private int[] generateAdditionalNER(JCas jcas,
		  DrugMentionAnnotation tokenDrugNER,
		  DrugChangeStatusAnnotation drugChangeStatus, int beginSpan, int endSpan,
		  int count, List globalNER) throws Exception
		  {
	  boolean noPriorMention = false;
	  boolean noPostMention = false;
	  int originalEndSpan = endSpan;
	  int originalBeginSpan = beginSpan;
	  NamedEntity neAnnot = new NamedEntity(jcas, tokenDrugNER.getBegin(),
			  tokenDrugNER.getEnd());
	  int beginChunk = drugChangeStatus.getEnd();
	  DrugMention compareDM = new DrugMention(jcas, beginChunk, endSpan);
	  DrugMention priorDM = new DrugMention(jcas, beginSpan, drugChangeStatus
			  .getBegin());
	  if ((priorDM.dosage == null) && (priorDM.strength == null) && (priorDM.frequency == null ) )
		  noPriorMention = true;
	  if ((compareDM.dosage == null) && (compareDM.strength == null) && (compareDM.frequency == null ) )
		  noPostMention = true;
	  count++;
	  if ( !noPriorMention)  {
		  if (priorDM.dosage != null) {
			  tokenDrugNER.setDosage(priorDM.getDosageElement());
			  tokenDrugNER.setDosageBegin(priorDM.getDosageBegin());
			  tokenDrugNER.setDosageEnd(priorDM.getDosageEnd());
		  }
		  if (priorDM.strength != null) {
			  tokenDrugNER.setStrength(priorDM.getStrengthElement());
			  tokenDrugNER.setStrengthBegin(priorDM.getStrengthBegin());
			  tokenDrugNER.setStrengthEnd(priorDM.getStrengthEnd());
		  }
		  if (priorDM.frequency != null) {
			  tokenDrugNER.setFrequency(priorDM.getFrequencyElement());
			  tokenDrugNER.setFrequencyBegin(priorDM.getFrequencyBegin());
			  tokenDrugNER.setFrequencyEnd(priorDM.getFrequencyEnd());
		  }
	  }
	  neAnnot.setTypeID(NERTypeIdentifier);
	  int [] updatedSpan = {beginSpan, endSpan};

	  List<NamedEntity> buildNewNER = new ArrayList<NamedEntity>();

	  buildNewNER.add(neAnnot);

	  if (drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.DECREASE) == 0)
	  {
		  int endChunk = 0;
		  int startChunk = 0;
		  int midChunk = 0;
		  if (noPriorMention) {//Look for highest value on right side 
			  startChunk =	getAdjustedWindowSpan(jcas,  beginChunk, endSpan, false)[0];
			  midChunk =  getAdjustedWindowSpan(jcas,  beginChunk, endSpan, true)[0];
			  endChunk = getAdjustedWindowSpan(jcas,  beginChunk, endSpan, true)[1];
		  }
		  updatedSpan[0] = beginChunk;
		  String [] changeStatusArray = new String [] {DrugChangeStatusToken.DECREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
		  generateDrugMentionsAndAnnotations(jcas, buildNewNER, beginChunk,
				  midChunk, tokenDrugNER, changeStatusArray, count,
				  globalNER);
		  if (noPriorMention) {
			  compareDM = new DrugMention(jcas, startChunk, endChunk);
			  if (compareDM.dosage != null) {
				  tokenDrugNER.setDosage(compareDM.getDosageElement());
				  tokenDrugNER.setDosageBegin(compareDM.getDosageBegin());
				  tokenDrugNER.setDosageEnd(compareDM.getDosageEnd());
			  }
			  if (compareDM.strength != null) {
				  tokenDrugNER.setStrength(compareDM.getStrengthElement());
				  tokenDrugNER.setStrengthBegin(compareDM.getStrengthBegin());
				  tokenDrugNER.setStrengthEnd(compareDM.getStrengthEnd());
			  }
			  if (compareDM.frequency != null) {
				  tokenDrugNER.setFrequency(compareDM.getFrequencyElement());
				  tokenDrugNER.setFrequencyBegin(compareDM.getFrequencyBegin());
				  tokenDrugNER.setFrequencyEnd(compareDM.getFrequencyEnd());
			  }
		  }
		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
	  } else if (drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.DECREASEFROM) == 0)
	  {
		  if (noPriorMention) {//Look for lowest value on right side 
			  beginChunk = getAdjustedWindowSpan(jcas,  beginChunk, endSpan, true)[0];
		  }
		  String [] changeStatusArray = new String [] {DrugChangeStatusToken.DECREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
		  generateDrugMentionsAndAnnotations(jcas,
				  buildNewNER, beginChunk, endSpan,
				  tokenDrugNER, changeStatusArray, count, globalNER);
		  if (noPriorMention) {
			  priorDM = new DrugMention(jcas, originalBeginSpan, beginChunk);
			  if (priorDM.dosage != null) {
				  tokenDrugNER.setDosage(priorDM.getDosageElement());
				  tokenDrugNER.setDosageBegin(priorDM.getDosageBegin());
				  tokenDrugNER.setDosageEnd(priorDM.getDosageEnd());
			  }
			  if (priorDM.strength != null) {
				  tokenDrugNER.setStrength(priorDM.getStrengthElement());
				  tokenDrugNER.setStrengthBegin(priorDM.getStrengthBegin());
				  tokenDrugNER.setStrengthEnd(priorDM.getStrengthEnd());
			  }
			  if (priorDM.frequency != null) {
				  tokenDrugNER.setFrequency(priorDM.getFrequencyElement());
				  tokenDrugNER.setFrequencyBegin(priorDM.getFrequencyBegin());
				  tokenDrugNER.setFrequencyEnd(priorDM.getFrequencyEnd());
			  }
		  }
		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);

	  } else if (drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.INCREASE) == 0)
	  {
		  if (noPriorMention) {//Look for highest value on right side 
			  endSpan =	getAdjustedWindowSpan(jcas,  beginChunk, endSpan, true)[0];
		  }
		  updatedSpan[0] = beginChunk;
		  String [] changeStatusArray = new String [] {DrugChangeStatusToken.INCREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
		  generateDrugMentionsAndAnnotations(jcas, buildNewNER, beginChunk,
				  endSpan, tokenDrugNER, changeStatusArray, count,
				  globalNER);
		  if (noPriorMention) {
			  compareDM = new DrugMention(jcas, endSpan, originalEndSpan);
			  if (compareDM.dosage != null) {
				  tokenDrugNER.setDosage(compareDM.getDosageElement());
				  tokenDrugNER.setDosageBegin(compareDM.getDosageBegin());
				  tokenDrugNER.setDosageEnd(compareDM.getDosageEnd());
			  }
			  if (compareDM.strength != null) {
				  tokenDrugNER.setStrength(compareDM.getStrengthElement());
				  tokenDrugNER.setStrengthBegin(compareDM.getStrengthBegin());
				  tokenDrugNER.setStrengthEnd(compareDM.getStrengthEnd());
			  }
			  if (compareDM.frequency != null) {
				  tokenDrugNER.setFrequency(compareDM.getFrequencyElement());
				  tokenDrugNER.setFrequencyBegin(compareDM.getFrequencyBegin());
				  tokenDrugNER.setFrequencyEnd(compareDM.getFrequencyEnd());
			  }
		  }
		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.OTHER);
	  } else if (drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.INCREASEFROM) == 0)
	  {
		  int startChunk = 0;
		  int endChunk = 0;
		  int midChunk = 0;
		  if (noPriorMention) {//Look for lowest value on right side 
			  startChunk = getAdjustedWindowSpan(jcas,  beginChunk, endSpan, false)[0];
			  midChunk =  getAdjustedWindowSpan(jcas,  beginChunk, endSpan, false)[1];
			  endChunk = getAdjustedWindowSpan(jcas,  beginChunk, endSpan, true)[1];
		  }
		  String [] changeStatusArray = new String [] {DrugChangeStatusToken.INCREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
		  generateDrugMentionsAndAnnotations(jcas, buildNewNER, startChunk, 
				  endChunk, tokenDrugNER,
				  changeStatusArray, count, globalNER);
		  if (noPriorMention) {
			  priorDM = new DrugMention(jcas, originalBeginSpan, midChunk);
			  if (priorDM.dosage != null) {
				  tokenDrugNER.setDosage(priorDM.getDosageElement());
				  tokenDrugNER.setDosageBegin(priorDM.getDosageBegin());
				  tokenDrugNER.setDosageEnd(priorDM.getDosageEnd());
			  }
			  if (priorDM.strength != null) {
				  tokenDrugNER.setStrength(priorDM.getStrengthElement());
				  tokenDrugNER.setStrengthBegin(priorDM.getStrengthBegin());
				  tokenDrugNER.setStrengthEnd(priorDM.getStrengthEnd());
			  }
			  if (priorDM.frequency != null) {
				  tokenDrugNER.setFrequency(priorDM.getFrequencyElement());
				  tokenDrugNER.setFrequencyBegin(priorDM.getFrequencyBegin());
				  tokenDrugNER.setFrequencyEnd(priorDM.getFrequencyEnd());
			  }
		  }
		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.NOCHANGE);
	  } else if (drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.STOP) == 0)
	  {
		  tokenDrugNER.setDrugChangeStatus(tokenDrugNER.getDrugChangeStatus());
	  } else if ((drugChangeStatus.getChangeStatus().compareTo(
			  DrugChangeStatusToken.OTHER) == 0)
			  || drugChangeStatus.getChangeStatus().compareTo(
					  DrugChangeStatusToken.SUM) == 0)
	  {


		  double strengthChange = 1;
		  double dosageChange = 1;
		  double frequencyChange = 1;
		  if (noPriorMention) {
			  int [] updateSpan =   getAdjustedWindowSpan(jcas,  beginChunk, endSpan, false);
			  compareDM = new DrugMention(jcas, endSpan, originalEndSpan);
			  if (compareDM.dosage != null) {
				  tokenDrugNER.setDosage(compareDM.getDosageElement());
				  tokenDrugNER.setDosageBegin(compareDM.getDosageBegin());
				  tokenDrugNER.setDosageEnd(compareDM.getDosageEnd());
			  }
			  if (compareDM.strength != null) {
				  tokenDrugNER.setStrength(compareDM.getStrengthElement());
				  tokenDrugNER.setStrengthBegin(compareDM.getStrengthBegin());
				  tokenDrugNER.setStrengthEnd(compareDM.getStrengthEnd());
			  }
			  if (compareDM.frequency != null) {
				  tokenDrugNER.setFrequency(compareDM.getFrequencyElement());
				  tokenDrugNER.setFrequencyBegin(compareDM.getFrequencyBegin());
				  tokenDrugNER.setFrequencyEnd(compareDM.getFrequencyEnd());
			  }
		  }
		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.OTHER);
		  if (compareDM.getStrengthElement() != null
				  && compareDM.getStrengthElement().compareTo("") != 0
				  && compareDM != null)
		  {
			  strengthChange = new Double(compareDM.parseDoubleValue(compareDM
					  .getStrengthElement())).doubleValue();

		  } else if (priorDM.getStrengthElement() != null
				  && priorDM.getStrengthElement().compareTo("") != 0
				  && priorDM.getStrengthElement().length() > 0)
		  {
			  int spacePosition = priorDM.getStrengthElement().indexOf(" ");
			  if (spacePosition > 0)
			  {
				  strengthChange = new Double(priorDM.parseDoubleValue(priorDM
						  .getStrengthElement().substring(0, spacePosition))).doubleValue();

			  } else
			  {
				  strengthChange = new Double(priorDM.parseDoubleValue(priorDM
						  .getStrengthElement())).doubleValue();

			  }
		  }
		  if (compareDM.getDosageElement() != null
				  && compareDM.getDosageElement().compareTo("") != 0)
		  {
			  dosageChange = new Double(compareDM.parseDoubleValue(compareDM
					  .getDosageElement())).doubleValue();
		  } else if (priorDM.getDosageElement() != null
				  && priorDM.getDosageElement().compareTo("") != 0)
		  {
			  dosageChange = new Double(priorDM.parseDoubleValue(priorDM
					  .getDosageElement())).doubleValue();
		  }
		  if (compareDM.getFrequencyElement() != null
				  && compareDM.getFrequencyElement().compareTo("") != 0)
		  {
			  frequencyChange = new Double(compareDM.parseDoubleValue(compareDM
					  .getFrequencyElement())).doubleValue();
		  } else if (priorDM.getFrequencyElement() != null
				  && priorDM.getFrequencyElement().compareTo("") != 0)
		  {
			  frequencyChange = new Double(priorDM.parseDoubleValue(priorDM
					  .getFrequencyElement())).doubleValue();
		  }

		  double strengthBefore = 1;
		  double dosageBefore = 1;
		  double frequencyBefore = 1;

		  if (priorDM.getStrengthElement() != null
				  && priorDM.getStrengthElement().compareTo("") != 0
				  && priorDM.getStrengthElement().length() > 0)
		  {
			  strengthBefore = new Double(priorDM.parseDoubleValue(priorDM
					  .getStrengthElement())).doubleValue();
			  tokenDrugNER.setStrength(priorDM.getStrengthElement());
			  tokenDrugNER.setStrengthBegin(priorDM.getStrengthBegin());
			  tokenDrugNER.setStrengthEnd(priorDM.getStrengthEnd());
		  } else if (tokenDrugNER.getStrength() != null
				  && tokenDrugNER.getStrength().compareTo("") != 0
				  && tokenDrugNER.getStrength().length() > 0)
		  {
			  boolean handledSeparator = false;
			  int hyphPosition = tokenDrugNER.getStrength().indexOf('-');
			  String hyphString = tokenDrugNER.getStrength();
			  if (hyphPosition > 0)
			  {
				  hyphString = tokenDrugNER.getStrength().substring(0, hyphPosition);

				  strengthBefore = new Double(compareDM.parseDoubleValue(compareDM
						  .convertFromTextToNum(hyphString))).doubleValue();
				  handledSeparator = true;
			  }
			  int spacePosition = hyphString.indexOf(" ");
			  if (spacePosition > 0)
			  {
				  hyphString = hyphString.substring(0, spacePosition);
				  strengthBefore = new Double(priorDM.parseDoubleValue(priorDM
						  .convertFromTextToNum(hyphString))).doubleValue();
				  handledSeparator = true;
			  }
			  if (!handledSeparator)
				  strengthBefore = new Double(compareDM.parseDoubleValue(tokenDrugNER
						  .getStrength())).doubleValue();
		  }
		  if (priorDM.getDosageElement() != null
				  && priorDM.getDosageElement().compareTo("") != 0
				  && priorDM.dosage != null)
		  {
			  dosageBefore = new Double(priorDM.getDosageElement()).doubleValue();
			  tokenDrugNER.setDosage(priorDM.getDosageElement());
			  tokenDrugNER.setDosageBegin(priorDM.getDosageBegin());
			  tokenDrugNER.setDosageEnd(priorDM.getDosageEnd());
		  } else if (tokenDrugNER.getDosage() != null
				  && tokenDrugNER.getDosage().compareTo("") != 0)
		  {
			  dosageBefore = new Double(compareDM.parseDoubleValue(tokenDrugNER
					  .getDosage())).doubleValue();
		  }
		  if (priorDM.getFrequencyElement() != null
				  && priorDM.getFrequencyElement().compareTo("") != 0)
		  {
			  frequencyBefore = new Double(priorDM.parseDoubleValue(priorDM
					  .getFrequencyElement())).doubleValue();
			  tokenDrugNER.setFrequency(priorDM.getFrequencyElement());

		  } else if (tokenDrugNER.getFrequency() != null
				  && tokenDrugNER.getFrequency().compareTo("") != 0)
		  {
			  frequencyBefore = new Double(compareDM.parseDoubleValue(tokenDrugNER
					  .getFrequency())).doubleValue();
		  }
		  if ((drugChangeStatus.getChangeStatus().compareTo(
				  DrugChangeStatusToken.SUM) == 0)
				  && (strengthChange > 1 && strengthBefore > 1 || strengthChange == strengthBefore))
		  {
			  Iterator findLF = FSUtil.getAnnotationsIteratorInSpan(jcas,
					  NewlineToken.type, neAnnot.getBegin(), beginChunk);
			  if (!findLF.hasNext())
			  {
				  if (frequencyChange <= 1 && frequencyBefore > 1)
					  tokenDrugNER.setFrequency("1.0");
				  String [] changeStatusArray = new String [] {DrugChangeStatusToken.SUM, new Integer (0).toString(), new Integer(0).toString()};
				  generateDrugMentionsAndAnnotations(jcas, buildNewNER, beginChunk,
						  endSpan, tokenDrugNER, changeStatusArray, count,
						  globalNER);

			  }

		  } 				
		  else if (strengthChange * dosageChange
				  * frequencyChange > strengthBefore
				  * dosageBefore * frequencyBefore) {
			  String [] changeStatusArray = new String [] {DrugChangeStatusToken.INCREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
			  generateDrugMentionsAndAnnotations(jcas,
					  buildNewNER, beginChunk,
					  endSpan, tokenDrugNER,
					  changeStatusArray, count, globalNER);
		  } 
		  else {
			  String [] changeStatusArray = new String [] {DrugChangeStatusToken.DECREASE, new Integer (drugChangeStatus.getBegin()).toString(), new Integer(drugChangeStatus.getEnd()).toString()};
			  generateDrugMentionsAndAnnotations(jcas,
					  buildNewNER, beginChunk,
					  endSpan, tokenDrugNER,
					  changeStatusArray, count, globalNER);
		  }

		  tokenDrugNER.setDrugChangeStatus(DrugChangeStatusToken.OTHER);
		  //      }
	  }
	  return updatedSpan;
		  }

  private edu.mayo.bmi.fsm.token.BaseToken adaptToFSMBaseToken(BaseToken obj)
      throws Exception
  {
    if (obj instanceof WordToken)
    {
      WordToken wta = (WordToken) obj;
      return new WordTokenAdapter(wta);
    } else if (obj instanceof NumToken)
    {
      NumToken nta = (NumToken) obj;
      if (nta.getNumType() == TokenizerAnnotator.TOKEN_NUM_TYPE_INTEGER)
      {
        return new IntegerTokenAdapter(nta);
      } else
      {
        return new DecimalTokenAdapter(nta);
      }
    } else if (obj instanceof PunctuationToken)
    {
      PunctuationToken pta = (PunctuationToken) obj;
      return new PunctuationTokenAdapter(pta);
    } else if (obj instanceof NewlineToken)
    {
      NewlineToken nta = (NewlineToken) obj;
      return new NewlineTokenAdapter(nta);
    } else if (obj instanceof ContractionToken)
    {
      ContractionToken cta = (ContractionToken) obj;
      return new ContractionTokenAdapter(cta);
    } else if (obj instanceof SymbolToken)
    {
      SymbolToken sta = (SymbolToken) obj;
      return new SymbolTokenAdapter(sta);
    }

    throw new Exception("No CDT adapter for class: " + obj.getClass());
  }

//  public Iterator getLookupTokenIterator(JCas jcas, int beginRange, int endRange)
//      throws AnnotatorInitializationException
//  {
//    List ltList = new ArrayList();
//
//    Iterator btaItr = FSUtil.getAnnotationsIteratorInSpan(jcas, BaseToken.type,
//        beginRange, endRange);
//
//    while (btaItr.hasNext())
//    {
//      BaseToken bta = (BaseToken) btaItr.next();
//      if (!(bta instanceof NewlineToken) || (bta instanceof PunctuationToken)
//          || (bta instanceof ContractionToken) || (bta instanceof SymbolToken))
//      {
//        LookupToken lt = new LookupAnnotationToJCasAdapter(bta);
//
//        // POS exclusion logic for first word lookup
//        // if (isTagExcluded(bta.getPennTag()))
//        if (isTagExcluded(bta.getNormalizedForm()))
//        {
//          lt.addStringAttribute(
//              FirstTokenPermutationImpl.LT_KEY_USE_FOR_LOOKUP, "false");
//        } else
//        {
//          lt.addStringAttribute(
//              FirstTokenPermutationImpl.LT_KEY_USE_FOR_LOOKUP, "true");
//        }
//
//        if (bta instanceof WordToken)
//        {
//          WordToken wta = (WordToken) bta;
//          String canonicalForm = wta.getCanonicalForm();
//
//          if (canonicalForm != null)
//          {
//            lt.addStringAttribute(CANONICAL_VARIANT_ATTR, canonicalForm);
//          }
//        }
//
//        ltList.add(lt);
//      }
//    }
//    return ltList.iterator();
//  }
//
//  private boolean isTagExcluded(String tag)
//  {
//    if ((iv_exclusionTagSet == null) || (tag == null))
//    {
//      return false;
//    }
//
//    return iv_exclusionTagSet.contains(tag.toLowerCase());
//  }

  private boolean findNextDrugEntityPost(int spanLength, int[][] elementSpan,
      NamedEntity nea, int endPhrase)
  {
    boolean patternFound = false;
    for (int l = 0; l < spanLength && !patternFound; l++)
    {
      if (elementSpan[l][0] != -1 && elementSpan[l][0] > nea.getBegin()
          && elementSpan[l][0] < endPhrase)
      {
        patternFound = true;
      }
    }
    return patternFound;
  }

  private boolean findNextDrugEntityPre(int spanLength, int[][] elementSpan,
      NamedEntity nea, int priorDrugEnd)
  {
    boolean patternFound = false;
    for (int l = 0; l < spanLength && !patternFound; l++)
    {
      if (elementSpan[l][0] != -1 && elementSpan[l][1] < nea.getBegin()
          && elementSpan[l][0] > priorDrugEnd)
      {
        patternFound = true;
      }
    }
    return patternFound;
  }

  private boolean findNextParenRelativeToNE(int spanLength,
      int[][] elementSpan, NamedEntity nea, int priorDrugEnd, int startOffset)
  {
    boolean patternFound = false;
    for (int l = startOffset; l < spanLength && !patternFound; l++)
    {
      if (elementSpan[l][0] != -1 && elementSpan[l][0] < nea.getBegin()
          && elementSpan[l][1] > priorDrugEnd)
      {
        patternFound = true;
      }
    }
    return patternFound;
  }

  private boolean findNextParenRelativeToElement(int spanLength,
      int[][] elementSpan, Annotation nea, int parenEnd, int startOffset)
  {
    boolean patternFound = false;
    for (int l = startOffset; l < spanLength && !patternFound; l++)
    {
      if (elementSpan[l][0] != -1 && elementSpan[l][0] < nea.getBegin()
          && elementSpan[l][1] == parenEnd && nea.getEnd() < elementSpan[l][1])
      {
        patternFound = true;
      }
    }
    return patternFound;
  }

  /**
   * Return true if exists more than one drug and reason within the span,
   * otherwise return false
   * 
   * @param jcas
   * @param begin
   * @param end
   * @return
   */
  private boolean hasMultipleDrugsInSpan(JCas jcas, int begin, int end)
  {
    int[] validNeTypes =
    { TypeSystemConst.NE_TYPE_ID_DRUG, TypeSystemConst.NE_TYPE_ID_UNKNOWN };
    int numDrugs = FSUtil.countAnnotationsInSpan(jcas, NamedEntity.type, begin,
        end, validNeTypes);
    return (numDrugs > 1);
  }

  /**
   * Return true if exists more than one drug and reason within the span,
   * otherwise return false
   * 
   * @param jcas
   * @param begin
   * @param end
   * @return
   */
  private boolean hasMultipleElementsInSpan(JCas jcas, int begin, int end)
  {
    int numElements = 0;
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        StrengthAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        FrequencyAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        FrequencyUnitAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        DosagesAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        FormAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        RouteAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        DurationAnnotation.type, begin, end) == true) ? 1 : 0);
    numElements += ((FSUtil.isAnnotationPresentInSpan(jcas,
        DrugChangeStatusAnnotation.type, begin, end) == true) ? 1 : 0);

    return (numElements > 1);
  }

    /**
     * 
     * @param jcas
     * @param begin
     * @param end
     * @return int[] - int[0] is begin offset and int[1] is end offset
     */
  private int[] getSentenceSpanContainingGivenSpan(JCas jcas, int begin, int end)
  {
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator iter = indexes.getAnnotationIndex(Sentence.type).iterator();
    int[] span = new int[2];

    while (iter.hasNext())
    {
      Sentence sa = (Sentence) iter.next();
      if (begin >= sa.getBegin() && end <= sa.getEnd())
      {
        span[0] = sa.getBegin();
        span[1] = sa.getEnd();
        // System.out.println("In setSentenceSpanContainingGivenSpan: begin="+span[0]+"|"+"end="+span[1]);
        break;
      }
    }

    return span;
  }
  private void findFSMInRange(JCas jcas, int begin, int end) throws Exception {
		NamedEntity ne = null;
		WordToken we = null;
		// grab iterator over tokens within this chunk
		Iterator btaItr = FSUtil.getAnnotationsInSpanIterator(jcas,
				BaseToken.type, begin,
				end+1);
		// do the same as above for named entities
		// grab iterator over tokens within this chunk
		Iterator neItr = FSUtil.getAnnotationsInSpanIterator(jcas,
				NamedEntity.type, begin,
				end+1);
		// List neTokenList = new ArrayList();

		// do the same as above for word entities
		// grab iterator over tokens within this chunk
		Iterator weItr = FSUtil.getAnnotationsInSpanIterator(jcas,
				WordToken.type, begin,
				end+1);
		List weTokenList = new ArrayList();
		while (weItr.hasNext()) {
			we = (WordToken) weItr.next();
			weTokenList.add(we);

		}
		List neTokenList = new ArrayList();


		while (neItr.hasNext()) {
			ne = (NamedEntity) neItr.next();

			neTokenList.add(ne);
		}


		// adapt JCas objects into objects expected by the
		// Finite state
		// machines
		List baseTokenList = new ArrayList();
		while (btaItr.hasNext()) {
			BaseToken bta = (BaseToken) btaItr
			.next();

			baseTokenList.add(adaptToFSMBaseToken(bta));
		}

		// execute FSM logic

		executeFSMs(jcas, baseTokenList, neTokenList,
				weTokenList);
  }
	/**
	 * 
	 * @param jcas
	 * @param begin
	 * @param end
	 * @return
	 */
	private int [] findOffsetsInPattern(JCas jcas, int begin, int end, int elementType, int[][] location, boolean highest) {
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator neItr = indexes.getAnnotationIndex(elementType).iterator();//FSUtil.getAnnotationsInSpanIterator(jcas, elementType, begin, end);
		int [] lastLocation =  {-1,-1};
		boolean wantMuliple = true;
	    if (elementType == StrengthAnnotation.type) {
     	 while (neItr.hasNext() && wantMuliple) {
     		 StrengthAnnotation nea = (StrengthAnnotation) neItr.next();

     		 if (nea.getBegin()>=begin && nea.getEnd() <= end ) {
         		 if (!highest) wantMuliple = false;
         		 lastLocation[0] = nea.getBegin();
         		 lastLocation[1] = nea.getEnd();
     		 }
  	    }
     } else if (elementType == FrequencyAnnotation.type) {
			while (neItr.hasNext()&& wantMuliple) {
				FrequencyAnnotation nea = (FrequencyAnnotation) neItr.next();
				 
	       		 if (nea.getBegin()>=begin && nea.getEnd() <= end ) {
	       			if (!highest) wantMuliple = false;
	 				lastLocation[0] = nea.getBegin();
		       		 lastLocation[1] = nea.getEnd();
	       		 }			}
		}  else if (elementType == DosagesAnnotation.type) {
     	 while (neItr.hasNext()&& wantMuliple) {
     		 DosagesAnnotation nea = (DosagesAnnotation) neItr.next();
     		 
     		 if (nea.getBegin()>=begin && nea.getEnd() <= end ) {
     			if (!highest) wantMuliple = false;
          		lastLocation[0] = nea.getBegin();
          		lastLocation[1] = nea.getEnd();
     		 }
  	    }
     }
	    
	    return lastLocation;
	}
  /**
   * return window span to find reasons for the given d
   * @param jcas
   * @return int[0] is begin offset and int[1] is end offset 
   * @throws Exception 
   */
  private int[] getAdjustedWindowSpan(JCas jcas,  int begin, int end, boolean highestRange) throws Exception {
	  int[] spanStrength = {-1, -1}, spanFrequency = {-1, -1}, spanDose = {-1, -1};
	  JFSIndexRepository indexes = jcas.getJFSIndexRepository();
	  int[] senSpan = {begin, end };

	  // Up to 10 of each type of signature item and drug mentions are allowed.  If more than that are available the 
	  // ArrayIndexOutOfBoundsException is thrown/caught and the sentence is skipped

	  int[][] strengthSpan = {{-1, -1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
	  int[][] freqSpan = {{-1, -1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
	  int[][] doseSpan = {{-1, -1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
	  int[][] parenthesisSpan = {{-1, -1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
	  int[][] statusSpan = {{-1, -1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};

	  int drugSpanLength = 0, strengthSpanLength = 0, freqSpanLength = 0 , doseSpanLength = 0 ;       

	  int  beginSpan = senSpan[0], endSpan = senSpan[1];

	  freqSpanLength = findInPattern(jcas, beginSpan, endSpan, FrequencyAnnotation.type, freqSpan);
	  doseSpanLength = findInPattern(jcas, beginSpan, endSpan, DosagesAnnotation.type, doseSpan);
	  int parenthesisSpanLength = findInPattern(jcas, beginSpan, endSpan, PunctuationToken.type, parenthesisSpan);
	  strengthSpanLength = findInPattern(jcas, beginSpan, endSpan, StrengthAnnotation.type, strengthSpan);
	  spanStrength =  highestRange?findOffsetsInPattern(jcas, beginSpan, endSpan, StrengthAnnotation.type, strengthSpan, highestRange):findOffsetsInPattern(jcas, beginSpan, endSpan, StrengthAnnotation.type, strengthSpan, highestRange);
	  spanFrequency = highestRange?findOffsetsInPattern(jcas, beginSpan, endSpan, FrequencyAnnotation.type, strengthSpan, highestRange):findOffsetsInPattern(jcas, beginSpan, endSpan, FrequencyAnnotation.type, strengthSpan, highestRange); 
	  spanDose = highestRange?findOffsetsInPattern(jcas, beginSpan, endSpan, DosagesAnnotation.type, doseSpan, highestRange):findOffsetsInPattern(jcas, beginSpan, endSpan, DosagesAnnotation.type, doseSpan, highestRange);

	  if (spanStrength[0] == -1 && spanFrequency[0] == -1 && spanDose[0] == -1)
		  return senSpan;
	  if (highestRange  && spanStrength[0]!=-1) {
		  senSpan = spanStrength[0] > spanFrequency[0]? spanStrength : spanFrequency; 
	  } else if (spanFrequency[0]!=-1) {
		  senSpan = spanStrength[0] < spanFrequency[0]? spanStrength : spanFrequency; 
	  }
	  return spanDose[0] > senSpan[0]? spanDose : senSpan;

  }

/**
 * 
 * @param jcas
 * @param begin
 * @param end
 * @return
 */
private int findInPattern(JCas jcas, int begin, int end, int elementType, int[][] location) {
	JFSIndexRepository indexes = jcas.getJFSIndexRepository();
	Iterator neItr = indexes.getAnnotationIndex(elementType).iterator();//FSUtil.getAnnotationsInSpanIterator(jcas, elementType, begin, end);
	int [] lastLocation =  {-1,-1};
	int counter = 0;
    if (elementType == StrengthAnnotation.type) {
 	 while (neItr.hasNext()) {
 		 StrengthAnnotation nea = (StrengthAnnotation) neItr.next();
 		 lastLocation[0] = nea.getBegin();
 		 lastLocation[1] = nea.getEnd();
 		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
 			 location[counter][0]= lastLocation[0];
 			 location[counter][1]= lastLocation[1];
 			 counter++;
 		 }
    }
 } else if (elementType == FrequencyAnnotation.type) {
		while (neItr.hasNext()) {
			FrequencyAnnotation nea = (FrequencyAnnotation) neItr.next();
			lastLocation[0] = nea.getBegin();
       		 lastLocation[1] = nea.getEnd();
       		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
       			 location[counter][0]= lastLocation[0];
       			 location[counter][1]= lastLocation[1];
       			 counter++;
       		 }			}
	} else if (elementType == FrequencyUnitAnnotation.type) {
		while (neItr.hasNext()) {
			FrequencyUnitAnnotation nea = (FrequencyUnitAnnotation) neItr.next();
			lastLocation[0] = nea.getBegin();
       		 lastLocation[1] = nea.getEnd();
       		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
       			 location[counter][0]= lastLocation[0];
       			 location[counter][1]= lastLocation[1];
       			 counter++;
       		 }
		}
	}    else if (elementType == DosagesAnnotation.type) {
 	 while (neItr.hasNext()) {
 		 DosagesAnnotation nea = (DosagesAnnotation) neItr.next();
 		lastLocation[0] = nea.getBegin();
		 lastLocation[1] = nea.getEnd();
 		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
 			 location[counter][0]= lastLocation[0];
 			 location[counter][1]= lastLocation[1];
 			 counter++;
 		 }
    }
 }else if (elementType == RouteAnnotation.type) {
		while (neItr.hasNext()) {
			RouteAnnotation nea = (RouteAnnotation) neItr.next();
			lastLocation[0] = nea.getBegin();
       		 lastLocation[1] = nea.getEnd();
       		 if (nea.getBegin()>=begin && nea.getEnd() <= end) {
       			 location[counter][0]= lastLocation[0];
       			 location[counter][1]= lastLocation[1];
       			 counter++;
       		 }
		}
	} else if (elementType == FormAnnotation.type) {
		while (neItr.hasNext()) {
			FormAnnotation nea = (FormAnnotation) neItr.next();
			lastLocation[0] = nea.getBegin();
       		 lastLocation[1] = nea.getEnd();
       		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
       			 location[counter][0]= lastLocation[0];
       			 location[counter][1]= lastLocation[1];
       			 counter++;
       		 }
		}
	}    
	else if (elementType == DurationAnnotation.type) {
 	 while (neItr.hasNext()) {
 		DurationAnnotation nea = (DurationAnnotation) neItr.next();
 		lastLocation[0] = nea.getBegin();
		 lastLocation[1] = nea.getEnd();
 		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
 			 location[counter][0]= lastLocation[0];
 			 location[counter][1]= lastLocation[1];
 			 counter++;
 		 }
    }
 	 
 } 
	else if (elementType == DrugChangeStatusAnnotation.type) {
   	 while (neItr.hasNext()) {
   		DrugChangeStatusAnnotation nea = (DrugChangeStatusAnnotation) neItr.next();
      		lastLocation[0] = nea.getBegin();
     		 lastLocation[1] = nea.getEnd();
     		 if (nea.getBegin()>=begin && nea.getEnd() <= end && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
     			 location[counter][0]= lastLocation[0];
     			 location[counter][1]= lastLocation[1];
     			 counter++;
     		 }
   	    }
      	 
      } else if (elementType == NamedEntity.type) {
   while (neItr.hasNext()) {
    	NamedEntity nea = (NamedEntity) neItr.next();
    		if(nea.getTypeID()==1 || nea.getTypeID()==0) {
    			lastLocation[0]= nea.getBegin();
    			lastLocation[1] = nea.getEnd(); 
 		 	if ((counter == 0 || lastLocation[0] != location[counter-1][0]) && (nea.getBegin()>=begin && nea.getEnd() <= end)) {
 		 		location[counter][0]= lastLocation[0];
 		 		location[counter][1]= lastLocation[1];
 		 		counter++;
 		 	}
    		 
    		}
    }
 } else if (elementType == PunctuationToken.type) {
 	 while (neItr.hasNext()) {
 		boolean foundPair = false;
 		PunctuationToken nea = (PunctuationToken) neItr.next();
 		 if (nea.getCoveredText().compareTo("(")==0)
   		lastLocation[0] = nea.getBegin();
 		 else if (nea.getCoveredText().compareTo(")")==0) {
  		 lastLocation[1] = nea.getEnd();
  		 foundPair = true;
 		 }
  	 if (nea.getBegin()>=begin && nea.getEnd() <= end && foundPair && (counter == 0  || (counter> 0 && lastLocation[0] !=  location[counter - 1][0]))) {
  			 location[counter][0]= lastLocation[0];
  			 location[counter][1]= lastLocation[1];
  			 counter++;
  		 }
	    }
   	 
   }
    
    return counter;
}


}

