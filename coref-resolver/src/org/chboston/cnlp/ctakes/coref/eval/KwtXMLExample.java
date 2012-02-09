package org.chboston.cnlp.ctakes.coref.eval;

import org.chboston.cnlp.ctakes.coref.eval.helpers.*;

import edu.mayo.bmi.coref.util.ParentPtrTree;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.Vector;

import Jama.Matrix;

/* @author Jiaping Zheng
 * 
 */
public class KwtXMLExample {

	Vector<String> pairs1, pairs2;
	Hashtable<String,Integer> markables1, markables2;
	Vector<Span> span1, span2;
	public KwtXMLExample () {
		pairs1 = new Vector<String>(); // antecedent_internal_ID!anaphor_internal_ID, ordered as listed in input file
		pairs2 = new Vector<String>();
		markables1 = new Hashtable<String,Integer>(); // internal_ID -> consolidated ID
		markables2 = new Hashtable<String,Integer>();
		span1 = new Vector<Span>(); // spans, ordered as listed in input file (from beginning to end of the report)
		span2 = new Vector<Span>();
	}

	public static void main(String[] args) {
		KwtXMLExample k = new KwtXMLExample();
		try {
			Vector<String> v1 = new Vector<String>(); // span1[i]'s internal ID
			Vector<String> v2 = new Vector<String>();
			k.readFile(new BufferedReader(new FileReader(args[0])), k.pairs1, k.span1, v1);
			k.readFile(new BufferedReader(new FileReader(args[1])), k.pairs2, k.span2, v2);

			SpanAlignment sa = new SpanAlignment(k.span1.toArray(new Span[k.span1.size()]), k.span2.toArray(new Span[k.span2.size()]));
			int[] id = sa.get1();
			for (int i = 0; i < id.length; i++)
				k.markables1.put(v1.get(i), id[i]);
			id = sa.get2();
			for (int i = 0; i < id.length; i++)
				k.markables2.put(v2.get(i), id[i]);

			int[][] a = new int[k.pairs1.size()][2];
			int[][] b = new int[k.pairs2.size()][2];

			int i = 0;
			for (String s : k.pairs1) {
				String[] ss = s.split("!");
				a[i][0] = k.markables1.get(ss[0]);
				a[i++][1] = k.markables1.get(ss[1]);
			}
			i = 0;
			for (String s : k.pairs2) {
				String[] ss = s.split("!");
				b[i][0] = k.markables2.get(ss[0]);
				b[i++][1] = k.markables2.get(ss[1]);
			}
			ParentPtrTree ppt1 = new ParentPtrTree(a);
			ParentPtrTree ppt2 = new ParentPtrTree(b);

			Matrix m = MatScore.partition(ppt1, ppt2);
			System.out.println("MUC                = " + MatScore.MUC(m));
			System.out.println("class-B3           = " + MatScore.B3(m,B3Type.CLASS));
			System.out.println("entity-B3          = " + MatScore.B3(m,B3Type.ENTITY));
			System.out.println("mention-based CEAF = " + MatScore.CEAF(m,CEAFType.MENTION));
			System.out.println("entity-based CEAF  = " + MatScore.CEAF(MatScore.calcPhi(ppt1,ppt2,MatType.matchingType(CEAFType.ENTITY)),CEAFType.ENTITY));
			System.out.println("KAlpha             = " + MatScore.KAlpha(ppt1,ppt2));
		} catch (Exception e) { e.printStackTrace(); }
	}

	private void readFile (BufferedReader br, Vector<String> pairs, Vector<Span> span, Vector<String> v) {
		try {
			String l;
			while ((l = br.readLine()) != null) {
				String[] s = l.split("\t");
				if (l.indexOf('!') != -1) 
					pairs.add(s[1]);
				else {
					int[] a = new int[s.length-1];
					for (int i = 1; i < s.length; i++)
						a[i-1] = Integer.parseInt(s[i]);
					span.add(new Span(a));
					v.add(s[0]);
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
