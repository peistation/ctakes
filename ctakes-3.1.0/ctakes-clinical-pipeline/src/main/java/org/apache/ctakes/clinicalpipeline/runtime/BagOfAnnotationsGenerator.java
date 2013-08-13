package org.apache.ctakes.clinicalpipeline.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import org.apache.ctakes.core.cr.FilesInDirectoryCollectionReader;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

public abstract class BagOfAnnotationsGenerator<T extends Annotation,K> {

	private String outputDir = null;
	private CollectionReader reader = null;
	private AnalysisEngine ae = null;
	private static final String defaultAEPath = "desc/analysis_engine/AggregatePlaintextUMLSProcessor.xml";
	private Class<T> classOfT;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public BagOfAnnotationsGenerator(String inputDir, String outputDir) throws UIMAException, IOException{
		this(inputDir, outputDir, null);
	}
	
	public BagOfAnnotationsGenerator(String inputDir, String outputDir, String aePath) throws UIMAException, IOException {
		reader = CollectionReaderFactory.createCollectionReaderFromPath("../ctakes-core/desc/collection_reader/FilesInDirectoryCollectionReader.xml", 
				FilesInDirectoryCollectionReader.PARAM_INPUTDIR, inputDir);
		this.ae = AnalysisEngineFactory.createAnalysisEngineFromPath(aePath == null ? defaultAEPath : aePath);
		this.outputDir = outputDir;
		this.classOfT = getClassOfT();
	}
	
	public void process() throws UIMAException, IOException{
		JCasIterable casIter = new JCasIterable(reader, ae);
		while(casIter.hasNext()){
			JCas jcas = casIter.next();
			String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
			
			// extract info from cas
			processCas(jcas, outputDir + File.separator + docId);
		}
		ae.destroy();
	}

	private void processCas(JCas jcas, String outputFilename) throws FileNotFoundException {
		PrintStream out = new PrintStream(outputFilename);

		Collection<T> annotations = JCasUtil.select(jcas, classOfT);
		for(T annot : annotations){
			K output = extractInformation(annot);
			if(output != null) out.println(output);
		}
		out.close();
	}
	
	protected abstract K extractInformation(T t);
	
	private Class<T> getClassOfT() {
		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<T>) superclass.getActualTypeArguments()[0];
	}
}
