package org.chboston.cnlp.ctakes.relationextractor.cpe;

import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.util.XMLInputSource;

/**
 * This class is used to run a CPE programatically rather than 
 * by means of the UIMA-supplied CPE Configurator tool. 
 * 
 * I currently use it to test my collection readers for 
 * UMLS entities and relations.
 * 
 * @author dmitriy dligach
 *
 */
public class RunMipacDegreeOfRelationReaderCPE extends Thread {

	private CollectionProcessingEngine mCPE;

	public RunMipacDegreeOfRelationReaderCPE() throws Exception {

		String descriptor = "desc/collection_processing_engine/TestMipacqDegreeOfRelationReaderCPE.xml";

		// parse CPE descriptor                                                                                                                                                                                                              
		CpeDescription cpeDesc = UIMAFramework.getXMLParser().parseCpeDescription(new XMLInputSource(descriptor));
		// instantiate CPE                                                                                                                                                                                                                   
		mCPE = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
		// Start Processing                                                                                                                                                                                                                  
		mCPE.process();
	}

	public static void main(String[] args) throws Exception {

		new RunMipacDegreeOfRelationReaderCPE();
	}
}

