package org.chboston.cnlp.ctakes.coref.uima.ae;

import java.io.IOException;

import edu.mayo.bmi.uima.core.type.relation.CoreferenceRelation;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.chboston.cnlp.ctakes.coref.uima.cc.InsertAnnotations;

public class PairVisualizer extends JCasAnnotator_ImplBase {

	private InsertAnnotations ia;
	private String annotID;
	private String srcTxtName;

	@Override
	public void initialize(UimaContext uc) throws ResourceInitializationException {
		String pprjFile = (String) uc.getConfigParameterValue("knowtPrj");
		String txtSrcDir = (String) uc.getConfigParameterValue("txtSrcDir");
		annotID = (String) uc.getConfigParameterValue("annotatorID");
		srcTxtName = (String) uc.getConfigParameterValue("srcTxtName");

		try {
			ia = new InsertAnnotations(pprjFile, txtSrcDir, annotID);
		} catch (TextSourceAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		FSIterator iter = jcas.getJFSIndexRepository().getAllIndexedFS(CoreferenceRelation.type);
		if (ia!=null) {
			while (ia.hasNextDoc()) {
				try {
					ia.next();
					TextSource ts = ia.getNextTextSource();
					if (ts.getName().equals(srcTxtName)) {
				    	String docText = ia.getNextTextSourceString();
				    	while (iter.hasNext()) {
				    		CoreferenceRelation cr = (CoreferenceRelation) iter.next();
				    		ia.createAnnotation(cr, ts, docText);
				    	}
				    	ia.saveProject();
						break;
					}
				} catch (TextSourceAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
