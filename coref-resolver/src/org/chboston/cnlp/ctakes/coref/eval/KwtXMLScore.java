package org.chboston.cnlp.ctakes.coref.eval;

import java.util.Vector;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import Jama.Matrix;
import org.chboston.cnlp.ctakes.coref.eval.helpers.*;

import edu.mayo.bmi.coref.util.ParentPtrTree;

/* @author Jiaping Zheng
 * 
 */
public class KwtXMLScore {
	public static void main (String[] args) {
		Vector<int[]> buf = new Vector<int[]>();
		ParentPtrTree key = null;
		ParentPtrTree res = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String l;
			while ((l = br.readLine()) != null) {
				if (l.indexOf("---") != -1) {
					key = new ParentPtrTree(buf.toArray(new int[buf.size()][2]));
					buf.clear();
				} else {
					String[] s = l.split("\t");
					int[] t = {Integer.parseInt(s[0]), Integer.parseInt(s[1])};
					buf.add(t);
				}
			}
			res = new ParentPtrTree(buf.toArray(new int[buf.size()][2]));

			Matrix m = MatScore.partition(key, res);
			System.out.println("MUC                = " + MatScore.MUC(m));
			System.out.println("class-B3           = " + MatScore.B3(m,B3Type.CLASS));
			System.out.println("entity-B3          = " + MatScore.B3(m,B3Type.ENTITY));
			System.out.println("mention-based CEAF = " + MatScore.CEAF(m,CEAFType.MENTION));
			System.out.println("entity-based CEAF  = " + MatScore.CEAF(MatScore.calcPhi(key,res,MatType.matchingType(CEAFType.ENTITY)),CEAFType.ENTITY));
			System.out.println("KAlpha             = " + MatScore.KAlpha(key,res));
		} catch (Exception e) { e.printStackTrace(); }
	}
}
