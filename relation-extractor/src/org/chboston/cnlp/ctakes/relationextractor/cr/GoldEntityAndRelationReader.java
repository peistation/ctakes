/*
 * Copyright: (c) 2012  Children's Hospital Boston, Regents of the University of Colorado 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Dmitriy Dligach
 */

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
import edu.mayo.bmi.uima.core.type.textsem.Modifier;
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
 *  TODO: Currently this reader does not normalize the roles of the arguments
 *  accross different corpora. It will simply add to the cas whatever is in the data.
 *  However, the roles were not consistently annotated accross different corpora 
 *  (e.g. Sharp and Share assign different roles to the modifiers and entity
 *  mentions that participate in degree_of relation). This issue needs to be addressed
 *  so that modles can be trained on data coming from different sources.
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
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

		// map knowtator mention ids to entity offsets (ArrayList needed to handle disjoint spans)
		HashMap<String, ArrayList<Span>> entityMentions = XMLReader.getEntityMentions(document);
		// map knowtator mention ids to entity types
		HashMap<String, String> entityTypes = XMLReader.getEntityTypes(document);
		// get the relations and their arguments
		ArrayList<RelationInfo> relationInfos = XMLReader.getRelations(document);

		// map unique spans of text to entity mention objects
		// we need this ensure that every entity mention is added only once
		HashMap<Span, EntityMention> spanToEntity = new HashMap<Span, EntityMention>();

		addDegreeOfRelations(jCas, entityMentions, entityTypes, relationInfos, spanToEntity);
		addTheRestOfRelations(jCas, entityMentions, entityTypes, relationInfos, spanToEntity);
		addTheRestOfEntities(jCas, entityMentions, entityTypes, spanToEntity);
	}
	
	/**
	 * Add to the CAS instances of degree_of relations of the form:
	 * Modifer degree_of EntityMention (e.g. "chronic pain")
	 */
	private void addDegreeOfRelations(
			JCas jCas, 
			HashMap<String, ArrayList<Span>> entityMentions,
			HashMap<String, String> entityTypes,
			ArrayList<RelationInfo> relationInfos,
			HashMap<Span, EntityMention> spanToEntity) {
		
		// relations between unique pairs of entities in gold standard
		HashSet<RelationInfo> uniqueRelations = new HashSet<RelationInfo>();
		// map unique spans of text to modifier objects
		HashMap<Span, Modifier> spanToModifier = new HashMap<Span, Modifier>();
		
		// add relations and relation arguments to the cas
		for(RelationInfo relationInfo : relationInfos) {

			// this reader only handles instances of degree_of
			if(! relationInfo.category.equals("degree_of")) {
				continue; 
			}

			// ignore instances where entity mention cannot be extracted by ctakes
			if(! Constants.modifierClasses.contains(entityTypes.get(relationInfo.id1)) && // is this a modifier? 
					Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1)) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue; 
			}
			if(! Constants.modifierClasses.contains(entityTypes.get(relationInfo.id2)) && // is this a modifier? 
					Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2)) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue; 
			}
			
			// only a single relation is allowed between same pair of gold entities
			if(uniqueRelations.contains(relationInfo)) {
				continue;
			}
			uniqueRelations.add(relationInfo);

			Span modifierSpan;
			int modifierType;
			// need to find out which of the two arguments is the modifier
			if(Constants.modifierClasses.contains(entityTypes.get(relationInfo.id1))) {
				Span first = entityMentions.get(relationInfo.id1).get(0);
				Span last = entityMentions.get(relationInfo.id1).get(entityMentions.get(relationInfo.id1).size() - 1);
				modifierSpan = new Span(first.start, last.end);
				modifierType = Mapper.getModifierTypeId(entityTypes.get(relationInfo.id1));
			} 
			else if(Constants.modifierClasses.contains(entityTypes.get(relationInfo.id2))) {
				Span first = entityMentions.get(relationInfo.id2).get(0);
				Span last = entityMentions.get(relationInfo.id2).get(entityMentions.get(relationInfo.id2).size() - 1);
				modifierSpan = new Span(first.start, last.end);
				modifierType = Mapper.getModifierTypeId(entityTypes.get(relationInfo.id2));
			}
			else {
				continue; // neither of the arguments is a modifier; probably an annotation error
			}
			
			Span entityMentionSpan;
			int entityMentionType;
			// need to find out which of the two arguments is the entity mention
			if(! Constants.modifierClasses.contains(entityTypes.get(relationInfo.id1))) {
				Span first = entityMentions.get(relationInfo.id1).get(0);
				Span last = entityMentions.get(relationInfo.id1).get(entityMentions.get(relationInfo.id1).size() - 1);
				entityMentionSpan = new Span(first.start, last.end);
				entityMentionType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1));
			} 
			else if(! Constants.modifierClasses.contains(entityTypes.get(relationInfo.id2))) {
				Span first = entityMentions.get(relationInfo.id2).get(0);
				Span last = entityMentions.get(relationInfo.id2).get(entityMentions.get(relationInfo.id2).size() - 1);
				entityMentionSpan = new Span(first.start, last.end);
				entityMentionType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2));
			}
			else {
				continue; // neither of the arguments is an entity mention; probably an annotation error
			}
			
			// create a modifier object and add it to the cas
			Modifier modifier = null;
			if(spanToModifier.containsKey(modifierSpan)) {
				// an modifier with the same span has already been added to the cas
				modifier = spanToModifier.get(modifierSpan);
			} 
			else {
				// this entity needs to be addded to the cas
				modifier = new Modifier(jCas, modifierSpan.start, modifierSpan.end);
				modifier.setTypeID(modifierType);
				modifier.setId(identifiedAnnotationId++);
				modifier.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				modifier.setConfidence(1);
				modifier.addToIndexes();
				spanToModifier.put(modifierSpan, modifier);
			}
			
			// create an entity mention object and add it to the cas
			EntityMention entityMention = null;
			if(spanToEntity.containsKey(entityMentionSpan)) {
				// an entity with the same span has already been added to the cas
				entityMention = spanToEntity.get(entityMentionSpan);
			} 
			else {
				// this entity needs to be addded to the cas
				entityMention = new EntityMention(jCas, entityMentionSpan.start, entityMentionSpan.end);
				entityMention.setTypeID(entityMentionType);
				entityMention.setId(identifiedAnnotationId++);
				entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention.setConfidence(1);
				entityMention.addToIndexes();
				spanToEntity.put(entityMentionSpan, entityMention);
			}

			RelationArgument relationArgument1 = new RelationArgument(jCas);
			relationArgument1.setId(relationArgumentId++);
			relationArgument1.setArgument(modifier);
			relationArgument1.setRole(Constants.shareModifierRole);

			RelationArgument relationArgument2 = new RelationArgument(jCas);
			relationArgument2.setId(relationArgumentId++);
			relationArgument2.setArgument(entityMention);
			relationArgument2.setRole(Constants.shareEntityMentionRole);

			BinaryTextRelation binaryTextRelation = new BinaryTextRelation(jCas);
			binaryTextRelation.setArg1(relationArgument1);
			binaryTextRelation.setArg2(relationArgument2);
			binaryTextRelation.setId(relationId++);
			binaryTextRelation.setCategory(relationInfo.category);
			binaryTextRelation.setDiscoveryTechnique(CONST.REL_DISCOVERY_TECH_GOLD_ANNOTATION);
			binaryTextRelation.setConfidence(1);
			binaryTextRelation.addToIndexes();
		}

	}
	
	/**
	 * Add to the CAS the instances of all relations of the form:
	 * [Argument] [relation] [Related_to] (e.g. [skin] location_of [rash]
	 * Create an EntityMention for both [Argument] and [Related_to].
	 */
	private void addTheRestOfRelations(
			JCas jCas, 
			HashMap<String, ArrayList<Span>> entityMentions,
			HashMap<String, String> entityTypes,
			ArrayList<RelationInfo> relationInfos,
			HashMap<Span, EntityMention> spanToEntity) {
		
		// relations between unique pairs of entities in gold standard
		HashSet<RelationInfo> uniqueRelations = new HashSet<RelationInfo>();
		
		// add relations and relation arguments to the cas
		for(RelationInfo relationInfo : relationInfos) {

			// read only relations needed for SHARP
			if(! Constants.relationSet.contains(relationInfo.category)) {
				continue; 
			}

			// ignore all entities that ctakes cannot extract (and relations between them)
			// e.g. all instances of degree_of relation will be discarded
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
			binaryTextRelation.setCategory(relationInfo.category);
			binaryTextRelation.setDiscoveryTechnique(CONST.REL_DISCOVERY_TECH_GOLD_ANNOTATION);
			binaryTextRelation.setConfidence(1);
			binaryTextRelation.addToIndexes();
		}
	}
	
	/**
	 * Add the entities, that haven't been added to the CAS while adding relation instances. 
	 */
	private void addTheRestOfEntities(
			JCas jCas, 
			HashMap<String, ArrayList<Span>> entityMentions,
			HashMap<String, String> entityTypes,
			HashMap<Span, EntityMention> spanToEntity) {

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
	
//	/**
//	 * Show relation instances that are currently present in the CAS. 
//	 * This can be useful for debugging.  
//	 */
//	private void showAddedRelations(JCas jCas, boolean showBothArguments) {
//		
//		for(BinaryTextRelation binaryTextRelation : org.uimafit.util.JCasUtil.select(jCas, BinaryTextRelation.class)) {
//			
//			String arg1 = binaryTextRelation.getArg1().getArgument().getCoveredText();
//			String role1 = binaryTextRelation.getArg1().getRole();
//			
//			String arg2 = binaryTextRelation.getArg2().getArgument().getCoveredText();
//			String role2 = binaryTextRelation.getArg2().getRole();
//			
//			String category = binaryTextRelation.getCategory();
//			
//			if(category.equals("degree_of")) { // <argument> degree_of <related_to> e.g. severe pain
//				
//				if(showBothArguments) {
//					System.err.format("<%s: %s> <%s: %s>\n", role1, arg1, role2, arg2);
//				}
//				
//				if(role1.equals("Argument")) {
//					System.out.println(arg1);
//				} else {
//					System.out.println(arg2);
//				}
//			}
//		}
//	}
}
