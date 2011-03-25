

/* First created by JCasGen Thu Nov 18 10:02:31 CST 2010 */
package edu.mayo.bmi.uima.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 18 10:02:31 CST 2010
 * XML source: C:/general_workspace/smoking status/desc/type_system/SmokingProductionTypeSystem.xml
 * @generated */
public class PunctuationToken extends BaseToken {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PunctuationToken.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PunctuationToken() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PunctuationToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PunctuationToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PunctuationToken(JCas jcas, int begin, int end) {
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

    