/*
 * Copyright: (c) 2010   Mayo Foundation for Medical Education and 
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
package edu.mayo.bmi.nlp.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.type.textspan.Sentence;

public class LemAssigner extends JCasAnnotator_ImplBase{

	// LOG4J logger based on class name
	public Logger logger = Logger.getLogger(getClass().getName());
	    
	@Override
	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);	    
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		logger.info(" process(JCas)");
        List<ConllDependencyNode> nodes = new ArrayList<ConllDependencyNode>();
        List<BaseToken> tokens          = new ArrayList<BaseToken>();

        AnnotationIndex nodeIndex = jCas.getAnnotationIndex(ConllDependencyNode.type);
        AnnotationIndex tokenIndex = jCas.getAnnotationIndex(BaseToken.type);
        FSIterator sentences = jCas.getAnnotationIndex(Sentence.type).iterator();

        while (sentences.hasNext()) {
            Sentence sentence = (Sentence) sentences.next();
            
            tokens.clear();
            nodes.clear();

            FSIterator tokenIterator = tokenIndex.subiterator(sentence);
            while (tokenIterator.hasNext()) {
                tokens.add((BaseToken) tokenIterator.next());
            }
            //logger.info(" new sentence: ");
            FSIterator nodeIterator = nodeIndex.subiterator(sentence);
            while (nodeIterator.hasNext()) {
                ConllDependencyNode node = (ConllDependencyNode) nodeIterator.next();
                if (node.getID()!=0) {
                    nodes.add(node);
                }
                //logger.info(node.getFORM()+" ");
            }

            
            
            ListIterator<BaseToken>           itt = tokens.listIterator();
            ListIterator<ConllDependencyNode> itn = nodes.listIterator();
            BaseToken           bt = null;
            ConllDependencyNode dn = null;
            if (tokens.size()>0 && nodes.size()>0) {
                // iterate through the parallel sorted lists
                if (itt.hasNext()) bt                  = itt.next();
                if (itn.hasNext()) dn                  = itn.next();
                if (dn != null) 
                    if (dn.getID()==0 && itn.hasNext()) 
                        dn = itn.next();
                while (itt.hasNext() || itn.hasNext()) {
                    if (bt.getBegin()==dn.getBegin() ) { // Allow ragged right edge //&& bt.getEnd()==dn.getEnd()) { 
                        dn.setLEMMA( bt.getNormalizedForm() );
                        if (dn.getLEMMA()==null)
                            dn.setLEMMA( dn.getFORM().toLowerCase() );
                        dn.addToIndexes();
                        if (itt.hasNext()) bt = itt.next();
                        if (itn.hasNext()) dn = itn.next();
                    } else if ( bt.getBegin()<dn.getBegin() ) {
                        if (itt.hasNext()) bt = itt.next(); else break;
                    } else if ( bt.getBegin()>dn.getBegin() ) {
                        // not every node will get a lemma b/c not all tokens are word tokens
                        if (itn.hasNext()) {
                            dn.setLEMMA( dn.getFORM().toLowerCase() );
                            dn.addToIndexes();
                            dn = itn.next(); 
                        } else break;
                    }
                }
                if (bt.getBegin()==dn.getBegin() && bt.getEnd()==dn.getEnd()) {
                    dn.setLEMMA( bt.getNormalizedForm() );
                    dn.addToIndexes();
                }
            }
            
		}
        
	}
	
}
