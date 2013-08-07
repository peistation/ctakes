/*    Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
@author Tim Miller
 * 
 */
package edu.mayo.bmi.coref.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashSet;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import opennlp.tools.parser.Parse;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.chboston.cnlp.ctakes.parser.treekernel.TreeExtractor;
import org.chboston.cnlp.ctakes.parser.util.TreeUtils;
import org.chboston.cnlp.util.FragmentUtils;
import org.chboston.cnlp.util.SimpleTree;

import edu.mayo.bmi.uima.coref.type.Markable;

public class SvmVectorCreator {
	HashSet<String> stopwords = null;
	private svm_model anaph_model = null;
	ArrayList<SimpleTree> frags = new ArrayList<SimpleTree>();
	
	public SvmVectorCreator(HashSet<String> stopwords){
		this.stopwords = stopwords;
	}

	public SvmVectorCreator(HashSet<String> stopwords, svm_model anaph){
		this.stopwords = stopwords;
		anaph_model = anaph;
	}

	public svm_node[] createAnaphoricityVector(Markable m, JCas aJCas) {
//		String vec = isGoldMarkable(m)?"1":"0";
		String[] feats = FeatureVector.getAnaphoricityFeatures();
		ArrayList<svm_node> nodeList = new ArrayList<svm_node>();
		
		AnaphoricityAttributeCalculator aac = new AnaphoricityAttributeCalculator(aJCas, m);
		aac.setStopWordsList(stopwords);
		for (int i = 0; i < feats.length; i++) {
			try {
				Object val = aac.getClass().getMethod("calcm"+feats[i]).invoke(aac);
				if (val instanceof String) {
					String s = (String) val;
					if (s.equals("yes") || s.equals("Y") || s.equals("C") || s.equals("P")) {
//						vec += " " + (i+1) + ":1";
						svm_node node = new svm_node();
						node.index = i+1;
						node.value = 1;
						nodeList.add(node);
					} else if (s.equals("S")) {
//						vec += " " + (i+1) + ":0.5";
						svm_node node = new svm_node();
						node.index = i+1;
						node.value = 0.5;
						nodeList.add(node);
					} else if (!s.equals("U") && !s.equals("N") && !s.equals("no") && !s.equals("I")){ //TODO: cleanup
						int ii = Integer.parseInt(s);
						if (ii>=0 && ii<=6){
//							vec += " " + (i+1) + ":" + (ii/6.0);
							svm_node node = new svm_node();
							node.index = i+1;
							node.value = ii/6.0;
							nodeList.add(node);
						}
						
					}
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		return nodeList.toArray(new svm_node[]{});
//		AnaphoricityVecInstance avi = new AnaphoricityVecInstance(aJCas);
//		avi.setVector(vec);
//		avi.setM(m.getCoveredText());
//		avi.setOffset(m.getBegin()+"-"+m.getEnd());
//		avi.addToIndexes();
	}

//	private void createPairVector(Markable m1, Markable m2, boolean needsAnaph, String classifier, JCas aJCas) {
////		if(!isGoldPair(m1,m2)) return;
//		// Calculate features
//		String[] feats = FeatureVector.getNECorefFeatures();
//
//		SyntaxAttributeCalculator sac = new SyntaxAttributeCalculator(aJCas, m1, m2);
////		PairAttributeCalculator pac = new PairAttributeCalculator(aJCas, m1, m2);
//		sac.setStopWordsList(stopwords);
////		String vec = isGoldPair(m1, m2)?"1":"0";
//		int ind = 0;
//		for (int i = 0; i < feats.length; i++, ind++) {
//			try {
////				if (feats[i].equals("Anaph")) {
////					double anaph_prob = needsAnaph ? calcAnaphoricity(aJCas, m2) : 1.0;
//////					vec += " " + (ind+1) + ":" + anaph_prob;
////					continue;
////				}
//				if (feats[i].contains(":")){
//					String[] catFeat = feats[i].split(":");
//					String featType = catFeat[0];
//					String featName = catFeat[1];
//					// if catFeat[0].equalsIgnoreCase("cat"){}   // Not necessary, no other feature namespaces used yet.
//					String methodName = "num" + featName;
//					//					Class<String> strClass = new Class<String>();
//					int num = (Integer) sac.getClass().getMethod(methodName, Markable.class).invoke(sac, m2);
//					methodName = "calc" + featType + featName;
//					Method method = sac.getClass().getMethod(methodName, Integer.class, Markable.class);
//					for(int j = 0; j < num; j++, ind++){
//						String val = (String) method.invoke(sac, j, m2);
//						if(val.equalsIgnoreCase("Y")){
////							vec += " " + (ind+1) + ":1";
//						}
//					}
//					continue;
//				}
//				Object val = sac.getClass().getMethod("calc"+feats[i]).invoke(sac);
//				if (val instanceof String) {
//					String s = (String) val;
//					if (s.equals("yes") || s.equals("Y") || s.equals("C")) {
////						vec += " " + (ind+1) + ":1";
//					}
////					else if (s.equals("no") || s.equals("N") || s.equals("I")) {
////						vec += " " + (i+1) + ":0.5";
////					}
//				}
//				else if (val instanceof Integer) {
//					int v = ((Integer)val).intValue();
//					if (v!=0) {
////						vec += " " + (ind+1) + ":" + ((double)v/(i==0?600:10));
//					}
//				}
//				else if (val instanceof Double) {
////					vec += " " + (ind+1) + ":" + val;
//				}
//			} catch (Exception e) { e.printStackTrace(); }
//		}
//
//	}

	private double calcAnaphoricity (JCas aJCas, Markable m) {
		// TODO: check
//		LinkedList<svm_node> ll = new LinkedList<svm_node>();
//		String[] feats = FeatureVector.getAnaphoricityFeatures();
//
//		AnaphoricityAttributeCalculator aac = new AnaphoricityAttributeCalculator(aJCas, m);
//		aac.setStopWordsList(stopwords);
//		for (int i = 0; i < feats.length; i++) {
//			try {
//				Object val = aac.getClass().getMethod("calcm"+feats[i]).invoke(aac);
//				if (val instanceof String) {
//					String s = (String) val;
//					if (s.equals("yes") || s.equals("Y") || s.equals("C") || s.equals("P")) {
//						svm_node n = new svm_node();
//						n.index = i+1;
//						n.value = 1;
//						ll.add(n);
//					} else if (s.equals("S")) {
//						svm_node n = new svm_node();
//						n.index = i+1;
//						n.value = 0.5;
//						ll.add(n);
//					} else if (!s.equals("U") && !s.equals("N") && !s.equals("no") && !s.equals("I")){
//						int iii = Integer.parseInt(s);
//						if (iii>=0 && iii<=6) {
//							svm_node n = new svm_node();
//							n.index = i+1;
//							n.value = iii/6.0;
//							ll.add(n);
//						}
//					}
//				}
//			} catch (Exception e) { e.printStackTrace(); }
//		}

		svm_node[] nodes = createAnaphoricityVector(m, aJCas);
		double[] prob = new double[2];
		svm.svm_predict_probability(anaph_model, nodes, prob);
		int[] labels = new int[2];
		svm.svm_get_labels(anaph_model, labels);
		int anaph_idx = labels[0]==1 ? 0 : 1;
		return prob[anaph_idx];
	}

	
	public svm_node[] getNodeFeatures(Markable anaphor, Markable antecedent, JCas aJCas) {
		return getNodeFeatures(anaphor, antecedent, aJCas, false);
	}
	
	public svm_node[] getNodeFeatures(Markable anaphor, Markable antecedent, JCas aJCas, boolean needsAnaph) {
		LinkedList<svm_node> nodes = new LinkedList<svm_node>();
		String[] feats = FeatureVector.getNECorefFeatures();
		SyntaxAttributeCalculator sac = new SyntaxAttributeCalculator(aJCas, antecedent, anaphor);
		sac.setStopWordsList(stopwords);
		int ind = 0;
		for (int i = 0; i < feats.length; i++, ind++) {
			try {
				if (feats[i].equals("Anaph")) {
					double anaph_prob = needsAnaph ? calcAnaphoricity(aJCas, anaphor) : 1.0;
//					vec += " " + (ind+1) + ":" + anaph_prob;
					svm_node n = new svm_node();
					n.index = ind+1;
					n.value = anaph_prob;
					nodes.add(n);
					continue;
				}
				if (feats[i].contains(":")){
					String[] catFeat = feats[i].split(":");
					String featType = catFeat[0];
					String featName = catFeat[1];
					// if catFeat[0].equalsIgnoreCase("cat"){}   // Not necessary, no other feature namespaces used yet.
					String methodName = "num" + featName;
					//					Class<String> strClass = new Class<String>();
					int num = (Integer) sac.getClass().getMethod(methodName, Markable.class).invoke(sac, anaphor);
					methodName = "calc" + featType + featName;
					Method method = sac.getClass().getMethod(methodName, Integer.class, Markable.class);
					for(int j = 0; j < num; j++, ind++){
						String val = (String) method.invoke(sac, j, anaphor);
						if(val.equalsIgnoreCase("Y")){
							svm_node n = new svm_node();
							n.index = ind+1;
							n.value = 1;
							nodes.add(n);
//							vec += " " + (ind+1) + ":1";
						}
					}
					continue;
				}else{
					Object val = sac.getClass().getMethod("calc"+feats[i]).invoke(sac);
					if (val instanceof String) {
						String s = (String) val;
						if (s.equals("yes") || s.equals("Y") || s.equals("C")) {
							//						vec += " " + (ind+1) + ":1";
							svm_node n = new svm_node();
							n.index = ind+1;
							n.value = 1;
							nodes.add(n);
						}
					}
					else if (val instanceof Integer) {
						int v = ((Integer)val).intValue();
						if (v!=0) {
							//						vec += " " + (ind+1) + ":" + ((double)v/(i==0?600:10));
							svm_node n = new svm_node();
							n.index = ind+1;
							n.value = (double) v; // ((double)v/(i==0?600:10));
							nodes.add(n);
						}
					}
					else if (val instanceof Double) {
						//					vec += " " + (ind+1) + ":" + val;
						if((Double)val != 0.0){
							svm_node n = new svm_node();
							n.index = ind+1;
							n.value = (Double) val;
							nodes.add(n);
						}
					}
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		if(frags != null && frags.size() > 0){
			SimpleTree tn = TreeExtractor.extractPathTree(MarkableTreeUtils.markableNode(aJCas, antecedent.getBegin(), antecedent.getEnd()),
					MarkableTreeUtils.markableNode(aJCas, anaphor.getBegin(), anaphor.getEnd()));
//			SimpleTree tn = TreeExtractor.extractPathEnclosedTree(MarkableTreeUtils.markableNode(aJCas, antecedent.getBegin(), antecedent.getEnd()),
//					MarkableTreeUtils.markableNode(aJCas, anaphor.getBegin(), anaphor.getEnd()),
//					aJCas);
			// now go over the tree fragment features:
			for(SimpleTree frag : frags){
				if(TreeUtils.contains(tn, frag)){
					svm_node n = new svm_node();
					n.index = ind+1;
					n.value = 1.0;
					nodes.add(n);
				}
				ind++;
			}
		}
		return nodes.toArray(new svm_node[]{});
	}


	public void setFrags(Collection<String> treeFrags) {
		for(String frag : treeFrags){
			frags.add(FragmentUtils.frag2tree(frag));
		}
	}
}
