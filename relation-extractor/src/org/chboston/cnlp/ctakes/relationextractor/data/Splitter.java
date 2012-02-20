package org.chboston.cnlp.ctakes.relationextractor.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Splitter {

	// public final String ALLFILELOC = "/home/dima/mipacq/iaa/data/for_iaa_only/corpus1_2/gold/xml_c1c2all_gold/";
	public final String ALLFILELOC = "/home/dima/temp/splitter/all/";
	public final String TRAINLOC = "/home/dima/temp/splitter/train/";
	public final String DEVLOC = "/home/dima/temp/splitter/dev/";
	public final String TESTLOC = "/home/dima/temp/splitter/test/";
	
	// percentages for training, development, and test sets
	double trainProportion;
	double devProportion;
	double testProportion;
	
	// number of files in train, dev, and test
	int totalSize;
	int trainSize;
	int devSize;
	int testSize;
	
	List<String> allFileNames = new ArrayList<String>();
		
	public Splitter(double trainProportion, double devProportion, double testProportion) throws IOException {
		
		this.trainProportion = trainProportion;
		this.devProportion = devProportion;
		this.testProportion = testProportion;
		
		File dir = new File(ALLFILELOC);
		allFileNames = Arrays.asList(dir.list());
		
		totalSize = allFileNames.size();
		trainSize = (int) Math.round(totalSize * trainProportion);
		devSize = (int) Math.round(totalSize * devProportion);
		testSize = totalSize - trainSize - devSize;
		
		Collections.shuffle(allFileNames);
		
		Stack<String> fileDispenser = new Stack<String>();
		fileDispenser.addAll(allFileNames);
		
		for(int i = 0; i < trainSize; i++) {
			String nextFile = fileDispenser.pop();
      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + TRAINLOC);
		}
		
		for(int i = 0; i < devSize; i++) {
			String nextFile = fileDispenser.pop();
      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + DEVLOC);
		}
		
		for(int i = 0; i < testSize; i++) {
			String nextFile = fileDispenser.pop();
      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + TESTLOC);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Splitter(0.8, 0.1, 0.1);
	}
}
