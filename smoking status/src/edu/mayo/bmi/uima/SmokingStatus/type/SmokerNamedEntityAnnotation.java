

/* First created by JCasGen Wed Nov 10 13:27:25 CST 2010 */
package edu.mayo.bmi.uima.SmokingStatus.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import edu.mayo.bmi.uima.core.type.NamedEntity;


/** 
 * Updated by JCasGen Thu Nov 18 10:02:29 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class SmokerNamedEntityAnnotation extends NamedEntity {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SmokerNamedEntityAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SmokerNamedEntityAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SmokerNamedEntityAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SmokerNamedEntityAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SmokerNamedEntityAnnotation(JCas jcas, int begin, int end) {
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
     
}

    