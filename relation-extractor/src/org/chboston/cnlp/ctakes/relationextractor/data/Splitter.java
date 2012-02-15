package org.chboston.cnlp.ctakes.relationextractor.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.chboston.cnlp.ctakes.relationextractor.knowtator.RelationInfo;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

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
	
	// file lists 
	List<String> allFileNames = new ArrayList<String>();
	List<String> trainFileNames = new ArrayList<String>();
	List<String> devFileNames = new ArrayList<String>();
	List<String> testFileNames = new ArrayList<String>();
	
	public Splitter(double trainProportion, double devProportion, double testProportion) {
		
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
		
		Stack<String> fileList = new Stack<String>();
		fileList.addAll(allFileNames);
		
		for(int i = 0; i < trainSize; i++) {
			String nextFile = fileList.pop();
			trainFileNames.add(nextFile);

			try {
	      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + TRAINLOC);
      } catch (IOException e) {
	      e.printStackTrace();
      }
		}
		
		for(int i = 0; i < devSize; i++) {
			String nextFile = fileList.pop();
			devFileNames.add(nextFile);
			
			try {
	      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + DEVLOC);
      } catch (IOException e) {
	      e.printStackTrace();
      }
		}
		
		for(int i = 0; i < testSize; i++) {
			String nextFile = fileList.pop();
			testFileNames.add(nextFile);
			
			try {
	      Runtime.getRuntime().exec("cp " + ALLFILELOC + nextFile + " " + TESTLOC);
      } catch (IOException e) {
	      e.printStackTrace();
      }
		}
	}
	
	public static void main(String[] args) {
		
		Splitter splitter = new Splitter(0.8, 0.1, 0.1);
	}
}
