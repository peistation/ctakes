package org.chboston.cnlp.ctakes.relationextractor.cpe;

import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.util.XMLInputSource;

public class CPEWrapper extends Thread {

	private CollectionProcessingEngine mCPE;

	public CPEWrapper() throws Exception {

		String descriptor = "/home/dima/sharp/relations/cpe/GoldRelationReaderCPE.xml";

		// parse CPE descriptor                                                                                                                                                                                                              
		CpeDescription cpeDesc = UIMAFramework.getXMLParser().parseCpeDescription(new XMLInputSource(descriptor));
		// instantiate CPE                                                                                                                                                                                                                   
		mCPE = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
		// Start Processing                                                                                                                                                                                                                  
		mCPE.process();
	}

	public static void main(String[] args) throws Exception {

		new CPEWrapper();
	}
}

