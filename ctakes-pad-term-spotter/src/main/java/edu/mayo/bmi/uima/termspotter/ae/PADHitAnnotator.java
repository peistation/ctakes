/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.mayo.bmi.uima.termspotter.ae;

/**
 * This annotator consumes two kinds of dictionary hits - Term and Location (ex: 'Occlusion' and 'Iliac artery')
 * to create a PADHit annotation. Default implementation supports 2 cases: (a) look for adjacent 
 * term and location, meaning with stop words separating them (b) look for term and location 
 * that fall in a certain window made up of certain annotation types - both of which are defined
 * in the descriptor file.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.uima.core.resource.FileResource;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import edu.mayo.bmi.uima.pad.type.PADHit;
import edu.mayo.bmi.uima.pad.type.PADLocation;
import edu.mayo.bmi.uima.pad.type.PADTerm;
import edu.mayo.bmi.uima.pad.util.JCasUtil;

public class PADHitAnnotator extends JCasAnnotator_ImplBase
{
  public static Logger iv_logger = Logger.getLogger(PADHitAnnotator.class);

  /**
   * Part 1 of the pair (of annotations) that makes up a hit
   */
  public static String ANNOTATION_PART_ONE_OF_PAIR = "ANNOTATION_PART_ONE_OF_PAIR";

  /**
   * Part 2 of the pair (of annotations) that makes up a hit
   */
  public static String ANNOTATION_PART_TWO_OF_PAIR = "ANNOTATION_PART_TWO_OF_PAIR";
  
  /** 
   * Count of annotations defined by DISTANCE_ANN_TYPE between 
   * {@link #ANNOTATION_PART_ONE_OF_PAIR} and {@link #ANNOTATION_PART_TWO_OF_PAIR}  
   **/
  public static String DISTANCE = "DISTANCE";  
  /**
   * Annotation type that is used to count the distance.
   */
  public static String DISTANCE_ANN_TYPE = "DISTANCE_ANN_TYPE";
  
  /**
   * Annotation type that defines the boundary within which the dictionary hits should be present. 
   */
  public static String BOUNDARY_ANN_TYPE = "BOUNDARY_ANN_TYPE";

  /**
   * Annotation type that defines a backup boundary within which the dictionary hits should be present.
   * This backup boundary will be used if no boundary annotations are present 
   */
  public static String BACKUP_BOUNDARY_ANN_TYPE = "BACKUP_BOUNDARY_ANN_TYPE";
  
  /**
   * Flag to specify whether to filter out negated hits identified for ANNOTATION_PART_ONE_OF_PAIR
   */
  public static String ANNOTATION_PART_ONE_FILTEROUT_NEGATED = "ANNOTATION_PART_ONE_FILTEROUT_NEGATED";

  /**
   * Flag to specify whether to filter out negated hits identified for ANNOTATION_PART_TWO_OF_PAIR
   */
  public static String ANNOTATION_PART_TWO_FILTEROUT_NEGATED = "ANNOTATION_PART_TWO_FILTEROUT_NEGATED";
  
  /**
   * List of type ids to be ignored from being considered as hits. Note these type ids are ids specified 
   * in the CSV dictionaries.
   */
  public static String ANN_PART_ONE_TYPES_TO_IGNORE = "ANN_PART_ONE_TYPES_TO_IGNORE";
  
  /**
   * List of type ids to be ignored from being considered as hits. Note these type ids are ids specified 
   * in the CSV dictionaries.
   */
  public static String ANN_PART_TWO_TYPES_TO_IGNORE = "ANN_PART_TWO_TYPES_TO_IGNORE";  
  
  public static int NO_WINDOW_SIZE_SPECIFIED = -1;
  public static int NO_ANNOTATION_TYPE_SPECIFIED = -1;
  
  public void initialize(UimaContext aCtx) 
  throws ResourceInitializationException
  {
    try
    {
      super.initialize(aCtx);
      
      termAndLocation = new ArrayList<Object>();
      term          = new ArrayList<Object>();
      location      = new ArrayList<Object>();
      boundaryAnns  = new ArrayList<Object>();
      ignoreP1Types = new ArrayList<Integer>();
      ignoreP2Types = new ArrayList<Integer>();
      
      FileResource resrcStopWords = (FileResource) aCtx.getResourceObject(STOP_WORDS_FILE);
      File stopWordsFile = resrcStopWords.getFile();

      //gather annotation types that will be used in pairs
      annotationPartOneOfPair = (String)aCtx.getConfigParameterValue(ANNOTATION_PART_ONE_OF_PAIR);
      annotationPartTwoOfPair = (String)aCtx.getConfigParameterValue(ANNOTATION_PART_TWO_OF_PAIR);
      
      //gather annotation types to be ignored from being considered as hits
      Integer[] ignorep1 = (Integer[])aCtx.getConfigParameterValue(ANN_PART_ONE_TYPES_TO_IGNORE);
      Integer[] ignorep2 = (Integer[])aCtx.getConfigParameterValue(ANN_PART_TWO_TYPES_TO_IGNORE);
      for(int i=0;ignorep1 != null && i<ignorep1.length;i++)
        ignoreP1Types.add(ignorep1[i]);
      for(int i=0;ignorep2!=null && i<ignorep2.length;i++)
        ignoreP2Types.add(ignorep2[i]);
      
      //gather flags to filter-out negated or not
      Boolean partFilterOutNegatedStr = (Boolean)aCtx.getConfigParameterValue(ANNOTATION_PART_ONE_FILTEROUT_NEGATED);
      if(!partFilterOutNegatedStr.booleanValue())
        partOneFilterOutNegated = false;
      partFilterOutNegatedStr = (Boolean)aCtx.getConfigParameterValue(ANNOTATION_PART_TWO_FILTEROUT_NEGATED);
      if(!partFilterOutNegatedStr.booleanValue())
        partTwoFilterOutNegated = false;

      //gather window size and annotation type
      String windowSize = (String)aCtx.getConfigParameterValue(DISTANCE);
      String annotationTypeName = (String)aCtx.getConfigParameterValue(DISTANCE_ANN_TYPE);
      String boundaryAnnTypeName = (String)aCtx.getConfigParameterValue(BOUNDARY_ANN_TYPE);
      String backupBoundaryAnnTypeName = (String)aCtx.getConfigParameterValue(BACKUP_BOUNDARY_ANN_TYPE);
      
      if(windowSize != null)
        iWindowSize = Integer.parseInt(windowSize);

      if(annotationTypeName != null)
        iAnnotationType = JCasUtil.getType(annotationTypeName);
      
      if(boundaryAnnTypeName != null)
        iBoundaryAnnType  = JCasUtil.getType(boundaryAnnTypeName);
      
      if(backupBoundaryAnnTypeName != null)
        iBackupBoundaryAnnType = JCasUtil.getType(backupBoundaryAnnTypeName);
      
      if(annotationPartOneOfPair != null)
        iAnnTypePartOneOfPair = JCasUtil.getType(annotationPartOneOfPair);

      if(annotationPartTwoOfPair != null)
        iAnnTypePartTwoOfPair = JCasUtil.getType(annotationPartTwoOfPair);
      
      if(iv_logger.getLevel() == Level.DEBUG)
      {  iv_logger.debug("Read parameters : WINDOW_SIZE["+windowSize+"] ANNOTATION_TYPE["+annotationTypeName+"]"); }
      
      loadStopWords(stopWordsFile);
    }
    catch(Exception e)
    { throw new ResourceInitializationException(e); }
  }

  public void process(JCas jcas) 
  throws AnalysisEngineProcessException
  {
    try
    {
      init();
      gatherBoundaries(jcas);
      gatherPairPartOne(jcas, partOneFilterOutNegated);
      gatherPairPartTwo(jcas, partTwoFilterOutNegated);
      sortHits();
      createUAHit(jcas);
    }
    catch(CASException ce)
    { throw new AnalysisEngineProcessException(ce); }
    catch(IllegalAccessException iae)
    { throw new AnalysisEngineProcessException(iae); }
    catch(NoSuchFieldException nsfe)
    { throw new AnalysisEngineProcessException(nsfe); }
    catch(ClassNotFoundException cnfe)
    { throw new AnalysisEngineProcessException(cnfe); }
  }
  
  /**
   * Cycle through the Terms and locations found using dictionary lookup
   * @param cas
   */
  
  private void createUAHit(JCas cas)
  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException
  {
    Annotation currAnn = null;
    Annotation prevAnn = null;
    
    for(int i=0; i<termAndLocation.size(); i++)
    {
      Annotation ann = (Annotation)termAndLocation.get(i);
      
      if(iv_logger.getLevel() == Level.DEBUG)
        iv_logger.debug("Processing annotation ["+ann.getCoveredText()+"] ["+ann.getClass().getName()+"]");
      
      if(ann == null) 
        continue;
      
      //  annotationPartOneOfPair = UATerm
      if(checkStandAlone(cas, ann))
        continue;
      
      if(currAnn == null)
      {
        currAnn = ann;
      }
      else
      {
        prevAnn = currAnn;
        currAnn = ann;

        /**
         * Annotations are not contained in the same boundary annotation, so, continue processing currAnn,
         * prevAnn cannot be considered.
         */
        if(!isInBoundaryWindow(currAnn, prevAnn))
          continue;
        
        //if 2 of same type (term, term) 
        //process just one and see if the next set is a combination
        if(!areSameUAType(currAnn, prevAnn))
        {
          if(iv_logger.getLevel() == Level.DEBUG)
            iv_logger.debug("NOT Same type - Processing prevAnn["+prevAnn.getCoveredText()+"] currAnn["+currAnn.getCoveredText()+"]");          
          processTermAndLocation(cas, prevAnn, currAnn);
          
          //currAnn = null;
        }
        else if(areSameUAType(currAnn, prevAnn))
        {
          if(iv_logger.getLevel() == Level.DEBUG)
            iv_logger.debug("NOT Same type - Processing prevAnn["+prevAnn.getCoveredText()+"] currAnn["+currAnn.getCoveredText()+"]");

          processTermAndLocation(cas, prevAnn, currAnn);
        }
      }
    }
  }
  
  /**
   * Check if the current annotation is stand alone, if true, create a hit return true
   * @param jcas
   * @param ann
   * @return
   */
  private boolean checkStandAlone(JCas cas, Annotation ann)
  throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException
  {
    if( JCasUtil.isInstanceOf(annotationPartOneOfPair, ann)&& ((PADTerm)ann).getIsStandAlone() == 1)
    {
      PADHit uah = new PADHit(cas);
      uah.setUaTerm((PADTerm)ann);
      uah.addToIndexes();
      return true;
    }
    
    if(JCasUtil.isInstanceOf(annotationPartTwoOfPair, ann )&& ((PADLocation)ann).getIsStandAlone() == 1)
    {
      PADHit uah = new PADHit(cas);
      uah.setUaLocation((PADLocation)ann);
      uah.addToIndexes();
      return true;
    }      
    return false;
  }
  
  /**
   * Checks if both the annotation fall in one boundary annotation
   * @param currAnn
   * @param prevAnn
   * @return
   */
  private boolean isInBoundaryWindow(Annotation currAnn, Annotation prevAnn)
  {
    for(int i=0; i<boundaryAnns.size(); i++)
    {
      Annotation window = (Annotation)boundaryAnns.get(i);
      
      if(window.getBegin() <= currAnn.getBegin() && window.getBegin() <= prevAnn.getBegin() && 
          window.getEnd() > currAnn.getEnd() && window.getEnd() > prevAnn.getEnd())
        return true;
    }
    return false;
  }
  
  /**
   * checks if the passed annotations are of the same type.
   * @param currAnn
   * @param ann
   * @return
   */
  
  private boolean areSameUAType(Annotation currAnn, Annotation ann)
  {
    if(currAnn.getClass().getName().equalsIgnoreCase(ann.getClass().getName()))
      return true;
    return false;
  }
  
  private void init()
  {
    term.clear();
    location.clear();
    termAndLocation.clear();  
    boundaryAnns.clear();
  }
  
  /**
   * Checks if the annotations passed are adjacent to each other and are instances of 
   * PADLocation and PADTerm.
   * @param jcas
   * @param prevAnn
   * @param currAnn
   */
  
  private void processTermAndLocation(JCas jcas, Annotation prevAnn, Annotation currAnn)
  {
    if(iv_logger.getLevel() == Level.DEBUG)
      iv_logger.debug("Processing term and loc["+prevAnn.getCoveredText()+"] currAnn["+currAnn.getCoveredText()+"]");
    
    if(isAdjacentWithWindowSize(jcas, prevAnn, currAnn, iAnnotationType, iWindowSize))
    {
      boolean isHit = false;
      PADLocation ual = null;
      PADTerm uat = null;
      
      if((prevAnn != null && prevAnn instanceof PADLocation) && 
          (currAnn != null && currAnn instanceof PADTerm))
      {
        ual = (PADLocation)prevAnn;
        uat = (PADTerm)currAnn;
        isHit = true;
      }
      else if((prevAnn != null && prevAnn instanceof PADTerm) && 
          (currAnn != null && currAnn instanceof PADLocation))
      {
        uat = (PADTerm)prevAnn;
        ual = (PADLocation)currAnn;
        isHit = true;
      }
      //TODO: I think stand alones are checked in one other place too.
      else if( prevAnn != null)
      {
        //standalone = 1 implies stand alone hit
        if(prevAnn instanceof PADTerm && ((PADTerm)prevAnn).getIsStandAlone() == 1)
        {
          uat = (PADTerm)prevAnn;
          ual = null;
          isHit = true;
        }
        else if(prevAnn instanceof PADLocation && ((PADLocation)prevAnn).getIsStandAlone() == 1)
        {
          ual = (PADLocation)prevAnn;
          uat = null;
          isHit = true;
        }
      }
      
      if(isHit)
      {
        PADHit uah = new PADHit(jcas);
        uah.setUaLocation(ual);
        uah.setUaTerm(uat);
        uah.addToIndexes();
      }
      
    }
  }
  
  /**
   * Checks to see of the two annotations are adjacent to each other in which case returns true.
   * This method also returns a true if the NEs just have stop words between them.
   * @param jcas
   * @param prevAnn
   * @param currAnn
   * @return
   */
  
  private boolean isAdjacentWithStopWords(JCas jcas, Annotation prevAnn, Annotation currAnn)
  {
    WordToken prevWta = getLastToken(prevAnn);
    WordToken currWta = getFirstToken(currAnn);
    
    if(prevWta == null || currWta == null)
      return false;
    
    if(prevWta.getTokenNumber() == (currWta.getTokenNumber() - 1))
      return true;
    
    List<WordToken> containedWTAs = getContainedWordTokens(jcas, prevWta, currWta);
    for(int i=0; i<containedWTAs.size(); i++)
    {
      WordToken tempWta = containedWTAs.get(i);
      if(stopWords.contains(tempWta.getCoveredText()))
          continue;
      else
        return false;
    }
    
    //processed all wordTokenAnnotations and did not find one that is not a non-stop word 
    return true;
  }
  
  /**
   * Checks if the annotations passed are adjacent. Adjacent is defined as one of the following:
   * (a) annotations next to each other
   * (b) annotations with stop words between them
   * (c) annotations that fall within the given window defined by number of annotations of the specified type
   * 
   * @param jcas
   * @param prevAnn
   * @param currAnn
   * @param annType
   * @param windowSize
   * @return
   */
  
  private boolean isAdjacentWithWindowSize(JCas jcas, Annotation prevAnn, 
                                            Annotation currAnn, int annType, int windowSize)
  {
    if(windowSize == NO_WINDOW_SIZE_SPECIFIED && annType == NO_ANNOTATION_TYPE_SPECIFIED)
      return isAdjacentWithStopWords(jcas, prevAnn, currAnn);
    
    //get the number of annotations of type passed in annType, 
    //if that is <= windowSize
    //  then consider that the annotations are adjacent
    
    int numAnns = countAnnotationsOfTypeInSpan(jcas, prevAnn, currAnn, annType);
    
    return ((numAnns<=windowSize)?true:false);
  }
  
  /**
   * Counts the number of annotation of type annType passed that fall between the two annotations
   * passed in prevAnn and currAnn 
   */
  private int countAnnotationsOfTypeInSpan(JCas jcas, Annotation prevAnn, Annotation currAnn, int annType)
  {
    int count = 1; //sentence containing the hit is the 0th
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> annotItr = indexes.getAnnotationIndex(annType).iterator();

    while(annotItr.hasNext())
    {
      Annotation tempAnn = (Annotation)annotItr.next();

      //ann is between prev and curr annotations
      if(tempAnn.getBegin() >= prevAnn.getEnd() && tempAnn.getEnd() <= currAnn.getBegin())
        count++;
    }
    
    return (count>0?count:-1);
  }
  
  /**
   * 
   * @param jcas
   * @param prevWta
   * @param currWta
   * @return
   */
  
  private List<WordToken> getContainedWordTokens(JCas jcas, WordToken prevWta, WordToken currWta)
  {
    List<WordToken> list = new ArrayList<WordToken>();
    
    
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> annotItr = indexes.getAnnotationIndex(WordToken.type).iterator();

    while (annotItr.hasNext())
    {
      WordToken wta = (WordToken)annotItr.next();
      if(wta.getBegin() >= prevWta.getEnd() && wta.getEnd() <= currWta.getBegin())
        list.add(wta);
    }
    
    return list;
  }
  
  private List<FeatureStructure> getTokens(Annotation ann)
  {
    List<FeatureStructure> l = new ArrayList<FeatureStructure>();
    if(ann instanceof PADLocation)
    {
      FSArray fsa = (((PADLocation)ann).getRelatedTokens());
      
      for(int i=0; fsa!= null && i<fsa.size(); i++)
      {
        if(fsa.get(i)!=null)
        l.add(fsa.get(i));
      }
    }
    
    if(ann instanceof PADTerm)
    {
      FSArray fsa = (((PADTerm)ann).getRelatedTokens());
      
      for(int i=0; fsa!= null && i<fsa.size(); i++)
      {
        if(fsa.get(i)!=null)
        l.add(fsa.get(i));
      }
    }
    
    return l;
  }
  
  private WordToken getFirstToken(Annotation ann)
  {
    WordToken wta = null;
    
    if(ann instanceof PADLocation)
    {
      FSArray fsa = (((PADLocation)ann).getRelatedTokens());
      
      if(fsa!= null && fsa.size()>0)
        wta = (WordToken)fsa.get(0);
    }
    
    if(ann instanceof PADTerm)
    {
      FSArray fsa = (((PADTerm)ann).getRelatedTokens());
      
      if(fsa!= null && fsa.size()>0)
        wta = (WordToken)fsa.get(0);
    }
    if(wta == null)
    {
      System.err.println("Error:");
      System.err.println("["+((ann!=null)?ann.getCoveredText():"")+"] does not have Word Token - "+getClass().getName());
    }
    
    return wta;
  }

  private WordToken getLastToken(Annotation ann)
  {
    WordToken wta = null;

    if(ann instanceof PADLocation)
    {
      FSArray fsa = (((PADLocation)ann).getRelatedTokens());
      
      for(int i=0;(fsa!= null && i<fsa.size() && fsa.get(i) !=null);i++)
        wta = (WordToken)fsa.get(i);
    }
    
    if(ann instanceof PADTerm)
    {
      FSArray fsa = (((PADTerm)ann).getRelatedTokens());

      for(int i=0;(fsa!= null && i<fsa.size() && fsa.get(i) !=null);i++)
        wta = (WordToken)fsa.get(i);
      
    }
    if(wta == null)
      System.err.println("Error: ["+ann.getCoveredText()+"] does not have Word Token - "+getClass().getName());
    
    return wta;
  }  
  
  private void storePartOneHit(Object termOrLoc)
  { 
    term.add(termOrLoc);
    termAndLocation.add(termOrLoc);
  }
  
  private void storePartTwoHit(Object termOrLoc)
  { 
    location.add(termOrLoc);
    termAndLocation.add(termOrLoc);
  }
  
  /**
   * Gather annotations that represent boundaries and sort them by offsets
   * @param jcas
   * @throws CASException
   */
  private void gatherBoundaries(JCas jcas)
  throws CASException
  {
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    Iterator<?> annotItr = indexes.getAnnotationIndex(iBoundaryAnnType).iterator();
    
    if(!annotItr.hasNext())
      annotItr = indexes.getAnnotationIndex(iBackupBoundaryAnnType).iterator();
    
    while(annotItr.hasNext())
      boundaryAnns.add(annotItr.next());
  }

  private void gatherPairPartOne(JCas jcas, boolean filterOutNegated)
  throws CASException
  {
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    
    //process all PADTerms
    Iterator<?> annotItr = indexes.getAnnotationIndex(iAnnTypePartOneOfPair).iterator();

    //if terms present store them
    while(annotItr.hasNext())
    {
      PADTerm uaTerm = (PADTerm)annotItr.next();

      if((uaTerm.getPolarity()<0 && filterOutNegated) || isInIgnoreTypesPartOne(uaTerm))//negated, so get next
      {
        iv_logger.info(uaTerm.getHitDictionaryValue() + " ignoring because negated");
        continue;
      }
      storePartOneHit(uaTerm);
    }
  }
  
  private void gatherPairPartTwo(JCas jcas, boolean filterOutNegated)
  throws CASException
  {
    JFSIndexRepository indexes = jcas.getJFSIndexRepository();
    
    //process all PADTerms
    Iterator<?> annotItr = indexes.getAnnotationIndex(iAnnTypePartTwoOfPair).iterator();

    //if terms present store them
    while(annotItr.hasNext())
    {
      PADLocation uaLoc = (PADLocation)annotItr.next();

      if((uaLoc.getPolarity()<0 && filterOutNegated) || isInIgnoreTypesPartTwo(uaLoc))//negated, so get next
      {
        iv_logger.info(uaLoc.getHitDictionaryValue() + " ignoring because negated");
        continue;
      }
      storePartTwoHit(uaLoc);
    }    
  }
  
  private boolean isInIgnoreTypesPartOne(PADTerm uaTerm)
  {
    for(int i=0; i<ignoreP1Types.size(); i++)
    {
      if(ignoreP1Types.get(i).intValue() == uaTerm.getTypeID())
        return true;
    }
    return false;
  }

  private boolean isInIgnoreTypesPartTwo(PADLocation uaLoc)
  {
    for(int i=0; i<ignoreP2Types.size(); i++)
    {
      if(ignoreP2Types.get(i).intValue() == uaLoc.getTypeID())
        return true;
    }
    return false;
  }
  

  
  protected void loadStopWords(File stopWordsFile)
  throws FileNotFoundException, IOException
  {
    String line;
    
    if(stopWords == null)
      stopWords = new ArrayList<String>();
    
    FileReader fr = new FileReader(stopWordsFile);
    BufferedReader br = new BufferedReader(fr);
    
    while((line = br.readLine())!= null)
    {
      String stopWord = line.trim();
      stopWords.add(stopWord);
    }
  }  
  
  private void sortHits()
  {
    HitComparator hc = new HitComparator();
    Collections.sort(term, hc);
    Collections.sort(location, hc);
    Collections.sort(termAndLocation, hc);
    Collections.sort(boundaryAnns,hc);
  }
  
  class HitComparator implements java.util.Comparator
  {
    public int compare(Object o1, Object o2)
    {
      Annotation a1 = (Annotation)o1;
      Annotation a2 = (Annotation)o2;
      
      return a1.getEnd() - a2.getBegin();
    }
    
  }


private List<Object> term;            //list containing all term annotations
  private List<Object> location;        //list containing all location annotations
  private List<Object> termAndLocation; //all terms and locations sorted by offset
  private List<String> stopWords;       //contains all stop words from a resource
  private List<Object> boundaryAnns;    //contains all boundary annotations
  private List<Integer> ignoreP1Types;   //contains all type ids (string representation of numbers) to be ignored for part 1
  private List<Integer> ignoreP2Types;   //contains all type ids (string representation of numbers) to be ignored for part 2
  private final String STOP_WORDS_FILE = "StopWordsFile";
  private boolean partOneFilterOutNegated = true; //flag to specify whether or not to filter out negated hits, default filter out
  private boolean partTwoFilterOutNegated = true; //flag to specify whether or not to filter out negated hits, default filter out
  
  private int iWindowSize = NO_WINDOW_SIZE_SPECIFIED;   //window size to identify pair of annotations as related
  private int iAnnotationType = NO_ANNOTATION_TYPE_SPECIFIED; //type used to define a window
  private int iBoundaryAnnType = NO_ANNOTATION_TYPE_SPECIFIED; //type used to define boundary across which pairs cannot exist.
  /**
   * backup type used to define boundary across which pairs cannot exist.
   */
  private int iBackupBoundaryAnnType = NO_ANNOTATION_TYPE_SPECIFIED; 
  
  private int iAnnTypePartOneOfPair; //part one class type of the annotation that makes up the pair
  private int iAnnTypePartTwoOfPair; //part two class type of the annotation that makes up the pair
  
  private String annotationPartOneOfPair = "";
  private String annotationPartTwoOfPair = "";

  
}

