package clear.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

import clear.dep.DepNode;
import clear.dep.DepTree;


import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;
import edu.mayo.bmi.uima.core.type.Sentence;

public class ClearDependencyUtility {
    public static ConllDependencyNode convert(JCas jcas, DepTree clearTree, Sentence sentence, List<BaseToken> tokens)  {
        
        ArrayList<ConllDependencyNode> uimaNodes = new ArrayList<ConllDependencyNode>(tokens.size()+1);
        
        // Create the root node first
        int rootBegin = tokens.get(0).getBegin();
        int rootEnd = tokens.get(tokens.size()-1).getEnd();
        uimaNodes.add( 0, new ConllDependencyNode(jcas, rootBegin, rootEnd));

        // Create all the other nodes
        for (int i=1; i<clearTree.size(); i++) {
            int nodeBegin = tokens.get(i-1).getBegin(); // assumes that tokens are off 1 from clearTree
            int nodeEnd = tokens.get(i-1).getEnd();
            ConllDependencyNode uimaNode = new ConllDependencyNode(jcas, nodeBegin, nodeEnd);
            uimaNodes.add(i,uimaNode);
        }
        
        // Set values in all the other nodes
        for (int i=1; i<clearTree.size(); i++) {
            

            DepNode clearNode = clearTree.get(i);
            ConllDependencyNode uimaNode = uimaNodes.get(i);

            uimaNode.setID(clearNode.id);
            uimaNode.setFORM(clearNode.form);
            uimaNode.setLEMMA(clearNode.lemma);
            uimaNode.setCPOSTAG(clearNode.pos);
            uimaNode.setPOSTAG(clearNode.pos);
            uimaNode.setFEATS("_");
            uimaNode.setHEAD(uimaNodes.get(clearNode.headId));
            uimaNode.setDEPREL(clearNode.deprel);
            uimaNode.setPHEAD(null);
            uimaNode.setPDEPREL("_");

             
       }
        
        for (int i=0; i<uimaNodes.size(); i++) {
            // Enter UIMA nodes into index
            uimaNodes.get(i).addToIndexes(jcas);
        }

        return uimaNodes.get(0); //return the root node
    }
    
    
}
