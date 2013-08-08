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
package data.chunk.genia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintStream;

public class Genia2PTB {

	public static void main(String[] args) {
		try {
			File geniaTreebankDirectory = new File(args[0]);
			File outputDirectory = new File(args[1]);
			int fileIndex = Integer.parseInt(args[2]);
			PrintStream mapping = new PrintStream(new File(args[3]));
			
			String fourZeros = "0000";
			
			for (File geniaFile : geniaTreebankDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".tree")) return true;
					return false;
				}
			})) {
				
				BufferedReader input = new BufferedReader(new FileReader(geniaFile));
				
				String fileNumber = ""+fileIndex++;
				fileNumber = fourZeros.substring(0,4-fileNumber.length()) + fileNumber;
				
				String fileName = "wsj_"+fileNumber+".mrg";
				mapping.println(geniaFile.getName()+"\t"+fileName);
				
				PrintStream output = new PrintStream(new File(outputDirectory, fileName));
				
				String line;
				while((line = input.readLine()) != null) {
					line = line.replaceAll("([^ )]+)(/)([^) ]+)", "($3 $1)");
					line = "("+line+")";
					output.println(line);
				}
				output.flush();
				output.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
}
