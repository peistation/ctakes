

/* First created by JCasGen Wed Aug 31 13:40:25 EDT 2011 */
package org.mipacq.annotation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** Hypothesized but negative example of coreference.  Used for debugging coreference model.
 * Updated by JCasGen Tue Nov 22 14:28:43 EST 2011
 * XML source: /home/tmill/cleanroomworkspace/ctakes/coref-resolver/desc/type-system/CorefTypes.xml
 * @generated */
public class NonCoreferenceRelation extends CoreferenceRelation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NonCoreferenceRelation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NonCoreferenceRelation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NonCoreferenceRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NonCoreferenceRelation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
}

    