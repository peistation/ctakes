package org.chboston.cnlp.ctakes.coref.uima.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;
import org.chboston.cnlp.ctakes.common.type.BooleanLabeledFS;
import org.chboston.cnlp.ctakes.coref.eval.helpers.Span;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanAlignment;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanOffsetComparator;
import org.chboston.cnlp.ctakes.parser.treekernel.TreeExtractor;
import org.chboston.cnlp.ctakes.parser.util.TreeUtils;
import org.chboston.cnlp.util.SimpleTree;

import edu.mayo.bmi.coref.util.CorefConsts;
import edu.mayo.bmi.coref.util.FSIteratorToList;
import edu.mayo.bmi.coref.util.GoldStandardLabeler;
import edu.mayo.bmi.coref.util.MarkableTreeUtils;
import edu.mayo.bmi.coref.util.PairAttributeCalculator;
import edu.mayo.bmi.coref.util.ParentPtrTree;
import edu.mayo.bmi.coref.util.SvmUtils;
import edu.mayo.bmi.coref.util.SvmVectorCreator;
import edu.mayo.bmi.uima.core.resource.FileLocator;
import edu.mayo.bmi.uima.core.resource.FileResource;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.coref.type.MarkablePairSet;
import edu.mayo.bmi.uima.coref.type.Markable;
import edu.mayo.bmi.uima.coref.type.DemMarkable;
import edu.mayo.bmi.uima.coref.type.NEMarkable;
import edu.mayo.bmi.uima.coref.type.PronounMarkable;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class ODIEVectorFileWriter extends CasConsumer_ImplBase {

	private Logger log = Logger.getLogger(this.getClass());
	private static final Integer NGRAM_THRESHOLD = 0;
	private String outputDir = null;
	private String goldStandardDir = null;
	private PrintWriter anaphOut = null;
	private PrintWriter neOut = null;
	private PrintWriter pronOut = null;
	private PrintWriter demOut = null;
	private PrintWriter neTreeOut = null;
	private PrintWriter pronTreeOut = null;
	private PrintWriter demTreeOut = null;
	private PrintWriter debug = null;
	private boolean initialized = false;
	private int posNeInst = 0;
	private int negNeInst = 0;
	private int posDemInst = 0;
	private int negDemInst = 0;
	private int posPronInst = 0;
	private int negPronInst = 0;
	private int posAnaphInst = 0;
	private int negAnaphInst = 0;
	//	private svm_problem anaphProb = null;
	private ArrayList<Integer> anaphLabels = new ArrayList<Integer>();
	private ArrayList<svm_node[]> anaphNodes = new ArrayList<svm_node[]>();
//	private ArrayList<Integer> corefLabels = new ArrayList<Integer>();
//	private ArrayList<svm_node[]> corefNodes = new ArrayList<svm_node[]>();
	//	private ArrayList<TopTreebankNode> corefPathTrees = new ArrayList<TopTreebankNode>();
//	private ArrayList<String> corefTypes = new ArrayList<String>();
	//	private ArrayList<Integer> neInds = new ArrayList<Integer>();
	private PairAttributeCalculator attr = null;
	private HashSet<String> stopwords;
	private ArrayList<String> treeFrags;
	private SvmVectorCreator vecCreator = null;
//	private int maxSpanID = 0;
	private GoldStandardLabeler labeler = null; //new GoldStandardLabeler();
	

//	Vector<Span> goldSpans = null;
//	Hashtable<String,Integer> goldSpan2id = null;
//	Vector<int[]> goldPairs = null;

//	Vector<Span> sysSpans = null;
//	Vector<int[]> sysPairs = null;
	//	private boolean printModels;
	private boolean printVectors;
	private boolean printTrees;
	private boolean anaphora;
	private boolean useFrags = false; 							// make a parameter once development is done...

	@Override
	public void initialize() throws ResourceInitializationException{
		outputDir = (String) getConfigParameterValue("outputDir");
		goldStandardDir = (String) getConfigParameterValue("goldStandardDir");
		//		printModels = (Boolean) getConfigParameterValue("writeModels");
		printVectors = (Boolean) getConfigParameterValue("writeVectors");
		printTrees = (Boolean) getConfigParameterValue("writeTrees");
//		upSample = (Boolean) getConfigParameterValue("upSample");
		anaphora = (Boolean) getConfigParameterValue("anaphora");

		try{
			// need to initialize parameters to default values (except where noted)
			File neDir = new File(outputDir + "/" + CorefConsts.NE + "/vectors/");
			neDir.mkdirs();
			File proDir = new File(outputDir + "/" + CorefConsts.PRON + "/vectors/");
			proDir.mkdirs();
			File demDir = new File(outputDir + "/" + CorefConsts.DEM + "/vectors/");
			demDir.mkdirs();
			if(printVectors){
				if(anaphora) anaphOut = new PrintWriter(outputDir + "/anaphor.trainingvectors.libsvm");
//				neOut = new PrintWriter(outputDir + "/" + CorefConsts.NE + "/training.libsvm");
//				demOut = new PrintWriter(outputDir + "/" + CorefConsts.DEM + "/training.libsvm");
//				pronOut = new PrintWriter(outputDir + "/" + CorefConsts.PRON + "/training.libsvm");
			}
			if(printTrees){
				neTreeOut = new PrintWriter(outputDir + "/" + CorefConsts.NE + "/trees.txt");
				demTreeOut = new PrintWriter(outputDir + "/" + CorefConsts.DEM + "/trees.txt");
				pronTreeOut = new PrintWriter(outputDir + "/" + CorefConsts.PRON + "/trees.txt");
				debug = new PrintWriter(new PrintWriter(outputDir + "/" + CorefConsts.NE + "/fulltrees_debug.txt"), true);
			}
			//			if(printModels){
			//				pathTreeOut = new PrintWriter(outputDir + "/" + CorefConsts.NE + "/matrix.out");
			//			}
			stopwords = new HashSet<String>();
			FileResource r = (FileResource) super.getUimaContext().getResourceObject("stopWords");
			BufferedReader br = new BufferedReader(new FileReader(r.getFile()));
			String l;
			while ((l = br.readLine())!=null) {
				l = l.trim();
				if (l.length()==0) continue;
				int i = l.indexOf('|');
				if (i > 0)
					stopwords.add(l.substring(0,i).trim());
				else if (i < 0)
					stopwords.add(l.trim());
			}
			File anaphModFile = FileLocator.locateFile("anaphoricity.mayo.rbf.model");
			svm_model anaphModel = svm.svm_load_model(anaphModFile.getAbsolutePath());
			vecCreator = new SvmVectorCreator(stopwords, anaphModel);
			r = (FileResource) super.getUimaContext().getResourceObject("treeFrags");
			Scanner scanner = new Scanner(r.getFile());
			if(useFrags){
				treeFrags = new ArrayList<String>();
				while(scanner.hasNextLine()){
					String line = scanner.nextLine();
					treeFrags.add(line.split(" ")[1]);
				}
				vecCreator.setFrags(treeFrags);
			}
			initialized = true;
		}catch(Exception e){
			System.err.println("Error initializing file writers.");
			throw new ResourceInitializationException();
		}
	}

	@Override
	public void processCas(CAS arg0) throws ResourceProcessException {
		//		System.err.println("processCas-ing");
		if(!initialized) return;
		JCas jcas;
		try {
			jcas = arg0.getCurrentView().getJCas();
		} catch (CASException e) {
			e.printStackTrace();
			System.err.println("No processing done in ODIEVectoFileWriter!");
			return;
		}

		String docId = DocumentIDAnnotationUtil.getDocumentID(jcas);
		docId = docId.substring(docId.lastIndexOf('/')+1, docId.length());
//		Hashtable<Integer, Integer> sysId2AlignId = new Hashtable<Integer, Integer>();
//		Hashtable<Integer, Integer> goldId2AlignId = new Hashtable<Integer, Integer>();
//		Hashtable<Integer, Integer> alignId2GoldId = new Hashtable<Integer, Integer>();
		if (docId==null) docId = "141471681_1";
		System.out.print("creating vectors for "+docId);
//		Vector<Span> goldSpans = loadGoldStandard(docId, goldSpan2id);
		int numPos = 0;

		FSIterator markIter = jcas.getAnnotationIndex(Markable.type).iterator();
		LinkedList<Annotation> lm = FSIteratorToList.convert(markIter);

//		while(markIter.hasNext()){
//			Markable m = (Markable) markIter.next();
//			String key = m.getBegin() + "-" + m.getEnd();
//			markables.put(key, m);
//		}
		
		labeler = new GoldStandardLabeler(goldStandardDir, docId, lm);

//		Vector<Span> sysSpans = loadSystemPairs(lm, docId);
		// align the spans


		FSIterator iter = null;
//		FSIterator iter = jcas.getJFSIndexRepository().getAllIndexedFS(AnaphoricityVecInstance.type);
//		int numVecs = corefNodes.size();
//		log.info(numVecs + " nodes at the start of processing...");

//		if(anaphora){
//			while(iter.hasNext()){
//				AnaphoricityVecInstance vec = (AnaphoricityVecInstance) iter.next();
//				String nodeStr = vec.getVector();
//				int label = getLabel(nodeStr);
//				if(label == 1) posAnaphInst++;
//				else if(label == 0) negAnaphInst++;
//				anaphLabels.add(label);
//				svm_node[] nodes = SvmUtils.getNodes(nodeStr);
//				anaphNodes.add(nodes);
//			}
//			return;
//		}
		
		if(printVectors){
			try {
				neOut = new PrintWriter(outputDir + "/" + CorefConsts.NE + "/vectors/" + docId + ".libsvm");
				demOut = new PrintWriter(outputDir + "/" + CorefConsts.DEM + "/vectors/" + docId + ".libsvm");
				pronOut = new PrintWriter(outputDir + "/" + CorefConsts.PRON + "/vectors/"+ docId + ".libsvm");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//		int ind = 0;
		iter = jcas.getJFSIndexRepository().getAllIndexedFS(MarkablePairSet.type);
		while(iter.hasNext()){
			//			VecInstance vec = (VecInstance) iter.next();
			MarkablePairSet pair = (MarkablePairSet) iter.next();
			Markable anaphor = pair.getAnaphor();
			String corefType = (anaphor instanceof NEMarkable ? CorefConsts.NE : (anaphor instanceof DemMarkable ? CorefConsts.DEM : CorefConsts.PRON));
			//			String nodeStr = vec.getVector();
			//			int label = getLabel(nodeStr);
			FSList pairList = pair.getAntecedentList();
			while(pairList instanceof NonEmptyFSList){
				NonEmptyFSList node = (NonEmptyFSList) pairList;
				BooleanLabeledFS labeledProb = (BooleanLabeledFS) node.getHead();
				int label = labeledProb.getLabel() ? 1 : 0;
				if(anaphora){
					if(label == 1) posAnaphInst++;
					else negAnaphInst++;
					anaphLabels.add(label);
					svm_node[] nodes = vecCreator.createAnaphoricityVector(anaphor, jcas);
					anaphNodes.add(nodes);
				}
				Markable antecedent = (Markable) labeledProb.getFeature();
				label = (labeler.isGoldPair(anaphor, antecedent) ? 1 : 0);
				if(label == 1){
					numPos++;
					if(corefType.equals(CorefConsts.NE)){
						posNeInst++;
						//					neInds.add(ind);
					}else if(corefType.equals(CorefConsts.DEM)){
						posDemInst++;
					}else if(corefType.equals(CorefConsts.PRON)){
						posPronInst++;
					}
				}
				else if(label == 0){
					if(corefType.equals(CorefConsts.NE)){
						negNeInst++;
						//					neInds.add(ind);
					}else if(corefType.equals(CorefConsts.DEM)){
						negDemInst++;
					}else if(corefType.equals(CorefConsts.PRON)){
						negPronInst++;
					}
				}
//				corefLabels.add(label);
//				corefTypes.add(corefType);				// need to add it every time so the indices match...
				//			corefPathTrees.add(pathTree);

				if(printVectors){
					svm_node[] nodes = vecCreator.getNodeFeatures(anaphor, antecedent, jcas); //getNodes(nodeStr);
//					corefNodes.add(nodes);
					PrintWriter writer = null;
					if(corefType.equals(CorefConsts.NE)){
						writer = neOut;
					}else if(corefType.equals(CorefConsts.PRON)){
						writer = pronOut;
					}else if(corefType.equals(CorefConsts.DEM)){
						writer = demOut;
					}
					writer.print(label);
					for(svm_node inst : nodes){
						writer.print(" ");
						writer.print(inst.index);
						writer.print(":");
						writer.print(inst.value);
					}
					writer.println();
					writer.flush();
				}

				if(printTrees){
					//					Markable anaphor = vec.getAnaphor();
					//					Markable antecedent = vec.getAntecedent();
					TreebankNode antecedentNode = MarkableTreeUtils.markableNode(jcas, antecedent.getBegin(), antecedent.getEnd());
					TreebankNode anaphorNode = MarkableTreeUtils.markableNode(jcas, anaphor.getBegin(), anaphor.getEnd());
					debug.println(TreeUtils.tree2str(antecedentNode));
					debug.println(TreeUtils.tree2str(anaphorNode));
//					TopTreebankNode pathTree = TreeExtractor.extractPathTree(antecedentNode, anaphorNode, jcas);
					SimpleTree pathTree = TreeExtractor.extractPathTree(antecedentNode, anaphorNode);
					SimpleTree petTree = TreeExtractor.extractPathEnclosedTree(antecedentNode, anaphorNode, jcas);
//					TopTreebankNode tree = mctTree;
//					String treeStr = TreeUtils.tree2str(tree);
//					String treeStr = mctTree.toString();
					String treeStr = pathTree.toString();
					PrintWriter writer = null;
					if(corefType.equals(CorefConsts.NE)){
						writer = neTreeOut;
					}else if(corefType.equals(CorefConsts.PRON)){
						writer = pronTreeOut;
					}else if(corefType.equals(CorefConsts.DEM)){
						writer = demTreeOut;
					}
					writer.print(label == 1 ? "+1" : "-1");
					writer.print(" |BT| ");
					writer.print(treeStr.replaceAll("\\) \\(", ")("));
					writer.println(" |ET|");
				}
				pairList = node.getTail();
				if(label == 1) break;
			}
		}
		if(printVectors){
			neOut.close();
			demOut.close();
			pronOut.close();
		}
//		numVecs = (corefNodes.size() - numVecs);
//		log.info("Document id: " + docId + " has " + numVecs + " pairwise instances.");
	}


	private int getLabel(String nodeStr) {
		return Integer.parseInt(nodeStr.substring(0,1));
	}

	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
	throws ResourceProcessException, IOException {
		super.collectionProcessComplete(arg0);
		//		System.err.println("collectionProcessComplete!");
		if(!initialized) return;

//		int numPos = 1;
//		int numNeg = 1;

		if(anaphora){
			double anaphRatio = (double) posAnaphInst / (double) negAnaphInst;
//			if(anaphRatio > 1.0) numNeg = (int) anaphRatio;
//			else numPos = (int) (1 / anaphRatio);
			for(int i = 0; i < anaphNodes.size(); i++){
				int label = anaphLabels.get(i);
//				int numIters = (label == 1 ? numPos : numNeg);
//				for(int j = 0; j < numIters; j++){
					anaphOut.print(label);
					for(svm_node node : anaphNodes.get(i)){
						anaphOut.print(" ");
						anaphOut.print(node.index);
						anaphOut.print(":");
						anaphOut.print(node.value);
					}
					anaphOut.println();
//				}
			}
			anaphOut.flush();
			anaphOut.close();
			return;
		}
		if(printVectors){
			neOut.close();
			demOut.close();
			pronOut.close();
		}

		if(printTrees){
			neTreeOut.flush();
			neTreeOut.close();
			demTreeOut.flush();
			demTreeOut.close();
			pronTreeOut.flush();
			pronTreeOut.close();
		}
	}

	private double[] listToDoubleArray(ArrayList<Integer> list) {
		double[] array = new double[list.size()];
		for(int i = 0; i < list.size(); i++){
			array[i] = (double) list.get(i);
		}
		return array;
	}
}
