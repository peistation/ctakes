
/* First created by JCasGen Wed Nov 10 13:27:26 CST 2010 */
package edu.mayo.bmi.uima.libsvm.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Nov 18 10:02:32 CST 2010
 * @generated */
public class DocumentID_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentID_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentID_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentID(addr, DocumentID_Type.this);
  			   DocumentID_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentID(addr, DocumentID_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentID.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.libsvm.type.DocumentID");
 
  /** @generated */
  final Feature casFeat_documentID;
  /** @generated */
  final int     casFeatCode_documentID;
  /** @generated */ 
  public String getDocumentID(int addr) {
        if (featOkTst && casFeat_documentID == null)
      jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.libsvm.type.DocumentID");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentID);
  }
  /** @generated */    
  public void setDocumentID(int addr, String v) {
        if (featOkTst && casFeat_documentID == null)
      jcas.throwFeatMissing("documentID", "edu.mayo.bmi.uima.libsvm.type.DocumentID");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentID_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_documentID = jcas.getRequiredFeatureDE(casType, "documentID", "uima.cas.String", featOkTst);
    casFeatCode_documentID  = (null == casFeat_documentID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentID).getCode();

  }
}



    