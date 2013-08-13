package org.apache.ctakes.assertion.pipelines;

import static org.apache.ctakes.assertion.util.AssertionTreeUtils.extractAboveLeftConceptTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.ctakes.assertion.eval.XMIReader;
import org.apache.ctakes.assertion.util.SemanticClasses;
import org.apache.ctakes.constituency.parser.util.AnnotationTreeUtils;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.syntax.TopTreebankNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.utils.tree.SimpleTree;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

public class GenerateTreeRepresentation{

	public static class Options extends Options_ImplBase {

		@Option(
				name = "--train-dir",
				usage = "specify the directory containing the XMI training files (for example, /NLP/Corpus/Relations/mipacq/xmi/train)",
				required = true)
		public File trainDirectory;
		
		@Option(
				name = "--output",
				usage = "The file to which the data points be written.",
				required = true)
		public File outFile;
	}
	
	protected static Options options = new Options();
	private static SemanticClasses sems = null; 
	private static PrintStream out = null;
	private static Logger log = Logger.getLogger(GenerateTreeRepresentation.class);
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws UIMAException, IOException {
	    options.parseOptions(args);
	    
	    out = new PrintStream(options.outFile);
	    List<File> trainFiles = Arrays.asList(options.trainDirectory.listFiles());

	    String[] paths = new String[trainFiles.size()];
	    for (int i = 0; i < paths.length; ++i) {
	      paths[i] = trainFiles.get(i).getPath();
	    }
	    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
	            XMIReader.class,
	            XMIReader.PARAM_FILES,
	            paths);
	       
	    JCasIterable casIter = new JCasIterable(reader);
	    while(casIter.hasNext()){
	    	JCas jcas = casIter.next();
	    	processDocument(jcas);
	    }
	    out.close();
	}

	public static void processDocument(JCas jcas) throws ResourceInitializationException, FileNotFoundException {
		log.info("Processing document: " + DocumentIDAnnotationUtil.getDocumentID(jcas));
		if(sems == null){
			sems = new SemanticClasses(FileLocator.locateFile("org/apache/ctakes/assertion/models/semantic_classes").getAbsolutePath());
		}
		Collection<IdentifiedAnnotation> mentions = JCasUtil.select(jcas, IdentifiedAnnotation.class);
		for(IdentifiedAnnotation mention : mentions){
			TopTreebankNode orig = AnnotationTreeUtils.getAnnotationTree(jcas, mention);
			if(orig == null){
				log.warn("Tree for entity mention: " + mention.getCoveredText() + " (" + mention.getBegin() + "-" + mention.getEnd() + ") is null.");
				continue;
			}
			SimpleTree tree = extractAboveLeftConceptTree(jcas, mention, sems);
//			if(mention.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT){
			if(mention.getUncertainty() == CONST.NE_UNCERTAINTY_PRESENT){
				out.print("+1 ");
			}else{
				out.print("-1 ");
			}
			
			out.print("|BT| ");
			out.print(tree.toString());
			out.println(" |ET|");
			out.flush();
		}
	}

}
