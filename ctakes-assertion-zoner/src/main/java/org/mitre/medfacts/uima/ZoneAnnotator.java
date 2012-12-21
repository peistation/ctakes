package org.mitre.medfacts.uima;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.ctakes.assertion.zoner.types.Heading;
import org.apache.ctakes.assertion.zoner.types.Subzone;
import org.apache.ctakes.assertion.zoner.types.Zone;
import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

//import org.mitre.medfacts.zoner.*;
import org.mitre.medfacts.zoner.ZonerCli;
import org.mitre.medfacts.zoner.ZonerCli.HeadingRange;
import org.mitre.medfacts.zoner.ZonerCli.Range;
//import org.mitre.medfacts.zoner.ZonerCliSimplified;
//import org.mitre.medfacts.zoner.ZonerCliSimplified.HeadingRange;
//import org.mitre.medfacts.zoner.ZonerCliSimplified.Range;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.ExternalResource;
import org.uimafit.descriptor.TypeCapability;

@TypeCapability(outputs =
{
  "org.apache.ctakes.assertion.zoner.types.Zone",
  "org.apache.ctakes.assertion.zoner.types.Zone:label",
  "org.apache.ctakes.assertion.zoner.types.Subzone",
  "org.apache.ctakes.assertion.zoner.types.Subzone:label",
  "org.apache.ctakes.assertion.zoner.types.Heading",
  "org.apache.ctakes.assertion.zoner.types.Heading:label"
})

public class ZoneAnnotator extends JCasAnnotator_ImplBase {
	public static final String PARAM_SECTION_REGEX_FILE_URI = "SectionRegex";
	
  @ConfigurationParameter(
      name = PARAM_SECTION_REGEX_FILE_URI,
      description = "xml configuration file with zone regular expression values",
      mandatory = true)
  protected URI sectionRegexFileUri;

  protected final Logger logger = Logger.getLogger(ZoneAnnotator.class.getName());
	
	//private ZonerCliSimplified zonerCli;
	
	@Override
	public void initialize (UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		// Create ZonerCli using the resource URI 
//		URI uri;
//		try {
//			uri = getContext().getResourceURI(PARAM_SECTION_REGEX_FILE_NAME);
//		} catch (ResourceAccessException e) {
//			e.printStackTrace();
//			throw new ResourceInitializationException(e);
//		}
		//zonerCli = new ZonerCliSimplified(sectionRegexFileUri);

	}
	
	private int countOfIndexOutOfBounds = 0;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
//	  ZonerCliSimplified zonerCli =
//	      new ZonerCliSimplified(sectionRegexFileUri);
    ZonerCli zonerCli =
      new ZonerCli(sectionRegexFileUri);
    
		zonerCli.setEntireContents(jcas.getDocumentText());
		// initialize converter once contents are set
		zonerCli.initialize();
		try {
			zonerCli.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
			//throw new AnalysisEngineProcessException(e);
		} catch (StringIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.format("string index out of bounds exception count: %d%n", ++countOfIndexOutOfBounds);
			return;
			//throw new AnalysisEngineProcessException(e);
		}
		// Add the zone annotations
		List<Range> rangeList = zonerCli.getRangeList();
		for (Iterator<Range> i = rangeList.iterator(); i.hasNext();  ) {
			Range r = i.next();
		    Zone zAnnot = new Zone(jcas);	
		    zAnnot.setBegin(r.getBegin());
		    zAnnot.setEnd(r.getEnd());
		    zAnnot.setLabel(r.getLabel());
		    zAnnot.addToIndexes();
		    logger.info(String.format("added new zone annotation [%d-%d] \"%s\"", zAnnot.getBegin(), zAnnot.getEnd(), zAnnot.getCoveredText()));
		}
		
		List<Range> subsectionRangeList = zonerCli.getSubsections();
		for (Iterator<Range> i = subsectionRangeList.iterator(); i.hasNext();  ) {
			Range r = i.next();
		    Subzone sAnnot = new Subzone(jcas);	
		    sAnnot.setBegin(r.getBegin());
		    sAnnot.setEnd(r.getEnd());
		    sAnnot.setLabel(r.getLabel());
		    sAnnot.addToIndexes();
		    logger.info(String.format("added new subzone annotation [%d-%d] \"%s\"", sAnnot.getBegin(), sAnnot.getEnd(), sAnnot.getCoveredText()));
		}
		
		
		
		// Add the heading annotations
		List<HeadingRange> headings = zonerCli.getHeadings();
		for (Iterator<HeadingRange> i = headings.iterator(); i.hasNext();  ) {
			HeadingRange r = i.next();
		    Heading hAnnot = new Heading(jcas);	
		    hAnnot.setBegin(r.getHeadingBegin());
		    hAnnot.setEnd(r.getHeadingEnd());
		    hAnnot.setLabel(r.getLabel());
		    hAnnot.addToIndexes();
		    logger.info(String.format("added new headingrange annotation [%d-%d] \"%s\"", hAnnot.getBegin(), hAnnot.getEnd(), hAnnot.getCoveredText()));
		}

	}

}
