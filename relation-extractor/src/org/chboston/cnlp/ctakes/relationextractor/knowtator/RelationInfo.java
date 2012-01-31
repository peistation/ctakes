package org.chboston.cnlp.ctakes.relationextractor.knowtator;

/**
 * Information about a relation that includes the info about the relation's arguments.
 * Can generate relation signatures which are basically string representations of a relation instance.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationInfo {

  public String id1; // id of the first argument
  public String id2; // id of the second argument
  public String position1; // position of first arg (e.g. Argument)
  public String position2; // semantic type of second arg (e.g. Related_to)
  public String relation;  // relation type e.g. co_occurs_with
  
  RelationInfo(String id1, String id2, String position1, String position2, String relation) {
    this.id1 = id1; // id of the first argument
    this.id2 = id2; // id of the second argument
    this.position1 = position1;
    this.position2 = position2;
    this.relation = relation;
  }
}
