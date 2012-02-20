package org.chboston.cnlp.ctakes.relationextractor.knowtator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

public class XMLReader {

	/**
	 * Get spans of named entity annotations indexed on knowtator mention id
	 */
  public static HashMap<String, ArrayList<Span>> getEntityMentions(Document document) {

  	// key: mention id, value: list of spans (need a list to handle disjoint spans)
  	HashMap<String, ArrayList<Span>> entityMentions = new HashMap<String, ArrayList<Span>>(); 

    try {
      Element elementRoot = document.getRootElement();
      List<?> annotations = elementRoot.getChildren("annotation");

      for (int i = 0; i < annotations.size(); i++) {
        Element elementAnnotation = (Element) annotations.get(i);

        List<?> elementSpans = elementAnnotation.getChildren("span");

        if(elementSpans.size() == 0) {
          continue; // spanless annotation, e.g. a relation; there should be no spannedText                                    
        }

        ArrayList<Span> spans = new ArrayList<Span>();
        for(int j = 0; j < elementSpans.size(); j++) {
          Element elementSpan = (Element) elementSpans.get(j);

          String start = elementSpan.getAttributeValue("start");
          String end = elementSpan.getAttributeValue("end");

          Span span = new Span(Integer.parseInt(start), Integer.parseInt(end));
          spans.add(span);
        }

        String mentionId = elementAnnotation.getChild("mention").getAttributeValue("id");                          
        
        entityMentions.put(mentionId, spans);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return entityMentions;
  }

  /**
   * Type of each named entity indexed on mention ids
   */
  public static HashMap<String, String> getEntityTypes(Document document) {

    // key: mention id, value: semantic type of the corresponding entity (e.g. "sign_symptom")                                      
    HashMap<String, String> entityTypes = new HashMap<String, String>();

    try {
      Element root = document.getRootElement();
      List<?> classMentions = root.getChildren("classMention");

      for (int i = 0; i < classMentions.size(); i++) {
        Element classMention = (Element) classMentions.get(i);
        String id = classMention.getAttributeValue("id");
        String cl = classMention.getChildText("mentionClass");
        entityTypes.put(id, cl);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return entityTypes;
  }
  
  public static ArrayList<RelationInfo> getRelations(Document document) {

    ArrayList<RelationInfo> relations = new ArrayList<RelationInfo>();

    try {
      Element root = document.getRootElement();

      // key: complexSlotMention id, value: complexSlotMention value                                                           
      HashMap<String, ArgumentInfo> hashComplexSlotMentions = new HashMap<String, ArgumentInfo>();

      // first read all complexSlotMentions which contain argument positions (Related_to or Argument)                         
      List<?> complexSlotMentions = root.getChildren("complexSlotMention");
      for (int i = 0; i < complexSlotMentions.size(); i++) {
        Element complexSlotMention = (Element) complexSlotMentions.get(i);

        String id = complexSlotMention.getAttributeValue("id");
        String value = complexSlotMention.getChild("complexSlotMentionValue").getAttributeValue("value");
        String position = complexSlotMention.getChild("mentionSlot").getAttributeValue("id"); // e.g. "Related_to"             

        hashComplexSlotMentions.put(id, new ArgumentInfo(value, position));
      }

      // now read all classMentions which have relation type and arguments (as hasSlotMention(s))                                 
      List<?> classMentions = root.getChildren("classMention");
      for (int i = 0; i < classMentions.size(); i++) {
        Element classMention = (Element) classMentions.get(i);
        List<?> hasSlotMentions = classMention.getChildren("hasSlotMention");

        if(hasSlotMentions.size() >= 2) {
          String relationType = classMention.getChildText("mentionClass");
          addRelation(relations, hasSlotMentions, hashComplexSlotMentions, relationType);  // save this relation and args
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return relations;
  }

  private static void addRelation(ArrayList<RelationInfo> relations, List<?> hasSlotMentions, 
  		HashMap<String, ArgumentInfo> hashComplexSlotMentions, String relationType) {
  	// add relation arguments and other relation information to the list of relations                                                  

  	// get the ids of the arguments; sometimes there are three hasSlotMention(s) but not all of them are arguments             
  	ArrayList<String> ids = new ArrayList<String>();
  	for(int i = 0; i < hasSlotMentions.size(); i++) {
  		String id = ((Element) hasSlotMentions.get(i)).getAttributeValue("id");
  		if(hashComplexSlotMentions.containsKey(id)) {
  			ids.add(id); // this is an argument                                                                                    
  		}
  	}

  	if(ids.size() != 2) {
  		return; // this classMention is not a relation (exactly two args are allowed)                                            
  	}

  	String id1 = hashComplexSlotMentions.get(ids.get(0)).value;          // obtain mention id1                                       
  	String position1 = hashComplexSlotMentions.get(ids.get(0)).position; // e.g. Argument                                                                                             

  	String id2 = hashComplexSlotMentions.get(ids.get(1)).value;          // obtain mention id2                                       
  	String position2 = hashComplexSlotMentions.get(ids.get(1)).position; // e.g. Related_to                                     

  	// a quick sanity check (this has failed before)
  	if(!position1.equals("Argument") && !position1.equals("Related_to")) {
  		System.out.println("unrecognized position: " + position1);
  		return;
  	}
  	if(!position2.equals("Argument") && !position2.equals("Related_to")) {
  		System.out.println("unrecognized position: " + position2);
  		return;
  	}

  	relations.add(new RelationInfo(id1, id2, position1, position2, relationType));
  }
}
