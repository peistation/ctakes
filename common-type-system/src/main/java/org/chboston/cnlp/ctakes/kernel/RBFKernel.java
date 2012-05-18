package org.chboston.cnlp.ctakes.kernel;

import libsvm.svm_node;

public class RBFKernel extends Kernel {

	double gamma = 0.0;

	public RBFKernel(double gamma){
		this.gamma = gamma;
	}

	public double eval(Object o1, Object o2){
		svm_node[] v1 = (svm_node[]) o1;
		svm_node[] v2 = (svm_node[]) o2;

		// TODO implement!	
		double squareNorm = 0;
		int i=0;
		int j=0;
		
		while(i < v1.length || j < v2.length){
			if(i < v1.length && j < v2.length && v1[i].index == v2[j].index){
				// return Math.exp(-gamma*(x_square[i]+x_square[j]-2*dot(x[i],x[j])));
				squareNorm += Math.pow(v1[i].value - v2[j].value, 2);
				i++;
				j++;
			}else if((i < v1.length && j == v2.length) || (i < v1.length && v1[i].index < v2[j].index)){
				squareNorm += Math.pow(v1[i].value, 2);
				i++;
			}else{
				squareNorm += Math.pow(v2[j].value, 2);
				j++;
			}
		}

		double sim = Math.exp(-gamma * squareNorm);
		return sim;
	}
}
