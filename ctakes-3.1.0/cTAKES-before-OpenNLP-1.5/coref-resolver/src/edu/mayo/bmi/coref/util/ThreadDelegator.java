package edu.mayo.bmi.coref.util;

import org.chboston.cnlp.ctakes.coref.treekernel.training.RowFillerThread;

public interface ThreadDelegator {
	public void threadDone(RowFillerThread t);
}
