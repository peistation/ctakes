package edu.mayo.bmi.uima.drugner.ae;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.ResultSpecification;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;

import edu.mayo.bmi.uima.core.util.FSUtil;
import edu.mayo.bmi.uima.core.util.JCasUtil;
import edu.mayo.bmi.uima.core.util.ParamUtil;

/**
 * Utility annotator that copy's data from an existing JCas object into a new
 * JCas object.
 * 
 * @author duffp
 * 
 */
public class CopyDrugAnnotator extends JTextAnnotator_ImplBase
{
	private int iv_srcType;

	private int iv_segmentType=0;
	
	/**
	 * Value is "sectionOverrideSet".  This parameter specifies which segments to skip.  
	 * The resulting range of tokens will include the entire contents of the span within
	 * the section(s) specified in the sectionOverrideSet parameters of the SegmentLookupWindow
	 * annotator.  The parameter should be of type String, should be multi-valued and optional. 
	 */
	public static final String PARAM_SEGMENTS_TO_SKIP = "sectionOverrideSet";
	    

    private Set iv_skipSegmentsSet;
	
	// constructor used to create a new instance of the destination
	// JCas object
	private Constructor iv_destContr;

	// key = source getter method (java.lang.reflect.Method)
	// val = destination setter method (java.lang.reflect.Method)
	private Map iv_getSetMap;

	public void initialize(AnnotatorContext annotCtx)
			throws AnnotatorInitializationException,
			AnnotatorConfigurationException
	{
		super.initialize(annotCtx);

		try
		{
			String className;
			className = (String) annotCtx.getConfigParameterValue("srcDrugObjClass");
			Class srcClass = Class.forName(className);
			iv_srcType = JCasUtil.getType(className);

			className = (String) annotCtx.getConfigParameterValue("destDrugObjClass");
			Class destClass = Class.forName(className);
			Class[] constrArgs = { JCas.class };
			iv_destContr = destClass.getConstructor(constrArgs);
			
			iv_skipSegmentsSet = ParamUtil.getStringParameterValuesSet(PARAM_SEGMENTS_TO_SKIP, annotCtx); 
			
			Map m = ParamUtil.getStringParameterValuesMap(
					"dataDrugBindMap",
					annotCtx,
					"|");
			iv_getSetMap = new HashMap();
			Iterator getterItr = m.keySet().iterator();
			while (getterItr.hasNext())
			{
				String getterMethName = (String) getterItr.next();
				String setterMethName = (String) m.get(getterMethName);

				Method getterMeth = srcClass.getMethod(getterMethName, null);

				// get corresponding setter that has compatible args
				Class[] setterArgs = { getterMeth.getReturnType() };
				Method setterMeth = destClass.getMethod(
						setterMethName,
						setterArgs);

				iv_getSetMap.put(getterMeth, setterMeth);
			}
		}
		catch (Exception e)
		{
			throw new AnnotatorInitializationException(e);
		}
	}

	public void process(JCas jcas, ResultSpecification rs)
			throws AnnotatorProcessException
	{
		// iterate over source objects in JCas
		JFSIndexRepository indexes = jcas.getJFSIndexRepository();
		Iterator srcObjItr = indexes.getAnnotationIndex(edu.mayo.bmi.uima.core.type.Segment.type).iterator();
		edu.mayo.bmi.uima.core.type.Segment segment = null;
		
		while (srcObjItr.hasNext())
		{
			TOP srcObj = (TOP) srcObjItr.next();

			Object[] constrArgVals = { jcas };
			try
			{
				// create new destination object
				TOP destObj = (TOP) iv_destContr.newInstance(constrArgVals);
				
				// Find the segments that are to be handled as complete sections
                boolean okayToSkip = false;
                boolean segmentMissing = iv_skipSegmentsSet.isEmpty();
				if (!segmentMissing){
					Iterator getSkipSegs = iv_skipSegmentsSet.iterator();
					segment = (edu.mayo.bmi.uima.core.type.Segment) srcObj;
					while (getSkipSegs.hasNext()  && !okayToSkip){
						if (getSkipSegs.next().equals(segment.getId())){

							okayToSkip = true;
						}
					}
					
				}
				// copy data from source to destination
				Iterator getterItr = iv_getSetMap.keySet().iterator();
				

				while (getterItr.hasNext() && (okayToSkip || segmentMissing))
				{
					Method getterMeth = (Method) getterItr.next();
					Method setterMeth = (Method) iv_getSetMap.get(getterMeth);

					Object val = getterMeth.invoke(srcObj, null);
					Object[] setterArgs = { val };
					setterMeth.invoke(destObj, setterArgs);
					
				}
				// add new destination object to JCas
				if (okayToSkip) {
					destObj.addToIndexes();
				} else {
					Iterator lookupWindows = FSUtil.getAnnotationsInSpanIterator(jcas, edu.mayo.bmi.uima.lookup.type.LookupWindowAnnotation.type, segment.getBegin(), segment.getEnd());
					while (lookupWindows.hasNext()){
						edu.mayo.bmi.uima.lookup.type.LookupWindowAnnotation lookup = (edu.mayo.bmi.uima.lookup.type.LookupWindowAnnotation) lookupWindows.next();
						edu.mayo.bmi.uima.lookup.type.DrugLookupWindowAnnotation drugLookup = new edu.mayo.bmi.uima.lookup.type.DrugLookupWindowAnnotation(jcas, lookup.getBegin(), lookup.getEnd());
						drugLookup.addToIndexes();
					}
				}
	
			}
			catch (Exception e)
			{
				throw new AnnotatorProcessException(e);
			}
		}
	}
}