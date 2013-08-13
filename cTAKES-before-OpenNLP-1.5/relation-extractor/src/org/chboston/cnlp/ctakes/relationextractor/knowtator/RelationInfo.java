package org.chboston.cnlp.ctakes.relationextractor.knowtator;

import java.util.HashSet;

import com.google.common.base.Objects;

/**
 * Information about a relation that includes the info about the relation's arguments.
 * 
 * @author dmitriy dligach
 *
 */
public class RelationInfo {

  public String id1;       // id of the first argument
  public String id2;       // id of the second argument
  public String role1;     // position of first arg (e.g. Argument)
  public String role2;     // semantic type of second arg (e.g. Related_to)
  public String category;  // relation type e.g. co_occurs_with
  
  RelationInfo(String id1, String id2, String role1, String role2, String category) {
    this.id1 = id1; // id of the first argument
    this.id2 = id2; // id of the second argument
    this.role1 = role1;
    this.role2 = role2;
    this.category = category;
  }
  
  @Override
  public String toString() {
  	return String.format("<%s, %s, %s, %s, %s>", id1, id2, role1, role2, category);
  }
  
  /*
   * Returns true if two relation instances (represented as RelationInfo objects)
   * have the same arguments. Useful for debugging mipacq data which may contain duplicate relation instances. 
   */
  @Override
  public boolean equals(Object object) {
  	
  	boolean isEqual = false;
  			
  	if(this.getClass() == object.getClass()) {
  		RelationInfo relationInfo = (RelationInfo) object;
  		isEqual = (this.id1.equals(relationInfo.id1) && this.id2.equals(relationInfo.id2));
  	}
  	
  	return isEqual;
  }
  
  /*
   * Hash code must match equals() method. 
   */
  @Override
  public int hashCode()
  {
  	return Objects.hashCode(this.id1, this.id2);
  }
  
  public static void main(String[] args) {
	
  	RelationInfo ri1 = new RelationInfo("1", "2", "Argument", "Related_to", "location_of");
  	RelationInfo ri2 = new RelationInfo("1", "2", "zzzzzzzz", "xxxxxxxxxx", "yyyyyyyyyyy");
  	RelationInfo ri3 = new RelationInfo("1", "2", "kkkkkkkk", "llllllllll", "mmmmmmmmmmm");
  	
  	System.out.println(ri1.equals(ri2));
  	
  	HashSet<RelationInfo> uniqueRelations = new HashSet<RelationInfo>();
  	
  	System.out.println(ri1.hashCode() + "\t" + ri2.hashCode());
  	
  	uniqueRelations.add(ri1);
  	uniqueRelations.add(ri2);
  	
  	System.out.println(uniqueRelations);
  	
  	System.out.println(uniqueRelations.contains(ri1));
  	System.out.println(uniqueRelations.contains(ri2));
  	System.out.println(uniqueRelations.contains(ri3));
  }
}