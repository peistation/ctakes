package edu.mayo.bmi.uima.pad.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContextException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import edu.mayo.bmi.dictionary.MetaDataHit;
import edu.mayo.bmi.lookup.vo.LookupHit;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.pad.type.PADLocation;


public class PADLocationConsumerImpl 
extends PADConsumerImpl
{

  public static Logger iv_logger = Logger.getLogger(PADLocationConsumerImpl.class);
  public static String STAND_ALONE = "STAND_ALONE";
  public static int TRUE = 1;
  public static int FALSE = 0;
  
  private static boolean skipRest = false;
  
  public PADLocationConsumerImpl(UimaContext aCtx, Properties props)
  throws AnnotatorContextException, IOException
  { 
  }

  public void consumeHits(JCas jcas, Iterator lhItr)
      throws AnalysisEngineProcessException
  {
    Iterator hitsByOffsetItr = organizeByOffset(lhItr);
    
    while (hitsByOffsetItr.hasNext())
    {
      //this is a hit from dictionary, just one in this case
      Collection hitsAtOffsetCol = (Collection) hitsByOffsetItr.next();
      
      Iterator lhAtOffsetItr = hitsAtOffsetCol.iterator();
      int neBegin = -1;
      int neEnd = -1;
      while (lhAtOffsetItr.hasNext())
      {
        LookupHit lh = (LookupHit) lhAtOffsetItr.next();

        neBegin = lh.getStartOffset();
        neEnd = lh.getEndOffset();
        MetaDataHit metaData = lh.getDictMetaDataHit();
        
        addLocation(jcas, metaData, neBegin, neEnd);
      }
    }
  }
  
  private void addLocation(JCas jcas, MetaDataHit metaData, int neBegin, int neEnd)
  {
	  
    String mfValue =null;
    boolean skipTerm = false;
    mfValue = metaData.getMetaFieldValue("1");
    //check if the location is standalone...
    String standAlone = metaData.getMetaFieldValue("2");
    
    iv_logger.log(Level.INFO, "Meta field 1 ["+mfValue+"] col2["+standAlone+"]");
    int cutOffForRevision = findOriginalReportSubSection(jcas);
    PADLocation locationTerm = new PADLocation(jcas);
    locationTerm.setHitDictionaryValue(mfValue);

    if(standAlone != null && standAlone.equalsIgnoreCase(STAND_ALONE))
      locationTerm.setIsStandAlone(TRUE);
    else {
      locationTerm.setIsStandAlone(FALSE);
      locationTerm.setTypeID(new Integer(standAlone).intValue());
    }
    locationTerm.setBegin(neBegin);
    locationTerm.setEnd(neEnd);
    
    List list = getTokenData(jcas, locationTerm);
    FSArray fsArr = new FSArray(jcas, list.size());

    for(int i=0; i<list.size(); i++) {
    	BaseToken wta = (BaseToken)list.get(i);
    	if (wta.getCoveredText().compareToIgnoreCase("at")!=0 || 
    			(wta.getCoveredText().compareTo("AT")==0 && wta.getPartOfSpeech().compareTo("IN")!= 0))
    		fsArr.set(i, wta);
    	else if (i ==0)
    		skipTerm = true;
    }
    String segStatus = getSubSectionStatus(locationTerm, jcas, cutOffForRevision);
    
    if (!skipTerm && (segStatus.compareTo("-1")==0 || segStatus.compareTo("1")!=0)) {
    	locationTerm.setRelatedTokens(fsArr);
    	String segId = getSegmentIdContaining(locationTerm, jcas);
    	locationTerm.setSegmentID(segId);
    
    //fsArr.addToIndexes();
    	locationTerm.addToIndexes();
    }
  }
}
