package edu.mayo.bmi.uima.pad.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.JCas;
import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.type.WordToken;
import edu.mayo.bmi.uima.pad.type.PADTerm;

public class PADTermConsumerImpl 
extends PADConsumerImpl
{
  public static Logger iv_logger = Logger.getLogger(PADTermConsumerImpl.class);
  public static int FALSE = 0;
  public static int TRUE  = 1;
  public static String STAND_ALONE = "STAND_ALONE";
  public PADTermConsumerImpl(AnnotatorContext aCtx, Properties props)
  throws AnnotatorContextException, IOException
  { 
  }


  
  public void consumeHits(JCas jcas, Iterator lhItr)
      throws AnnotatorProcessException
  {
    Iterator<?> hitsByOffsetItr = organizeByOffset(lhItr);
    
    while (hitsByOffsetItr.hasNext())
    {
      //this is a hit from dictionary, just one in this case
      Collection<?> hitsAtOffsetCol = (Collection<?>) hitsByOffsetItr.next();
      
      Iterator<?> lhAtOffsetItr = hitsAtOffsetCol.iterator();
      int neBegin = -1;
      int neEnd = -1;
      while (lhAtOffsetItr.hasNext())
      {
        LookupHit lh = (LookupHit) lhAtOffsetItr.next();

        neBegin = lh.getStartOffset();
        neEnd = lh.getEndOffset();
        MetaDataHit metaData = lh.getDictMetaDataHit();
        
        addTerm(jcas, metaData, neBegin, neEnd);
      }
    }
  }
  
  private void addTerm(JCas jcas, MetaDataHit metaData, int neBegin, int neEnd)
  {
    String mfValue =null;
    
    mfValue = metaData.getMetaFieldValue("1");
    //read data for isStandAlone.
    String mfIsStandAlone = metaData.getMetaFieldValue("2"); 
    
    iv_logger.log(Level.INFO, "Meta field 1 ["+mfValue+"] col2["+mfIsStandAlone+"]");
    
    int cutOffForRevision = findOriginalReportSubSection(jcas);
    PADTerm uaTerm = new PADTerm(jcas);
    uaTerm.setHitDictionaryValue(mfValue);

    uaTerm.setBegin(neBegin);
    uaTerm.setEnd(neEnd);
    
    String segStatus = getSubSectionStatus(uaTerm, jcas, cutOffForRevision);

    if(mfIsStandAlone != null && mfIsStandAlone.equalsIgnoreCase(STAND_ALONE))
      uaTerm.setIsStandAlone(TRUE);
    else {
      uaTerm.setIsStandAlone(FALSE);
      uaTerm.setTypeID(new Integer(mfIsStandAlone).intValue());
    }


    List<?> list = getTokenData(jcas, uaTerm);
    FSArray fsArr = new FSArray(jcas, list.size());
    
    for(int i=0; i<list.size(); i++)
      fsArr.set(i, (WordToken)list.get(i));
    
    uaTerm.setRelatedTokens(fsArr);
   
    if (segStatus.compareTo("-1")==0 || segStatus.compareTo("1")!=0) {
    	String segId = getSegmentIdContaining(uaTerm, jcas);
    	uaTerm.setSegmentID(segId);
    
    //fsArr.addToIndexes();
    	uaTerm.addToIndexes();
    } 
  }
  
}
