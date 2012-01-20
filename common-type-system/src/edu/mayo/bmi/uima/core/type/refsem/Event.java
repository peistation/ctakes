

/* First created by JCasGen Thu Jan 19 17:46:26 EST 2012 */
package edu.mayo.bmi.uima.core.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** An event, which may bear some temporal properties and relationships.
 * Updated by JCasGen Fri Jan 20 10:31:31 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class Event extends Element {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Event.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Event() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Event(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Event(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: properties

  /** getter for properties - gets 
   * @generated */
  public EventProperties getProperties() {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_properties == null)
      jcasType.jcas.throwFeatMissing("properties", "edu.mayo.bmi.uima.core.type.refsem.Event");
    return (EventProperties)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Event_Type)jcasType).casFeatCode_properties)));}
    
  /** setter for properties - sets  
   * @generated */
  public void setProperties(EventProperties v) {
    if (Event_Type.featOkTst && ((Event_Type)jcasType).casFeat_properties == null)
      jcasType.jcas.throwFeatMissing("properties", "edu.mayo.bmi.uima.core.type.refsem.Event");
    jcasType.ll_cas.ll_setRefValue(addr, ((Event_Type)jcasType).casFeatCode_properties, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    