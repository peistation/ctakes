package org.chboston.cnlp.ctakes.relationextractor.cr;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.RelationInfo;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.Span;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.uimafit.component.JCasAnnotator_ImplBase;

import edu.mayo.bmi.uima.core.type.constants.CONST;
import edu.mayo.bmi.uima.core.type.relation.BinaryTextRelation;
import edu.mayo.bmi.uima.core.type.relation.RelationArgument;
import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;

/**
 * Read named entity annotations and relations between them 
 * from knowtator xml files into the CAS. 
 * 
 * Assumptions:
 * 
 *   - A pair of entities can only have a single relation between them
 *   - An entity can have only a single semantic type
 *   
 * For each relation instance in the gold standard, this reader will:
 * 
 *   - Check if the arguments of this relation instance can be extracted
 *     by CTAKEs automatically. If one of them cannot, this relation 
 *     instance and the entities will be skipped. 
 *   - Check if another relation between a pair of entities with the same 
 *     knowtator mention ids has already been added to the cas. If it has,
 *     the reader will not add a new relation between these entities.
 *
 *  This reader will also make sure each entity is added to the cas only once.
 *  E.g. the cas may already contain an entity if it participates in another
 *  relation that's already been added to the cas or due to an error in the gold 
 *  standard (i.e. if it was annotated twice -- such weirdness does happen).
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityAndRelationReader extends JCasAnnotator_ImplBase {

	// paramater that should contain the path to knowtator xml files
	public static final String PARAM_INPUTDIR = "InputDirectory";
	// path to knowtator xml files
	public static File inputDirectory;
	// counter for assigning entity ids
	public int identifiedAnnotationId;
	// counter for assigning relation ids
	public int relationId;
	// counter for assigning relation argument ids
	public int relationArgumentId;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		inputDirectory = new File((String)aContext.getConfigParameterValue(PARAM_INPUTDIR));
		
		identifiedAnnotationId = 0;
		relationId = 0;
		relationArgumentId = 0;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		String documentID = DocumentIDAnnotationUtil.getDocumentID(jCas);
		if (documentID == null) {
			throw new IllegalArgumentException("DocumentID must be present in CAS");
		}
		File goldFile = new File(inputDirectory, documentID + ".knowtator.xml");

		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(goldFile);
		} catch (JDOMException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

		// map knowtator mention ids to entity offsets (ArrayList needed to handle disjoint spans)
		HashMap<String, ArrayList<Span>> entityMentions = XMLReader.getEntityMentions(document);
		// map knowtator mention ids to entity types
		HashMap<String, String> entityTypes = XMLReader.getEntityTypes(document);
		// get relations and their arguments
		ArrayList<RelationInfo> relationInfos = XMLReader.getRelations(document);

		// relations between unique pairs of entities in gold standard
		HashSet<RelationInfo> uniqueRelations = new HashSet<RelationInfo>();
		// map unique spans of text to entity mention objects
		HashMap<Span, EntityMention> spanToEntity = new HashMap<Span, EntityMention>();

		// add relations and relation arguments to the cas
		for(RelationInfo relationInfo : relationInfos) {

			// read only relations needed for SHARP
			if(! Constants.sharpRelations.contains(relationInfo.relation)) {
				continue; 
			}

			// ignore all entities that ctakes cannot extract (and relations between them)
			if(Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1)) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue;
			}
			if(Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2)) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue;
			}

			// only a single relation is allowed between same pair of gold entities
			if(uniqueRelations.contains(relationInfo)) {
				continue;
			}
			uniqueRelations.add(relationInfo);

			// for disjoint spans, just ignore the gap
			Span first1 = entityMentions.get(relationInfo.id1).get(0);
			Span last1 = entityMentions.get(relationInfo.id1).get(entityMentions.get(relationInfo.id1).size() - 1);
			Span span1 = new Span(first1.start, last1.end);

			EntityMention entityMention1 = null;
			if(spanToEntity.containsKey(span1)) {
				// an entity with the same span has already been added to the cas
				entityMention1 = spanToEntity.get(span1);
			} 
			else {
				// this entity still needs to be addded to the cas
				entityMention1 = new EntityMention(jCas, span1.start, span1.end);
				entityMention1.setTypeID(Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1)));
				entityMention1.setId(identifiedAnnotationId++);
				entityMention1.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention1.setConfidence(1);
				entityMention1.addToIndexes();
				spanToEntity.put(span1, entityMention1);
			}

			// again, rememeber that some entities have disjoint spans 
			Span first2 = entityMentions.get(relationInfo.id2).get(0);
			Span last2 = entityMentions.get(relationInfo.id2).get(entityMentions.get(relationInfo.id2).size() - 1);
			Span span2 = new Span(first2.start, last2.end);

			EntityMention entityMention2 = null;
			if(spanToEntity.containsKey(span2)) {
				// an entity with this span already exists in the cas
				entityMention2 = spanToEntity.get(span2);
			}
			else {
				// this entity still needs to be added to the cas
				entityMention2 = new EntityMention(jCas, span2.start, span2.end);
				entityMention2.setTypeID(Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2)));
				entityMention2.setId(identifiedAnnotationId++);
				entityMention2.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention2.setConfidence(1);
				entityMention2.addToIndexes();
				spanToEntity.put(span2, entityMention2);
			}

			RelationArgument relationArgument1 = new RelationArgument(jCas);
			relationArgument1.setId(relationArgumentId++);
			relationArgument1.setArgument(entityMention1);
			relationArgument1.setRole(relationInfo.role1);

			RelationArgument relationArgument2 = new RelationArgument(jCas);
			relationArgument2.setId(relationArgumentId++);
			relationArgument2.setArgument(entityMention2);
			relationArgument2.setRole(relationInfo.role2);

			BinaryTextRelation binaryTextRelation = new BinaryTextRelation(jCas);
			binaryTextRelation.setArg1(relationArgument1);
			binaryTextRelation.setArg2(relationArgument2);
			binaryTextRelation.setId(relationId++);
			binaryTextRelation.setCategory(relationInfo.relation);
			binaryTextRelation.setDiscoveryTechnique(CONST.REL_DISCOVERY_TECH_GOLD_ANNOTATION);
			binaryTextRelation.setConfidence(1);
			binaryTextRelation.addToIndexes();
		}

		// add the rest of entities to the cas
		for(Map.Entry<String, ArrayList<Span>> entry : entityMentions.entrySet()) {

			Span first = entry.getValue().get(0);
			Span last = entry.getValue().get(entry.getValue().size() - 1);
			Span span = new Span(first.start, last.end);

			// has this span been added already?
			if(spanToEntity.containsKey(span)) {
				continue;
			}

			// ignore all entities that cannot be found by ctakes
			if(Mapper.getEntityTypeId(entityTypes.get(entry.getKey())) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue;
			}

			EntityMention entityMention = new EntityMention(jCas, span.start, span.end);
			entityMention.setTypeID(Mapper.getEntityTypeId(entityTypes.get(entry.getKey())));
			entityMention.setId(identifiedAnnotationId++);
			entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
			entityMention.setConfidence(1);
			entityMention.addToIndexes();

			spanToEntity.put(span, entityMention);
		}
	}
}
