package org.chboston.cnlp.ctakes.coref.treekernel.training;

import java.util.ArrayList;

import org.chboston.cnlp.ctakes.kernel.Kernel;
import org.chboston.cnlp.ctakes.parser.treekernel.TreeKernel;

import edu.mayo.bmi.coref.util.ThreadDelegator;

import opennlp.tools.parser.Parse;

public class RowFillerThread extends Thread {
	private ArrayList<Double> row = null;
	private static ArrayList<?> objects;
	private Kernel kernel = null;
	private ThreadDelegator parent = null;
	private int rowNum = 0;
	private long start;
	
	public RowFillerThread(ThreadDelegator parent, Kernel k, ArrayList<Double> row, int i){
		this.kernel = k;
		this.row = row;
		this.rowNum = i;
		this.parent = parent;
	}
	
	@Override
	public void run() {
		super.run();
		if(rowNum % 100 == 0) start = System.currentTimeMillis();
		Object p1 = objects.get(rowNum);
		for(int j = 0; j <= rowNum; j++){
			Object p2 = objects.get(j);
			double sim = kernel.eval(p1,p2);
			row.add(sim);
		}
		
		if(rowNum % 100 == 0){
			System.out.println("Row " + rowNum + " took " + (System.currentTimeMillis()-start)+  "ms");
		}
		parent.threadDone(this);
	}
	
	public void setRow(ArrayList<Double> row, int i){
		this.row = row;
		this.rowNum = i;
	}

	public static void setObjects(ArrayList<?> objects) {
		RowFillerThread.objects = objects;
	}
	
	public int getRowNum(){
		return rowNum;
	}
}
