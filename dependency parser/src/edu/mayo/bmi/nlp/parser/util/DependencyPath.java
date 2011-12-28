package edu.mayo.bmi.nlp.parser.util;

import java.util.LinkedList;

import edu.mayo.bmi.nlp.parser.type.ConllDependencyNode;

public class DependencyPath extends LinkedList<ConllDependencyNode>{
	
	private ConllDependencyNode commonNode = null;
	
	public void setCommonNode(ConllDependencyNode node) {
		commonNode = node;
	}
	public boolean isCommonNode(ConllDependencyNode node) {
		return commonNode==node;
	}	

	public String toString() {
		StringBuilder str = new StringBuilder();
		
		boolean foundCommon = false;
		for (ConllDependencyNode node : this) {
			if (isCommonNode(node)) {
				foundCommon = true;
				str.append( node.getFORM() );
				continue;
			}
			str.append( foundCommon? 
					">"+node.getDEPREL()+"-"+node.getFORM() : 
						node.getFORM()+"-"+node.getDEPREL()+"<");
		}
		return str.toString();
	}
	
}
