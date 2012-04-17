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
 * Read instances of degree_of relation from knowtator xml files into the CAS. 
 * 
 * To understand the code better, please read the comments at the top of the
 * relation reader that handles all the other relations (i.e. "location_of" etc.)
 * 
 * @author dmitriy dligach
 *
 */
public class MipacqDegreeOfRelationReader extends JCasAnnotator_ImplBase {

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
		// get the relations and their arguments
		ArrayList<RelationInfo> relationInfos = XMLReader.getRelations(document);

		// relations between unique pairs of entities in gold standard
		HashSet<RelationInfo> uniqueRelations = new HashSet<RelationInfo>();
		// map unique spans of text to entity mention objects
		HashMap<Span, EntityMention> spanToEntity = new HashMap<Span, EntityMention>();
		// map unique spans of text to modifier objects
		HashMap<Span, Modifier> spanToModifier = new HashMap<Span, Modifier>();
		
		// add relations and relation arguments to the cas
		for(RelationInfo relationInfo : relationInfos) {

			// this reader only handles instances of degree_of
			if(! relationInfo.category.equals("degree_of")) {
				continue; 
			}

			// restrict the types of Related_to arguments to the ones that ctakes can extract
			if(relationInfo.role1.equals(Constants.mipacqRelatedToName) &&  
					(Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1)) == CONST.NE_TYPE_ID_UNKNOWN)) {
				continue;
			}
			if(relationInfo.role2.equals(Constants.mipacqRelatedToName) &&
					Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2)) == CONST.NE_TYPE_ID_UNKNOWN) {
				continue;
			}

			// only a single relation is allowed between same pair of gold entities
			if(uniqueRelations.contains(relationInfo)) {
				continue;
			}
			uniqueRelations.add(relationInfo);

			Span argumentSpan;
			int argumentType;
			// need to find out which of the two arguments is the first semantic argument (i.e. "Argument")
			if(relationInfo.role1.equals(Constants.mipacqArgumentName)) {
				Span first = entityMentions.get(relationInfo.id1).get(0);
				Span last = entityMentions.get(relationInfo.id1).get(entityMentions.get(relationInfo.id1).size() - 1);
				argumentSpan = new Span(first.start, last.end);
				argumentType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1));
			} 
			else if(relationInfo.role2.equals(Constants.mipacqArgumentName)) {
				Span first = entityMentions.get(relationInfo.id2).get(0);
				Span last = entityMentions.get(relationInfo.id2).get(entityMentions.get(relationInfo.id2).size() - 1);
				argumentSpan = new Span(first.start, last.end);
				argumentType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2));
			}
			else {
				continue; // invalid value in role1
			}
			
			Span relatedToSpan;
			int relatedToType;
			// need to find out which of the two arguments is the second semantic argument (i.e. "Related_to")
			if(relationInfo.role1.equals(Constants.mipacqRelatedToName)) {
				Span first = entityMentions.get(relationInfo.id1).get(0);
				Span last = entityMentions.get(relationInfo.id1).get(entityMentions.get(relationInfo.id1).size() - 1);
				relatedToSpan = new Span(first.start, last.end);
				relatedToType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id1));
			} 
			else if(relationInfo.role2.equals(Constants.mipacqRelatedToName)) {
				Span first = entityMentions.get(relationInfo.id2).get(0);
				Span last = entityMentions.get(relationInfo.id2).get(entityMentions.get(relationInfo.id2).size() - 1);
				relatedToSpan = new Span(first.start, last.end);
				relatedToType = Mapper.getEntityTypeId(entityTypes.get(relationInfo.id2));
			}
			else {
				continue; // invalid value in role2
			}
			
			// create a modifier for "Argument"
			Modifier modifier = null;
			if(spanToModifier.containsKey(argumentSpan)) {
				// an entity with the same span has already been added to the cas
				modifier = spanToModifier.get(argumentSpan);
			} 
			else {
				// this entity needs to be addded to the cas
				modifier = new Modifier(jCas, argumentSpan.start, argumentSpan.end);
				modifier.setTypeID(argumentType);
				modifier.setId(identifiedAnnotationId++);
				modifier.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				modifier.setConfidence(1);
				modifier.addToIndexes();
				spanToModifier.put(argumentSpan, modifier);
			}
			
			// create an entity mention for "Related_to"
			EntityMention entityMention = null;
			if(spanToEntity.containsKey(relatedToSpan)) {
				// an entity with the same span has already been added to the cas
				entityMention = spanToEntity.get(relatedToSpan);
			} 
			else {
				// this entity needs to be addded to the cas
				entityMention = new EntityMention(jCas, relatedToSpan.start, relatedToSpan.end);
				entityMention.setTypeID(relatedToType);
				entityMention.setId(identifiedAnnotationId++);
				entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention.setConfidence(1);
				entityMention.addToIndexes();
				spanToEntity.put(relatedToSpan, entityMention);
			}

			RelationArgument relationArgument1 = new RelationArgument(jCas);
			relationArgument1.setId(relationArgumentId++);
			relationArgument1.setArgument(modifier);
			relationArgument1.setRole(Constants.mipacqArgumentName);

			RelationArgument relationArgument2 = new RelationArgument(jCas);
			relationArgument2.setId(relationArgumentId++);
			relationArgument2.setArgument(entityMention);
			relationArgument2.setRole(Constants.mipacqRelatedToName);

			BinaryTextRelation binaryTextRelation = new BinaryTextRelation(jCas);
			binaryTextRelation.setArg1(relationArgument1);
			binaryTextRelation.setArg2(relationArgument2);
			binaryTextRelation.setId(relationId++);
			binaryTextRelation.setCategory(relationInfo.category);
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
	
	private void showAddedRelations(JCas jCas, boolean showBothArguments) {
		
		for(BinaryTextRelation binaryTextRelation : org.uimafit.util.JCasUtil.select(jCas, BinaryTextRelation.class)) {
			
			String arg1 = binaryTextRelation.getArg1().getArgument().getCoveredText();
			String role1 = binaryTextRelation.getArg1().getRole();
			
			String arg2 = binaryTextRelation.getArg2().getArgument().getCoveredText();
			String role2 = binaryTextRelation.getArg2().getRole();
			
			String category = binaryTextRelation.getCategory();
			
			if(category.equals("degree_of")) { // <argument> degree_of <related_to> e.g. severe pain
				
				if(showBothArguments) {
					System.err.format("<%s: %s> <%s: %s>\n", role1, arg1, role2, arg2);
				}
				
				if(role1.equals("Argument")) {
					System.out.println(arg1);
				} else {
					System.out.println(arg2);
				}
			}
		}
	}
}
