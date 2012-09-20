/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
* @author Tim Miller
*/
package org.chboston.cnlp.ctakes.parser.util;

import opennlp.tools.parser.Parse;

import org.chboston.cnlp.util.SimpleTree;

import edu.mayo.bmi.uima.core.type.syntax.TopTreebankNode;
import edu.mayo.bmi.uima.core.type.syntax.TreebankNode;

import java.util.List;
import java.util.ArrayList;

public class TreeUtils {

	public static List<TreebankNode> getNodeList(TopTreebankNode tree){
		ArrayList<TreebankNode> list = new ArrayList<TreebankNode>();
		list.add(tree);
		int ind = 0;	
		while(ind < list.size()){
			TreebankNode cur = list.get(ind);
			for(int i = 0; i < cur.getChildren().size(); i++){
				list.add(cur.getChildren(i));
			}
			ind++;
		}
		return list;
	}
	
	public static List<Parse> getNodeList(Parse tree){
		ArrayList<Parse> list = new ArrayList<Parse>();
		list.add(tree);
		int ind = 0;	
		while(ind < list.size()){
			Parse cur = list.get(ind);
			Parse[] children = cur.getChildren();
			for(int i = 0; i < children.length; i++){
				list.add(children[i]);
			}
			ind++;
		}
		return list;
	}
	
	public static String tree2str(TreebankNode pathTree){
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		try{
		buff.append(pathTree.getNodeType());
		}catch(Exception e){
			System.err.println("Caught NPE");
		}
		if(pathTree.getChildren().size() == 1 && pathTree.getChildren(0).getLeaf()){
			buff.append(" ");
			buff.append(pathTree.getChildren(0).getNodeType());
//			buff.append(")");
		}else{
			for(int i = 0; i < pathTree.getChildren().size(); i++){
				buff.append(" ");
				buff.append(tree2str(pathTree.getChildren(i)));
			}
		}
		buff.append(")");
		return buff.toString();
	}

//	public static boolean contains(TreebankNode n, SimpleTree frag){
//		if(fragmentMatch(n,frag)) return true;
//		
//		for(int i = 0; i < n.getChildren().size(); i++){
//			if(fragmentMatch(n.getChildren(i), frag)) return true;
//		}
//		return false;
//	}
//	
//	private static boolean fragmentMatch(TreebankNode n, SimpleTree frag){
//		boolean same = false;
//		if(n.getNodeType().equals(frag.cat) && (frag.children.size() == 0 || n.getChildren().size() == frag.children.size())){
//			same = true;
//			for(int i = 0; i < frag.children.size(); i++){
//				if(!fragmentMatch(n.getChildren(i), frag.children.get(i))){
//					same = false;
//					break;
//				}
//			}
//		}
//		return same;
//	}
	
	public static boolean contains(SimpleTree node, SimpleTree frag){
		if(fragmentMatch(node,frag)) return true;
		
		for(int i = 0; i < node.children.size(); i++){
			if(contains(node.children.get(i), frag)) return true;
		}
		return false;
	}

	private static boolean fragmentMatch(SimpleTree node, SimpleTree frag){
		boolean same = false;
		if(node.cat.equals(frag.cat) && (frag.children.size() == 0 || node.children.size() == frag.children.size())){
			same = true;
			for(int i = 0; i < frag.children.size(); i++){
				if(!fragmentMatch(node.children.get(i), frag.children.get(i))){
					same = false;
					break;
				}
			}
		}
		return same;
	}
}

