/*   Copyright 2011 Children's Hospital Boston
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
 @author Tim Miller
 * 
 */
package edu.mayo.bmi.coref.util;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.chboston.cnlp.ctakes.parser.uima.type.TerminalTreebankNode;
import org.chboston.cnlp.ctakes.parser.uima.type.TreebankNode;

import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;

public class MarkableDepUtils {

	public static ConllDependencyNode markableNode(JCas jcas, int begin, int end, TreebankNode n) {
		// Find head of markable using treebanknode
		TerminalTreebankNode term = MarkableTreeUtils.getHead(n);
		FSIterator<Annotation> iter = jcas.getAnnotationIndex(ConllDependencyNode.type).iterator();
		ConllDependencyNode best = null;
		
		while(iter.hasNext()){
			Annotation a = iter.next();
			if(a.getBegin() == term.getBegin() && a.getEnd() == term.getEnd()){
				best = (ConllDependencyNode) a;
				break;
			}
		}
		return best;
	}

}
