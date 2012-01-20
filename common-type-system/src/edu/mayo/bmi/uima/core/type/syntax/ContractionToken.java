

/* First created by JCasGen Thu Jan 19 17:46:27 EST 2012 */
package edu.mayo.bmi.uima.core.type.syntax;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Equivalent to cTAKES: edu.mayo.bmi.uima.core.type.ContractionToken
 * Updated by JCasGen Fri Jan 20 10:31:46 EST 2012
 * XML source: C:/Users/CH150124/workspace_SHARPn/common-type-system/desc/common_type_system.xml
 * @generated */
public class ContractionToken extends BaseToken {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ContractionToken.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ContractionToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ContractionToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ContractionToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ContractionToken(JCas jcas, int begin, int end) {
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

    