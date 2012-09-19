/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
//
//package edu.mayo.bmi.test.preprocessor;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URLDecoder;
//import java.util.Map;
//
//import edu.mayo.bmi.nlp.preprocessor.ClinicalNotePreProcessor;
//import edu.mayo.bmi.nlp.preprocessor.DocumentMetaData;
//
//import junit.framework.TestCase;
//
///**
// * Unit tests for the ClinicalNotePreProcessor class.  These test the class
// * against clinical note XML data and determine whether the preprocessor
// * successfully parsed out the correct info.
// * 
// * @author Mayo Clinic
// */
//public class ClinicalNotePreProcessorTest extends TestCase
//{
//    private ClinicalNotePreProcessor iv_cnotePreProcessor;
//    private String iv_cnoteXML;
//
//    /**
//     * Constructor for ClinicalNotePreProcessorTest.
//     * @param arg0
//     */
//    public ClinicalNotePreProcessorTest(String arg0)
//    {
//        super(arg0);
//    }
//
//    /*
//     * @see TestCase#setUp()
//     */
//    protected void setUp() throws Exception
//    {
//        super.setUp();
//
//        String dtdLocation = "resources/cda/NotesIIST_RTF.DTD";
//        File dtd = new File(dtdLocation);
//        iv_cnotePreProcessor = new ClinicalNotePreProcessor(dtd, false);
//
//		//String cnoteLocationOnCp = "/test/data/testpatient_cn_1.xml";
//		String cnoteLocationOnCp = "../../../../../data/testpatient_cn_1.xml";
//        String cnoteLocation =
//            URLDecoder.decode(getClass().getResource(cnoteLocationOnCp).getPath());
//        
//        if (cnoteLocation == null) {
//        	throw new FileNotFoundException("Unable to find: " + cnoteLocationOnCp);
//        }
//        iv_cnoteXML = load(cnoteLocation);
//    }
//
//    /*
//     * @see TestCase#tearDown()
//     */
//    protected void tearDown() throws Exception
//    {
//        super.tearDown();
//    }
//
//    /**
//     * Tests the process method.
//     */
//    public void testProcess()
//    {
//        try
//        {
//            DocumentMetaData dmd = iv_cnotePreProcessor.process(iv_cnoteXML);
//
//            // validate document properties
//            String docID = "000000000";
//            String serviceCode = "MNT";
//            Map docProperties = dmd.getMetaData();
//            String cnote_docID =
//                (String) docProperties.get(
//                    ClinicalNotePreProcessor.MD_KEY_DOC_ID);
//            String cnote_serviceCode =
//                (String) docProperties.get(
//                    ClinicalNotePreProcessor.MD_KEY_SERVICE_CODE);
//            assertEquals(docID, cnote_docID);
//            assertEquals(serviceCode, cnote_serviceCode);
//
//            // validate each section
//            // TODO Consider validating each section           
//        }
//        catch (Exception e)
//        {
//        	e.printStackTrace(System.err);
//            fail(e.getMessage());
//        }
//    }
//
//    /**
//     * Loads text from a file.
//     * @param filename
//     * @return
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    private String load(String filename)
//        throws FileNotFoundException, IOException
//    {
//        String msg = "";
//        File f = new File(filename);
//        BufferedReader br = new BufferedReader(new FileReader(f));
//        String line = br.readLine();
//        while (line != null)
//        {
//            msg += line + "\n";
//            line = br.readLine();
//        }
//        br.close();
//
//        return msg;
//    }
//}
