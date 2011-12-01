

/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSList;


/** 
 * Updated by JCasGen Thu Dec 01 13:16:44 EST 2011
 * XML source: C:/Users/CH150124/Apps/ohnlp-cTAKES-trunk/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class CollectionRelation extends Relation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(CollectionRelation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected CollectionRelation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public CollectionRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public CollectionRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: members

  /** getter for members - gets 
   * @generated */
  public FSList getMembers() {
    if (CollectionRelation_Type.featOkTst && ((CollectionRelation_Type)jcasType).casFeat_members == null)
      jcasType.jcas.throwFeatMissing("members", "org.mipacq.annotation.type.CollectionRelation");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((CollectionRelation_Type)jcasType).casFeatCode_members)));}
    
  /** setter for members - sets  
   * @generated */
  public void setMembers(FSList v) {
    if (CollectionRelation_Type.featOkTst && ((CollectionRelation_Type)jcasType).casFeat_members == null)
      jcasType.jcas.throwFeatMissing("members", "org.mipacq.annotation.type.CollectionRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((CollectionRelation_Type)jcasType).casFeatCode_members, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    