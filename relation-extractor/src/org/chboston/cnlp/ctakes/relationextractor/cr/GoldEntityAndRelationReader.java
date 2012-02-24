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
 * from knowtator xml files into the CAS
 * 
 * @author dmitriy dligach
 *
 */
public class GoldEntityAndRelationReader extends JCasAnnotator_ImplBase {

	// read all the relations in the data or just the SHARP ones?
	public final boolean readOnlySharpRelations = true;
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

				if(readOnlySharpRelations) {
					if(! Constants.sharpRelations.contains(relation.relation)) {
						continue; // only load SHARP relations
					}
				}
				
				Span span1 = entityMentions.get(relation.id1).get(0); // just the first part of a disjoint span for now
				EntityMention entityMention1 = new EntityMention(jCas, span1.start, span1.end);
				entityMention1.setTypeID(Mapper.getEntityTypeId(entityTypes.get(relation.id1)));
				entityMention1.setId(identifiedAnnotationId++);
				entityMention1.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention1.setConfidence(1);
				entityMention1.addToIndexes();
				
				Span span2 = entityMentions.get(relation.id2).get(0); // just the first part of a disjoint span for now
				EntityMention entityMention2 = new EntityMention(jCas, span2.start, span2.end);
				entityMention2.setTypeID(Mapper.getEntityTypeId(entityTypes.get(relation.id2)));
				entityMention2.setId(identifiedAnnotationId++);
				entityMention2.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention2.setConfidence(1);
				entityMention2.addToIndexes();

				addedEntities.add(relation.id1); // save to skip later when adding the rest of entities
				addedEntities.add(relation.id2); // save to skip later when adding the rest of entities
				
				RelationArgument relationArgument1 = new RelationArgument(jCas);
				relationArgument1.setId(relationArgumentId++);
				relationArgument1.setArgument(entityMention1);
				relationArgument1.setRole(relation.position1);
				
				RelationArgument relationArgument2 = new RelationArgument(jCas);
				relationArgument2.setId(relationArgumentId++);
				relationArgument2.setArgument(entityMention2);
				relationArgument2.setRole(relation.position2);
				
				BinaryTextRelation binaryTextRelation = new BinaryTextRelation(jCas);
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

				EntityMention entityMention = new EntityMention(jCas, span.start, span.end);
				entityMention.setTypeID(Mapper.getEntityTypeId(entityTypes.get(entry.getKey())));
				entityMention.setId(identifiedAnnotationId++);
				entityMention.setDiscoveryTechnique(CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION);
				entityMention.setConfidence(1);
				entityMention.addToIndexes();
			}
	}
}