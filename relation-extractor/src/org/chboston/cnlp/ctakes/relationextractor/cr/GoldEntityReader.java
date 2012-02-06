package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.Span;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.uimafit.component.JCasAnnotator_ImplBase;

import edu.mayo.bmi.uima.core.type.constants.CONST;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * Read named entity annotations from knowtator xml files into the CAS
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityReader extends JCasAnnotator_ImplBase {

	// paramater that should contain the path to knowtator xml files
	public static final String PARAM_INPUTDIR = "InputDirectory";
	// path to knowtator xml files
	public static String inputDirectory;
	// counter for assigning entity ids
	public int identifiedAnnotationId;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		inputDirectory = (String)aContext.getConfigParameterValue(PARAM_INPUTDIR);
		identifiedAnnotationId = 0;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		try {
			JCas initView = jCas.getView(CAS.NAME_DEFAULT_SOFA); 
			String goldFilePath = inputDirectory + DocumentIDAnnotationUtil.getDocumentID(jCas) + ".knowtator.xml";
			
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(new File(goldFilePath));
			
      // map knowtator mention ids to entity offsets
			HashMap<String, ArrayList<Span>> entityMentions = XMLReader.getEntityMentions(document);
			// map knowtator mention ids to entity types
			HashMap<String, String> entityTypes = XMLReader.getEntityTypes(document);
			
			for(Map.Entry<String, ArrayList<Span>> entry : entityMentions.entrySet()) {
				
				// for now just use the first part of a disjoint span
				Span span = entry.getValue().get(0); 

				EntityMention entityMention = new EntityMention(initView, span.start, span.end);
				entityMention.setTypeID(getTypeId(entityTypes.get(entry.getKey())));
				entityMention.setId(identifiedAnnotationId++);
				entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention.setConfidence(1);
				
				entityMention.addToIndexes();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Map entity type to its integer id
	 */
	public int getTypeId(String entityType) {

		if(entityType.equals("Chemicals_and_drugs")) return CONST.NE_TYPE_ID_DRUG;
		else if(entityType.equals("Disorders")) return CONST.NE_TYPE_ID_DISORDER;
		else if(entityType.equals("Finding")) return CONST.NE_TYPE_ID_FINDING;
		else if(entityType.equals("Procedures")) return CONST.NE_TYPE_ID_PROCEDURE;
		else if(entityType.equals("Anatomy")) return CONST.NE_TYPE_ID_ANATOMICAL_SITE;
		else return CONST.NE_TYPE_ID_UNKNOWN;
	}
}