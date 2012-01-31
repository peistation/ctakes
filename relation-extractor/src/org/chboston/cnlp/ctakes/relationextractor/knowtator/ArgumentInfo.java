package org.chboston.cnlp.ctakes.relationextractor.knowtator;

/**
 * Information about a relation argument.
 * 
 * It typically looks something like this in a knowtator xml file:
 * 
 *   <complexSlotMention id="Relations_Sept21_Schema_Set02_Instance_90018">
 *   <mentionSlot id="Related_to" />
 *   <complexSlotMentionValue value="Relations_Sept21_Schema_Instance_30350" />
 *   </complexSlotMention>
 * 
 * This xml is parsed and stored in this class.
 * 
 * @author dmitriy dligach
 *
 */
public class ArgumentInfo {

  public String value;    // value of "value" attribute above
  public String position; // e.g. "Related_to"
  
  ArgumentInfo(String value, String position) {
    this.value = value;
    this.position = position;
  }
}
