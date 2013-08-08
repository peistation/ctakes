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
package data.chunk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Chunklink2OpenNLP {

	public static void main(String[] args) throws IOException {
		System.out.println("usage: java Chunklink2OpenNLP <input-file> <output-file>");
		System.out.println("\twhere <input-file> is the output from chunklink_2-2-2000_for_conll.pl (see data/chunk/genia/README)");
		System.out.println("\twhere <output-file> is chunking data suitable for training with the OpenNLP ChunkerME");
		
		BufferedReader input = new BufferedReader(new FileReader(args[0]));
		PrintStream out= new PrintStream(args[1]);

		String line;
		while((line=input.readLine())!= null) {
			if(line.trim().equals(""))
				out.println("");
			else if(line.startsWith("#"))
				continue;
			else {
		  		line = line.trim();
				String[] columns = line.split("\\s+");
	  			out.println(columns[4]+" "+columns[3]+" "+columns[2]);
	  		}
		  }
	      out.flush();
		  out.close();
	  }

}
