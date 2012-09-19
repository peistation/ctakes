package org.chboston.cnlp.ctakes.kernel;

import libsvm.svm_node;

public abstract class Kernel{
	protected static double dotProd(svm_node[] v1, svm_node[] v2){
		double sim = 0.0;
		int i = 0;
		int j = 0;
		
		while(i < v1.length && j < v2.length){
			if(v1[i].index == v2[j].index){
				sim += (v1[i].value * v2[j].value);
				i++;
				j++;
			}else if(v1[i].index < v2[j].index){
				i++;
			}else if(v1[i].index > v2[j].index){
				j++;
			}else{
				System.err.println("Don't know how this is possible!");
			}
		}
		return sim;
	}
	
	public abstract double eval(Object o1, Object o2);

}


