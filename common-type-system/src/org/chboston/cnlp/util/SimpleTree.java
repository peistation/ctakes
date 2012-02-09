package org.chboston.cnlp.util;

import java.util.ArrayList;

public class SimpleTree {
	public String cat;
	public ArrayList<SimpleTree> children;
	public SimpleTree parent = null;
	
	public SimpleTree(String c){
		this(c,null);
	}
	
	public SimpleTree(String c, SimpleTree p){
		cat = c;
		children = new ArrayList<SimpleTree>();
		parent = p;
	}
	
	public void addChild(SimpleTree t){
		children.add(t);
	}
	
	@Override
	public String toString(){
		StringBuffer buff = new StringBuffer();
		
		buff.append("(");
		buff.append(cat);
		buff.append(" ");
		if(children.size() == 1 && children.get(0).children.size() == 0){
			buff.append(children.get(0).cat);
		}else{
			for(int i = 0; i < children.size(); i++){
				if(i != 0){
					buff.append(" ");
				}
				buff.append(children.get(i).toString());
			}
		}
		buff.append(")");
		return buff.toString();
	}
	
	public static void main(String[] args){
		SimpleTree t = new SimpleTree("TOP");
		t.addChild(new SimpleTree("S"));
		t.children.get(0).addChild(new SimpleTree("NP"));
		t.children.get(0).addChild(new SimpleTree("VP"));
		t.children.get(0).children.get(0).addChild(new SimpleTree("i"));
		t.children.get(0).children.get(1).addChild(new SimpleTree("ran"));
		System.out.println(t.toString());
	}
}
