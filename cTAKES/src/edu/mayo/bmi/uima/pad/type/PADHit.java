

/* First created by JCasGen Tue Jul 20 20:16:46 CDT 2010 */
package edu.mayo.bmi.uima.pad.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.NamedEntity;


/** Would represent one of the following
Term (in the case of a stand alone term)
Term + Location
Location + term
 * Updated by JCasGen Tue Jul 20 20:16:46 CDT 2010
 * XML source: C:/general_workspace/PAD term spotter/desc/type_system/PADSiteAndTerm.xml
 * @generated */
public class PADHit extends NamedEntity {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PADHit.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PADHit() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PADHit(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PADHit(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PADHit(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: uaTerm

  /** getter for uaTerm - gets 
   * @generated */
  public PADTerm getUaTerm() {
    if (PADHit_Type.featOkTst && ((PADHit_Type)jcasType).casFeat_uaTerm == null)
      jcasType.jcas.throwFeatMissing("uaTerm", "edu.mayo.bmi.uima.pad.type.PADHit");
    return (PADTerm)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((PADHit_Type)jcasType).casFeatCode_uaTerm)));}
    
  /** setter for uaTerm - sets  
   * @generated */
  public void setUaTerm(PADTerm v) {
    if (PADHit_Type.featOkTst && ((PADHit_Type)jcasType).casFeat_uaTerm == null)
      jcasType.jcas.throwFeatMissing("uaTerm", "edu.mayo.bmi.uima.pad.type.PADHit");
    jcasType.ll_cas.ll_setRefValue(addr, ((PADHit_Type)jcasType).casFeatCode_uaTerm, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: uaLocation

  /** getter for uaLocation - gets 
   * @generated */
  public PADLocation getUaLocation() {
    if (PADHit_Type.featOkTst && ((PADHit_Type)jcasType).casFeat_uaLocation == null)
      jcasType.jcas.throwFeatMissing("uaLocation", "edu.mayo.bmi.uima.pad.type.PADHit");
    return (PADLocation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((PADHit_Type)jcasType).casFeatCode_uaLocation)));}
    
  /** setter for uaLocation - sets  
   * @generated */
  public void setUaLocation(PADLocation v) {
    if (PADHit_Type.featOkTst && ((PADHit_Type)jcasType).casFeat_uaLocation == null)
      jcasType.jcas.throwFeatMissing("uaLocation", "edu.mayo.bmi.uima.pad.type.PADHit");
    jcasType.ll_cas.ll_setRefValue(addr, ((PADHit_Type)jcasType).casFeatCode_uaLocation, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    