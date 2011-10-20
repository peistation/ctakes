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
package edu.mayo.bmi.utils.xcas_comparison;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;


public class ConvertAndCompare {


	public static void main(String[] args) {
		// key = cni, value = open source
		Hashtable<String, String> map = new Hashtable<String, String>();
		map.put("edu.mayo.bmi.uima.common.type.DocumentID",
				"edu.mayo.bmi.uima.common.types.DocumentIDAnnotation");
		map.put("edu.mayo.bmi.uima.lookup.type.LookupWindowAnnotation",
				"edu.mayo.bmi.uima.lookup.types.LookupWindowAnnotation");
//		Specify your mappings here
/*		map.put("",
				"uima.tt.MCAnnotation");
		map.put("",
				"uima.tt.TCAnnotation");
		map.put("",
				"uima.tt.WHPAnnotation");
		map.put("",
				"uima.tt.OBJAnnotation");
		map.put("",
				"uima.tt.PSUBAnnotation");
		map.put("",
				"uima.tt.SUBAnnotation");
		map.put("",
				"uima.tt.AdjAnnotation");
		map.put("",
				"uima.tt.CNPAnnotation");
		map.put("",
				"uima.tt.NPAnnotation");
		map.put("",
				"uima.tt.NPListAnnotation");
		map.put("",
				"uima.tt.NPPAnnotation");
		map.put("",
				"uima.tt.PPAnnotation");
		map.put("",
				"uima.tt.PVGAnnotation");
		map.put("",
				"uima.tt.VGAnnotation");
*/
		map.put("edu.mayo.bmi.uima.cdt.type.RomanNumberalAnnotation",
				"edu.mayo.bmi.uima.cdt.types.RomanNumberalAnnotation");
		map.put("edu.mayo.bmi.uima.cdt.type.FractionAnnotation",
				"edu.mayo.bmi.uima.cdt.types.FractionAnnotation");
		map.put("edu.mayo.bmi.uima.cdt.type.DateAnnotation",
				"edu.mayo.bmi.uima.cdt.types.DateAnnotation");
		map.put("edu.mayo.bmi.uima.cdt.type.ProblemListAnnotation",
				"edu.mayo.bmi.uima.cdt.types.ProblemListAnnotation");
		map.put("edu.mayo.bmi.uima.cdt.type.MeasurementAnnotation",
				"edu.mayo.bmi.uima.cdt.types.MeasurementAnnotation");
		map.put("edu.mayo.bmi.uima.cdt.type.PersonTitleAnnotation",
				"edu.mayo.bmi.uima.cdt.types.PersonTitleAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.Segment",
				"edu.mayo.bmi.uima.common.types.SegmentAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.Sentence",
				"edu.mayo.bmi.uima.common.types.SentenceAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.WordToken",
				"edu.mayo.bmi.uima.common.types.WordTokenAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.NumToken",
				"edu.mayo.bmi.uima.common.types.NumTokenAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.PunctuationToken",
				"edu.mayo.bmi.uima.common.types.PunctTokenAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.SymbolToken",
				"edu.mayo.bmi.uima.common.types.SymbolTokenAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.NewlineToken",
				"edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.NamedEntity",
				"edu.mayo.bmi.uima.common.types.NamedEntityAnnotation");
		map.put("edu.mayo.bmi.uima.core.type.UmlsConcept",
				"edu.mayo.bmi.uima.common.types.UmlsConcept");
		map.put("edu.mayo.bmi.uima.core.type.OntologyConcept",
				"edu.mayo.bmi.uima.common.types.OntologyConcept");

//		Prepare the list of attributes to ignore when comparing elements
		Const.init();
//		Initialize a processor
		XcasProcessor p = new XcasProcessor();
//		Process/parse the two files specified in args[0] and args[1]
		File f1 = new File(args[0]);
		if (!f1.exists()) { System.err.println(args[0]+" not exist!"); System.exit(1); }
		File f2 = new File(args[1]);
		if (!f2.exists()) { System.err.println(args[2]+" not exist!"); System.exit(1); }
		XcasFile xf1 = p.process(f1);
		XcasFile xf2 = p.process(f2);
//		Change xf1 to xf2 if the second command line argument is the open source output
		for (XcasAnnotation a : xf1.getAllAnnotations())
			if (map.containsKey(a.getType()))
					a.setType(map.get(a.getType()));
//		Construct an XcasDiff object from the two XcasFiles
		XcasDiff d = new XcasDiff(xf1, xf2);
//		Print differences to stdout
		d.printDiff();
//		Print an HTML summary to file specified in args[2]
		try {
			d.printHTML(new FileWriter(args[2]));
			System.out.println();
			System.out.println("HTML summary written to "+args[2]);
		} catch (IOException e) { e.printStackTrace(); }
	}

}
