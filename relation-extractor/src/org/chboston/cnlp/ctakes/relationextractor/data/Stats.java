package org.chboston.cnlp.ctakes.relationextractor.data;

import java.io.File;
import java.io.FilenameFilter;

import java.io.IOException;
import java.util.HashMap;

import org.chboston.cnlp.ctakes.relationextractor.knowtator.RelationInfo;
import org.chboston.cnlp.ctakes.relationextractor.knowtator.XMLReader;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Calculate relation frequencies in a data set that consists of XML files exported from Knowtator.
 * 
 * @author dmitriy dligach
 *
 */
public class Stats {
		
	// read all the relations in the data or just the SHARP ones?
	public static final boolean readOnlySharpRelations = true;
	
	public static void compute(String inputDir) throws JDOMException, IOException {
		
		File dir = new File(inputDir);
		
		// key: relation, value: total instances of this relation
	  HashMap<String, Integer> relationCounts = new HashMap<String, Integer>(); 
		// total number of relations
	  int totalRelationCount = 0;
		
	  FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		
		for(String file : dir.list(filter)) {

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(inputDir, file));

			for(RelationInfo relationInfo : XMLReader.getRelations(document)) {
	    	
				if(readOnlySharpRelations) {
	    		if(! Constants.sharpRelationsSelected.contains(relationInfo.category)) {
	    			continue; // ignore this relation
	    		}
	    	}
	  
				totalRelationCount++;
				
				if(relationCounts.containsKey(relationInfo.category)) {
					relationCounts.put(relationInfo.category, relationCounts.get(relationInfo.category) + 1);
				} 
				else {
					relationCounts.put(relationInfo.category, 1);
				}
			}
		}

		System.out.println(inputDir);
		reportRelationStats(relationCounts, totalRelationCount);
	}
	
  public static void reportRelationStats(HashMap<String, Integer> relationCounts, int totalRelationCount) {

    for(String relation : relationCounts.keySet()) {
    	System.out.format("%-25s %5d (%.2f%%)\n", relation, relationCounts.get(relation), (double) relationCounts.get(relation) * 100 / totalRelationCount);
    }
    
    System.out.format("\n%-25s %5d (%d%%)\n\n", "total", totalRelationCount, 100);
  }
  
	public static void main(String[] args) throws JDOMException, IOException {
		
		Stats.compute(Constants.shareAllXmlPath);
		Stats.compute(Constants.shareTrainXmlPath);
		Stats.compute(Constants.shareDevXmlPath);
		Stats.compute(Constants.shareTestXmlPath);
	}
}
