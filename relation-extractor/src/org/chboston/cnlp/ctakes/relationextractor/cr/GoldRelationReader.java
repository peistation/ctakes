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
import org.chboston.cnlp.ctakes.relationextractor.knowtator.RelationInfo;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.Span;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.uimafit.component.JCasAnnotator_ImplBase;

import edu.mayo.bmi.uima.core.type.constants.CONST;
import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.relation.RelationArgument;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * Read named entity annotations from knowtator xml files into the CAS
 * 
 * @author dmitriy dligach
 *
 */
public class GoldRelationReader extends JCasAnnotator_ImplBase {

	// paramater that should contain the path to knowtator xml files
	public static final String PARAM_INPUTDIR = "InputDirectory";
	// path to knowtator xml files
	public static String inputDirectory;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		inputDirectory = (String)aContext.getConfigParameterValue(PARAM_INPUTDIR);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		try {
			JCas initView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
			String goldFilePath = inputDirectory + DocumentIDAnnotationUtil.getDocumentID(jCas) + ".knowtator.xml";
			
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(new File(goldFilePath));
			
			HashMap<String, ArrayList<Span>> entityMentions = XMLReader.getEntityMentions(document);
			HashMap<String, String> entityTypes = XMLReader.getEntityTypes(document);
			ArrayList<RelationInfo> relations = XMLReader.getRelations(document);
			
			for(RelationInfo relation : relations) {
				Span span1 = entityMentions.get(relation.id1).get(0);
				EntityMention entityMention1 = new EntityMention(initView, span1.start, span1.end);
				entityMention1.addToIndexes();
				
				Span span2 = entityMentions.get(relation.id2).get(0);
				EntityMention entityMention2 = new EntityMention(initView, span2.start, span2.end);
				entityMention2.addToIndexes();
				
				RelationArgument relationArgument1 = new RelationArgument(initView);
				relationArgument1.setArgument(entityMention1);
				relationArgument1.setRole(relation.position1);
				relationArgument1.addToIndexes();
				
				RelationArgument relationArgument2 = new RelationArgument(initView);
				relationArgument2.setArgument(entityMention2);
				relationArgument2.setRole(relation.position2);
				relationArgument2.addToIndexes();
				
				BinaryTextRelation binaryTextRelation = new BinaryTextRelation(initView);
				binaryTextRelation.setArg1(relationArgument1);
				binaryTextRelation.setArg2(relationArgument2);
				binaryTextRelation.setCategory(relation.relation);
				binaryTextRelation.addToIndexes();
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