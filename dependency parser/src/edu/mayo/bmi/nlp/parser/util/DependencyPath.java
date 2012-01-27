package edu.mayo.bmi.nlp.parser.util;

import java.util.LinkedList;

import edu.mayo.bmi.uima.core.type.syntax.ConllDependencyNode;

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
				str.append( node.getForm() );
				continue;
			}
			str.append( foundCommon? 
					">"+node.getDeprel()+"-"+node.getForm() : 
						node.getForm()+"-"+node.getDeprel()+"<");
		}
		return str.toString();
	}
	
}
