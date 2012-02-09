package org.chboston.cnlp.ctakes.coref.eval;

import edu.mayo.bmi.coref.util.ParentPtrTree;

/* @author Jiaping Zheng
 * @author Tim Miller
 */
public class BlancScore {

	int rc = 0;
	int wn = 0;
	int wc = 0;
	int rn = 0;
	int all;
	
	
//	public static double score (ParentPtrTree key, ParentPtrTree response) {
	public void addDocumentScore(int[][] key, int[][] response){
		int rc = 0;
		int wn = 0;
		int wc = 0;
		int error = 0;
		int all;

		ParentPtrTree pptGold = null;
		ParentPtrTree pptSys = null;
	
		int min = Integer.MAX_VALUE;
		for(int i = 0; i < key.length; i++){
			for(int j = 0; j < key[i].length; j++){
				if(key[i][j] < min){
					min = key[i][j];
				}
			}
		}
		for(int i = 0; i < response.length; i++){
			for(int j = 0; j < response[i].length; j++){
				if(response[i][j] < min){
					min = response[i][j];
				}
			}
		}
		
		// min should be 1 not zero:
		min--;
		
		for(int i = 0; i < key.length; i++){
			for(int j = 0; j < key[i].length; j++){
				key[i][j] -= min;
			}
		}
		for(int i = 0; i < response.length; i++){
			for(int j = 0; j < response[i].length; j++){
				response[i][j] -= min;
			}
		}
		
		pptGold = new ParentPtrTree(key);
		pptSys = new ParentPtrTree(response);
		
		int smaller, max;
		int s1 = pptGold.getSize();
		int s2 = pptSys.getSize();
		if (s1 < s2) {
			all = s2 * (s2 - 1) / 2;
			smaller = s1;
			max = s2;
		} else {
			all = s1 * (s1 - 1) / 2;
			smaller = s2;
			max = s1;
		}
		for (int i = 0; i < max; i++)
			for (int j = i+1; j < max; j++)
				if (j < smaller) {
					boolean eqv1 = !pptGold.differ(i, j);
					boolean eqv2 = !pptSys.differ(i, j);
					if (eqv1 && eqv2) rc++;
//					else if (eqv1 || eqv2) error++;
					else if(eqv1 && !eqv2) wn++;
					else if(!eqv1 && eqv2) wc++;
				} else {
					if (j < s1) {
						if (!pptGold.differ(i, j)) wn++;
					}
					else {
						if (!pptSys.differ(i, j)) wc++;
					}
				}

//		int rn = all - rc - error;
		int rn = all - rc - wn - wc;
		this.rc += rc;
		this.rn += rn;
		this.wn += wn;
		this.wc += wc;
//		return 1.0 - (all * error) / ((error + 2.0 * rc) * (error + 2.0 * rn));
	}
	
	public double getScore(){
		double Pc = (float) rc / (rc+wc);
		double Rc = (float) rc / (rc+wn);
		double Pn = (float) rn / (rn+wn);
		double Rn = (float) rn / (rn+wc);
		double Fc = 2 * Pc * Rc / (Pc + Rc);
		double Fn = 2 * Pn * Rn / (Pn + Rn);
		return (Fc + Fn) / 2.0;	
	}
}
