package org.chboston.cnlp.ctakes.relationextractor.data;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Randomly split a set of files in a directory into three sets: train, dev, and test.
 * 
 * @author dmitriy dligach
 *
 */
public class Splitter {

	public final String ALLFILELOC = "/home/dima/sharp/cloud/share/xml/all/";
	public final String TRAINLOC = "/home/dima/sharp/cloud/share/xml/train/";
	public final String DEVLOC = "/home/dima/sharp/cloud/share/xml/dev/";
	public final String TESTLOC = "/home/dima/sharp/cloud/share/xml/test/";
	
	// number of files in train, dev, and test
	int totalSize;
	int trainSize;
	int devSize;
	int testSize;
	
	List<String> allFileNames = new ArrayList<String>();
		
	/**
	 * Specify percentages of the split.
	 */
	public Splitter(double trainProportion, double devProportion, double testProportion) {

		File dir = new File(ALLFILELOC);
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};

		allFileNames = Arrays.asList(dir.list(filter));

		totalSize = allFileNames.size();

		trainSize = (int) Math.round(totalSize * trainProportion);
		devSize = (int) Math.round(totalSize * devProportion);
		testSize = totalSize - trainSize - devSize;

	}
	
	/**
	 * Specify the size of each set.
	 */
	public Splitter(int trainSize, int devSize, int testSize) {

		File dir = new File(ALLFILELOC);
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		
		allFileNames = Arrays.asList(dir.list(filter));
		
		this.trainSize = trainSize;
		this.devSize = devSize;
		this.testSize = testSize;
	}
	
	public void createDirectories() throws IOException {
		
		Runtime.getRuntime().exec("mkdir " + TRAINLOC);
		Runtime.getRuntime().exec("mkdir " + DEVLOC);
		Runtime.getRuntime().exec("mkdir " + TESTLOC);
	}
	
	public void split() throws IOException {

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
		
		Splitter s = new Splitter(80, 25, 25);
		s.createDirectories();
		s.split();
	}
}
