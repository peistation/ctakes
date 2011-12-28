/*    Copyright 2011 Children's Hospital Boston
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

import org.chboston.cnlp.ctakes.parser.uima.type.*;

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

}

