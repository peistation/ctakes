package org.chboston.cnlp.util;

public class FragmentUtils {

	public static SimpleTree frag2tree(String frag){
		char[] chars = frag.toCharArray();
		int ind = frag.indexOf('(', 1);
		String type = frag.substring(1, ind);
		SimpleTree root = new SimpleTree(type);
		SimpleTree cur = root;
		int lpar, rpar, oldind;
		while(ind < chars.length){
			if(chars[ind] == '('){
				SimpleTree nt = null;
				lpar = frag.indexOf('(', ind+1);
				rpar = frag.indexOf(')', ind+1);
				oldind = ind;
				ind = (lpar < rpar  && lpar != -1 ? lpar : rpar);
				type = frag.substring(oldind+1, ind);
				nt = new SimpleTree(type, cur);
				cur.addChild(nt);
				cur = nt;
			}else if(chars[ind] == ')'){
				// if close paren, go up a level and move to next index,
				// which is guaranteed to be another paren
				cur = cur.parent;
				ind++;
			}
		}
		return root;
	}	
}
