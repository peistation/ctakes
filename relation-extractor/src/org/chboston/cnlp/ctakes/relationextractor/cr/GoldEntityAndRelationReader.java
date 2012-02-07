package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.uimafit.util.JCasUtil;

/**
 * Read named entity annotations and relations between them 
 * from knowtator xml files into the CAS
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityAndRelationReader extends JCasAnnotator_ImplBase {

	// paramater that should contain the path to knowtator xml files
	public static final String PARAM_INPUTDIR = "InputDirectory";
	// path to knowtator xml files
	public static String inputDirectory;
	// counter for assigning entity ids
	public int identifiedAnnotationId;
	// counter for assigning relation ids
	public int relationId;
	// counter for assigning relation argument ids
	public int relationArgumentId;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		inputDirectory = (String)aContext.getConfigParameterValue(PARAM_INPUTDIR);
		
		identifiedAnnotationId = 0;
		relationId = 0;
		relationArgumentId = 0;
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
			// get relations and their arguments
			ArrayList<RelationInfo> relations = XMLReader.getRelations(document);

			// mention ids of entities that are already added to the CAS
			HashSet<String> addedEntities = new HashSet<String>();
			
			// add relations and relation arguments to the CAS
			for(RelationInfo relation : relations) {
				
				Span span1 = entityMentions.get(relation.id1).get(0); // just the first part of a disjoint span for now
				EntityMention entityMention1 = new EntityMention(initView, span1.start, span1.end);
				entityMention1.setTypeID(getTypeId(entityTypes.get(relation.id1)));
				entityMention1.setId(identifiedAnnotationId++);
				entityMention1.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention1.setConfidence(1);
				entityMention1.addToIndexes();
				
				Span span2 = entityMentions.get(relation.id2).get(0); // just the first part of a disjoint span for now
				EntityMention entityMention2 = new EntityMention(initView, span2.start, span2.end);
				entityMention2.setTypeID(getTypeId(entityTypes.get(relation.id2)));
				entityMention2.setId(identifiedAnnotationId++);
				entityMention2.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention2.setConfidence(1);
				entityMention2.addToIndexes();

				addedEntities.add(relation.id1); // save to skip later when adding the rest of entities
				addedEntities.add(relation.id2); // save to skip later when adding the rest of entities
				
				RelationArgument relationArgument1 = new RelationArgument(initView);
				relationArgument1.setId(relationArgumentId++);
				relationArgument1.setArgument(entityMention1);
				relationArgument1.setRole(relation.position1);
				
				RelationArgument relationArgument2 = new RelationArgument(initView);
				relationArgument2.setId(relationArgumentId++);
				relationArgument2.setArgument(entityMention2);
				relationArgument2.setRole(relation.position2);
				
				BinaryTextRelation binaryTextRelation = new BinaryTextRelation(initView);
				binaryTextRelation.setArg1(relationArgument1);
				binaryTextRelation.setArg2(relationArgument2);
				binaryTextRelation.setId(relationId++);
				binaryTextRelation.setCategory(relation.relation);
				binaryTextRelation.setDiscoveryTechnique(CONST.REL_DISCOVERY_TECH_GOLD_ANNOTATION);
				binaryTextRelation.setConfidence(1);
				binaryTextRelation.addToIndexes();
			}
			
			
			// add the rest of entities to the CAS
			for(Map.Entry<String, ArrayList<Span>> entry : entityMentions.entrySet()) {
				if(addedEntities.contains(entry.getKey())) {
					continue; // this entry is already added
				}
				
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