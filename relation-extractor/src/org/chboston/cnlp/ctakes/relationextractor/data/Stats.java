package org.chboston.cnlp.ctakes.relationextractor.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.chboston.cnlp.ctakes.relationextractor.knowtator.RelationInfo;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class Stats {
	
	// public final String ALLFILELOC = "/home/dima/mipacq/iaa/data/for_iaa_only/corpus1_2/gold/xml_c1c2all_gold/";
	public static String ALLFILELOC = "/home/dima/temp/splitter/all/";
	public static String TRAINLOC = "/home/dima/temp/splitter/train/";
	public static String DEVLOC = "/home/dima/temp/splitter/dev/";
	public static String TESTLOC = "/home/dima/temp/splitter/test/";
	
	public static void compute(String inputDir) {
		
		File dir = new File(inputDir);

		int totalRelations = 0;
		
		for(String file : dir.list()) {
			if(!file.endsWith(".xml")) {
				continue;
			}
			
			try {
				String fullPath = inputDir + file;
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(new File(fullPath));
				
				ArrayList<RelationInfo> relations = XMLReader.getRelations(document);
				totalRelations = totalRelations + relations.size();
				
			} catch(Exception e) {
				System.out.println("exception: " + e);
			}
		}
		System.out.println(inputDir + ": " + totalRelations);
	}
	
	public static void main(String[] args) {
		
		Stats.compute(Stats.ALLFILELOC);
		Stats.compute(Stats.TRAINLOC);
		Stats.compute(Stats.DEVLOC);
		Stats.compute(Stats.TESTLOC);
	}
}
